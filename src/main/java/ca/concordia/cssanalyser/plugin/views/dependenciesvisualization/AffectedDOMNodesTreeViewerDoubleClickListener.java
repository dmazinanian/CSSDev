package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.w3c.dom.Node;

import com.crawljax.core.state.StateVertex;

import ca.concordia.cssanalyser.dom.DOMNodeWrapper;
import ca.concordia.cssanalyser.io.IOHelper;
import ca.concordia.cssanalyser.plugin.annotations.CSSAnnotation;
import ca.concordia.cssanalyser.plugin.annotations.CSSAnnotationType;
import ca.concordia.cssanalyser.plugin.utility.AnnotationsUtil;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;
import ca.concordia.cssanalyser.plugin.utility.ViewsUtil;

public class AffectedDOMNodesTreeViewerDoubleClickListener implements IDoubleClickListener {

	@Override
	public void doubleClick(DoubleClickEvent event) {
		TreeSelection treeSelection = (TreeSelection) event.getSelection();
		TreePath treePath = treeSelection.getPaths()[0];
		Object selectedElement = treeSelection.getFirstElement();
		if (selectedElement instanceof StateVertex) {
			StateVertex stateVertex = (StateVertex) selectedElement;
			AffectedDOMNodesTreeViewerContentProvider affectedDOMNodesTreeViewerContentProvider = 
					(AffectedDOMNodesTreeViewerContentProvider)((TreeViewer)event.getViewer()).getContentProvider();
			List<Object> domNodeWrapperChilds = Arrays.asList(affectedDOMNodesTreeViewerContentProvider.getChildren(stateVertex));
			openDOMAndAnnotateNodes(stateVertex, domNodeWrapperChilds.toArray(new DOMNodeWrapper[]{}));
		} else if (selectedElement instanceof DOMNodeWrapper) {			
			DOMNodeWrapper domNodeWrapper = (DOMNodeWrapper) selectedElement;
			StateVertex stateVertex = (StateVertex)treePath.getParentPath().getLastSegment();
			openDOMAndAnnotateNodes(stateVertex, domNodeWrapper);
		}
	}

	private void openDOMAndAnnotateNodes(StateVertex stateVertex, DOMNodeWrapper... domNodeWrappers) {
		
		IEditorPart openHTMLFileFromStateVertex = openHTMLFileFromStateVertex(stateVertex);
		
		List<CSSAnnotation> annotations = new ArrayList<>();
		
		for (DOMNodeWrapper domNodeWrapper : domNodeWrappers) {
			String nodeHTML;
			try {
				nodeHTML = getInnerHTML(domNodeWrapper.getNode());
				int offset = stateVertex.getDom().indexOf(nodeHTML);
				if (offset >= 0) {
				Position position = new Position(offset, nodeHTML.length());
				CSSAnnotation annotation = new CSSAnnotation(CSSAnnotationType.AFFECTED_DOM_NODE, 
					LocalizedStrings.get(Keys.AFFECTED_DOM_NODES), position);
				annotations.add(annotation);
				}
			} catch (TransformerException e) {
				e.printStackTrace();
			}
			
		}
		
		AnnotationsUtil.setAnnotations(annotations, (StructuredTextEditor) openHTMLFileFromStateVertex);	
		
	}

	private IEditorPart openHTMLFileFromStateVertex(StateVertex stateVertex) {
		File fileToOpen;
		try {
			fileToOpen = File.createTempFile(stateVertex.getName() + "_", ".html");
			IOHelper.writeStringToFile(stateVertex.getDom(), fileToOpen.getAbsolutePath());
			return ViewsUtil.openExternalFile(fileToOpen.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getInnerHTML(Node node) throws TransformerConfigurationException, TransformerException {
		StringWriter sw = new StringWriter();
		Result result = new StreamResult(sw);
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer proc = factory.newTransformer();
		proc.setOutputProperty(OutputKeys.METHOD, "html");
		proc.transform(new DOMSource(node), result);
		return sw.toString();
	}
	
}
