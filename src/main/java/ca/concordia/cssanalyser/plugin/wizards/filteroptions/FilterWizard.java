package ca.concordia.cssanalyser.plugin.wizards.filteroptions;

import java.util.List;

import org.eclipse.jface.wizard.Wizard;

import ca.concordia.cssanalyser.plugin.utility.DuplicationInfo;

public class FilterWizard extends Wizard {
	
	private FilterOptions fileterOptions;
	private FilterOptionsWizardPage filterOptionsPage;
	private final List<DuplicationInfo> duplicationInfoList;
	
	public FilterWizard(FilterOptions filterOptions, List<DuplicationInfo> duplicationInfoList) {
		this.fileterOptions = filterOptions;
		this.duplicationInfoList = duplicationInfoList;
	}
	
	@Override
	public void addPages() {
		filterOptionsPage = new FilterOptionsWizardPage(fileterOptions, duplicationInfoList);
		addPage(filterOptionsPage);
	}

	@Override
	public boolean performFinish() {
		fileterOptions = filterOptionsPage.getFilterOptions();
		return true;
	}

	public FilterOptions getFilterOptions() {
		return fileterOptions;
	}

}