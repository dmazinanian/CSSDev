package ca.concordia.cssdev.plugin.views.dependenciesvisualization;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.crawljax.core.state.StateVertex;

import ca.concordia.cssanalyser.dom.DOMNodeWrapper;

public class AffectedDOMNodesTreeViewerComparator extends ViewerComparator {
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		String name1, name2;
		if (e1.getClass() == e2.getClass()) {
			if (e1 instanceof StateVertex) {
				name1 = ((StateVertex)e1).getName();
				name2 = ((StateVertex)e2).getName();
				if ("index[]".equals(name1)) {
					return -1;
				} else if ("index[]".equals(name2)) {
					return 1;
				} else {
					return name1.compareTo(name2);
				}
			} else if (e1 instanceof DOMNodeWrapper) {
				name1 = ((DOMNodeWrapper) e1).getNode().toString();
				name2 = ((DOMNodeWrapper) e2).getNode().toString();
				return name1.compareTo(name2);
			} 
		}
		return super.compare(viewer, e1, e2);
	}
	
}
