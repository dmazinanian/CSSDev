package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;

import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtil;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependency;

public class DepdendencyDetailsFigure extends Figure {
	
	private final Set<String> dependencyLabels = new LinkedHashSet<>();
	
	public DepdendencyDetailsFigure(CSSValueOverridingDependency cssValueOverridingDependency) {
		
		setBorder(new MarginBorder(2, 5, 2, 5));
		
		setLayoutManager(new ToolbarLayout());
		
		Label overriddenPropertiesLabel = new Label(LocalizedStrings.get(Keys.OVERRIDDEN_PROPERTIES) + ":");
		add(overriddenPropertiesLabel);
		addDependency(cssValueOverridingDependency);
		
	}

	public void addDependency(CSSValueOverridingDependency cssValueOverridingDependency) {
		for (String stringLabel : cssValueOverridingDependency.getDependencyLabels()) {
			if (!dependencyLabels.contains(stringLabel)) {
				dependencyLabels.add(stringLabel);
				Label label = new Label(stringLabel);
				label.setTextPlacement(PositionConstants.WEST);
				label.setFont(PreferencesUtil.getTextEditorFont());
				add(label);
			}
		}
	}
	
	public boolean hasDependencyLabel(String label) {
		return dependencyLabels.contains(label);
	}

}
