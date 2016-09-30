package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;

public class DependenciesLegendPane extends ScalableFreeformLayeredPane {

	public DependenciesLegendPane() {
		
		FreeformLayer formLayer = new FreeformLayer();
		formLayer.setLayoutManager(new FreeformLayout());
		add(formLayer, "Primary");
		
		RoundedRectangle roundedRectangle = new RoundedRectangle();
		roundedRectangle.setAntialias(SWT.ON);
			
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 2;
		gridLayout.verticalSpacing = 5;
		roundedRectangle.setLayoutManager(gridLayout);
		
		Figure selectorPart = new Figure();
		selectorPart.setLayoutManager(new ToolbarLayout());
		SelectorFigure selector = new SelectorFigure();
		selectorPart.add(selector);
		roundedRectangle.add(selectorPart);
		
		Label label = new Label(LocalizedStrings.get(Keys.SELECTOR));
		roundedRectangle.add(label);
		
		RoundedConnection cascadingConnection = new RoundedConnection(new Point(6, 38), new Point(24, 42), 1);
		cascadingConnection.setForegroundColor(VisualizationConstants.CASCADING_DEPENDENCY_COLOR);
		roundedRectangle.add(cascadingConnection);
		
		Label cascadingLabel = new Label(LocalizedStrings.get(Keys.CASCADING_DEPENDENCY));
		roundedRectangle.add(cascadingLabel);
		
		RoundedConnection specificityConnection = new RoundedConnection(new Point(6, 58), new Point(24, 62), 1);
		specificityConnection.setForegroundColor(VisualizationConstants.SPECIFICITY_DEPENDENCY_COLOR);
		roundedRectangle.add(specificityConnection);
		
		Label specificityLabel = new Label(LocalizedStrings.get(Keys.SPECIFICITY_DEPENDENCY));
		roundedRectangle.add(specificityLabel);
		
		formLayer.add(roundedRectangle, new Rectangle(0, 0, -1, -1));
				
	}
	
}
