package ca.concordia.cssdev.plugin.wizards.duplication.declarationsdiffview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class PropertyCheckBox extends ExtendedCheckBox {
	
	private final PropertyLabel label;

	public PropertyCheckBox(Composite parent, String text, boolean bold) {
		super(parent, SWT.NONE);
		label = new PropertyLabel(this, text, bold);
		createArea(label);
		addCheckBoxSelectionListener(new ExtendedCheckboxSelectionListener() {
			@Override
			public void selectionChanged(Object source, boolean selected) {
				label.setLabelEnabled(selected);
			}
		});
	}
	
	public void addDeclarationValueLabel(ValueLabel declarationValueLabel) {
		label.addDeclarationValueLabel(declarationValueLabel);
	}
	
	public PropertyLabel getPropertyLabel() {
		return label;
	}
}
