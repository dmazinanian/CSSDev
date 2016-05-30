package ca.concordia.cssanalyser.plugin.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorType;
import ca.concordia.cssanalyser.plugin.utility.DuplicationInfo;

public class MixinMigrationRefactoringDescriptor extends RefactoringDescriptor {
	
	private final DuplicationInfo duplicationInfo;
	private final IFile selectedFile;
	private final String mixinName;
	private final PreprocessorType preprocessorType;

	protected MixinMigrationRefactoringDescriptor(String description, String comment, DuplicationInfo duplicationInfo, IFile sourceFile, String mixinName, PreprocessorType type) {
		super(MixinMigrationRefactoring.REFACTORING_ID, sourceFile.getProject().getName(), description, comment, RefactoringDescriptor.NONE);
		this.duplicationInfo = duplicationInfo;
		this.selectedFile = sourceFile;
		this.mixinName = mixinName;
		this.preprocessorType = type;
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus arg0) throws CoreException {
		MixinMigrationRefactoring mixinMigrationRefactoring = new MixinMigrationRefactoring(duplicationInfo, selectedFile, preprocessorType);
		mixinMigrationRefactoring.setMixinName(mixinName);
		return mixinMigrationRefactoring;
	}

}
