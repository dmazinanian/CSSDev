package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;

public class DependenciesFigurePaneMouseListener implements MouseMotionListener, MouseListener {

	private final DependenciesFigurePane dependenciesPane;
	private Point lastClickedPosition;
	private IFigure lastClickedFigureAtLocation;

	public DependenciesFigurePaneMouseListener(DependenciesFigurePane dependenciesPane) {
		this.dependenciesPane = dependenciesPane;
	}

	@Override
	public void mouseDoubleClicked(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {
		lastClickedPosition = arg0.getLocation();
		lastClickedFigureAtLocation = dependenciesPane.findFigureAt(lastClickedPosition);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		lastClickedPosition = null;
		lastClickedFigureAtLocation = null;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		if (lastClickedPosition != null && lastClickedFigureAtLocation != null) {
			//System.out.println(figureAtLocation);
			if (lastClickedFigureAtLocation instanceof SelectorFigure) {
				SelectorFigure selectorFigure = (SelectorFigure) lastClickedFigureAtLocation;
				selectorFigure.translate(arg0.x - lastClickedPosition.x, arg0.y - lastClickedPosition.y);
				selectorFigure.resetConnectionsPosition();
				lastClickedPosition = arg0.getLocation();
				dependenciesPane.repaint();
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mouseHover(MouseEvent arg0) {}

	@Override
	public void mouseMoved(MouseEvent arg0) {}
	
}
