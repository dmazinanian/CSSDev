package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtil;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSIntraSelectorValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependency;

public class SelectorFigure extends RoundedRectangle {

	private final Map<Declaration, List<CSSIntraSelectorValueOverridingDependency>> intraDeclarationDependencies = new HashMap<>();
	private final List<CSSValueOverridingDependency> outgoingDependencies = new ArrayList<>();
	private final List<RoundedConnection> outgoingConnections = new ArrayList<>();
	private final List<RoundedConnection> incomingConnections = new ArrayList<>();
	private Selector selector;
	
	public SelectorFigure() {
		this(null);
	}
	
	public SelectorFigure(Selector selector) {
		this.selector = selector;
		ToolbarLayout toolbarLayout = new ToolbarLayout();
		toolbarLayout.setSpacing(5);
		setLayoutManager(toolbarLayout);
		setBackgroundColor(VisualizationConstants.SELECTOR_BG_COLOR);
		setBorder(new MarginBorder(5, 10, 5, 10));
		setForegroundColor(VisualizationConstants.SELECTOR_BORDER_COLOR);
		setOpaque(true);
		setFont(PreferencesUtil.getTextEditorFont());
		
		if (selector != null) {
			Label selectorName = new Label(selector.toString());
			selectorName.setLabelAlignment(PositionConstants.LEFT);
			selectorName.setToolTip(new Label(selector.getParentStyleSheet().getFilePath()));
			selectorName.setForegroundColor(VisualizationConstants.SELECTOR_COLOR);
			add(selectorName);
			
			if (selector.getMediaQueryLists().size() > 0) {
				Label mediaQuery = new Label(selector.getMediaQueryLists().toString());
				
				add(mediaQuery);
			}
		} else {
			add(new Label(""));
		}
		
		setAntialias(SWT.ON);
	}

	public void addIntraSelectorDependency(CSSIntraSelectorValueOverridingDependency dependency) {
		List<CSSIntraSelectorValueOverridingDependency> dependenciesFromThisDeclaration = 
				intraDeclarationDependencies.get(dependency.getDeclaration1());
		if (dependenciesFromThisDeclaration == null) {
			dependenciesFromThisDeclaration = new ArrayList<>();
			intraDeclarationDependencies.put(dependency.getDeclaration1(), dependenciesFromThisDeclaration);
		}
		dependenciesFromThisDeclaration.add(dependency);
	}

	public void addDependency(CSSValueOverridingDependency interSelectorDependency) {
		outgoingDependencies.add(interSelectorDependency);
	}
	
	public List<CSSValueOverridingDependency> getInterSelectorDependencies() {
		return outgoingDependencies;
	}
	
	public void addOutgoingConnection(RoundedConnection connection) {
		outgoingConnections.add(connection);
	}
	
	public List<RoundedConnection> getOutgoingConnections() {
		return outgoingConnections;
	}
	
	public void addIncomingConnection(RoundedConnection connection) {
		incomingConnections.add(connection);
	}
	
	public List<RoundedConnection> getIncomingConnections() {
		return incomingConnections;
	}

	public Selector getSelector() {
		return selector;
	}

	public void resetConnectionsPosition() {
		for (RoundedConnection verticalRoundedConnection : outgoingConnections) {
			verticalRoundedConnection.setSource(new Point(this.getLocation().x, this.getLocation().y + this.getPreferredSize().height / 2));
		}
		
		for (RoundedConnection verticalRoundedConnection : incomingConnections) {
			verticalRoundedConnection.setTarget(new Point(this.getLocation().x, this.getLocation().y + this.getPreferredSize().height / 2));
		}
	}

}
