package ca.concordia.cssanalyser.plugin.utility;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;

import ca.concordia.cssanalyser.analyser.duplication.items.Item;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class DuplicationInfo {
	
	private final ItemSet itemSet;
	private final IFile iFile;
	
	public DuplicationInfo(ItemSet itemSet, IFile iFile) {
		this.itemSet = itemSet;
		this.iFile = iFile;
	}

	public String getSelectorNames() {
		StringBuilder result = new StringBuilder();
		for (Iterator<Selector> iterator = itemSet.getSupport().iterator(); iterator.hasNext(); ) {
			result.append(iterator.next());
			if (iterator.hasNext())
				result.append(", ");
		}
		return result.toString();
	}

	public String getDeclarationNames() {
		StringBuilder result = new StringBuilder();
		for (Iterator<Item> iterator = itemSet.iterator(); iterator.hasNext(); ) {
			Item currentItem = iterator.next();
			if (currentItem.containsDifferencesInValues()) {
				result.append(currentItem.getDeclarationWithMinimumChars().getProperty());
			} else {
				result.append(currentItem.getDeclarationWithMinimumChars());
			}
			if (iterator.hasNext())
				result.append(", ");
		}
		return result.toString();
	}
	
	public double getRank() {
		return -1;
	}

	public IFile getSourceIFile() {
		return this.iFile;
	}

	public ItemSet getItemSet() {
		return this.itemSet;
	}

	public boolean hasDifferences() {
		return itemSet.containsDifferencesInValues();
	}
}
