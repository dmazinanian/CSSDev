package ca.concordia.cssdev.plugin.utility;

import java.util.List;

import org.eclipse.core.resources.IFile;

import ca.concordia.cssdev.plugin.annotations.CSSAnnotation;

public class AnalysisResultsStorage {
	
	private final IFile correspondingFile;
	private final List<DuplicationInfo> duplicationInfoList;
	private final List<CSSAnnotation> annotations;
	
	public AnalysisResultsStorage(IFile correspondingFile, List<DuplicationInfo> duplicationInfoList, List<CSSAnnotation> annotations) {
		this.correspondingFile = correspondingFile;
		this.duplicationInfoList = duplicationInfoList;
		this.annotations = annotations;
	}
	
	public IFile getCorrespondingFile() {
		return correspondingFile;
	}

	public List<DuplicationInfo> getDuplicationInfoList() {
		return duplicationInfoList;
	}

	public List<CSSAnnotation> getAnnotations() {
		return annotations;
	}

}
