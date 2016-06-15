package ca.concordia.cssanalyser.plugin.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ca.concordia.cssanalyser.plugin.utility.FontUtil;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtil;

public class CodeLabel extends Composite {
	
	private final Color foreGroundColor;
	private final Label label;

	public CodeLabel(Composite composite, boolean bold, Color foreGroundColor) {
		super(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);
		label = new Label(this, SWT.NONE);
		this.foreGroundColor = foreGroundColor;
		label.setForeground(foreGroundColor);
		final Font font = PreferencesUtil.getTextEditorFont();
		label.setFont(bold ? FontUtil.getBoldFont(font) : font);
		pack();
	}

	public void setText(String text) {
		label.setText(text);
		getParent().layout();
	}

	public void setLabelEnabled(boolean selection) {
		if (selection) {
			label.setForeground(foreGroundColor);
		} else {
			label.setForeground(MixinDeclarationDiffView.DISABLED_LABEL_COLOR);
		}
	}
	
	public Label getUnderlayingLabel() {
		return label;
	}
	
	@Override
	public void setBackground(Color color) {
		label.setBackground(color);
		super.setBackground(color);
	}

	@Override
	public void addMouseListener(MouseListener listener) {
		label.addMouseListener(listener);
		super.addMouseListener(listener);
	}
	
}
