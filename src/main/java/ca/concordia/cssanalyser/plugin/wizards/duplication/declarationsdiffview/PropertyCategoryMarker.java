package ca.concordia.cssanalyser.plugin.wizards.duplication.declarationsdiffview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;

import ca.concordia.cssanalyser.csshelper.CSSPropertyCategory;
import ca.concordia.cssanalyser.plugin.utility.DuplicationInfo;

public class PropertyCategoryMarker extends Composite {

	PropertyCategoryMarker(Composite parent, CSSPropertyCategory propertyCategory) {
		super(parent, SWT.NONE);
		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent arg0) {
				DuplicationInfo.drawCategoryMarks(arg0.gc, 0, 0, propertyCategory);				
			}
		});
		setToolTipText(propertyCategory.toString());
	}

}
