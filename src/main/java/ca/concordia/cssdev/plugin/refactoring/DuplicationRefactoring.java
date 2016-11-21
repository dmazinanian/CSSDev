package ca.concordia.cssdev.plugin.refactoring;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

import ca.concordia.cssdev.plugin.utility.DuplicationInfo;

public abstract class DuplicationRefactoring extends Refactoring {

	protected final DuplicationInfo duplicationInfo;
	
	public DuplicationRefactoring(DuplicationInfo duplicationInfo) {
		this.duplicationInfo = duplicationInfo;
	}

	public abstract UserInputWizardPage getUserInputPage(); 
	
}
