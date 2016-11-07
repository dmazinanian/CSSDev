package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.plugin.utility.Constants;
import ca.concordia.cssanalyser.plugin.utility.ImagesUtil;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;
import ca.concordia.cssanalyser.plugin.wizards.dependenciesvisualization.SelectorSearchWizard;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;


public class DependenciesVisualizationView extends ViewPart {
	
	public static final String ID = "ca.concordia.cssanalyser.plugin.views.dependenciesvisualization.DependenciesVisualizationView";

	private static final int LEGEND_AREA_WIDTH = 250;
	private static final double ZOOM_INCRENENT = 0.1;

	private FigureCanvas figureCanvas;
	private FigureCanvas legendArea;
	
	private IAction resetZoomAction;
	private IAction searchForSelectorAction;
	private IAction clearResultsAction;

	private CSSValueOverridingDependencyList dependencies;
	
	@Override
	public void createPartControl(Composite parent) {
		
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		
		clearResultsAction = new Action() {
			@Override
			public void run() {
				((DependenciesFigurePane)figureCanvas.getContents()).clearSearchResults();
				clearResultsAction.setEnabled(false);
			}
		};
		clearResultsAction.setText(LocalizedStrings.get(Keys.CLEAR_RESULTS));
		clearResultsAction.setToolTipText(LocalizedStrings.get(Keys.CLEAR_RESULTS));
		clearResultsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		clearResultsAction.setEnabled(false);
		toolBarManager.add(clearResultsAction);
		
		searchForSelectorAction = new Action() {
			@Override
			public void run() {
				SelectorSearchWizard selectorSearchWizard = new SelectorSearchWizard(DependenciesVisualizationView.this);
				WizardDialog wizardDialog = new WizardDialog(getViewSite().getShell(), selectorSearchWizard);
				wizardDialog.open();
			}
		};
		searchForSelectorAction.setText(LocalizedStrings.get(Keys.SELECTOR_SEARCH));
		searchForSelectorAction.setToolTipText(LocalizedStrings.get(Keys.SELECTOR_SEARCH));
		searchForSelectorAction.setImageDescriptor(ImagesUtil.getImageDescriptor(Constants.SEARCH_ICON));
		toolBarManager.add(searchForSelectorAction);
		
		resetZoomAction = new Action() {
			@Override
			public void run() {
				resetZoom();
			}
		};
		resetZoomAction.setText("100%");
		resetZoomAction.setToolTipText(LocalizedStrings.get(Keys.RESET_ZOOM));
		toolBarManager.add(resetZoomAction);
		
		parent.setLayout(new GridLayout());
		
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		parent.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				int legendWidth = LEGEND_AREA_WIDTH;
				int width = parent.getClientArea().width;
				int weightFigure = width - legendWidth;
				int weightLegend = legendWidth;
				sashForm.setWeights(new int[] { weightFigure, weightLegend });
			}
		});
		
		Composite figureComposite = new Composite(sashForm, SWT.NONE);
		figureComposite.setLayout(new FillLayout());
		figureCanvas = new FigureCanvas(figureComposite, SWT.DOUBLE_BUFFERED);
		figureCanvas.setBackground(ColorConstants.white);
		figureCanvas.addMouseWheelListener(new MouseWheelListener() {
			public void mouseScrolled(MouseEvent e) {
				if ((e.stateMask & SWT.CTRL) != 0){
					zoom(e.count, new Point(e.x, e.y));
				}

			}
		});
		
		Composite legendComposite = new Composite(sashForm, SWT.NONE);
		legendComposite.setLayout(new FillLayout());
		legendArea = new FigureCanvas(legendComposite, SWT.DOUBLE_BUFFERED);
		legendArea.setBackground(ColorConstants.white);

	}

	public void showDependenciesGraph(CSSValueOverridingDependencyList dependencies, SubMonitor progressMonitor) {
		this.dependencies = dependencies;
		figureCanvas.setViewport(new FreeformViewport());
		DependenciesFigurePane dependenciesFigurePane = new DependenciesFigurePane(dependencies, progressMonitor);
		figureCanvas.setContents(dependenciesFigurePane);
		legendArea.setViewport(new FreeformViewport());
		DependenciesLegendPane dependenciesLegendPane = new DependenciesLegendPane();
		dependenciesLegendPane.setDependenciesDisplaySettingsChangeListener(dependenciesFigurePane.getDependenciesDisplaySettingsChangedListener());
		legendArea.setContents(dependenciesLegendPane);
	}
	
	private void zoom(int count, Point point) {
		ScalableFreeformLayeredPane root = (ScalableFreeformLayeredPane)figureCanvas.getContents();
		FreeformViewport viewport = (FreeformViewport)root.getParent();
		double scale = root.getScale();
		if (count > 0) {
			scale += ZOOM_INCRENENT;
		} else { 
			scale -= ZOOM_INCRENENT;
			if (scale <= 0) {
				scale = 0;
			}
		}				
		if (scale > 1) {
			viewport.setHorizontalLocation((int) (point.x * (scale - 1) + viewport.getViewLocation().x));
			viewport.setVerticalLocation((int) (point.y * (scale - 1) +  viewport.getViewLocation().y));
		}
		resetZoomAction.setText(String.format("%d%%", (int)(Math.round(scale * 100))));
		root.setScale(scale);
	}
	
	protected void resetZoom() {
		ScalableFreeformLayeredPane root = (ScalableFreeformLayeredPane)figureCanvas.getContents();
		root.setScale(1);
		resetZoomAction.setText("100%");
	}
	
	@Override
	public void setFocus() {}

	public void performSelectorSearch(String selectorNameToSearch, String mediaToSearch) {
		DependenciesFigurePane dependenciesFigurePane = (DependenciesFigurePane)figureCanvas.getContents();
		dependenciesFigurePane.unhiglightFigures();
		boolean found = false;
		for (SelectorFigure selectorFigure : dependenciesFigurePane.getSelecorFigures()) {
			Selector selector = selectorFigure.getSelector();
			if (selector.toString().contains(selectorNameToSearch.trim()))  {
				if (!"".equals(mediaToSearch.trim()) &&
						!selector.getMediaQueryLists().toString().contains(mediaToSearch)) {
					continue;
				}
				selectorFigure.highlight();
				found = true;
			}
		}
		clearResultsAction.setEnabled(found);
	}
	
	public void highlightSelectors(Iterable<Selector> selectors) {
		DependenciesFigurePane dependenciesFigurePane = (DependenciesFigurePane)figureCanvas.getContents();
		dependenciesFigurePane.unhiglightFigures();
		for (Selector selector : selectors) {
			dependenciesFigurePane.highlightFigure(selector);
		}
	}
	
	public CSSValueOverridingDependencyList getOverridingDependencies() {
		return dependencies;
	}

}
