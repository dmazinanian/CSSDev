package ca.concordia.cssanalyser.plugin.refactoring.grouping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.cssmodel.LocationInfo;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.plugin.activator.Activator;
import ca.concordia.cssanalyser.plugin.refactoring.DuplicationRefactoring;
import ca.concordia.cssanalyser.plugin.refactoring.OffsetLength;
import ca.concordia.cssanalyser.plugin.refactoring.OffsetLengthList;
import ca.concordia.cssanalyser.plugin.refactoring.RefactoringUtil;
import ca.concordia.cssanalyser.plugin.utility.DuplicationInfo;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtil;
import ca.concordia.cssanalyser.refactoring.RefactorDuplicationsToGroupingSelector;
import ca.concordia.cssanalyser.refactoring.RefactorToSatisfyDependencies;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

public class GroupingRefactoring extends DuplicationRefactoring {
	
	public static final String REFACTORING_ID = "ca.concordia.cssanalyser.plugin.groupingSelectors";
	
	private final CSSValueOverridingDependencyList dependenciesToHold;
	private List<Integer> newOrdering;
	private StyleSheet refactoredStyleSheet;
	
	public GroupingRefactoring(DuplicationInfo duplicationInfo) {
		this(duplicationInfo, null);
	}

	public GroupingRefactoring(DuplicationInfo duplicationInfo, CSSValueOverridingDependencyList dependenciesToHold) {
		super(duplicationInfo);
		this.dependenciesToHold = dependenciesToHold;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor arg0) throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor arg0) throws CoreException, OperationCanceledException {
		RefactoringStatus refactoringStatus = new RefactoringStatus();
		StyleSheet originalStyleSheet = duplicationInfo.getItemSet().getSupport().iterator().next().getParentStyleSheet();
    	refactoredStyleSheet = new RefactorDuplicationsToGroupingSelector(originalStyleSheet).groupingRefactoring(duplicationInfo.getItemSet());
    	newOrdering = new ArrayList<>();
    	if (dependenciesToHold != null) {
    		new RefactorToSatisfyDependencies().refactorToSatisfyOverridingDependencies(refactoredStyleSheet, dependenciesToHold, newOrdering);
	    	if (newOrdering.size() == 0) {
	    		refactoringStatus.addFatalError(LocalizedStrings.get(Keys.REFACTORING_NOT_APPLICABLE_DUE_TO_DEPENDENCIES));
	    	}
    	}
		return refactoringStatus;
	}

	@Override
	public Change createChange(IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException {
		ItemSet itemSet = duplicationInfo.getItemSet();
		progressMonitor.beginTask(LocalizedStrings.get(Keys.CREATING_CHANGE), 1);
		Set<Selector> emptySelectors = itemSet.getEmptySelectorsAfterRefactoring();
		Set<Declaration> declarationsToRemove = itemSet.getDeclarationsToBeRemoved();
		GroupingSelector newGrouping = itemSet.getGroupingSelector();
		String newGroupingSelectorText = getSelectorText(newGrouping, System.lineSeparator());
		
		TextFileChange result = new TextFileChange(duplicationInfo.getSourceIFile().getName(), duplicationInfo.getSourceIFile());
	    // Add the root
	    MultiTextEdit fileChangeRootEdit = new MultiTextEdit();
	    result.setEdit(fileChangeRootEdit);    
	    String fileContents;
		try {
			fileContents = IOHelper.readFileToString(duplicationInfo.getSourceIFile().getLocation().toOSString());
		    
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
		    if (this.dependenciesToHold == null) {
		    	// Just add it to the end
		    	InsertEdit insertNewGroupingEdit = new InsertEdit(fileContents.length(), System.lineSeparator() + System.lineSeparator() + newGroupingSelectorText);
		    	fileChangeRootEdit.addChild(insertNewGroupingEdit);
		    	result.addTextEditGroup(new TextEditGroup(String.format(LocalizedStrings.get(Keys.ADD_GROUPING_SELECTOR), newGrouping), insertNewGroupingEdit));
		    } else {
		    	if (newOrdering.size() == 0) {
		    		// We should not get to this point, normally
		    		throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
		    				LocalizedStrings.get(Keys.REFACTORING_NOT_APPLICABLE_DUE_TO_DEPENDENCIES))); 
		    	} else {
		    		List<Selector> selectorsList = new ArrayList<>();
		    		for (Selector selector : refactoredStyleSheet.getAllSelectors()) {
		    			selectorsList.add(selector);
		    		}
		    		boolean groupingSelectorAdded = false;
		    		for (int i = 0; i < newOrdering.size(); i++) {
		    			if (newOrdering.get(i) != i + 1) {
		    				// There is a change, we should re-order something
		    				if (newOrdering.get(i) == selectorsList.size()) { 
		    					/*
		    					 * The last selector is the grouping one,
		    					 * which does not exist in the file. So we need to simply insert it
		    					 * before the selector in the i'th position
		    					 */
		    					int positionToAdd = selectorsList.get(i).getLocationInfo().getOffset();
		    					if (positionToAdd > 0) {
		    						newGroupingSelectorText = System.lineSeparator() + System.lineSeparator() + newGroupingSelectorText;
		    					}
		    					InsertEdit insertNewGroupingEdit = new InsertEdit(positionToAdd, newGroupingSelectorText);
		    					fileChangeRootEdit.addChild(insertNewGroupingEdit);
		    					result.addTextEditGroup(new TextEditGroup(String.format(LocalizedStrings.get(Keys.ADD_GROUPING_SELECTOR), newGrouping), insertNewGroupingEdit));
		    					groupingSelectorAdded = true;
		    				} else {
		    					// Remove from the old place, put in the new place
		    					Selector selectorToReorder = selectorsList.get(newOrdering.get(i) - 1);
		    					if (!emptySelectors.contains(selectorToReorder)) {
		    						LocationInfo oldLocation = selectorToReorder.getLocationInfo();
		    						OffsetLength oldLocationExpanded = RefactoringUtil.expandAreaToRemove(fileContents, oldLocation);
		    						DeleteEdit deleteEdit = new DeleteEdit(oldLocationExpanded.getOffset(), oldLocationExpanded.getLength());
		    						fileChangeRootEdit.addChild(deleteEdit);
		    						int positionToInsert = -1;
		    						if (i == selectorsList.size() - 1) {
		    							positionToInsert = fileContents.length();
		    						} else {
		    							positionToInsert = selectorsList.get(i).getLocationInfo().getOffset();
		    						}
		    						String selectorText = getSelectorText(selectorToReorder, System.lineSeparator());
		    						if (positionToInsert > 0) {
		    							selectorText = System.lineSeparator() + System.lineSeparator() + selectorText;
		    						}
		    						InsertEdit insertEdit = new InsertEdit(positionToInsert, selectorText);
		    						fileChangeRootEdit.addChild(insertEdit);

		    						result.addTextEditGroup(new TextEditGroup(String.format(LocalizedStrings.get(Keys.REORDER_SELECTORS), selectorToReorder), 
		    								new TextEdit[] { deleteEdit, insertEdit }));
		    					}
		    				}
		    			}
		    		}
		    		if (!groupingSelectorAdded) {
				    	InsertEdit insertNewGroupingEdit = new InsertEdit(fileContents.length(), System.lineSeparator() + newGroupingSelectorText);
				    	fileChangeRootEdit.addChild(insertNewGroupingEdit);
				    	result.addTextEditGroup(new TextEditGroup(String.format(LocalizedStrings.get(Keys.ADD_GROUPING_SELECTOR), newGrouping), insertNewGroupingEdit));
		    		}
		    	}
		    }
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
	    CompositeChange change = new CompositeChange(getName(), (new Change[]{ result })) {
	    	@Override
	    	public ChangeDescriptor getDescriptor() {
	    		String description = String.format(LocalizedStrings.get(Keys.GROUP_DECLARATIONS_IN_SELECTORS), 
	    				duplicationInfo.getDeclarationNames(),
	    				duplicationInfo.getSelectorNames());
	    		return new RefactoringChangeDescriptor(new GroupingRefactoringDescriptor(duplicationInfo, description, null));
	    	}
	    };
	    progressMonitor.done();
	    return change;
	}

	private String getSelectorText(Selector selector, String newLineChar) {
		StringBuilder selectorString = new StringBuilder();
		selectorString.append(selector.toString()).append(" {").append(newLineChar);		
		for (Iterator<Declaration> iterator = selector.getDeclarations().iterator(); iterator.hasNext();) {
			Declaration declaration = iterator.next();
			selectorString.append(PreferencesUtil.getTabString());
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
