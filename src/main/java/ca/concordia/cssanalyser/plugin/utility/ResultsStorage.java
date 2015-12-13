package ca.concordia.cssanalyser.plugin.utility;

import java.util.List;

import org.eclipse.core.resources.IFile;

import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.plugin.annotations.CSSAnnotation;

public class ResultsStorage {
	
	private final IFile correspondingFile;
	private final List<ItemSet> itemSets;
	private final List<CSSAnnotation> annotations;
	
	public ResultsStorage(IFile correspondingFile, List<ItemSet> itemSets, List<CSSAnnotation> annotations) {
		this.correspondingFile = correspondingFile;
		this.itemSets = itemSets;
		this.annotations = annotations;
	}
	
	public IFile getCorrespondingFile() {
		return correspondingFile;
	}

	public List<ItemSet> getItemSets() {
		return itemSets;
	}

	public List<CSSAnnotation> getAnnotations() {
		return annotations;
	}

}
