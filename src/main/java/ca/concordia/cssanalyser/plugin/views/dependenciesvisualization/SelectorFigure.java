package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.SWT;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtil;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSIntraSelectorValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependency;

public class SelectorFigure extends RoundedRectangle {

	private final Map<Declaration, List<CSSIntraSelectorValueOverridingDependency>> incomingIntraDeclarationDependencies = new HashMap<>();
	private final List<CSSValueOverridingDependency> incomingIntraSelectorDependencies = new ArrayList<>();
	private final List<CSSValueOverridingDependency> outgoingIntraSelectorDependencies = new ArrayList<>();
	private final Set<CSSValueOverridingDependency> markedDependencies = new HashSet<>();
	private final List<SelectorFigureConnection> outgoingConnections = new ArrayList<>();
	private final List<SelectorFigureConnection> incomingConnections = new ArrayList<>();
	private final Selector selector;
	
	public SelectorFigure() {
		this(null);
	}
	
	public SelectorFigure(Selector selector) {
		this.selector = selector;
		ToolbarLayout toolbarLayout = new ToolbarLayout();
		toolbarLayout.setSpacing(2);
		setLayoutManager(toolbarLayout);
		setBackgroundColor(VisualizationConstants.SELECTOR_BG_COLOR);
		setBorder(new MarginBorder(5, 10, 5, 10));
		setForegroundColor(VisualizationConstants.SELECTOR_BORDER_COLOR);
		setOpaque(true);
		setAntialias(SWT.ON);
		setFont(PreferencesUtil.getTextEditorFont());
		
		if (selector != null) {
			Figure selectorNameFigure = new Figure();
			selectorNameFigure.setLayoutManager(new GridLayout(2, false));
			
			Label selectorName = new Label(getLabelString(selector.toString(), 15));
			selectorName.setLabelAlignment(PositionConstants.LEFT);
			selectorName.setToolTip(new SelectorTooltip(selector));
			selectorName.setForegroundColor(VisualizationConstants.SELECTOR_COLOR);
			selectorNameFigure.add(selectorName);
			
			selectorNameFigure.add(new SelectorToolsFigure(selector));
			
			add(selectorNameFigure);
			
			if (selector.getMediaQueryLists().size() > 0) {
				Label mediaQuery = new Label(getLabelString(selector.getMediaQueryLists().toString(), 15));
				add(mediaQuery);
			}
		} else {
			add(new Label(""));
		}
		
	}
	
	private String getLabelString(String string, int maxLength) {
		if (string.length() > maxLength) {
			string = string.substring(0, maxLength - 3) + "...";
		}
		return string;
	}

	public Selector getSelector() {
		return selector;
	}

	public void addIntraSelectorDependency(CSSIntraSelectorValueOverridingDependency dependency) {
		List<CSSIntraSelectorValueOverridingDependency> dependenciesFromThisDeclaration = 
				incomingIntraDeclarationDependencies.get(dependency.getDeclaration1());
		if (dependenciesFromThisDeclaration == null) {
			dependenciesFromThisDeclaration = new ArrayList<>();
			incomingIntraDeclarationDependencies.put(dependency.getDeclaration1(), dependenciesFromThisDeclaration);
		}
		dependenciesFromThisDeclaration.add(dependency);
	}
	
	public List<CSSIntraSelectorValueOverridingDependency> getIntraSelectorValueOverridingDependencies() {
		List<CSSIntraSelectorValueOverridingDependency> toReturn = new ArrayList<>();
		incomingIntraDeclarationDependencies.values().forEach(list -> toReturn.addAll(list));
		return toReturn;
	}

	public void addOutgoingInterSelectorDependency(CSSValueOverridingDependency interSelectorDependency) {
		outgoingIntraSelectorDependencies.add(interSelectorDependency);
	}
	
	public List<CSSValueOverridingDependency> getOutgoingInterSelectorDependencies() {
		return outgoingIntraSelectorDependencies;
	}
	
	public void addIncomingInterSelectorDependency(CSSValueOverridingDependency interSelectorDependency) {
		incomingIntraSelectorDependencies.add(interSelectorDependency);
	}
	
	public List<CSSValueOverridingDependency> getIncomingInterSelectorDependencies() {
		return incomingIntraSelectorDependencies;
	}
	
	public void addOutgoingConnection(SelectorFigureConnection connection) {
		outgoingConnections.add(connection);
	}
	
	public List<SelectorFigureConnection> getOutgoingConnections() {
		return outgoingConnections;
	}
	
	public void addIncomingConnection(SelectorFigureConnection connection) {
		incomingConnections.add(connection);
	}
	
	public List<SelectorFigureConnection> getIncomingConnections() {
		return incomingConnections;
	}

	public void resetConnectionsPositions() {
		outgoingConnections.forEach(connection -> connection.calculateAnchorPoints());
		incomingConnections.forEach(connection -> connection.calculateAnchorPoints());
	}

	public CSSValueOverridingDependency getDependencyTo(SelectorFigure selectorFigure) {
		for (CSSValueOverridingDependency cssValueOverridingDependency : outgoingIntraSelectorDependencies) {
			if (cssValueOverridingDependency.getRealSelector2() == selectorFigure.getSelector()) {
				return cssValueOverridingDependency;
			}
		}
		return null;
	}
	
	public SelectorFigureConnection getConnectionTo(SelectorFigure selectorFigure) {
		CSSValueOverridingDependency dependencyTo = getDependencyTo(selectorFigure);
		for (SelectorFigureConnection selectorFigureConnection : outgoingConnections) {
			for (CSSValueOverridingDependency cssValueOverridingDependency : selectorFigureConnection.getDependencies()) {
				if (cssValueOverridingDependency == dependencyTo) {
					return selectorFigureConnection;
				}
			}
		}
		return null;
	}

	public void markReducedDependency(CSSValueOverridingDependency dep) {
		markedDependencies.add(dep);
	}
	
	public boolean isReducedDependency(CSSValueOverridingDependency dep) {
		return markedDependencies.contains(dep);
	}

	public void highlight() {
		setForegroundColor(VisualizationConstants.SELECTED_SELECTOR_COLOR);
	}

	public void unhighlight() {
		setForegroundColor(VisualizationConstants.SELECTOR_BORDER_COLOR);
	}

}
