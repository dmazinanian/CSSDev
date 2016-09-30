package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.part.ViewPart;

import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

public class DependenciesVisualizationView extends ViewPart {
	
	public static final String ID = "ca.concordia.cssanalyser.plugin.views.dependenciesvisualization.DependenciesVisualizationView";

	private FigureCanvas figureCanvas;
	private FigureCanvas legendArea;
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		
		Composite figureComposite = new Composite(sashForm, SWT.NONE);
		figureComposite.setLayout(new FillLayout());
		figureCanvas = new FigureCanvas(figureComposite, SWT.DOUBLE_BUFFERED);
		figureCanvas.setBackground(ColorConstants.white);
		
		Composite legendComposite = new Composite(sashForm, SWT.NONE);
		legendComposite.setLayout(new FillLayout());
		legendArea = new FigureCanvas(legendComposite, SWT.DOUBLE_BUFFERED);
		legendArea.setBackground(ColorConstants.white);
		
		parent.addListener(SWT.Resize, new Listener() {
	        @Override
	        public void handleEvent(Event arg0) {
	        	int legendWidth = 220;
	        	int width = parent.getClientArea().width;
	            int weightFigure = width - legendWidth;
	            int weightLegend = legendWidth;
	    		sashForm.setWeights(new int[] { weightFigure, weightLegend });
	        }
	    });
	}

	public void showDependenciesGraph(CSSValueOverridingDependencyList dependencies, IProgressMonitor progressMonitor) {
		figureCanvas.setViewport(new FreeformViewport());
		figureCanvas.setContents(new DependenciesFigurePane(dependencies, progressMonitor));
		legendArea.setViewport(new FreeformViewport());
		legendArea.setContents(new DependenciesLegendPane());
	}
	
	@Override
	public void setFocus() {}
	
}
