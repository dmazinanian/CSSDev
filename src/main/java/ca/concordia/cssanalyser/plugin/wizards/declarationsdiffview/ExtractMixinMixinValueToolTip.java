package ca.concordia.cssanalyser.plugin.wizards.declarationsdiffview;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinDeclaration;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinLiteral;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinParameter;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinValue;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;

public class ExtractMixinMixinValueToolTip extends ExtractMixinAbstractTooltip {

	private final MixinDeclaration mixinDeclaration;
	private final MixinValue mixinValue;

	public ExtractMixinMixinValueToolTip(Control control, MixinDeclaration declaration, MixinValue value) {
		super(control);
		this.mixinDeclaration = declaration;
		mixinValue = value;
	}

	@Override
	protected void createTooltipArea(Composite tooltipArea) {
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.MIXIN_VALUE_NAME));
		createCodeLabel(tooltipArea, mixinValue.toString());
		
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.MIXIN_DECLARATION));
		createCodeLabel(tooltipArea, mixinDeclaration.getMixinDeclarationString());
		
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.MIXIN_VALUE_TYPE));
		createNormalLabel(tooltipArea, getMixinValueType(mixinValue));
		
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.STYLED_PROPERTIES));
		createNormalLabel(tooltipArea, getPropertyAndLayersString(mixinDeclaration.getPropertyAndLayerForMixinValue(mixinValue)));
	}
	

	private String getMixinValueType(MixinValue mixinValue) {
		Keys stringKey = null;
		if (mixinValue instanceof MixinLiteral) {
			stringKey = Keys.MIXIN_LITERAL;
		} else if (mixinValue instanceof MixinParameter) {
			stringKey = Keys.MIXIN_PARAMETER;
		}
		return LocalizedStrings.get(stringKey);
	}

}
