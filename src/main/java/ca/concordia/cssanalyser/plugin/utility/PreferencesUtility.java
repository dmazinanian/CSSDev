package ca.concordia.cssanalyser.plugin.utility;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

public class PreferencesUtility {

	public enum ColorConstants {

		PROPERY("PROPERTY_NAME"),
		LITERAL("PROPERTY_VALUE");
		
		private String eclipseConstant;
		
		ColorConstants(String eclipseConstant) {
			this.eclipseConstant = eclipseConstant;
		}
		
		public String getValue() { return eclipseConstant; }
	}

	private static final String CSS_PREFERENCES_QUALIFIER = "org.eclipse.wst.css";
	
	public static String getTabString() {
		ScopedPreferenceStore scopedPreferenceStore = getPreferencesStore("core");
		String indentationChar = scopedPreferenceStore.getString("indentationChar");
		int indentationSize = scopedPreferenceStore.getInt("indentationSize");
		switch (indentationChar) {
		case "space":
			String tabChar = "";
			for (int i = 0; i < indentationSize; i++)
				tabChar += " ";
			return tabChar;
		case "tab":
		default:
			return "\t";
		}
	}
	
	public static Font getTextEditorFont() {
		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		ITheme currentTheme = themeManager.getCurrentTheme();
		FontRegistry fontRegistry = currentTheme.getFontRegistry();
		return fontRegistry.get("org.eclipse.wst.sse.ui.textfont");
	}
	
	public static Color getCSSColor(ColorConstants colorConstant) {
		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		ITheme currentTheme = themeManager.getCurrentTheme();
		ColorRegistry colorRegistry = currentTheme.getColorRegistry();
		return colorRegistry.get(colorConstant.getValue());
	}

	private static ScopedPreferenceStore getPreferencesStore(String qualifierSuffix) {
		return new ScopedPreferenceStore(InstanceScope.INSTANCE, CSS_PREFERENCES_QUALIFIER + "." + qualifierSuffix);
	}

	public static Color getTextEditorBackgroundColor() {
		return new Color (Display.getCurrent(), 255, 255, 255);
	}
}
