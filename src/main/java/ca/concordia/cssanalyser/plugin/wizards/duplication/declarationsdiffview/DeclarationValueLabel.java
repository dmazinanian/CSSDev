package ca.concordia.cssanalyser.plugin.wizards.duplication.declarationsdiffview;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;

public class DeclarationValueLabel extends ValueLabel {
	
	private final Collection<DeclarationValue> declarationValues;

	public DeclarationValueLabel(Composite parent, Color valueColor, boolean bold, Collection<DeclarationValue> values) {
		super(parent, valueColor, bold);
		this.declarationValues = values;
		updateLabel();
	}

	@Override
	public void updateLabel() {
		setText(getDeclarationValuesString(declarationValues));
	}
	
	private String getDeclarationValuesString(Collection<DeclarationValue> values) {
		String textToReturn = "";
		for (Iterator<DeclarationValue> iterator = values.iterator(); iterator.hasNext();) {
			DeclarationValue declarationValue = iterator.next();
			textToReturn += declarationValue;
			if (iterator.hasNext()) {
				textToReturn += " ";
			}
		}
		return textToReturn;
	}
}
