package ca.concordia.cssdev.plugin.refactoring.mixins;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class MixinMigrationRefactoringDescriptor extends RefactoringDescriptor {
	
	private final MixinDuplicationInfo duplicationInfo;

	protected MixinMigrationRefactoringDescriptor(MixinDuplicationInfo duplicationInfo, String description, String comment) {
		super(MixinMigrationRefactoring.REFACTORING_ID, duplicationInfo.getSourceIFile().getProject().getName(), description, comment, RefactoringDescriptor.NONE);
		this.duplicationInfo = duplicationInfo;
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus arg0) throws CoreException {
		MixinMigrationRefactoring mixinMigrationRefactoring = new MixinMigrationRefactoring(duplicationInfo);
		return mixinMigrationRefactoring;
	}

}
