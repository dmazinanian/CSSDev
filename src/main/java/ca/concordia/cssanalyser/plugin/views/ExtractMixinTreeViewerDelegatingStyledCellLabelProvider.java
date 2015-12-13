package ca.concordia.cssanalyser.plugin.views;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Font;

import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtility;

public class ExtractMixinTreeViewerDelegatingStyledCellLabelProvider extends DelegatingStyledCellLabelProvider {
	
	public ExtractMixinTreeViewerDelegatingStyledCellLabelProvider(MixinMigrationOpportunity<?> mixinMigrationOpportunity, int columnIndex) {
		super(new ExtractMixinTreeViewerStyledLabelProvider(mixinMigrationOpportunity, columnIndex));
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
