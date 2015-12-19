package ca.concordia.cssanalyser.plugin.views;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Font;

import ca.concordia.cssanalyser.plugin.utility.PreferencesUtility;

public class ExtractMixinTreeViewerDelegatingStyledCellLabelProvider extends DelegatingStyledCellLabelProvider {
	
	public ExtractMixinTreeViewerDelegatingStyledCellLabelProvider(ExtractMixinTreeViewerContentProvider contentProvider, int columnIndex) {
		super(new ExtractMixinTreeViewerStyledLabelProvider(contentProvider, columnIndex));
	}
	
	@Override
	public Font getFont(Object element) {
		return PreferencesUtility.getTextEditorFont();
	}
	
	@Override
	public void update(ViewerCell cell) {
		super.update(cell);
	}
	
	/*
	 * Dummy ToolTip methods seem to be necessary
	 * to show the custom ToolTip
	 */
	@Override
	public String getToolTipText(Object element) {
		return "";
	}
	
	@Override
	public int getToolTipDisplayDelayTime(Object object) {
		return 500;
	}
}
