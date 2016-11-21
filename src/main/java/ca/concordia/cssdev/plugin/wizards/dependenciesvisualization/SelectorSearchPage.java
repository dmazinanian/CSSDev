package ca.concordia.cssdev.plugin.wizards.dependenciesvisualization;

 
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ca.concordia.cssdev.plugin.utility.LocalizedStrings;
import ca.concordia.cssdev.plugin.utility.LocalizedStrings.Keys;

public class SelectorSearchPage extends WizardPage {
	
	private Text selectorNameToSearchText;
	private Text mediaToSearchText;

	protected SelectorSearchPage() {
		super(LocalizedStrings.get(Keys.SELECTOR_SEARCH));
	}

	@Override
	public void createControl(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		
		new Label(parent, SWT.NONE).setText(LocalizedStrings.get(Keys.SELECTOR) + ":");
		selectorNameToSearchText = new Text(parent, SWT.BORDER);
		selectorNameToSearchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		new Label(parent, SWT.NONE).setText(LocalizedStrings.get(Keys.MEDIA_QUERY) + ":");
		mediaToSearchText = new Text(parent, SWT.BORDER);
		mediaToSearchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		setControl(selectorNameToSearchText);
	}
	
	public String getSelectorNameToSearch() {
		return selectorNameToSearchText.getText();
	}
	
	public String getMediaToSearch() {
		return mediaToSearchText.getText();
	}
	
}
