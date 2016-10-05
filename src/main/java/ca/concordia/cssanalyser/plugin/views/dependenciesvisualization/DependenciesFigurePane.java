package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.plugin.annotations.CSSAnnotation;
import ca.concordia.cssanalyser.plugin.annotations.CSSAnnotationType;
import ca.concordia.cssanalyser.plugin.utility.AnnotationsUtil;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;
import ca.concordia.cssanalyser.plugin.utility.ViewsUtil;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSInterSelectorValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSInterSelectorValueOverridingDependency.InterSelectorDependencyReason;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSIntraSelectorValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

public class DependenciesFigurePane extends ScalableFreeformLayeredPane {
	
	private static final int SELECTORS_GAP_X = 5;
	private static final int SELECTORS_GAP_Y = 20;
	private final Map<Selector, SelectorFigure> selectorFigures = new HashMap<>();
	private final Map<Integer, List<SelectorFigure>> selectorFigureLevels = new HashMap<>();

	public DependenciesFigurePane(CSSValueOverridingDependencyList dependencies, IProgressMonitor iProgressMonitor) {
		
		setOpaque(true); // This is necessary for the mouse listener to work
		DependenciesFigurePaneMouseListener dependenciesFigurePaneMouseListener = new DependenciesFigurePaneMouseListener(this);
		addMouseListener(dependenciesFigurePaneMouseListener);
		addMouseMotionListener(dependenciesFigurePaneMouseListener);
		
		ConnectionLayer connections = new ConnectionLayer();
		add(connections, "Connections");
		
		FreeformLayer formLayer = new FreeformLayer();
		formLayer.setLayoutManager(new FreeformLayout());
		add(formLayer, "Primary");
		
		iProgressMonitor.beginTask(LocalizedStrings.get(Keys.GENERATING_DEPENDENCY_VISUALIZATION), 100);
				
		for (CSSValueOverridingDependency cssValueOverridingDependency : dependencies) {
			if (iProgressMonitor.isCanceled()) {
				return;
			}
			if (cssValueOverridingDependency instanceof CSSIntraSelectorValueOverridingDependency) {
				CSSIntraSelectorValueOverridingDependency intraSelectorDependency = 
						(CSSIntraSelectorValueOverridingDependency) cssValueOverridingDependency;
			
				Selector selector = getRealSelector(intraSelectorDependency.getSelector1());
				
				SelectorFigure selectorFigure = selectorFigures.get(selector);
				if (selectorFigure == null) {
					selectorFigure = new SelectorFigure(selector);
					selectorFigures.put(selector, selectorFigure);
				}
				selectorFigure.addIntraSelectorDependency(intraSelectorDependency);
			} else if (cssValueOverridingDependency instanceof CSSInterSelectorValueOverridingDependency) {
				Selector selector1 = getRealSelector(cssValueOverridingDependency.getSelector1());
				SelectorFigure selector1Figure = selectorFigures.get(selector1);
				if (selector1Figure == null) {
					selector1Figure = new SelectorFigure(selector1);
					selectorFigures.put(selector1, selector1Figure);
				}
				selector1Figure.addOutgoingInterSelectorDependency(cssValueOverridingDependency);
				
				Selector selector2 = getRealSelector(cssValueOverridingDependency.getSelector2());
				SelectorFigure selector2Figure = selectorFigures.get(selector2);
				if (selector2Figure == null) {
					selector2Figure = new SelectorFigure(selector2);
					selectorFigures.put(selector2, selector2Figure);
				}
				selector2Figure.addIncomingInterSelectorDependency(cssValueOverridingDependency);
			}
		}
		
		// Transitive Reduction
		applyTransitiveReduction();
		
		iProgressMonitor.worked(20);
		
		initializeSelectorFigureLevels();
		
		int maxWidth = getMaxLayerWidth();
		
		int lastLevelY = SELECTORS_GAP_Y;
		
		for (int level : selectorFigureLevels.keySet()) {
			int width = getLevelWidth(level);
			int startX = (maxWidth - width) / 2 + SELECTORS_GAP_X;
			
			int xSoFar = 0;
			for (SelectorFigure selectorFigure : selectorFigureLevels.get(level)) {
				formLayer.add(selectorFigure, new Rectangle(startX + xSoFar, lastLevelY, -1, -1));
				xSoFar += selectorFigure.getPreferredSize().width() + SELECTORS_GAP_X;
			}
			
			lastLevelY += getLevelHight(level) + SELECTORS_GAP_Y;
		}
		
		formLayer.validate();
		
		if (iProgressMonitor.isCanceled()) {
			return;
		}
		iProgressMonitor.worked(30);
		
		for (SelectorFigure sourceFigure : selectorFigures.values()) {
			if (iProgressMonitor.isCanceled()) {
				return;
			}
			Point source = new Point(sourceFigure.getLocation().x + sourceFigure.getPreferredSize().width / 2,
					sourceFigure.getLocation().y + sourceFigure.getPreferredSize().height);
			for (CSSValueOverridingDependency cssValueOverridingDependency : sourceFigure.getOutgoingInterSelectorDependencies()) {
				if (!sourceFigure.isMarkedDependency(cssValueOverridingDependency)) {
					Selector endSelector = getRealSelector(cssValueOverridingDependency.getSelector2());
					SelectorFigure destinationFigure = selectorFigures.get(endSelector);
					Point destination = new Point(destinationFigure.getLocation().x + destinationFigure.getPreferredSize().width / 2, 
							destinationFigure.getLocation().y /*+ destinationFigure.getPreferredSize().height / 2*/);
					RoundedConnection connection = new RoundedConnection(source, destination, 0 - 0);
					if (cssValueOverridingDependency instanceof CSSInterSelectorValueOverridingDependency) {
						CSSInterSelectorValueOverridingDependency cssInterSelectorValueOverridingDependency = 
								(CSSInterSelectorValueOverridingDependency) cssValueOverridingDependency;
						if (cssInterSelectorValueOverridingDependency.getDependencyReason() == InterSelectorDependencyReason.DUE_TO_CASCADING) {
							connection.setForegroundColor(VisualizationConstants.CASCADING_DEPENDENCY_COLOR);
						} else if (cssInterSelectorValueOverridingDependency.getDependencyReason() == InterSelectorDependencyReason.DUE_TO_SPECIFICITY) {
							connection.setForegroundColor(VisualizationConstants.SPECIFICITY_DEPENDENCY_COLOR);
						}
						if (!cssInterSelectorValueOverridingDependency.areMediaQueryListsEqual()) {
							connection.setLineStyle(SWT.LINE_DASH);
						}
					}
					sourceFigure.addOutgoingConnection(connection);
					destinationFigure.addIncomingConnection(connection);

					unhover(connection);
					connections.add(connection);
					connection.addMouseMotionListener(new MouseMotionListener() {

						@Override
						public void mouseMoved(MouseEvent arg0) {
							if (connection.pointIsIn(arg0.x, arg0.y)) {
								hover(connection);
							} else {
								unhover(connection);
							}
							dependenciesFigurePaneMouseListener.mouseMoved(arg0);
						}

						@Override
						public void mouseHover(MouseEvent arg0) {}

						@Override
						public void mouseExited(MouseEvent arg0) {
							unhover(connection);
						}

						@Override
						public void mouseEntered(MouseEvent arg0) {}

						@Override
						public void mouseDragged(MouseEvent arg0) {
							dependenciesFigurePaneMouseListener.mouseDragged(arg0);
						}
					});
				}
			}

			sourceFigure.addMouseListener(new  MouseListener() {
				
				private boolean shouldHoverOn = true;
				
				@Override
				public void mouseReleased(MouseEvent arg0) {
					if (shouldHoverOn) {
						unhoverAllConnections(sourceFigure);
						dependenciesFigurePaneMouseListener.mouseReleased(arg0);
					}
				}
				
				@Override
				public void mousePressed(MouseEvent arg0) { 
					hoverAllConnections(sourceFigure);
					dependenciesFigurePaneMouseListener.mousePressed(arg0);
				}

				@Override
				public void mouseDoubleClicked(MouseEvent arg0) {
					Selector selector = sourceFigure.getSelector();
					if (selector != null) {
						if ((arg0.getState() & SWT.CTRL) != 0) {
							IEditorPart editor = ViewsUtil.openEditor(selector.getParentStyleSheet().getFilePath());
							List<CSSAnnotation> annotations = new ArrayList<>();
							Position position = new Position(selector.getLocationInfo().getOffset(), selector.getLocationInfo().getLength());
							annotations.add(new CSSAnnotation(CSSAnnotationType.STYLE_RULE, selector.toString(), position));
							AnnotationsUtil.setAnnotations(annotations, (StructuredTextEditor)editor);
						} else {
							if (shouldHoverOn) {
								hoverAllConnections(sourceFigure);
							} else {
								unhoverAllConnections(sourceFigure);
							}
							shouldHoverOn = !shouldHoverOn;
						}
					}
				}
			});
			
			sourceFigure.addMouseMotionListener(new MouseMotionListener() {
				@Override
				public void mouseMoved(MouseEvent arg0) {
					arg0.consume();
				}
				
				@Override
				public void mouseHover(MouseEvent arg0) {
					arg0.consume();
				}
				
				@Override
				public void mouseExited(MouseEvent arg0) {
					arg0.consume();
				}
				
				@Override
				public void mouseEntered(MouseEvent arg0) {
					arg0.consume();
				}
				
				@Override
				public void mouseDragged(MouseEvent arg0) {
					dependenciesFigurePaneMouseListener.mouseDragged(arg0);
				}
			});
		}
		
		iProgressMonitor.done();
		
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
		return selectorFigureLevels.keySet().stream()
			.map(level -> getLevelWidth(level))
			.max(Comparator.naturalOrder())
			.get();
	}

	private void applyTransitiveReduction() {
		// Transitive Reduction
		for (SelectorFigure selectorFigure : selectorFigures.values()) {
			for (CSSValueOverridingDependency cssValueOverridingDependency : selectorFigure.getOutgoingInterSelectorDependencies()) {
				SelectorFigure targetSelectorFigure = selectorFigures.get(getRealSelector(cssValueOverridingDependency.getSelector2()));
				for (CSSValueOverridingDependency cssValueOverridingDependency2 : targetSelectorFigure.getOutgoingInterSelectorDependencies()) {
					CSSValueOverridingDependency dep = selectorFigure.gesDependencyTo(selectorFigures.get(getRealSelector(cssValueOverridingDependency2.getSelector2())));
					if (dep != null) {
						selectorFigure.markReducedDependency(dep);
					}
				}
			}
		}
	}

	private void initializeSelectorFigureLevels() {
		for (SelectorFigure selectorFigure : selectorFigures.values()) {
			List<CSSValueOverridingDependency> incomingDependencies = selectorFigure.getIncomingInterSelectorDependencies();
			if (incomingDependencies.size() == 0) {// Rroot, first level 
				addChildFigures(selectorFigure, 0, new HashSet<>());
			}
		}
	}

	private void addChildFigures(SelectorFigure selectorFigure, int level, Set<Selector> markedSelectors) {
		if (!markedSelectors.contains(getRealSelector(selectorFigure.getSelector()))) {
			markedSelectors.add(getRealSelector(selectorFigure.getSelector()));
			List<SelectorFigure> levelFigures = selectorFigureLevels.get(level);
			if (levelFigures == null) {
				levelFigures = new ArrayList<>();
				selectorFigureLevels.put(level, levelFigures);
			}
			levelFigures.add(selectorFigure);
			for (CSSValueOverridingDependency outgoingDependency : selectorFigure.getOutgoingInterSelectorDependencies()) {
				SelectorFigure childFigure = selectorFigures.get(getRealSelector(outgoingDependency.getSelector2()));
				addChildFigures(childFigure, level + 1, markedSelectors);
			}
		}
	}
	
	private void hover(RoundedConnection connection) {
		connection.setAlpha(VisualizationConstants.DEPENDENCY_CONNECTIONS_ALPHA_HOVERED);
		connection.setLineWidth(VisualizationConstants.DEPENDENCY_CONNECTIONS_WIDTH_HOVERED);
	}
	
	private void unhover(RoundedConnection connection) {
		connection.setAlpha(VisualizationConstants.DEPENDENCY_CONNECTIONS_ALPHA_INITIAL);
		connection.setLineWidth(VisualizationConstants.DEPENDENCY_CONNECTIONS_WIDTH_INITIAL);
	}
	
	private void hoverAllConnections(SelectorFigure selectorFigure) {
		for (RoundedConnection verticalRoundedConnection : selectorFigure.getOutgoingConnections()) {
			hover(verticalRoundedConnection);
		}
		for (RoundedConnection verticalRoundedConnection : selectorFigure.getIncomingConnections()) {
			hover(verticalRoundedConnection);
		}
	}
	
	private void unhoverAllConnections(SelectorFigure selectorFigure) {
		for (RoundedConnection verticalRoundedConnection : selectorFigure.getOutgoingConnections()) {
			unhover(verticalRoundedConnection);
		}
		for (RoundedConnection verticalRoundedConnection : selectorFigure.getIncomingConnections()) {
			unhover(verticalRoundedConnection);
		}
	}

	static Selector getRealSelector(Selector selector) {
		if (selector instanceof BaseSelector) {
			BaseSelector baseSelector = (BaseSelector) selector;
			if (baseSelector.getParentGroupingSelector() != null) {
				selector = baseSelector.getParentGroupingSelector();
			}
		}
		return selector;
	}

}
