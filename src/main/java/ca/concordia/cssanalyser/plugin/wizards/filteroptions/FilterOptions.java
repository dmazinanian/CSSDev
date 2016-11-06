package ca.concordia.cssanalyser.plugin.wizards.filteroptions;

import java.util.HashSet;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.media.MediaQueryList;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;

public class FilterOptions {
	
	private final Set<Selector> selectorsToInclude;
	private final Set<String> propertiesToInclude;
	private final Set<MediaQueryList> mediaQueryListsToInclude;
	
	public FilterOptions() {
		selectorsToInclude = new HashSet<>();
		propertiesToInclude = new HashSet<>();
		mediaQueryListsToInclude = new HashSet<>();
	}
	
	private FilterOptions(Set<MediaQueryList> mediaQueryListsToInclude, Set<Selector> selectorsToInclude, Set<String> propertiesToInclude) {
		this.mediaQueryListsToInclude = mediaQueryListsToInclude;
		this.selectorsToInclude = selectorsToInclude;
		this.propertiesToInclude = propertiesToInclude;
	}
	
	public void clearFilters() {
		selectorsToInclude.clear();
		propertiesToInclude.clear();
		mediaQueryListsToInclude.clear();
	}
	
	public void includeSelector(Selector selector) {
		selectorsToInclude.add(selector);
	}
	
	public void excludeSelector(Selector selector) {
		selectorsToInclude.remove(selector);
	}
	
	public void includeProperty(String property) {
		propertiesToInclude.add(property);
	}
	
	public void excludeProperty(String property) {
		propertiesToInclude.remove(property);
	}
	
	public void includeMediaQuery(MediaQueryList mediaQuery) {
		mediaQueryListsToInclude.add(mediaQuery);
	}
	
	public void excludeMediaQuery(MediaQueryList mediaQuery) {
		mediaQueryListsToInclude.remove(mediaQuery);
	}

	public Set<Selector> getSelectorsToInclude() {
		return selectorsToInclude;
	}
	
	public Set<String> getPropertiesToInclude() {
		return propertiesToInclude;
	}
	
	public Set<MediaQueryList> getMediaQueryListsToInclude() {
		return mediaQueryListsToInclude;
	}
	
	public FilterOptions getCopy() {
		return new FilterOptions(new HashSet<>(mediaQueryListsToInclude), new HashSet<>(selectorsToInclude), new HashSet<>(propertiesToInclude));
	}

	public boolean includes(MediaQueryList mediaQueryList) {
		for (MediaQueryList mql : mediaQueryListsToInclude) {
			if (mql.mediaQueryListEquals(mediaQueryList)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean includes(Selector selector) {
		return selectorsToInclude.contains(selector);
	}
	
	public boolean includes(String property) {
		return propertiesToInclude.contains(property);
	}
	
}
