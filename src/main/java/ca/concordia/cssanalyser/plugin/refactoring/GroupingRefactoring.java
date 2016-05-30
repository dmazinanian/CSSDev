package ca.concordia.cssanalyser.plugin.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.plugin.utility.DuplicationInfo;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtility;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

public class GroupingRefactoring extends DuplicationRefactoring {
	
	public static final String REFACTORING_ID = "ca.concordia.cssanalyser.plugin.groupingSelectors";
	
	private final CSSValueOverridingDependencyList dependenciesToHold;
	
	public GroupingRefactoring(DuplicationInfo duplicationInfo, IFile selectedFile) {
		this(duplicationInfo, null, selectedFile);
	}

	public GroupingRefactoring(DuplicationInfo duplicationInfo, CSSValueOverridingDependencyList dependenciesToHold, IFile selectedFile) {
		super(duplicationInfo, selectedFile);
		this.dependenciesToHold = dependenciesToHold;
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
		ItemSet itemSet = duplicationInfo.getItemSet();
		progressMonitor.beginTask(LocalizedStrings.get(Keys.CREATING_CHANGE), 1);
		Set<Selector> emptySelectors = itemSet.getEmptySelectorsAfterRefactoring();
		Set<Declaration> declarationsToRemove = itemSet.getDeclarationsToBeRemoved();
		GroupingSelector newGrouping = itemSet.getGroupingSelector();
		String newGroupingSelectorText = getSelectorText(newGrouping, System.lineSeparator());
		TextFileChange result = new TextFileChange(sourceFile.getName(), sourceFile);
	    // Add the root
	    MultiTextEdit fileChangeRootEdit = new MultiTextEdit();
	    result.setEdit(fileChangeRootEdit);    
	    String fileContents;
		try {
			fileContents = IOHelper.readFileToString(sourceFile.getLocation().toOSString());
		    
		    OffsetLengthList offsetsAndLengths = new OffsetLengthList();
		    
		    // First, remove empty selectors
		    for (Selector selector : emptySelectors) {
		    	LocationInfo locationInfo = selector.getLocationInfo();
	    		OffsetLength offsetLength = RefactoringUtil.expandAreaToRemove(fileContents, locationInfo);
	    		offsetsAndLengths.add(offsetLength);
		    }
		    
		    // Then remove other declarations
		    for (Declaration declarationToRemove : declarationsToRemove) {
		    	if (!emptySelectors.contains(declarationToRemove.getSelector())) {
		    		LocationInfo locationInfo = declarationToRemove.getLocationInfo();
		    		OffsetLength offsetLength = RefactoringUtil.expandAreaToRemove(fileContents, locationInfo);
		    		offsetsAndLengths.add(offsetLength);
		    	}
		    }
		    
		    List<DeleteEdit> deleteEdits = new ArrayList<>();
		    for (OffsetLength offsetAndLength : offsetsAndLengths.getNonOverlappingOffsetsAndLengths()) {
		    	DeleteEdit deleteEdit = new DeleteEdit(offsetAndLength.getOffset(), offsetAndLength.getLength());
		    	deleteEdits.add(deleteEdit);
		    }
		    DeleteEdit[] deleteEditsArray = deleteEdits.toArray(new DeleteEdit[]{});
		    fileChangeRootEdit.addChildren(deleteEditsArray);
		    result.addTextEditGroup(new TextEditGroup(LocalizedStrings.get(Keys.REMOVE_DUPLICATED_DECLARATIONS), deleteEditsArray));
		    
		    // Add grouping selector
		    if (fileContents.charAt(fileContents.length() - 1) == '}')
				newGroupingSelectorText = System.lineSeparator() + System.lineSeparator() + newGroupingSelectorText;
		    InsertEdit insertNewGroupingEdit;
		    if (this.dependenciesToHold == null) {
		    	insertNewGroupingEdit = new InsertEdit(fileContents.length(), newGroupingSelectorText);	 
		    } else {
		    	insertNewGroupingEdit = null;
		    }
		    fileChangeRootEdit.addChild(insertNewGroupingEdit);
		    result.addTextEditGroup(new TextEditGroup(String.format(LocalizedStrings.get(Keys.ADD_GROUPING_SELECTOR), newGrouping), insertNewGroupingEdit));
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
	    CompositeChange change = new CompositeChange(getName(), (new Change[]{ result })) {
	    	@Override
	    	public ChangeDescriptor getDescriptor() {
	    		String description = String.format(LocalizedStrings.get(Keys.GROUP_DECLARATIONS_IN_SELECTORS), 
	    				duplicationInfo.getDeclarationNames(),
	    				duplicationInfo.getSelectorNames());
	    		return new RefactoringChangeDescriptor(new GroupingRefactoringDescriptor(description, null, duplicationInfo, sourceFile));
	    	}
	    };
	    progressMonitor.done();
	    return change;
	}

	private String getSelectorText(Selector newGrouping, String newLineChar) {
		StringBuilder selectorString = new StringBuilder();
		selectorString.append(newGrouping.toString()).append(" {").append(newLineChar);		
		for (Iterator<Declaration> iterator = newGrouping.getDeclarations().iterator(); iterator.hasNext();) {
			Declaration declaration = iterator.next();
			selectorString.append(PreferencesUtility.getTabString());
			selectorString.append(declaration.toString());
			if (iterator.hasNext())
				selectorString.append(";").append(newLineChar);
			else
				selectorString.append(newLineChar);
		}
		selectorString.append("}");
		return selectorString.toString();
	}

	@Override
	public String getName() {
		return "Grouping Selectors";
	}

	@Override
	public UserInputWizardPage getUserInputPage() {
		return null;
	}

}
