package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Display;


public class RoundedConnection extends Shape {
	
	private Point source, target;
	private int width;
	private Path path;
	private Rectangle bounds;
	private GC gc;
	
	
	public RoundedConnection(Point source, Point target, int width) {
		this.source = source;
		this.target = target;
		this.width = width;
		redrawPath();
		setAntialias(SWT.ON);
	}

	public void redrawPath() {
		Point control = new Point(source.x - width, source.y + (target.y - source.y) / 2);
		
		double angleX = -2 * control.x + 2 * target.x;
		double angleY = -2 * control.y + 2 * target.y;
		double tangentSlope = angleY / angleX;
		
		double capAngle = Math.PI / 6;
		
		int length = 10; 
		
		double newSlope1 = (tangentSlope + Math.tan(capAngle)) / (1 - tangentSlope * Math.tan(capAngle));
		double x = length / (-Math.sqrt(1 + newSlope1 * newSlope1)) + target.x;
		double y = target.y + newSlope1 * (x - target.x); //(newSlope1 * ((length / Math.sqrt(1 + newSlope1 * newSlope1))) + target.y);
		Point arrow1 = new Point((int)x, (int)y);
		
		double newSlope2 = (tangentSlope + Math.tan(-capAngle) ) / (1 - tangentSlope * Math.tan(-capAngle));
		x = -length / (Math.sqrt(1 + newSlope2 * newSlope2)) + target.x;
		y = target.y + newSlope2 * (x - target.x);//(newSlope2 * ((length / Math.sqrt(1 + newSlope2 * newSlope2))) + target.y);
		Point arrow2 = new Point((int)x, (int)y);
		
		path = new Path(Display.getCurrent());
		path.moveTo(source.x, source.y);
		path.quadTo(control.x, control.y, target.x, target.y);	
		path.lineTo(arrow1.x, arrow1.y);
		path.moveTo(target.x, target.y);
		path.lineTo(arrow2.x, arrow2.y);
		
		gc = new GC(path.getDevice());
		
		int minx = Collections.min(Arrays.asList(source.x, target.x, control.x, arrow1.x, arrow2.x));
		int maxx = Collections.max(Arrays.asList(source.x, target.x, control.x, arrow1.x, arrow2.x));
		int miny = Collections.min(Arrays.asList(source.y, target.y, control.y, arrow1.y, arrow2.y));
		int maxy = Collections.max(Arrays.asList(source.y, target.y, control.y, arrow1.y, arrow2.y));
		
		bounds = new Rectangle(minx - getLineWidth(), miny - getLineWidth(), maxx - minx + 2 * getLineWidth(), maxy - miny + 2 * getLineWidth());
		// float[] f = new float[4];
		// path.getBounds(f);
		//bounds = Rectangle((int)f[0], (int)f[1], (int)f[2], (int)f[3]);
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
	protected void fillShape(Graphics arg0) { }

	public boolean pointIsIn(int x, int y) {
		return path.contains(x, y, gc, true);
	}

	public int getConnectionWidth() {
		return width;
	}
	
	public void setSource(Point source) {
		this.source = source;
		redrawPath();
	}

	public void setTarget(Point target) {
		this.target = target;
		redrawPath();
	}
	
	public void setConnectionWidth(int width) {
		this.width = width;
		redrawPath();
	}
	
}
