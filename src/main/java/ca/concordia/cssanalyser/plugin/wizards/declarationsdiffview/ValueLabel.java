package ca.concordia.cssanalyser.plugin.wizards.declarationsdiffview;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

public abstract class ValueLabel extends CodeLabel {

	public ValueLabel(Composite parent, Color valueColor, boolean bold) {
		super(parent, bold, valueColor);
	}

	public abstract void updateLabel();

}
