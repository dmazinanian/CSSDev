package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

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
	public void mouseDragged(MouseEvent event) {
		Point newLocation = event.getLocation();
		
		if (lastClickedPosition != null && newLocation != null && lastClickedFigureAtLocation != null) {
			if (lastClickedFigureAtLocation.getClass() == SelectorFigure.class) {
				Dimension offset = newLocation.getDifference(lastClickedPosition);
				SelectorFigure selectorFigure = (SelectorFigure)lastClickedFigureAtLocation;
				UpdateManager updateManager = lastClickedFigureAtLocation.getParent().getUpdateManager();
				LayoutManager layoutMgr = lastClickedFigureAtLocation.getParent().getLayoutManager();
				Rectangle bounds = lastClickedFigureAtLocation.getBounds().getCopy();
				updateManager.addDirtyRegion(lastClickedFigureAtLocation.getParent(), bounds);
				for (SelectorFigureConnection connection : selectorFigure.getOutgoingConnections()) {
					connection.getParent().getUpdateManager().addDirtyRegion(connection.getParent(), connection.getBounds().getCopy());
				}
				for (SelectorFigureConnection connection : selectorFigure.getIncomingConnections()) {
					connection.getParent().getUpdateManager().addDirtyRegion(connection.getParent(), connection.getBounds().getCopy());
				}
				selectorFigure.translate(offset.width, offset.height);
				selectorFigure.resetConnectionsPositions();
				bounds = bounds.getCopy().translate(offset.width, offset.height);
				layoutMgr.setConstraint(selectorFigure, bounds);
				updateManager.addDirtyRegion(lastClickedFigureAtLocation.getParent(), bounds);
				for (SelectorFigureConnection connection : selectorFigure.getOutgoingConnections()) {
					connection.getParent().getUpdateManager().addDirtyRegion(connection.getParent(), connection.getBounds().getCopy());
				}
				for (SelectorFigureConnection connection : selectorFigure.getIncomingConnections()) {
					connection.getParent().getUpdateManager().addDirtyRegion(connection.getParent(), connection.getBounds().getCopy());
				}
			}
		}
		lastClickedPosition = newLocation;
		event.consume();
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
