package ca.concordia.cssdev.plugin.utility;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import ca.concordia.cssdev.plugin.activator.Activator;

public class FilesUtil {
	
	public static String getAbsolutePathForFile(String relativePath) {
		URL fileURL;
		try {
			fileURL = FileLocator.toFileURL(FileLocator.find(Platform.getBundle(Activator.PLUGIN_ID), new Path(relativePath), null));
			return new File(fileURL.getFile()).getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
}
