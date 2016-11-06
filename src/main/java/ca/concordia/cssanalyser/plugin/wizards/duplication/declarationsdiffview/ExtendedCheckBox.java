package ca.concordia.cssanalyser.plugin.wizards.duplication.declarationsdiffview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public abstract class ExtendedCheckBox extends Composite {
	
	public interface ExtendedCheckboxSelectionListener {
		void selectionChanged(Object source, boolean selected);
	}
	
	private final List<ExtendedCheckboxSelectionListener> checkBoxListeners;
	private final Button checkBox;
	private Composite otherComposite;
	
	public ExtendedCheckBox(Composite parent, int style) {
		super(parent, style);
		checkBoxListeners = new ArrayList<>();
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);
		checkBox = new Button(this, SWT.CHECK);
		checkBox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {	
					notifyCheckBoxListeners();
				}
			});
	}
	
	protected void createArea(Composite otherComposite) {
		if (this.otherComposite == null && otherComposite != null) {
			this.otherComposite = otherComposite;
			otherComposite.addMouseListener(new MouseListener() {
				@Override
				public void mouseUp(MouseEvent e) {
					checkBox.setSelection(!checkBox.getSelection());
					notifyCheckBoxListeners();
				}
				@Override
				public void mouseDown(MouseEvent e) {}
				@Override
				public void mouseDoubleClick(MouseEvent e) {}
			});
		}
	}
	
	public void setSelection(boolean selected) {
		checkBox.setSelection(selected);
	}
	
	public boolean getSelection() {
		return checkBox.getSelection();
	}

	private void notifyCheckBoxListeners() {
		for (ExtendedCheckboxSelectionListener listener : checkBoxListeners) {
			listener.selectionChanged(checkBox, checkBox.getSelection());
		}
	}
	
	protected Composite getComposite() {
		return otherComposite;
	}
	
	@Override
	public void setBackground(Color color) {
		checkBox.setBackground(color);
		if (otherComposite != null)
			otherComposite.setBackground(color);
		super.setBackground(color);
	};
	
	public void addCheckBoxSelectionListener(ExtendedCheckboxSelectionListener propertyCheckboxSelectionAdapter) {
		checkBoxListeners.add(propertyCheckboxSelectionAdapter);
	}
	
}
