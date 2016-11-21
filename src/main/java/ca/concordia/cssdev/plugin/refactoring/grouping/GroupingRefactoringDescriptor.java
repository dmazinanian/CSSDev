package ca.concordia.cssdev.plugin.refactoring.grouping;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ca.concordia.cssdev.plugin.utility.DuplicationInfo;

public class GroupingRefactoringDescriptor extends RefactoringDescriptor {
	
	private final DuplicationInfo duplicationInfo;

	protected GroupingRefactoringDescriptor(DuplicationInfo duplicationInfo, String description, String comment) {
		super(GroupingRefactoring.REFACTORING_ID, duplicationInfo.getSourceIFile().getProject().getName(), description, comment, RefactoringDescriptor.NONE);
		this.duplicationInfo = duplicationInfo;
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus arg0) throws CoreException {
		return new GroupingRefactoring(duplicationInfo);
	}

}
