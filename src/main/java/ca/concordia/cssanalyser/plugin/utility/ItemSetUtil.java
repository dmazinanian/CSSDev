package ca.concordia.cssanalyser.plugin.utility;

import java.util.Iterator;

import ca.concordia.cssanalyser.analyser.duplication.items.Item;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class ItemSetUtil {

	public static String getSelectorNames(ItemSet itemSet) {
		String result = "";
		for (Iterator<Selector> iterator = itemSet.getSupport().iterator(); iterator.hasNext(); ) {
			result += iterator.next();
			if (iterator.hasNext())
				result += ", ";
		}
		return result;
	}

	public static String getDeclarationNames(ItemSet itemSet) {
		String result = "";
		for (Iterator<Item> iterator = itemSet.iterator(); iterator.hasNext(); ) {
			Item currentItem = iterator.next();
			if (currentItem.containsDifferencesInValues()) {
				result += currentItem.getDeclarationWithMinimumChars().getProperty();
			} else {
				result += currentItem.getDeclarationWithMinimumChars();
			}
			if (iterator.hasNext())
				result += ", ";
		}
		return result;
	}

}
