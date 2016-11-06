package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;

import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.plugin.activator.Activator;
import ca.concordia.cssanalyser.plugin.utility.Constants;
import ca.concordia.cssanalyser.plugin.utility.ViewsUtil;

public class SelectorToolsFigure extends Figure {
	
	private static class MyImageButton extends ImageFigure {
		
		public MyImageButton(String tooltipText, String icon, Runnable runnable) {
			super(Activator.getImageDescriptor(icon).createImage());
			setToolTip(new Label(tooltipText));
			setCursor(Cursors.HAND);
			addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent arg0) {}
				
				@Override
				public void mousePressed(MouseEvent arg0) {
					runnable.run();
				}
				@Override
				public void mouseDoubleClicked(MouseEvent arg0) {}
			});
		}
		
	}

	public SelectorToolsFigure(Selector selector) {
		FlowLayout layout = new FlowLayout(true);
		setLayoutManager(layout);
		Runnable showAffectedDomElements = () -> ((AffectedDOMNodesView)ViewsUtil.openView(AffectedDOMNodesView.ID)).showAffectedDOMNodesFor(selector);
		add(new MyImageButton("Show affected DOM elements", Constants.DOM_ICON, showAffectedDomElements));
	}
	
}
