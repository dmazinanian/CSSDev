package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Insets;

import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class SelectorTooltip extends Figure {

	public SelectorTooltip(Selector selector) {
		
		setBorder(new MarginBorder(new Insets(5)));
		
		setLayoutManager(new ToolbarLayout());
		
		Label selectorName = new Label(selector.toString());
		add(selectorName);
		
		if (selector.getMediaQueryLists().size() > 0) {
			Label media = new Label(selector.getMediaQueryLists().toString());
			add(media);
		}
		
	}
	
}
