package ca.concordia.cssanalyser.plugin.utility;

import java.util.Iterator;

import ca.concordia.cssanalyser.analyser.duplication.items.Item;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorMigrationOpportunitiesDetectorFactory;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorType;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;

public class DuplicationInfo {
	
	private final ItemSet itemSet;
	private final PreprocessorType preprocessorType;
	private StyleSheet styleSheet;
	
	public DuplicationInfo(StyleSheet styleSheet, PreprocessorType preprocessorType, ItemSet itemSet) {
		this.styleSheet = styleSheet;
		this.preprocessorType = preprocessorType;
		this.itemSet = itemSet;
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

	public MixinMigrationOpportunity<?> geMixinMigrationOpportunity() {
		return PreprocessorMigrationOpportunitiesDetectorFactory.get(preprocessorType, styleSheet).getMixinOpportunityFromItemSet(itemSet);
	}

	public ItemSet getItemSet() {
		return this.itemSet;
	}

	public boolean hasDifferences() {
		return itemSet.containsDifferencesInValues();
	}
}
