package ca.concordia.cssdev.plugin.views.dependenciesvisualization;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import ca.concordia.cssdev.plugin.utility.Constants;
import ca.concordia.cssdev.plugin.utility.LocalizedStrings;
import ca.concordia.cssdev.plugin.utility.LocalizedStrings.Keys;

public class DependenciesLegendPane extends ScalableFreeformLayeredPane {
	
	interface DependenciesDisplaySettingChangeListener {
		void settingsChanged(DependencyType type, boolean value);
	}
	
	private DependenciesDisplaySettingChangeListener clickListener;
	private boolean showCascadingDependencies = true;
	private boolean showSpecificityDependencies = true;
	private boolean showImportanceDependencies = true;
	
	private class ItemClickedMouseListener implements MouseListener {
		
		private final DependencyType type;

		public ItemClickedMouseListener(DependencyType type) {
			this.type = type;
		}
		
		@Override
		public void mouseReleased(MouseEvent arg0) {
			if (clickListener != null) {
				switch (type) {
				case CASCADING:
					showCascadingDependencies = !showCascadingDependencies;
					clickListener.settingsChanged(type, showCascadingDependencies);
					break;
				case SPECIFICITY:
					showSpecificityDependencies = !showSpecificityDependencies;
					clickListener.settingsChanged(type, showSpecificityDependencies);
					break;
				case IMPORTANCE:
					showImportanceDependencies = !showImportanceDependencies;
					clickListener.settingsChanged(type, showImportanceDependencies);
				default:
					break;
				}
			}
		}
		
		@Override
		public void mousePressed(MouseEvent arg0) {}
		
		@Override
		public void mouseDoubleClicked(MouseEvent arg0) {}
	}

	public DependenciesLegendPane() {
		
		FreeformLayer formLayer = new FreeformLayer();
		formLayer.setLayoutManager(new FreeformLayout());
		add(formLayer, "Primary");
		
		RoundedRectangle roundedRectangle = new RoundedRectangle();
		roundedRectangle.setAntialias(SWT.ON);
		roundedRectangle.setCornerDimensions(new Dimension(10, 10));
		roundedRectangle.setForegroundColor(Constants.DEPENDENCY_VISUALIZATION_LEGEND_BORDER_COLOR);
		roundedRectangle.setBackgroundColor(Constants.DEPENDENCY_VISUALIZATION_LEGEND_BG_COLOR);
		roundedRectangle.setLayoutManager(new FlowLayout());
		roundedRectangle.setBorder(new MarginBorder(5));
		
		Figure legendContents = new Figure();
		roundedRectangle.add(legendContents);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 2;
		gridLayout.verticalSpacing = 5;
		legendContents.setLayoutManager(gridLayout);
		legendContents.setForegroundColor(Constants.DEPENDENCY_VISUALIZATION_LEGEND_FORECOLOR);
		
		Figure selectorPart = new Figure();
		selectorPart.setLayoutManager(new ToolbarLayout());
		SelectorFigure selector = new SelectorFigure();
		selectorPart.add(selector);
		legendContents.add(selectorPart);
		
		Label label = new Label(LocalizedStrings.get(Keys.SELECTOR));
		legendContents.add(label);
		
		QuadraticConnection cascadingConnection = new QuadraticConnection(new Point(11, 44), new Point(29, 48), new Point(15, 46));
		cascadingConnection.setForegroundColor(Constants.CASCADING_DEPENDENCY_COLOR);
		cascadingConnection.setAntialias(SWT.ON);
		cascadingConnection.setCursor(Cursors.HAND);
		cascadingConnection.addMouseListener(new ItemClickedMouseListener(DependencyType.CASCADING));
		legendContents.add(cascadingConnection);
		
		Label cascadingLabel = new Label(LocalizedStrings.get(Keys.CASCADING_DEPENDENCY));
		cascadingLabel.setCursor(Cursors.HAND);
		cascadingLabel.addMouseListener(new ItemClickedMouseListener(DependencyType.CASCADING));

		legendContents.add(cascadingLabel);
		
		QuadraticConnection specificityConnection = new QuadraticConnection(new Point(11, 64), new Point(29, 68), new Point(15, 66));
		specificityConnection.setForegroundColor(Constants.SPECIFICITY_DEPENDENCY_COLOR);
		specificityConnection.setAntialias(SWT.ON);
		specificityConnection.setCursor(Cursors.HAND);
		specificityConnection.addMouseListener(new ItemClickedMouseListener(DependencyType.SPECIFICITY));
		legendContents.add(specificityConnection);
		
		Label specificityLabel = new Label(LocalizedStrings.get(Keys.SPECIFICITY_DEPENDENCY));
		specificityLabel.setCursor(Cursors.HAND);
		specificityLabel.addMouseListener(new ItemClickedMouseListener(DependencyType.SPECIFICITY));
		legendContents.add(specificityLabel);
		
		QuadraticConnection importanceConnection = new QuadraticConnection(new Point(11, 84), new Point(29, 88), new Point(15, 86));
		importanceConnection.setForegroundColor(Constants.IMPORTANCE_DEPENDENCY_COLOR);
		importanceConnection.setAntialias(SWT.ON);
		importanceConnection.setCursor(Cursors.HAND);
		importanceConnection.addMouseListener(new ItemClickedMouseListener(DependencyType.IMPORTANCE));
		legendContents.add(importanceConnection);
		
		Label importanceConnectionLabel = new Label(LocalizedStrings.get(Keys.IMPORTANCE_DEPENDENCY));
		importanceConnectionLabel.addMouseListener(new ItemClickedMouseListener(DependencyType.IMPORTANCE));
		importanceConnectionLabel.setCursor(Cursors.HAND);
		legendContents.add(importanceConnectionLabel);
		
		QuadraticConnection mediaQueryConnection = new QuadraticConnection(new Point(11, 104), new Point(29, 108), new Point(15, 106));
		mediaQueryConnection.setForegroundColor(Constants.MEDIA_QEURY_DEPENDENCY_COLOR);
		mediaQueryConnection.setLineStyle(SWT.LINE_DASH);
		mediaQueryConnection.setAntialias(SWT.ON);
		legendContents.add(mediaQueryConnection);
		
		Label  mediaQueryLabel = new Label(LocalizedStrings.get(Keys.MEDIA_QUERY_DEPENDENCY));
		legendContents.add(mediaQueryLabel);
		
		formLayer.add(roundedRectangle, new Rectangle(0, 0, -1, -1));
		
	}
	
	public void setDependenciesDisplaySettingsChangeListener(DependenciesDisplaySettingChangeListener listener) {
		clickListener = listener;
	}
	
}
