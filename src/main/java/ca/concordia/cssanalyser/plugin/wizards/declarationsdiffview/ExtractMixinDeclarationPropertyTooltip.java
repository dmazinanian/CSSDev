package ca.concordia.cssanalyser.plugin.wizards.declarationsdiffview;

import java.util.Set;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.MultiValuedDeclaration;
import ca.concordia.cssanalyser.cssmodel.declaration.PropertyAndLayer;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.declaration.SingleValuedDeclaration;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;

public class ExtractMixinDeclarationPropertyTooltip extends ExtractMixinAbstractTooltip {

	private final Declaration declaration;

	public ExtractMixinDeclarationPropertyTooltip(Control control, Declaration declaration) {
		super(control);
		this.declaration = declaration;
	}

	@Override
	protected void createTooltipArea(Composite tooltipArea) {

		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.SELECTOR));
		createCodeLabel(tooltipArea, declaration.getSelector().toString());
		
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.DECLARATION));
		createCodeLabel(tooltipArea, declaration.toString());
		
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.STYLED_PROPERTIES));
		Set<PropertyAndLayer> allSetPropertyAndLayers = declaration.getAllSetPropertyAndLayers(); 
		String allSetPropertyAndLayersString = getPropertyAndLayersString(allSetPropertyAndLayers);
		createNormalLabel(tooltipArea, allSetPropertyAndLayersString);
	
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.DECLARATION_TYPE));
		createNormalLabel(tooltipArea, getDeclarationTypeString(declaration));
	
	}
	
	private String getDeclarationTypeString(Declaration declaration) {
		Keys stringKey = null;
		if (declaration.getClass() == SingleValuedDeclaration.class) {
			stringKey = Keys.SINGLE_VALUED_DECLARATION;
		} else if (declaration.getClass() == MultiValuedDeclaration.class) {
			stringKey = Keys.MULTI_VALUED_DECLARATION;
		} else if (declaration.getClass() == ShorthandDeclaration.class) {
			ShorthandDeclaration shorthnad = (ShorthandDeclaration)declaration;
			if (shorthnad.isVirtual()) {
				stringKey = Keys.VIRTUAL_SHORTHAND_DECLARATION;
			} else {
				stringKey = Keys.SHORTHAND_DECLARATION;
			}
		} else {
			return "";
		}
		return LocalizedStrings.get(stringKey);
	}

}
