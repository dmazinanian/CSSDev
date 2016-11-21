package ca.concordia.cssdev.plugin.refactoring;

public class RefactoringNotApplicableException extends Exception {
	
	public RefactoringNotApplicableException(String message) {
		super(message);
	}
	
	public RefactoringNotApplicableException(Throwable throwable) {
		super(throwable);
	}

	private static final long serialVersionUID = 1L;
	
}
