package ca.concordia.cssdev.plugin.wizards.duplication.declarationsdiffview;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinParameter;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinValue;

public class MixinDeclarationValueLabel extends ValueLabel {

	private final MixinValue mixinValue;
	
	public MixinDeclarationValueLabel(Composite parent, Color valueColor, boolean bold, MixinValue mixinValue) {
		super(parent, valueColor, bold);
		this.mixinValue = mixinValue;
		updateLabel();
	}

	@Override
	public void updateLabel() {
		if (mixinValue instanceof MixinParameter) {
			MixinParameter mixinParameter = (MixinParameter) mixinValue;
			setText(mixinParameter.getName());
		} else {
			setText(mixinValue.toString());
		}
	}
}
