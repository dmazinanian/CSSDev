package ca.concordia.cssanalyser.plugin.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;

public abstract class DuplicationRefactoring extends Refactoring {

	protected final ItemSet itemSet;
	protected final IFile sourceFile;
	
	public DuplicationRefactoring(ItemSet itemSet, IFile sourceFile) {
		this.itemSet = itemSet;
		this.sourceFile = sourceFile;
	}

	public abstract UserInputWizardPage getUserInputPage(); 
	
}
