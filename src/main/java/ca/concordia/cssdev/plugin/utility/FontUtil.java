package ca.concordia.cssdev.plugin.utility;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class FontUtil {

	public static Font getBoldFont(Font font) {
		FontData fontData = font.getFontData()[0];
		Font fontToReturn = new Font(Display.getCurrent(), 
				new FontData(fontData.getName(), fontData
			    .getHeight(), SWT.BOLD));
		return fontToReturn;
	}
	
}
