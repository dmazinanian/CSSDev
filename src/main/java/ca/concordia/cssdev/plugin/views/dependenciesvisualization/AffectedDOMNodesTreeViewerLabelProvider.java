package ca.concordia.cssdev.plugin.views.dependenciesvisualization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.xerces.dom.AttrImpl;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.custom.StyleRange;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.util.XPathHelper;

import ca.concordia.cssanalyser.dom.DOMNodeWrapper;
import ca.concordia.cssdev.plugin.utility.Constants;
import ca.concordia.cssdev.plugin.utility.ImagesUtil;
import ca.concordia.cssdev.plugin.utility.ViewsUtil;
import ca.concordia.cssdev.plugin.views.duplicationrefactoring.DuplicationRefactoringView;

public class AffectedDOMNodesTreeViewerLabelProvider extends StyledCellLabelProvider {
		
	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		if (element instanceof StateVertex) {
			StateVertex stateVertex = (StateVertex) element;			
			String documentPath = getDocumentPath(stateVertex);
			cell.setText(documentPath);
			cell.setImage(ImagesUtil.getImageDescriptor(Constants.HTML_FILE_ICON).createImage());
			cell.setStyleRanges(new StyleRange[] { getHighlightStylingRange(documentPath, " [") });
		} else if (element instanceof DOMNodeWrapper) {
			DOMNodeWrapper domNodeWrapper = (DOMNodeWrapper) element;
			String domNodeWrapperString = getDOMnodeWrapperString(domNodeWrapper);
			cell.setText(domNodeWrapperString);
			cell.setImage(ImagesUtil.getImageDescriptor(Constants.DOM_ICON).createImage());
			cell.setStyleRanges(new StyleRange[] { getHighlightStylingRange(domNodeWrapperString, "> ") });
		}
	}

	private StyleRange getHighlightStylingRange(String inString, String fromString) {
		int stylingPosition = inString.indexOf(fromString);
		StyleRange range = new StyleRange();
		range.start = stylingPosition;
		range.length = inString.length() - stylingPosition + 1;
		range.foreground = Constants.HIGHLIGHTED_TEXT;
		return range;
	}

	private String getDOMnodeWrapperString(DOMNodeWrapper domNodeWrapper) {
		StringBuilder builder = new StringBuilder("<");
		Node node = domNodeWrapper.getNode();
		builder.append(node.getNodeName());
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attrNode = attributes.item(i);
			Map<String, List<String>> keyValuePairs = new HashMap<>();   
			if (attrNode instanceof AttrImpl) {
				AttrImpl attrImpl = (AttrImpl) attrNode;
				String name = attrImpl.getName();
				List<String> valueList = keyValuePairs.get(name);
				if (valueList == null) {
					valueList = new ArrayList<>();
					keyValuePairs.put(name, valueList);
				}
				valueList.add(attrImpl.getValue());
			} else {
				throw new RuntimeException("What is " + attrNode.getClass().getName());
			}
			for (Iterator<String> attrNameIterator = keyValuePairs.keySet().iterator(); attrNameIterator.hasNext();) {
				String attrName = attrNameIterator.next();
				builder.append(" ").append(attrName).append("=").append("\"");
				for (Iterator<String> attrKeyIterator = keyValuePairs.get(attrName).iterator(); attrKeyIterator.hasNext();) {
					String value = attrKeyIterator.next();
					builder.append(value);
					if (attrKeyIterator.hasNext()) {
						builder.append(" ");
					}
					
				}
				builder.append("\"");
				if (attrNameIterator.hasNext()) {
					builder.append(" ");
				}
			}
		}
		builder.append("> ");
		builder.append(XPathHelper.getXPathExpression(node));
		return builder.toString();
	}
	
	private String getDocumentPath(StateVertex stateVertex) {
		CrawlSession crawlSession = ((DuplicationRefactoringView)ViewsUtil.getView(DuplicationRefactoringView.ID)).getCrawlSession();
		StateFlowGraph sfg = crawlSession.getStateFlowGraph();
		return stateVertex.getName() + " " + sfg.getShortestPath(sfg.getInitialState(), stateVertex).toString();
	}

}
