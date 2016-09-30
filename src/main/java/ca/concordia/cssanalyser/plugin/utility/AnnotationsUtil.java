package ca.concordia.cssanalyser.plugin.utility;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import ca.concordia.cssanalyser.plugin.annotations.CSSAnnotation;

public class AnnotationsUtil {
	
	public static void clearAnnotations(StructuredTextEditor ste) {
		AnnotationModel annotationModel = (AnnotationModel)ste.getDocumentProvider().getAnnotationModel(ste.getEditorInput());
		annotationModel.removeAllAnnotations();
	}
	
	public static void setAnnotations(List<CSSAnnotation> annotations, StructuredTextEditor ste) {
		clearAnnotations(ste);
		if (ste != null && annotations.size() > 0) {
			AnnotationModel annotationModel = (AnnotationModel)ste.getDocumentProvider().getAnnotationModel(ste.getEditorInput());
			for (CSSAnnotation annotation : annotations) {
				annotationModel.addAnnotation(annotation, annotation.getPosition());
			}
			Position firstAnnotationPosition = annotations.get(0).getPosition();
			for (int i = 1; i < annotations.size(); i++) {
				if (firstAnnotationPosition.getOffset() >  annotations.get(i).getPosition().getOffset()) {
					firstAnnotationPosition = annotations.get(i).getPosition();
				}
			}
			IDocument document = ste.getDocumentProvider().getDocument(ste.getEditorInput());
			ste.getSelectionProvider().setSelection(new TextSelection(document, firstAnnotationPosition.getOffset(), 0));
		}
	}
}
