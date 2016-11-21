package ca.concordia.cssdev.plugin.wizards.duplication;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

import ca.concordia.cssdev.plugin.refactoring.DuplicationRefactoring;

public class DuplicationRefactoringWizard extends RefactoringWizard {
	
	private DuplicationRefactoring refactoring;
	
	public DuplicationRefactoringWizard(DuplicationRefactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE | NO_BACK_BUTTON_ON_STATUS_DIALOG);	
		this.refactoring = refactoring;
	}

	@Override
	protected void addUserInputPages() {
		UserInputWizardPage userInputPage = refactoring.getUserInputPage();
		if (userInputPage != null)
			addPage(userInputPage);
	}

}
