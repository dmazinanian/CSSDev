package ca.concordia.cssanalyser.plugin.activator;

import java.util.Locale;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "css-analyser-eclipse-plugin";
	private static Activator plugin;
	public Activator() {}
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		LocalizedStrings.setBundle(Locale.getDefault());
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
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
