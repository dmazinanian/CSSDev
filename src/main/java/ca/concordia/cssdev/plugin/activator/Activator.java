package ca.concordia.cssdev.plugin.activator;

import java.util.Locale;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ca.concordia.cssdev.plugin.utility.LocalizedStrings;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "ca.concordia.cssdev.plugin";
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
	
}
