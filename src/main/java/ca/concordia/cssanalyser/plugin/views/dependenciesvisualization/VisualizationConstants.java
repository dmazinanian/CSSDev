package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public interface VisualizationConstants {
	
	public final Color SELECTOR_COLOR = new Color(Display.getCurrent(), new RGB(0, 0, 0));
	public final Color SELECTOR_BG_COLOR = new Color(Display.getCurrent(), new RGB(235, 235, 235));
	public final Color SELECTOR_BORDER_COLOR = new Color(Display.getCurrent(), new RGB(80, 80, 80));
	public final Color CASCADING_DEPENDENCY_COLOR = new Color(Display.getCurrent(), new RGB(255, 50, 50));
	public final Color SPECIFICITY_DEPENDENCY_COLOR = new Color(Display.getCurrent(), new RGB(50, 50, 255));
	public final Integer DEPENDENCY_CONNECTIONS_ALPHA_INITIAL = 10;
	public final Integer DEPENDENCY_CONNECTIONS_ALPHA_HOVERED = 255;
	public final int DEPENDENCY_CONNECTIONS_WIDTH_INITIAL = 1;
	public final int DEPENDENCY_CONNECTIONS_WIDTH_HOVERED = 2;
	
}
