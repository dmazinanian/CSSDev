package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import org.eclipse.draw2d.AbstractPointListShape;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Display;


public class QuadraticConnection extends AbstractPointListShape {
	
	final static double CAP_ANGLE = Math.PI / 6;
	final static int CAP_LENGTH = 10;
	
	private Point source, destination, control;
	private Path path;
	private Rectangle bounds;
	private GC gc;

	public QuadraticConnection() {}
	
	public QuadraticConnection(Point source, Point destination, Point control) {
		this.source = source;
		this.destination = destination;
		this.control = control;
		redrawPath();
	}

	protected void redrawPath() {
		
		if (source != null && destination != null) {
			
			if (control == null) {
				// Put the control at the midpoint, the line will look straight
				control = new Point((destination.x + source.x) / 2, (destination.y + source.y) / 2);
			}

			double angleX = -2 * control.x + 2 * destination.x;
			double angleY = -2 * control.y + 2 * destination.y;
			double tanglentLineAngle = Math.atan2(angleY, angleX);
			
			Point arrow1 = new Point((int)(destination.x - CAP_LENGTH * Math.cos(tanglentLineAngle - CAP_ANGLE)), 
					(int)(destination.y - CAP_LENGTH * Math.sin(tanglentLineAngle - CAP_ANGLE)));
			
			Point arrow2 = new Point((int)(destination.x - CAP_LENGTH * Math.cos(tanglentLineAngle + CAP_ANGLE)), 
					(int)(destination.y - CAP_LENGTH * Math.sin(tanglentLineAngle + CAP_ANGLE)));
			
			path = new Path(Display.getCurrent());
			path.moveTo(source.x, source.y);
			path.quadTo(control.x, control.y, destination.x, destination.y);	
			path.lineTo(arrow1.x, arrow1.y);
			path.moveTo(destination.x, destination.y);
			path.lineTo(arrow2.x, arrow2.y);

			gc = new GC(path.getDevice());

			float[] boundsArray = new float[4];
			path.getBounds(boundsArray);
			bounds = new Rectangle((int)boundsArray[0] - getLineWidth(), (int)boundsArray[1] - getLineWidth(), 
					(int)boundsArray[2] + 2 * getLineWidth(), (int)boundsArray[3] + 2 * getLineWidth());
			
		}
	}

	@Override
	public Rectangle getBounds() {
		return bounds;
	}

	@Override
	protected void outlineShape(Graphics g) {
		g.setLineStyle(getLineStyle());
		g.drawPath(path);		
	}

	@Override
	protected boolean shapeContainsPoint(int x, int y) {
		return path.contains(x, y, gc, true);
	}

	@Override
	protected void fillShape(Graphics arg0) { }

	protected void setSource(Point source) {
		this.source = source;
	}

	protected void setDestination(Point target) {
		this.destination = target;
	}

	protected void setControl(Point control) {
		this.control = control;
	}

}
