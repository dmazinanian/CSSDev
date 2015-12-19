package ca.concordia.cssanalyser.plugin.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
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

import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.cssmodel.LocationInfo;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorMigrationOpportunitiesDetector;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorMigrationOpportunitiesDetectorFactory;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorType;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssanalyser.parser.CSSParser;
import ca.concordia.cssanalyser.parser.CSSParserFactory;
import ca.concordia.cssanalyser.parser.CSSParserFactory.CSSParserType;
import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.plugin.utility.ItemSetUtil;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtility;
import ca.concordia.cssanalyser.plugin.views.MixinDiffWizardPage;

public class MixinMigrationRefactoring extends DuplicationRefactoring {
	
	public static final String REFACTORING_ID = "ca.concordia.cssanalyser.plugin.extractMixin";
	
	private final MixinMigrationOpportunity<?> mixinMigrationOpportunity;
	
	public MixinMigrationRefactoring(ItemSet selectedItemSet, IFile sourceFile, PreprocessorType preprocessorType) throws ParseException {
		super(selectedItemSet, sourceFile);
		CSSParser parser = CSSParserFactory.getCSSParser(CSSParserType.LESS);
		StyleSheet styleSheet = parser.parseExternalCSS(sourceFile.getLocation().toOSString());
		PreprocessorMigrationOpportunitiesDetector<?> preprocessorOpportunities = PreprocessorMigrationOpportunitiesDetectorFactory.get(preprocessorType, styleSheet);
		mixinMigrationOpportunity = preprocessorOpportunities.getMixinOpportunityFromItemSet(selectedItemSet);
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor arg0)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor arg0)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		progressMonitor.beginTask(LocalizedStrings.get(Keys.CREATING_CHANGE), 1);
		TextFileChange result = new TextFileChange(sourceFile.getName(), sourceFile);
		MultiTextEdit fileChangeRootEdit = new MultiTextEdit();
		result.setEdit(fileChangeRootEdit);

		String newMixinString = this.mixinMigrationOpportunity.toString();

		try {
			String fileContents = IOHelper.readFileToString(sourceFile.getLocation().toOSString());
			if (fileContents.charAt(fileContents.length() - 1) == '}')
				newMixinString = System.lineSeparator() + System.lineSeparator() + newMixinString;
			
			OffsetLengthList offsetsAndLengths = new OffsetLengthList();
			
			// 1- Remove the declarations being parameterized
			// MixinMigrationOpportunity#getDeclarationsToBeRemoved() returns declarations, including 
			// virtual shorthands. We need to extract the real ones from them!
			Set<Declaration> realDeclarationsToRemove = getRealDeclarationsToRemove(this.mixinMigrationOpportunity.getDeclarationsToBeRemoved());
			for (Declaration declarationToRemove : realDeclarationsToRemove) {
				LocationInfo locationInfo = declarationToRemove.getLocationInfo();
	    		OffsetLength offsetLength = RefactoringUtil.expandAreaToRemove(fileContents, locationInfo);
	    		offsetsAndLengths.add(offsetLength);
			}
			
			List<DeleteEdit> deleteEdits = new ArrayList<>();
			for (OffsetLength offsetAndLength : offsetsAndLengths.getNonOverlappingOffsetsAndLengths()) {
		    	DeleteEdit deleteEdit = new DeleteEdit(offsetAndLength.getOffset(), offsetAndLength.getLength());
		    	deleteEdits.add(deleteEdit);
		    }
			DeleteEdit[] deleteEditsArray = deleteEdits.toArray(new DeleteEdit[]{});
			fileChangeRootEdit.addChildren(deleteEditsArray);
			result.addTextEditGroup(new TextEditGroup(LocalizedStrings.get(Keys.REMOVE_DUPLICATED_DECLARATIONS), deleteEditsArray));
			
			// Add declarations if necessary
			List<InsertEdit> insertEdits = new ArrayList<>();
			for (Declaration declaration : this.mixinMigrationOpportunity.getDeclarationsToBeAdded()) {
				Selector parentSelector = declaration.getSelector();
				int lastCharOfSelectorOffset = getLastCharOfSelectorOffset(parentSelector);
				String declarationString = declaration.toString();
				InsertEdit newDeclarationInsertEdit = new InsertEdit(lastCharOfSelectorOffset, declarationString);	
				insertEdits.add(newDeclarationInsertEdit);
			}
			if (insertEdits.size() > 0) {
				InsertEdit[] insertEditsArray = insertEdits.toArray(new InsertEdit[]{});
				fileChangeRootEdit.addChildren(insertEditsArray);	
				result.addTextEditGroup(new TextEditGroup(LocalizedStrings.get(Keys.ADD_NECESSARY_DECLARATIONS), insertEditsArray));
			}
			
			// 3- Add the new Mixin 
			InsertEdit newMixinInsertEdit = new InsertEdit(fileContents.length(), newMixinString);	 
			fileChangeRootEdit.addChild(newMixinInsertEdit);
			result.addTextEditGroup(new TextEditGroup(String.format(LocalizedStrings.get(Keys.ADD_MIXIN_DECLARATION), this.mixinMigrationOpportunity.getMixinName()),
					newMixinInsertEdit));
			
			insertEdits.clear();
			// 4- Add Mixin calls to the involved selectors
			for (Selector involvedSelector : this.mixinMigrationOpportunity.getInvolvedSelectors()) {									
				String mixinCallString = this.mixinMigrationOpportunity.getMixinReferenceString(involvedSelector);
				int lastCharOfSelectorOffset = getLastCharOfSelectorOffset(involvedSelector);
				mixinCallString = PreferencesUtility.getTabString() + mixinCallString + System.lineSeparator();
				InsertEdit mixinCallInsertEdit = new InsertEdit(lastCharOfSelectorOffset, mixinCallString);	 
				fileChangeRootEdit.addChild(mixinCallInsertEdit);	
				result.addTextEditGroup(new TextEditGroup(
						String.format(LocalizedStrings.get(Keys.ADD_MIXIN_CALL), involvedSelector),
						mixinCallInsertEdit));
			}
			
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	    CompositeChange change = new CompositeChange(getName(), (new Change[]{ result })) {
	    	@Override
	    	public ChangeDescriptor getDescriptor() {
	    		String description = String.format(LocalizedStrings.get(Keys.EXTRACT_MIXIN_FROM_DECLARATIONS_IN_SELECTORS),
	    				mixinMigrationOpportunity.getMixinName(),
	    				ItemSetUtil.getDeclarationNames(itemSet),
	    				ItemSetUtil.getSelectorNames(itemSet));
	    		MixinMigrationRefactoringDescriptor refactoringDescriptor = 
	    				new MixinMigrationRefactoringDescriptor(description, null, itemSet, sourceFile, 
	    						mixinMigrationOpportunity.getMixinName(),
	    						mixinMigrationOpportunity.getPreprocessorType());
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
	
	public void setMixinName(String mixinName) {
		mixinMigrationOpportunity.setMixinName(mixinName);
	}

	@Override
	public UserInputWizardPage getUserInputPage() {
		return new MixinDiffWizardPage(this);
	}

	public MixinMigrationOpportunity<?> getMixinMigrationOpportunity() {
		return mixinMigrationOpportunity;
	}

}
