package ca.concordia.cssdev.plugin.wizards.duplication.declarationsdiffview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import ca.concordia.cssanalyser.cssmodel.declaration.PropertyAndLayer;
import ca.concordia.cssdev.plugin.utility.LocalizedStrings;
import ca.concordia.cssdev.plugin.utility.PreferencesUtil;
import ca.concordia.cssdev.plugin.utility.LocalizedStrings.Keys;

public abstract class ExtractMixinAbstractTooltip extends ToolTip {
	
	public ExtractMixinAbstractTooltip(Control control) {
		super(control);
		setShift(new Point(-2, -2));
	}
	
	@Override
	public boolean isHideOnMouseDown() {
		return false;
	}
	
	protected abstract void createTooltipArea(Composite tooltipArea);
	
	@Override
	protected Composite createToolTipContentArea(Event event, Composite parent) {
		Composite tooltipContentArea = getTooltipContentArea(parent);
		createTooltipArea(tooltipContentArea);
		return tooltipContentArea;
	}
	
	private Composite getTooltipContentArea(Composite parent) {
		Composite tooltipArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 10;
		tooltipArea.setLayout(layout);
		return tooltipArea;
	}

	protected Label createCodeLabel(Composite tooltipArea, String labelText) {
		Label label = new Label(tooltipArea, SWT.BORDER);
		label.setText(labelText);
		label.setFont(PreferencesUtil.getTextEditorFont());
		label.setBackground(PreferencesUtil.getTextEditorBackgroundColor());
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		return label;
	}
	
	protected Label createNormalLabel(Composite tooltipArea, String labelText) {
		Label label = new Label(tooltipArea, SWT.NONE);
		label.setText(labelText);
		return label;
	}

	protected Label createDescriptionLabel(Composite tooltipArea, String labelText) {
		Label label = createNormalLabel(tooltipArea, labelText + ":");
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(label.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(Display.getCurrent());		
		label.setFont(boldFont);
		return label;
	}
	
	protected String getPropertyAndLayersString(PropertyAndLayer correspondingStylePropertyAndLayer) {
		List<PropertyAndLayer> dummyList = new ArrayList<>();
		dummyList.add(correspondingStylePropertyAndLayer);
		return getPropertyAndLayersString(dummyList);
	}
	
	protected String getPropertyAndLayersString(Collection<PropertyAndLayer> allSetPropertyAndLayers) {
		Map<Integer, List<PropertyAndLayer>> propertiesAndLayersMap = new HashMap<>();
		for (PropertyAndLayer propertyAndLayer : allSetPropertyAndLayers) {
			int layer = propertyAndLayer.getPropertyLayer();
			List<PropertyAndLayer> propertiesAndLayersListForThisLayer = propertiesAndLayersMap.get(layer);
			if (propertiesAndLayersListForThisLayer == null) {
				propertiesAndLayersListForThisLayer = new ArrayList<>();
				propertiesAndLayersMap.put(layer, propertiesAndLayersListForThisLayer);
			}
			propertiesAndLayersListForThisLayer.add(propertyAndLayer);
		}
		
		StringBuilder toReturn = new StringBuilder();
		if (propertiesAndLayersMap.keySet().size() == 1) {
			List<PropertyAndLayer> list = propertiesAndLayersMap.get(propertiesAndLayersMap.keySet().iterator().next());
			getPropertiesForLayerString(toReturn, list, false);
		} else if (propertiesAndLayersMap.keySet().size() > 1) {
			for (Iterator<Integer> iterator = propertiesAndLayersMap.keySet().iterator(); iterator.hasNext();) {
				int layer = iterator.next();
				toReturn.append(LocalizedStrings.get(Keys.PROPERTY_LAYER) + " " + layer + ":\n");
				List<PropertyAndLayer> list = propertiesAndLayersMap.get(layer);
				getPropertiesForLayerString(toReturn, list, true);
				if (iterator.hasNext())
					toReturn.append("\n\n");
			}
		}
		
		return toReturn.toString();
	}
	
	private void getPropertiesForLayerString(StringBuilder builder, List<PropertyAndLayer> list, boolean shouldIndent) {
		for (Iterator<PropertyAndLayer> iterator = list.iterator(); iterator.hasNext();) {
			PropertyAndLayer propertyAndLayer = iterator.next();
			if (shouldIndent) {
				builder.append("  ");
			}
			builder.append(propertyAndLayer.getPropertyName());
			if (iterator.hasNext())
				builder.append("\n");
		}
	}
}
