package ca.concordia.cssdev.plugin.views.dependenciesvisualization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSInterSelectorValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSIntraSelectorValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;
import ca.concordia.cssdev.plugin.annotations.CSSAnnotation;
import ca.concordia.cssdev.plugin.annotations.CSSAnnotationType;
import ca.concordia.cssdev.plugin.utility.AnnotationsUtil;
import ca.concordia.cssdev.plugin.utility.ViewsUtil;
import ca.concordia.cssdev.plugin.views.dependenciesvisualization.DependenciesLegendPane.DependenciesDisplaySettingChangeListener;

public class DependenciesFigurePane extends ScalableFreeformLayeredPane {
	
	private static final int SELECTORS_GAP_X = 5;
	private static final int SELECTORS_GAP_Y = 20;
	private final Map<Selector, SelectorFigure> selectorFigures = new HashMap<>();
	private final Map<Integer, List<SelectorFigure>> selectorFigureLevels = new HashMap<>();
	private final List<SelectorFigure> selectedSelectorFigures = new ArrayList<>();
	private final DependenciesDisplaySettingChangeListener dependenciesDisplaySettingChangeListener = new DependenciesDisplaySettingChangeListener() {
		@Override
		public void settingsChanged(DependencyType type, boolean value) {
			switch(type) {
			case CASCADING:
				showCascadingConnections = value;
				break;
			case SPECIFICITY:
				showSpecificityConnections = value;
				break;
			case IMPORTANCE:
				showImportanceConnections = value;
				break;
			default:
				break;
			}
			resetConnectionsDisplaySettings();
		}
	};
	private boolean showCascadingConnections = true,
			showSpecificityConnections = true,
			showImportanceConnections = true;
	

	public DependenciesFigurePane(CSSValueOverridingDependencyList dependencies, SubMonitor subMonitor) {
		
		setOpaque(true); // This is necessary for the mouse listener to work
		DependenciesFigurePaneMouseListener dependenciesFigurePaneMouseListener = new DependenciesFigurePaneMouseListener(this);
		addMouseListener(dependenciesFigurePaneMouseListener);
		addMouseMotionListener(dependenciesFigurePaneMouseListener);
		
		ConnectionLayer connections = new ConnectionLayer();
		add(connections, "Connections");
		
		FreeformLayer formLayer = new FreeformLayer();
		formLayer.setLayoutManager(new FreeformLayout());
		add(formLayer, "Primary");
		
		subMonitor.setWorkRemaining(100);
		
		createSelectorFiguresFromDependencies(dependencies);
		
		applyTransitiveReduction();
		
		subMonitor.worked(20);
		
		initializeSelectorFigureLevels();
		
		addSelectorFigures(formLayer);
		addSelectorFigureListeners(dependenciesFigurePaneMouseListener);
		
		subMonitor.worked(30);
		
		addConnections(connections);
		
		subMonitor.done();
		
		hasLoops();
	}

	private void hasLoops() {
		for (SelectorFigure selectorFigure : selectorFigures.values()) {
			if (selectorFigure.getIncomingConnections().size() == 0) {
				Set<Selector> visited = new HashSet<>();
				visited.add(selectorFigure.getSelector());
				Stack<SelectorFigure> stack = new Stack<>();
				stack.push(selectorFigure);
				while (!stack.isEmpty()) {
					SelectorFigure currentFigure = stack.pop();
					for (SelectorFigureConnection selectorFigureConnection : currentFigure.getOutgoingConnections()) {
						SelectorFigure destinationFigure = selectorFigureConnection.getDestinationFigure();
						if (!visited.contains(destinationFigure.getSelector())) {
							stack.push(destinationFigure);
							visited.add(destinationFigure.getSelector());
						}
					}
				}
				
			}
		}
	}

	private void addSelectorFigureListeners(DependenciesFigurePaneMouseListener dependenciesFigurePaneMouseListener) {
		for (SelectorFigure selectorFigure : selectorFigures.values()) {
			selectorFigure.addMouseListener(new  MouseListener() {

				private boolean shouldHoverOn = true;

				@Override
				public void mouseReleased(MouseEvent arg0) {
					if (shouldHoverOn) {
						unhoverAllConnections(selectorFigure);
					}
				}

				@Override
				public void mousePressed(MouseEvent arg0) { 
					hoverAllConnections(selectorFigure);
				}

				@Override
				public void mouseDoubleClicked(MouseEvent arg0) {
					Selector selector = selectorFigure.getSelector();
					if (selector != null) {
						if ((arg0.getState() & SWT.CTRL) != 0) {
							IEditorPart editor = ViewsUtil.openEditor(selector.getParentStyleSheet().getFilePath());
							List<CSSAnnotation> annotations = new ArrayList<>();
							Position position = new Position(selector.getLocationInfo().getOffset(), selector.getLocationInfo().getLength());
							annotations.add(new CSSAnnotation(CSSAnnotationType.STYLE_RULE, selector.toString(), position));
							AnnotationsUtil.setAnnotations(annotations, (StructuredTextEditor)editor);
						} else {
							if (shouldHoverOn) {
								disableAllConnectionsExceptFor(selectorFigure);
								hoverAllConnections(selectorFigure);
							} else {
								enableAllConnectionsExceptFor(selectorFigure);
								unhoverAllConnections(selectorFigure);
							}
							shouldHoverOn = !shouldHoverOn;
						}
					}
				}
			});
			selectorFigure.addMouseListener(dependenciesFigurePaneMouseListener);
			selectorFigure.addMouseMotionListener(dependenciesFigurePaneMouseListener);
		}
	}

	private void addConnections(ConnectionLayer connections) {
		for (SelectorFigure sourceFigure : selectorFigures.values()) {
			addInterSelectorConnections(connections, sourceFigure);
			addIntraSelectorConnections(connections, sourceFigure);
		}
	}

	private void addIntraSelectorConnections(ConnectionLayer connections, SelectorFigure selectorFigure) {
		List<CSSIntraSelectorValueOverridingDependency> dependencies = selectorFigure.getIntraSelectorValueOverridingDependencies();
		if (dependencies.size() > 0) {
			SelectorFigureConnection connection = new SelectorFigureConnection(selectorFigure, selectorFigure);
			connection.setAntialias(SWT.ON);
			connection.unhover();
			connection.setConnectionType(DependencyType.CASCADING);
			selectorFigure.addOutgoingConnection(connection);
			selectorFigure.addIncomingConnection(connection);
			DepdendencyDetailsFigure toolTipFigure = null; 
			for (CSSValueOverridingDependency dependency : dependencies) {
				if (toolTipFigure == null) {
					toolTipFigure = new DepdendencyDetailsFigure(dependency);
				} else {
					toolTipFigure.addDependency(dependency);
				}
				connection.addDependency(dependency);
			}
			connection.setToolTip(toolTipFigure);
			connections.add(connection);
		}
	}

	private void addInterSelectorConnections(ConnectionLayer connections, SelectorFigure sourceFigure) {
		for (CSSValueOverridingDependency cssValueOverridingDependency : sourceFigure.getOutgoingInterSelectorDependencies()) {
			CSSInterSelectorValueOverridingDependency cssInterSelectorValueOverridingDependency = 
							(CSSInterSelectorValueOverridingDependency) cssValueOverridingDependency;
			if (!sourceFigure.isReducedDependency(cssValueOverridingDependency)) {
				SelectorFigure targetFigure = selectorFigures.get(cssValueOverridingDependency.getRealSelector2());
				SelectorFigureConnection connection = sourceFigure.getConnectionTo(targetFigure);
				if (connection != null) {
					if (connection.getToolTip() != null) {
						DepdendencyDetailsFigure depdendencyDetailsFigure = (DepdendencyDetailsFigure)connection.getToolTip();
						depdendencyDetailsFigure.addDependency(cssValueOverridingDependency);
					}
				} else {
					connection = new SelectorFigureConnection(sourceFigure, targetFigure);
					connection.setConnectionType(getDependencyType(cssInterSelectorValueOverridingDependency));
					if (!cssInterSelectorValueOverridingDependency.areMediaQueryListsEqual()) {
						connection.setLineStyle(SWT.LINE_DASH);
					}
					connection.setAntialias(SWT.ON);
					connection.unhover();
					sourceFigure.addOutgoingConnection(connection);
					targetFigure.addIncomingConnection(connection);
					connection.setToolTip(new DepdendencyDetailsFigure(cssValueOverridingDependency));
					connections.add(connection);
				}
				connection.addDependency(cssValueOverridingDependency);
			}
		}
	}

	private DependencyType getDependencyType(CSSInterSelectorValueOverridingDependency cssInterSelectorValueOverridingDependency) {
		DependencyType dependencyType = null;
		switch (cssInterSelectorValueOverridingDependency.getDependencyReason()) {
		case DUE_TO_CASCADING:
			dependencyType = DependencyType.CASCADING;
			break;
		case DUE_TO_SPECIFICITY:
			dependencyType = DependencyType.SPECIFICITY;
			break;
		case DUE_TO_IMPORTANCE:
			dependencyType = DependencyType.IMPORTANCE;
			break;
		default:
			break;
		}
		return dependencyType;
	}

	private void addSelectorFigures(FreeformLayer formLayer) {
		int maxWidth = getMaxLayerWidth();
		
		int lastLevelY = SELECTORS_GAP_Y;
		
		for (int level : selectorFigureLevels.keySet()) {
			int levelWidth = getLevelWidth(level);
			int startX = (maxWidth - levelWidth) / 2 + SELECTORS_GAP_X;
			int xSoFar = 0;
			for (SelectorFigure selectorFigure : selectorFigureLevels.get(level)) {
				formLayer.add(selectorFigure, new Rectangle(startX + xSoFar, lastLevelY, -1, -1));
				xSoFar += selectorFigure.getPreferredSize().width() + SELECTORS_GAP_X;
			}
			lastLevelY += getLevelHight(level) + SELECTORS_GAP_Y;
		}
		
		formLayer.validate();
	}

	private void createSelectorFiguresFromDependencies(CSSValueOverridingDependencyList dependencies) {
		Set<Integer> visitedDependenciesHashCodes = new HashSet<>();
		for (CSSValueOverridingDependency cssValueOverridingDependency : dependencies) {
			if (!visitedDependenciesHashCodes.contains(cssValueOverridingDependency.getSpecialHashCode())) {
				visitedDependenciesHashCodes.add(cssValueOverridingDependency.getSpecialHashCode());
				if (cssValueOverridingDependency instanceof CSSIntraSelectorValueOverridingDependency) {
					CSSIntraSelectorValueOverridingDependency intraSelectorDependency = 
							(CSSIntraSelectorValueOverridingDependency) cssValueOverridingDependency;
					Selector selector = intraSelectorDependency.getRealSelector1();
					SelectorFigure selectorFigure = selectorFigures.get(selector);
					if (selectorFigure == null) {
						selectorFigure = new SelectorFigure(selector);
						selectorFigures.put(selector, selectorFigure);
					}
					selectorFigure.addIntraSelectorDependency(intraSelectorDependency);
				} else if (cssValueOverridingDependency instanceof CSSInterSelectorValueOverridingDependency) {
					Selector selector1 = cssValueOverridingDependency.getRealSelector1();
					SelectorFigure selector1Figure = selectorFigures.get(selector1);
					if (selector1Figure == null) {
						selector1Figure = new SelectorFigure(selector1);
						selectorFigures.put(selector1, selector1Figure);
					}
					selector1Figure.addOutgoingInterSelectorDependency(cssValueOverridingDependency);

					Selector selector2 = cssValueOverridingDependency.getRealSelector2();
					SelectorFigure selector2Figure = selectorFigures.get(selector2);
					if (selector2Figure == null) {
						selector2Figure = new SelectorFigure(selector2);
						selectorFigures.put(selector2, selector2Figure);
					}
					selector2Figure.addIncomingInterSelectorDependency(cssValueOverridingDependency);
				}
			}
		}
	}

	protected void resetConnectionsDisplaySettings() {
		for (SelectorFigure selectorFigure : selectorFigures.values()) {
			for (SelectorFigureConnection selectorFigureConnection : selectorFigure.getOutgoingConnections()) {
				switch (selectorFigureConnection.getDependencyType()) {
				case CASCADING:
					selectorFigureConnection.setVisible(showCascadingConnections);
					break;
				case SPECIFICITY:
					selectorFigureConnection.setVisible(showSpecificityConnections);
					break;
				case IMPORTANCE:
					selectorFigureConnection.setVisible(showImportanceConnections);
					break;
				default:
					break;
				}
			}
		}
	}

	private int getLevelHight(int level) {
		int levelHeight = -1;
		List<SelectorFigure> figures = selectorFigureLevels.get(level);
		if (figures != null) {
			for (SelectorFigure figure : figures) {
				int height = figure.getPreferredSize().height();
				if (height > levelHeight) {
					levelHeight += height;
				}
			}
			levelHeight += SELECTORS_GAP_Y;
		}
		return levelHeight;
	}

	private int getLevelWidth(int level) {
		int levelWidth = 0;
		List<SelectorFigure> figures = selectorFigureLevels.get(level);
		if (figures != null) {
			for (Iterator<SelectorFigure> iterator = figures.iterator(); iterator.hasNext();) {
				SelectorFigure figure = iterator.next();
				levelWidth += figure.getPreferredSize().width();
				if (iterator.hasNext()) {
					levelWidth += SELECTORS_GAP_X;
				}
			}
		}
		return levelWidth;
	}
	
	private int getMaxLayerWidth() {
		if (!selectorFigureLevels.isEmpty()) {
		return selectorFigureLevels.keySet().stream()
			.map(level -> getLevelWidth(level))
			.max(Comparator.naturalOrder())
			.get();
		} else {
			return 0;
		}
	}

	private void applyTransitiveReduction() {
		for (SelectorFigure selectorFigure : selectorFigures.values()) {
			Set<Selector> indirectReachableSelectors = getIndirectReachableSelectors(selectorFigure);
			for (Selector selector : indirectReachableSelectors) {
				CSSValueOverridingDependency dependencyTo = selectorFigure.getDependencyTo(selectorFigures.get(selector));
				if (dependencyTo != null) {
					selectorFigure.markReducedDependency(dependencyTo);
				}
			}
		}
	}

	private Set<Selector> getIndirectReachableSelectors(SelectorFigure selectorFigure) {
		Set<Selector> toReturn = new HashSet<>();
		Set<Selector> whatToExclude = new HashSet<>();
		whatToExclude.add(selectorFigure.getSelector());
		for (CSSValueOverridingDependency outGoingDependency : selectorFigure.getOutgoingInterSelectorDependencies()) {
			toReturn.addAll(getReachableSelectorFigures(outGoingDependency, whatToExclude));
		}
		return toReturn;
	}

	private Set<Selector> getReachableSelectorFigures(CSSValueOverridingDependency incomingDependency, Set<Selector> whatToExclude) {
		Set<Selector> toReturn = new HashSet<>();
		SelectorFigure selectorFigure = selectorFigures.get(incomingDependency.getRealSelector2());
		selectorFigure.getOutgoingInterSelectorDependencies().forEach(outGoingDependency -> {
			Set<String> labels1 = new HashSet<>(incomingDependency.getDependencyLabels());
			Set<String> labels2 = new HashSet<>(outGoingDependency.getDependencyLabels());
			labels1.retainAll(labels2);
			if (labels1.size() > 0) {
				Selector targetSelector = outGoingDependency.getRealSelector2();
				if (!whatToExclude.contains(targetSelector)) {
					whatToExclude.add(targetSelector);
					toReturn.add(targetSelector);
					toReturn.addAll(getReachableSelectorFigures(outGoingDependency, whatToExclude));
				}
			}
		});
		return toReturn;
	}

	private void initializeSelectorFigureLevels() {
		List<SelectorFigure> withOnlyIntraSelectorDependencies = new ArrayList<>();
		List<SelectorFigure> postponedForLastLayer = new ArrayList<>();
		Set<Selector> visitedSelectors = new HashSet<>();
		for (SelectorFigure selectorFigure : selectorFigures.values()) {
			boolean onlyHasIntraSelectorDependencies = selectorFigure.getIncomingInterSelectorDependencies().size() == 0 &&
					selectorFigure.getOutgoingInterSelectorDependencies().size() == 0;
			if (onlyHasIntraSelectorDependencies) {
				withOnlyIntraSelectorDependencies.add(selectorFigure);
			} else {
				if (selectorFigure.getIncomingInterSelectorDependencies().size() == 0) {// Root, first level 
					addChildFigures(selectorFigure, 0, visitedSelectors, postponedForLastLayer);
				}
			}
		}
		int lastLevel = selectorFigureLevels.size();
		for (SelectorFigure selectorFigure : postponedForLastLayer) {
			addSelectorFigureToLevel(selectorFigure, lastLevel);
		}
		for (SelectorFigure selectorFigure : withOnlyIntraSelectorDependencies) {
			addSelectorFigureToLevel(selectorFigure, lastLevel + 1);
		}
	}

	private void addChildFigures(SelectorFigure selectorFigure, int level, Set<Selector> markedSelectors, List<SelectorFigure> postponedForLastLayer) {
		if (!markedSelectors.contains(selectorFigure.getSelector())) {
			markedSelectors.add(selectorFigure.getSelector());
			if (getUnmarkedOutgoingDependencies(selectorFigure).size() == 0) {
				postponedForLastLayer.add(selectorFigure);
			} else {
				addSelectorFigureToLevel(selectorFigure, level);
				for (CSSValueOverridingDependency outgoingDependency : selectorFigure.getOutgoingInterSelectorDependencies()) {
					if (!selectorFigure.isReducedDependency(outgoingDependency)) {
						SelectorFigure childFigure = selectorFigures.get(outgoingDependency.getRealSelector2());
						addChildFigures(childFigure, level + 1, markedSelectors, postponedForLastLayer);
					}
				}
			}
		}
	}

	private List<CSSValueOverridingDependency> getUnmarkedOutgoingDependencies(SelectorFigure selectorFigure) {
		List<CSSValueOverridingDependency> toReturn = new ArrayList<>();
		for (CSSValueOverridingDependency cssValueOverridingDependency : selectorFigure.getOutgoingInterSelectorDependencies()) {
			if (!selectorFigure.isReducedDependency(cssValueOverridingDependency)) {
				toReturn.add(cssValueOverridingDependency);
			}
		}
		return toReturn;
	}

	private void addSelectorFigureToLevel(SelectorFigure selectorFigure, int level) {
		List<SelectorFigure> levelFigures = selectorFigureLevels.get(level);
		if (levelFigures == null) {
			levelFigures = new ArrayList<>();
			selectorFigureLevels.put(level, levelFigures);
		}
		levelFigures.add(selectorFigure);
	}
	
	private void hoverAllConnections(SelectorFigure selectorFigure) {
		selectorFigure.getOutgoingConnections().forEach(connection -> connection.hover());
		selectorFigure.getIncomingConnections().forEach(connection -> connection.hover());
	}
	
	private void unhoverAllConnections(SelectorFigure selectorFigure) {
		selectorFigure.getOutgoingConnections().forEach(connection -> connection.unhover());
		selectorFigure.getIncomingConnections().forEach(connection -> connection.unhover());		
	}
	
	private void disableAllConnectionsExceptFor(SelectorFigure selectorFigure) {
		setAllConnectionsEnabledExceptFor(selectorFigure, false);
	}
	
	private void enableAllConnectionsExceptFor(SelectorFigure selectorFigure) {
		setAllConnectionsEnabledExceptFor(selectorFigure, true);
	}
	
	private void setAllConnectionsEnabledExceptFor(SelectorFigure selectorFigure, boolean enabled) {
		selectorFigures.values().stream().filter(figure -> figure != selectorFigure)
		.forEach(figure -> {
			figure.getOutgoingConnections().stream().filter(connection -> connection.getDestinationFigure() != selectorFigure)
				.forEach(connection -> connection.setEnabled(enabled));
			figure.getIncomingConnections().stream().filter(connection -> connection.getSourceFigure() != selectorFigure)
				.forEach(connection -> connection.setEnabled(enabled));
		});
	}

	public DependenciesDisplaySettingChangeListener getDependenciesDisplaySettingsChangedListener() {
		return dependenciesDisplaySettingChangeListener;
	}
	
	public void highlightFigure(Selector selector) {
		SelectorFigure selectorFigure = selectorFigures.get(selector);
		if (selectorFigure != null) {
			selectorFigure.highlight();
			selectedSelectorFigures.add(selectorFigure);
		}
	}
	
	public void unhiglightFigures() {
		for (SelectorFigure selectorFigure : selectedSelectorFigures) {
			selectorFigure.unhighlight();
		}
		selectedSelectorFigures.clear();
	}

	public void clearSearchResults() {
		unhiglightFigures();
	}

	public Collection<SelectorFigure> getSelecorFigures() {
		return selectorFigures.values();
	}
}
