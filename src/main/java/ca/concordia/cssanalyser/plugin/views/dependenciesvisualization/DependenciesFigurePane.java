package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
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
		
		Map<Selector, SelectorFigure> selectorFigures = new HashMap<>();
		
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
					selectorFigure = new SelectorFigure(intraSelectorDependency.getSelector1());
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
				selector1Figure.addDependency(cssValueOverridingDependency);
				
				Selector selector2 = getRealSelector(cssValueOverridingDependency.getSelector2());
				SelectorFigure selector2Figure = selectorFigures.get(selector2);
				if (selector2Figure == null) {
					selector2Figure = new SelectorFigure(selector2);
					selectorFigures.put(selector2, selector2Figure);
				}
			}
		}
		
		iProgressMonitor.worked(20);
		
		final int SELECTORS_GAP = 15;
		final int SELECTORS_X = 100;
		int selectorY = SELECTORS_GAP;
		
		SortedSet<Integer> specifities = new TreeSet<>(); 
		selectorFigures.values().forEach(selectorFigure -> specifities.add(getSelectorSpecificity(selectorFigure)));
		
		Map<Integer, Integer> specificityLevels = new HashMap<>();
		specifities.forEach(specifity -> specificityLevels.put(specifity, specificityLevels.size()));
		
		for (SelectorFigure selectorFigure : selectorFigures.values()) {
			if (iProgressMonitor.isCanceled()) {
				return;
			}
			formLayer.add(selectorFigure, new Rectangle(SELECTORS_X + SELECTORS_X * specificityLevels.get(getSelectorSpecificity(selectorFigure)), selectorY, -1, -1));
			selectorY += selectorFigure.getPreferredSize().height + SELECTORS_GAP;
		}
		
		formLayer.validate();
		
		iProgressMonitor.worked(30);
		
		for (SelectorFigure sourceFigure : selectorFigures.values()) {
			if (iProgressMonitor.isCanceled()) {
				return;
			}
			Point source = new Point(sourceFigure.getLocation().x, sourceFigure.getLocation().y + sourceFigure.getPreferredSize().height / 2);
			for (CSSValueOverridingDependency cssValueOverridingDependency : sourceFigure.getInterSelectorDependencies()) {
				Selector endSelector = getRealSelector(cssValueOverridingDependency.getSelector2());
				SelectorFigure destinationFigure = selectorFigures.get(endSelector);
				Point destination = new Point(destinationFigure.getLocation().x, destinationFigure.getLocation().y + destinationFigure.getPreferredSize().height / 2);
				RoundedConnection connection = new RoundedConnection(source, destination, SELECTORS_X - SELECTORS_GAP);
				if (cssValueOverridingDependency instanceof CSSInterSelectorValueOverridingDependency) {
					CSSInterSelectorValueOverridingDependency cssInterSelectorValueOverridingDependency = 
							(CSSInterSelectorValueOverridingDependency) cssValueOverridingDependency;
					if (cssInterSelectorValueOverridingDependency.getDependencyReason() == InterSelectorDependencyReason.DUE_TO_CASCADING) {
						connection.setForegroundColor(VisualizationConstants.CASCADING_DEPENDENCY_COLOR);
					} else if (cssInterSelectorValueOverridingDependency.getDependencyReason() == InterSelectorDependencyReason.DUE_TO_SPECIFICITY) {
						connection.setForegroundColor(VisualizationConstants.SPECIFICITY_DEPENDENCY_COLOR);
					}
					if (cssInterSelectorValueOverridingDependency.areMediaQueryListsDifferent()) {
						connection.setLineStyle(SWT.LINE_DASH);
					}
				}
				unhover(connection);
				sourceFigure.addOutgoingConnection(connection);
				destinationFigure.addIncomingConnection(connection);
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

	private int getSelectorSpecificity(SelectorFigure selectorFigure) {
		int specificity = 0;
		if (selectorFigure.getSelector() instanceof GroupingSelector) {
			GroupingSelector groupingSelector = (GroupingSelector) selectorFigure.getSelector();
			specificity = groupingSelector.getBaseSelectors().iterator().next().getSpecificity();
		} else if (selectorFigure.getSelector() instanceof BaseSelector) {
			BaseSelector baseSelector = (BaseSelector) selectorFigure.getSelector();
			specificity = baseSelector.getSpecificity();
		}
		return specificity;
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

	private Selector getRealSelector(Selector selector) {
		if (selector instanceof BaseSelector) {
			BaseSelector baseSelector = (BaseSelector) selector;
			if (baseSelector.getParentGroupingSelector() != null) {
				selector = baseSelector.getParentGroupingSelector();
			}
		}
		return selector;
	}

}
