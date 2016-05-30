package ca.concordia.cssanalyser.plugin.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

import ca.concordia.cssanalyser.plugin.utility.DuplicationInfo;

public abstract class DuplicationRefactoring extends Refactoring {

	protected final DuplicationInfo duplicationInfo;
	protected final IFile sourceFile;
	
	public DuplicationRefactoring(DuplicationInfo duplicationInfo, IFile sourceFile) {
		this.duplicationInfo = duplicationInfo;
		this.sourceFile = sourceFile;
	}

	public abstract UserInputWizardPage getUserInputPage(); 
	
}
