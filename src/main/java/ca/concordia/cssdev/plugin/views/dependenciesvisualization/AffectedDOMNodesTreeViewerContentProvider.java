package ca.concordia.cssdev.plugin.views.dependenciesvisualization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.w3c.dom.Document;

import com.crawljax.core.state.StateVertex;

import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.dom.DOMNodeWrapper;
import ca.concordia.cssanalyser.dom.DOMNodeWrapperList;

public class AffectedDOMNodesTreeViewerContentProvider implements ITreeContentProvider {

	private final Selector selector;
	private final Map<StateVertex, List<DOMNodeWrapper>> stateVertexToNodeWrapperMap;
	private final Map<DOMNodeWrapper, StateVertex> domNodesToStateVerticesMap;
	
	public AffectedDOMNodesTreeViewerContentProvider() {
		this(null, null);
	}
	
	public AffectedDOMNodesTreeViewerContentProvider(Selector selector, Iterable<StateVertex> stateVertices) {
		this.selector = selector;
		this.stateVertexToNodeWrapperMap = new HashMap<>();
		this.domNodesToStateVerticesMap = new HashMap<>();
		if (selector != null && stateVertices != null) {
			for (StateVertex stateVertex : stateVertices) {
				try {
					List<DOMNodeWrapper> domNodeWrappers = getDOMNodeWrappers(stateVertex.getDocument());
					stateVertexToNodeWrapperMap.put(stateVertex, domNodeWrappers);
					for (DOMNodeWrapper domNodeWrapper: domNodesToStateVerticesMap.keySet()) {
						domNodesToStateVerticesMap.put(domNodeWrapper, stateVertex);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public Object[] getChildren(Object element) {
		if (element instanceof StateVertex) {
			StateVertex stateVertex = (StateVertex) element;
			List<DOMNodeWrapper> domNodeList = stateVertexToNodeWrapperMap.get(stateVertex);
			if (domNodeList != null) {
				return domNodeList.toArray();
			}
		}
		return new Object[] {};
	}
	
	@Override
	public Object[] getElements(Object element) {
		if (stateVertexToNodeWrapperMap != null) {
			return stateVertexToNodeWrapperMap.keySet().toArray();
		}
		return new Object[] {};
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof DOMNodeWrapper) {
			return domNodesToStateVerticesMap.get(element);
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof StateVertex) {
			List<DOMNodeWrapper> domNodeList = stateVertexToNodeWrapperMap.get(element);
			return domNodeList != null && domNodeList.size() > 0;
		}
		return false;
	}
	
	private List<DOMNodeWrapper> getDOMNodeWrappers(Document document) {
		DOMNodeWrapperList domNodeWrapperList = new DOMNodeWrapperList();
		if (selector instanceof BaseSelector) {
			BaseSelector baseSelector = (BaseSelector) selector;
			domNodeWrapperList = baseSelector.getSelectedNodes(document);				
		} else if (selector instanceof GroupingSelector) {
			GroupingSelector groupingSelector = (GroupingSelector) selector;
			for (BaseSelector baseSelector : groupingSelector) {
				domNodeWrapperList.addAll(baseSelector.getSelectedNodes(document));
			}
		}
		List<DOMNodeWrapper> nodes = new ArrayList<>();
		domNodeWrapperList.forEach(domNodeWrapper -> nodes.add(domNodeWrapper));
		return nodes;
	}


}
