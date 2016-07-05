package ca.concordia.cssanalyser.plugin.wizards.declarationsdiffview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class SelectorCheckBox extends ExtendedCheckBox {
	
	private final CodeLabel label;

	public SelectorCheckBox(Composite parent, String text, boolean bold) {
		super(parent, SWT.NONE);
		label = new CodeLabel(this, bold, MixinDeclarationDiffView.SELECTOR_COLOR);
		label.setText(text);
		createArea(label);
		addCheckBoxSelectionListener(new ExtendedCheckboxSelectionListener() {
			@Override
			public void selectionChanged(Object source, boolean selected) {
				label.setLabelEnabled(selected);
			}
		});
	}
	
	public CodeLabel getSelectorLabel() {
		return label;
	}
}
