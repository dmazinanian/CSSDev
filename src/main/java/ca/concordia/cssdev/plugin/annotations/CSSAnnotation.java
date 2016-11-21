package ca.concordia.cssdev.plugin.annotations;


import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;

public class CSSAnnotation extends Annotation {
	
	private final Position position;
	
	public CSSAnnotation(CSSAnnotationType type, String text, Position position) {
		super(type.toString(), false, text);
		this.position = position;
	}
	
	public Position getPosition() {
		return this.position;
	}
}
