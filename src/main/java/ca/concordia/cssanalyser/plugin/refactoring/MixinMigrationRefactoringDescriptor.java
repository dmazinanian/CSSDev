package ca.concordia.cssanalyser.plugin.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorType;
import ca.concordia.cssanalyser.parser.ParseException;

public class MixinMigrationRefactoringDescriptor extends RefactoringDescriptor {
	
	private final ItemSet itemSet;
	private final IFile selectedFile;
	private final String mixinName;
	private final PreprocessorType preprocessorType;

	protected MixinMigrationRefactoringDescriptor(String description, String comment, ItemSet itemSet, IFile sourceFile, String mixinName, PreprocessorType type) {
		super(MixinMigrationRefactoring.REFACTORING_ID, sourceFile.getProject().getName(), description, comment, RefactoringDescriptor.NONE);
		this.itemSet = itemSet;
		this.selectedFile = sourceFile;
		this.mixinName = mixinName;
		this.preprocessorType = type;
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus arg0) throws CoreException {
		try {
			MixinMigrationRefactoring mixinMigrationRefactoring = new MixinMigrationRefactoring(itemSet, selectedFile, preprocessorType);
			mixinMigrationRefactoring.setMixinName(mixinName);
			return mixinMigrationRefactoring;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

}
