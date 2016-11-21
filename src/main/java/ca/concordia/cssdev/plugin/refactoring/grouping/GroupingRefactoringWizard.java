package ca.concordia.cssdev.plugin.refactoring.grouping;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class GroupingRefactoringWizard extends RefactoringWizard {

	public GroupingRefactoringWizard(Refactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE | NO_BACK_BUTTON_ON_STATUS_DIALOG);
		
	}

	@Override
	protected void addUserInputPages() {
		// TODO Auto-generated method stub

	}

}
