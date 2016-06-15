package ca.concordia.cssanalyser.plugin.views;

import java.util.Collection;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;

public class ExtractMixinDeclarationValueTooltip extends ExtractMixinAbstractTooltip {

	private Declaration declaration;
	private Collection<DeclarationValue> values;

	public ExtractMixinDeclarationValueTooltip(Control control, Declaration declaration, Collection<DeclarationValue> value) {
		super(control);
		this.declaration = declaration;
		this.values = value;
	}

	@Override
	protected void createTooltipArea(Composite tooltipArea) {
		if (values == null) {
			tooltipArea.dispose();
		} else {
			createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.DECLARATION_VALUE));
			createCodeLabel(tooltipArea, values.toString());

			createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.DECLARATION));
			createCodeLabel(tooltipArea, declaration.toString());

			createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.STYLED_PROPERTIES));
			DeclarationValue firstValue = values.iterator().next();
			createNormalLabel(tooltipArea, getPropertyAndLayersString(firstValue.getCorrespondingStylePropertyAndLayer()));
		}
	}
	
}
