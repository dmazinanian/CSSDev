package ca.concordia.cssdev.plugin.wizards.dependenciesvisualization;

import org.eclipse.jface.wizard.Wizard;

import ca.concordia.cssdev.plugin.views.dependenciesvisualization.DependenciesVisualizationView;

public class SelectorSearchWizard extends Wizard {
	
	private final DependenciesVisualizationView dependenciesView;
	private final SelectorSearchPage selectorSearchPage;

	public SelectorSearchWizard(DependenciesVisualizationView dependenciesView) {
		this.dependenciesView = dependenciesView;
		selectorSearchPage = new SelectorSearchPage();
	}

	@Override
	public void addPages() {
		addPage(selectorSearchPage);
	}
	
	@Override
	public boolean performFinish() {
		dependenciesView.performSelectorSearch(selectorSearchPage.getSelectorNameToSearch(),
				selectorSearchPage.getMediaToSearch());
		return true;
	}

}
