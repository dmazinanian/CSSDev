package ca.concordia.cssdev.plugin.wizards.duplication.declarationsdiffview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class PropertyLabel extends Composite {
	
	private final String text;
	private final boolean bold;
	private final List<ValueLabel> declarationValueLabels;
	
	protected CodeLabel propertyLabel;
	protected CodeLabel colonLabel;
	
	public PropertyLabel(Composite parent, String text, boolean bold) {
		super(parent, SWT.NONE);
		this.text = text;
		this.bold = bold;
		this.declarationValueLabels = new ArrayList<>();
		initComposite();
		addWidgets();
	}

	private void initComposite() {
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		layout.marginHeight = 1;
		setLayout(layout);
	}
	
	protected void addWidgets() {
		propertyLabel = new CodeLabel(this, bold, MixinDeclarationDiffView.PROPERTY_FOREGROUND_COLOR);
		propertyLabel.setText(text);
		GridData propertyLayoutData = new GridData();
		propertyLayoutData.horizontalIndent = 3;
		propertyLabel.setLayoutData(propertyLayoutData);
		
		colonLabel = new CodeLabel(this, bold, MixinDeclarationDiffView.NORMAL_TEXT_COLOR);
		colonLabel.setText(":");
	}
	
	protected void setLabelEnabled(boolean selection) {
		propertyLabel.setLabelEnabled(selection);
		colonLabel.setLabelEnabled(selection);
		for (ValueLabel valueLabel : declarationValueLabels) {
			valueLabel.setLabelEnabled(selection);
		}
	}
	
	public void addDeclarationValueLabel(ValueLabel mixinDeclarationValueLabel) {
		declarationValueLabels.add(mixinDeclarationValueLabel);
	}
	
	public Control getObjectForTooltip() {
		return propertyLabel.getUnderlayingLabel();
	}
	
	@Override
	public void setBackground(Color color) {
		propertyLabel.setBackground(color);
		colonLabel.setBackground(color);
		super.setBackground(color);
	}
	
	@Override
	public void addMouseListener(MouseListener listener) {
		propertyLabel.addMouseListener(listener);
		colonLabel.addMouseListener(listener);
		super.addMouseListener(listener);
	}
	
	public CodeLabel getPropertyLabel() {
		return propertyLabel;
	}

}
