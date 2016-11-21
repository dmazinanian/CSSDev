package ca.concordia.cssdev.plugin.utility;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Point;

import ca.concordia.cssdev.plugin.activator.Activator;

public class ImagesUtil {

	public static ImageDescriptor getImageDescriptor(String path) {
		return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, path);
	}

	public static ImageDescriptor getOverlayedImagesDescriptor(ImageDescriptor main, ImageDescriptor overlay, Point position) {
		CompositeImageDescriptor descriptor = new CompositeImageDescriptor() {
						
			@Override
			protected Point getSize() {
				return new Point(main.getImageData().width, main.getImageData().height);
			}
			
			@Override
			protected void drawCompositeImage(int arg0, int arg1) {
				drawImage(main.getImageData(), 0, 0);
				drawImage(overlay.getImageData(), position.x, position.y);
			}
		};
		
		return descriptor;
	}

}
