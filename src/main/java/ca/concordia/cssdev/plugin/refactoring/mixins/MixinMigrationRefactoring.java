package ca.concordia.cssdev.plugin.refactoring.mixins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEditGroup;

import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.cssmodel.LocationInfo;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.migration.topreprocessors.TransformationStatus;
import ca.concordia.cssanalyser.migration.topreprocessors.TransformationStatus.TransformationStatusEntry;
import ca.concordia.cssanalyser.migration.topreprocessors.less.LessMixinMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssdev.plugin.refactoring.DuplicationRefactoring;
import ca.concordia.cssdev.plugin.refactoring.OffsetLength;
import ca.concordia.cssdev.plugin.refactoring.OffsetLengthList;
import ca.concordia.cssdev.plugin.refactoring.RefactoringUtil;
import ca.concordia.cssdev.plugin.utility.LocalizedStrings;
import ca.concordia.cssdev.plugin.utility.PreferencesUtil;
import ca.concordia.cssdev.plugin.utility.LocalizedStrings.Keys;
import ca.concordia.cssdev.plugin.wizards.duplication.declarationsdiffview.MixinDiffWizardPage;

public class MixinMigrationRefactoring extends DuplicationRefactoring {
	
	public static final String REFACTORING_ID = "ca.concordia.cssanalyser.plugin.extractMixin";
	
	private final MixinDiffWizardPage mixinDiffWizardPage;
	
	public MixinMigrationRefactoring(MixinDuplicationInfo duplicationInfo) {
		super(duplicationInfo);
		mixinDiffWizardPage = new MixinDiffWizardPage(duplicationInfo);
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor progressMonitor)
			throws CoreException, OperationCanceledException {
		progressMonitor.beginTask(LocalizedStrings.get(Keys.CHECKING_PRECONDITIONS), 1);
		progressMonitor.subTask(LocalizedStrings.get(Keys.CHECKING_PRECONDITIONS));
		RefactoringStatus refactoringStatus = new RefactoringStatus();
		TransformationStatus status = ((MixinDuplicationInfo)duplicationInfo).getMixinMigrationOpportunity().preservesPresentation();
		if (!status.isOK()) {
			refactoringStatus.addError(LocalizedStrings.get(Keys.BREAK_PRESENTATION_ERROR));
			for (TransformationStatusEntry transformationStatusEntry : status.getStatusEntries()) {
				refactoringStatus.addError(transformationStatusEntry.toString());
			}
		}
		progressMonitor.done();
		return refactoringStatus;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor arg0)
			throws CoreException, OperationCanceledException {
		RefactoringStatus refactoringStatus = new RefactoringStatus();
		return refactoringStatus;
	}

	@Override
	public Change createChange(IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		progressMonitor.beginTask(LocalizedStrings.get(Keys.CREATING_CHANGE), 100);
		TextFileChange resultingChange = new TextFileChange(duplicationInfo.getSourceIFile().getName(), duplicationInfo.getSourceIFile());
		MultiTextEdit fileChangeRootEdit = new MultiTextEdit();
		resultingChange.setEdit(fileChangeRootEdit);

		MixinMigrationOpportunity<?> mixinMigrationOpportunity = ((MixinDuplicationInfo)duplicationInfo).getMixinMigrationOpportunity();
		try {
			String fileContents = IOHelper.readFileToString(duplicationInfo.getSourceIFile().getLocation().toOSString());
			
			String newMixinString = mixinMigrationOpportunity.toString();
			if (fileContents.charAt(fileContents.length() - 1) == '}')
				newMixinString = System.lineSeparator() + System.lineSeparator() + newMixinString;
			
			OffsetLengthList offsetsAndLengths = new OffsetLengthList();
			
			// 1- Remove the declarations being parameterized
			progressMonitor.subTask(LocalizedStrings.get(Keys.CREATING_REMOVE_DECLARATIONS_CHANGE));
			progressMonitor.worked(25);
			Iterable<Declaration> realDeclarationsToRemove = mixinMigrationOpportunity.getDeclarationsToBeRemoved();
			for (Declaration declarationToRemove : realDeclarationsToRemove) {
				LocationInfo locationInfo = declarationToRemove.getLocationInfo();
	    		OffsetLength offsetLength = RefactoringUtil.expandAreaToRemove(fileContents, locationInfo);
	    		offsetsAndLengths.add(offsetLength);
			}
			
			TextEditGroup textEditGroup = new TextEditGroup(LocalizedStrings.get(Keys.REMOVE_DUPLICATED_DECLARATIONS));
			for (OffsetLength offsetAndLength : offsetsAndLengths.getNonOverlappingOffsetsAndLengths()) {
		    	DeleteEdit deleteEdit = new DeleteEdit(offsetAndLength.getOffset(), offsetAndLength.getLength());
		    	textEditGroup.addTextEdit(deleteEdit);
		    }
			fileChangeRootEdit.addChildren(textEditGroup.getTextEdits());
			resultingChange.addTextEditGroup(textEditGroup);
			
			// Add declarations if necessary
			progressMonitor.subTask(LocalizedStrings.get(Keys.CREATING_ADD_DECLARATIONS_CHANGE));
			progressMonitor.worked(25);
			List<InsertEdit> insertEdits = new ArrayList<>();
			for (Declaration declaration : mixinMigrationOpportunity.getDeclarationsToBeAdded()) {
				Selector parentSelector = declaration.getSelector();
				int lastCharOfSelectorOffset = getLastCharOfSelectorOffset(parentSelector);
				String declarationString = PreferencesUtil.getTabString() + declaration.toString() + ";" + System.lineSeparator();
				InsertEdit newDeclarationInsertEdit = new InsertEdit(lastCharOfSelectorOffset, declarationString);	
				insertEdits.add(newDeclarationInsertEdit);
			}
			if (insertEdits.size() > 0) {
				InsertEdit[] insertEditsArray = insertEdits.toArray(new InsertEdit[]{});
				fileChangeRootEdit.addChildren(insertEditsArray);	
				resultingChange.addTextEditGroup(new TextEditGroup(LocalizedStrings.get(Keys.ADD_NECESSARY_DECLARATIONS), insertEditsArray));
			}
			
			// 3- Add the new Mixin 
			progressMonitor.subTask(LocalizedStrings.get(Keys.CREATING_ADD_MIXIN_CHANGE));
			progressMonitor.worked(25);
			// Add a comment showing that the mixin was extracted from what selectors
			StringBuilder commentStringBuilder = new StringBuilder();
			commentStringBuilder.append("/*").append(System.lineSeparator());
			commentStringBuilder.append(" * ").append(LocalizedStrings.get(Keys.NEW_MIXIN_EXTRACTED_FROM)).append(System.lineSeparator());
			for (Selector involvedSelector : mixinMigrationOpportunity.getInvolvedSelectors()) {
				commentStringBuilder.append(" * ")
					.append(involvedSelector)
					.append(" <")
					.append(involvedSelector.getLocationInfo().getLineNumber())
					.append(", ")
					.append(involvedSelector.getLocationInfo().getColumnNumber())
					.append(">")
					.append(System.lineSeparator());
			}
			commentStringBuilder.append("*/").append(System.lineSeparator());
			newMixinString = commentStringBuilder.toString() + newMixinString;
			InsertEdit newMixinInsertEdit = new InsertEdit(fileContents.length(), newMixinString);	 
			fileChangeRootEdit.addChild(newMixinInsertEdit);
			resultingChange.addTextEditGroup(new TextEditGroup(String.format(LocalizedStrings.get(Keys.ADD_MIXIN_DECLARATION), mixinMigrationOpportunity.getMixinName()),
					newMixinInsertEdit));

			// 4- Add Mixin calls to the involved selectors
			progressMonitor.subTask(LocalizedStrings.get(Keys.CREATING_ADD_MIXIN_CALLS_CHANGE));
			progressMonitor.worked(25);
			for (Selector involvedSelector : mixinMigrationOpportunity.getInvolvedSelectors()) {
				String mixinCallString = mixinMigrationOpportunity.getMixinReferenceString(involvedSelector);
				mixinCallString = System.lineSeparator() + PreferencesUtil.getTabString() + mixinCallString + System.lineSeparator();
				try {
					Declaration[] positionsMap = mixinMigrationOpportunity.getMixinCallPosition(involvedSelector);

					if (positionsMap == null) { // don't touch anything, just add the call to the end
						
						int lastCharOfSelectorOffset = getLastCharOfSelectorOffset(involvedSelector);
						int previousRealCharIndex = lastCharOfSelectorOffset;
						/*
						 * If the last declaration of the selector is not being deleted,
						 * we should check for the character after that to see
						 * if it's a comma. If not, we should add a comma before adding the mixin call. 
						 */
						Declaration lastDeclaration = null;
						for (Declaration declaration : involvedSelector.getDeclarations()) {
							if (lastDeclaration == null || declaration.getDeclarationNumber() > lastDeclaration.getDeclarationNumber()) {
								lastDeclaration = declaration;
							}
						}
						
						if (!((Set<Declaration>)realDeclarationsToRemove).contains(lastDeclaration)) {
							previousRealCharIndex = lastDeclaration.getLocationInfo().getOffset() + lastDeclaration.getLocationInfo().getLength();
							if (fileContents.charAt(previousRealCharIndex) != ';') {
								mixinCallString = ";" + mixinCallString;
							}
						}
						
						InsertEdit insertNewMixinCall = new InsertEdit(previousRealCharIndex, mixinCallString);
						fileChangeRootEdit.addChild(insertNewMixinCall);
						resultingChange.addTextEditGroup(new TextEditGroup(String.format(LocalizedStrings.get(Keys.ADD_MIXIN_CALL), involvedSelector),
								insertNewMixinCall));
					} else {
						TextEditGroup addAndReOrderEditGroup = new TextEditGroup(
								String.format(LocalizedStrings.get(Keys.ADD_MIXIN_CALL_REORDER_DECLARTIONS), 
										mixinMigrationOpportunity.getMixinName(), involvedSelector));
						// Remove all declarations
						for (Declaration declaration : involvedSelector.getDeclarations()) {
							Set<Declaration> involvedDeclarations = mixinMigrationOpportunity.getInvolvedDeclarations(involvedSelector);
							if (!involvedDeclarations.contains(declaration)) {
								OffsetLength expandedAreaToRemove = RefactoringUtil.expandAreaToRemove(fileContents, declaration.getLocationInfo());
								DeleteEdit deleteEdit = new DeleteEdit(expandedAreaToRemove.getOffset(), expandedAreaToRemove.getLength());
								addAndReOrderEditGroup.addTextEdit(deleteEdit);
							}
						}

						for (int i = 0; i < positionsMap.length; i++) {
							Declaration declaration = positionsMap[i];
							String stringToAdd;
							if ("MIXIN".equals(declaration.getProperty().toUpperCase())) {
								stringToAdd = mixinCallString;
							} else {
								stringToAdd = PreferencesUtil.getTabString() + declaration.toString();
								if (i != positionsMap.length - 1) {
									 stringToAdd += ";";
								}
								stringToAdd += System.lineSeparator();
							}
							InsertEdit insertEdit = new InsertEdit(involvedSelector.getLocationInfo().getOffset() + involvedSelector.getLocationInfo().getLength() - 1, stringToAdd);
							addAndReOrderEditGroup.addTextEdit(insertEdit);
						}
						fileChangeRootEdit.addChildren(addAndReOrderEditGroup.getTextEdits());
						resultingChange.addTextEditGroup(addAndReOrderEditGroup);	
					}
				} catch (Exception ex){
					FileLogger.getLogger(LessMixinMigrationOpportunity.class).warn(ex.getMessage() + "\n" + this.toString());
				}
			}
			
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	    CompositeChange change = new CompositeChange(getName(), (new Change[]{ resultingChange })) {
	    	@Override
	    	public ChangeDescriptor getDescriptor() {
	    		String description = String.format(LocalizedStrings.get(Keys.EXTRACT_MIXIN_FROM_DECLARATIONS_IN_SELECTORS),
	    				mixinMigrationOpportunity.getMixinName(),
	    				duplicationInfo.getDeclarationNames(),
	    				duplicationInfo.getSelectorNames());
	    		MixinMigrationRefactoringDescriptor refactoringDescriptor = 
	    				new MixinMigrationRefactoringDescriptor((MixinDuplicationInfo)duplicationInfo, description, null);
				return new RefactoringChangeDescriptor(refactoringDescriptor);
	    	}
	    };
	    progressMonitor.done();
	    return change;
	}

	private int getLastCharOfSelectorOffset(Selector selector) {
		LocationInfo locationInfo = selector.getLocationInfo();
		return locationInfo.getOffset() + locationInfo.getLength() - 1;
	}

	/**
	 * Now I am using the method that the core provides.
	 * @deprecated
	 * @param declarationsToBeRemoved
	 * @return
	 */
	@SuppressWarnings("unused")
	private Set<Declaration> getRealDeclarationsToRemove(Iterable<Declaration> declarationsToBeRemoved) {
		Set<Declaration> toReturn = new HashSet<>();
		for (Declaration declarationToRemove : declarationsToBeRemoved) {
			toReturn.addAll(getRealDeclarationsToRemove(declarationToRemove));
		}
		return toReturn;
	}

	private Set<Declaration> getRealDeclarationsToRemove(Declaration declarationToRemove) {
		Set<Declaration> toReturn = new HashSet<>();
		if (declarationToRemove.isVirtualIndividualDeclarationOfAShorthand()) {
			ShorthandDeclaration parentShorthand = declarationToRemove.getParentShorthand();
			if (!(parentShorthand.isVirtual())) {
				declarationToRemove = parentShorthand;
			}
		} 
		
		if (declarationToRemove instanceof ShorthandDeclaration) {
			ShorthandDeclaration shorthandDeclaration = (ShorthandDeclaration)declarationToRemove;
			if (shorthandDeclaration.isVirtual()) {
				for (Declaration d : shorthandDeclaration.getIndividualDeclarations()) {
					toReturn.addAll(getRealDeclarationsToRemove(d));
				}
			} else {
				toReturn.add(declarationToRemove);
			}
		} else {
			toReturn.add(declarationToRemove);
		}
		return toReturn;
	}

	@Override
	public String getName() {
		return "Extract Mixin";
	}

	@Override
	public UserInputWizardPage getUserInputPage() {
		return mixinDiffWizardPage;
	}
	
}
