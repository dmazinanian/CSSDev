package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;

import ca.concordia.cssanalyser.plugin.utility.Constants;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependency;

public class SelectorFigureConnection extends QuadraticConnection {

	private final SelectorFigure sourceFigure;
	private final SelectorFigure destinationFigure;
	private final List<CSSValueOverridingDependency> forDependencies = new ArrayList<>();
	private DependencyType dependencyType;

	private enum AnchorSide {
		TOP, 
		RIGHT,
		BOTTOM,
		LEFT;
	}

	public SelectorFigureConnection(SelectorFigure sourceFigure, SelectorFigure destinationFigure) {
		this.sourceFigure = sourceFigure;
		this.destinationFigure = destinationFigure;
		calculateAnchorPoints();
		addMouseListener(new SelectorFigureConnectionMouseListener());		
		addMouseMotionListener(new SelectorFigureConnectionMouseListener());
	}
	
	public void calculateAnchorPoints() {
		Point sourceBottom = getAnchorPosition(getSourceFigure(), AnchorSide.BOTTOM);
		if (getSourceFigure() == getDestinationFigure()) {
			setSource(new Point(sourceBottom.x - 10, sourceBottom.y));
			setDestination(new Point(sourceBottom.x + 10, sourceBottom.y));
			setControl(new Point(sourceBottom.x, sourceBottom.y + 30));
		} else {
			Point sourceTop = getAnchorPosition(getSourceFigure(), AnchorSide.TOP);
			Point destinationTop = getAnchorPosition(getDestinationFigure(), AnchorSide.TOP);
			Point destinationBottom = getAnchorPosition(getDestinationFigure(), AnchorSide.BOTTOM);
			if (sourceBottom.y < destinationTop.y) {
				setSource(sourceBottom);
				setDestination(destinationTop);
				setControl(null);
			} else if (sourceTop.y > destinationBottom.y) {
				setSource(sourceTop);
				setDestination(destinationBottom);
				setControl(null);
			} else {
				setSource(sourceBottom);
				setDestination(destinationBottom);
				setControl(new Point((sourceBottom.x + destinationBottom.x) / 2, Math.max(sourceBottom.y, destinationBottom.y) + 30));
			}
		}
		
		redrawPath();
		
	}
	
	public void hover() {
		setAlpha(Constants.DEPENDENCY_CONNECTIONS_ALPHA_HOVERED);
		setLineWidth(Constants.DEPENDENCY_CONNECTIONS_WIDTH_HOVERED);
	}
	
	public void unhover() {
		setAlpha(Constants.DEPENDENCY_CONNECTIONS_ALPHA_INITIAL);
		setLineWidth(Constants.DEPENDENCY_CONNECTIONS_WIDTH_INITIAL);
	}
	
	@Override
	public void setEnabled(boolean value) {
		if (value) {
			setAlpha(Constants.DEPENDENCY_CONNECTIONS_ALPHA_INITIAL);
		} else {
			setAlpha(Constants.DEPENDENCY_CONNECTIONS_ALPHA_DESABLED);
		}
		super.setEnabled(value);
	}

	private Point getAnchorPosition(SelectorFigure figure, AnchorSide side) {
		int x = figure.getLocation().x;
		int y = figure.getLocation().y;
		switch (side) {
		case TOP:
			x += figure.getPreferredSize().width / 2;
			break;
		case BOTTOM:
			x += figure.getPreferredSize().width / 2;
			y += figure.getPreferredSize().height;
			break;
		case LEFT:
			y += figure.getPreferredSize().height / 2;
			break;
		case RIGHT:
			x += figure.getPreferredSize().width;
			y += figure.getPreferredSize().height / 2;
			break;
		default:
			break;
		}
		return new Point(x, y);
	}
	
	public void addDependency(CSSValueOverridingDependency dependency) {
		forDependencies.add(dependency);
	}

	public List<CSSValueOverridingDependency> getDependencies() {
		return forDependencies;
	}
	
	public void setConnectionType(DependencyType type) {
		dependencyType = type;
		switch(type) {
		case CASCADING:
			setForegroundColor(Constants.CASCADING_DEPENDENCY_COLOR);
			break;
		case IMPORTANCE:
			setForegroundColor(Constants.IMPORTANCE_DEPENDENCY_COLOR);
			break;
		case SPECIFICITY:
			setForegroundColor(Constants.SPECIFICITY_DEPENDENCY_COLOR);
			break;
		default:
			break;
		}
	}
	
	public DependencyType getDependencyType() {
		return dependencyType;
	}

	public SelectorFigure getSourceFigure() {
		return sourceFigure;
	}

	public SelectorFigure getDestinationFigure() {
		return destinationFigure;
	}

}
