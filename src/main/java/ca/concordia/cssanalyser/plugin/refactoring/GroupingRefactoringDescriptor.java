package ca.concordia.cssanalyser.plugin.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;

public class GroupingRefactoringDescriptor extends RefactoringDescriptor {
	
	private final ItemSet itemSet;
	private final IFile selectedFile;

	protected GroupingRefactoringDescriptor(String description, String comment, ItemSet itemSet, IFile sourceFile) {
		super(GroupingRefactoring.REFACTORING_ID, sourceFile.getProject().getName(), description, comment, RefactoringDescriptor.NONE);
		this.itemSet = itemSet;
		selectedFile = sourceFile;
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus arg0) throws CoreException {
		return new GroupingRefactoring(itemSet, selectedFile);
	}

}
