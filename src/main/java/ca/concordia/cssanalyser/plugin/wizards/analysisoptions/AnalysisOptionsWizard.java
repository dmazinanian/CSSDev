package ca.concordia.cssanalyser.plugin.wizards.analysisoptions;

import org.eclipse.jface.wizard.Wizard;

import ca.concordia.cssanalyser.plugin.utility.AnalysisOptions;

public class AnalysisOptionsWizard extends Wizard {
	
	private AnalysisOptions analysisOptions;
	private AnalysisOptionsWizardPage analysisOptionsPage;
	
	public AnalysisOptionsWizard(AnalysisOptions analysisOptions) {
		this.analysisOptions = analysisOptions;
	}
	
	@Override
	public void addPages() {
		analysisOptionsPage = new AnalysisOptionsWizardPage(analysisOptions);
		addPage(analysisOptionsPage);
	}

	@Override
	public boolean performFinish() {
		analysisOptions = analysisOptionsPage.getAnalysisOptions();
		return true;
	}

	public AnalysisOptions getAnalysisOptions() {
		return analysisOptions;
	}

}
