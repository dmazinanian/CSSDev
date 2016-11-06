package ca.concordia.cssanalyser.plugin.wizards.filteroptions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.media.MediaQuery;
import ca.concordia.cssanalyser.cssmodel.media.MediaQueryList;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.plugin.utility.DuplicationInfo;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;

public class FilterUtil {
	
	public static Set<MediaQueryList> getUniqueMediaQueryLists(List<DuplicationInfo> duplicationInfoList) {
		Set<MediaQueryList> toReturn = new HashSet<>();
		toReturn.add(getAllMediaQuery());
		for (DuplicationInfo duplicationInfo : duplicationInfoList) {
			for (MediaQueryList mediaQueryList : duplicationInfo.getItemSet().getMediaQueryLists()) {
				boolean dontAdd = false;
				for (MediaQueryList mql : toReturn) {
					if (mql.mediaQueryListEquals(mediaQueryList)) {
						dontAdd = true;
						break;
					}
				}
				if (!dontAdd) {
					toReturn.add(mediaQueryList);
				}
			}
		}
		return toReturn;
	}

	public static MediaQueryList getAllMediaQuery() {
		MediaQueryList allMediaQuery = new MediaQueryList();
		allMediaQuery.addMediaQuery(new MediaQuery("[" + LocalizedStrings.get(Keys.NO_MEDIA_QUERY) + "]"));
		return allMediaQuery;
	}

	public static Set<Selector> getUniqueSelectors(List<DuplicationInfo> duplicationInfoList) {
		Set<Selector> toReturn = new HashSet<>();
		for (DuplicationInfo duplicationInfo : duplicationInfoList) {
			for (Selector selector : duplicationInfo.getItemSet().getSupport()) {
				toReturn.add(selector);
			}
		}
		return toReturn;
	}
	
	public static Set<String> getUniqueProperties(List<DuplicationInfo> duplicationInfoList) {
		Set<String> toReturn = new HashSet<>();
		for (DuplicationInfo duplicationInfo : duplicationInfoList) {
			for (Declaration declaration : duplicationInfo.getItemSet().getRepresentativeDeclarations()) {
				String property = declaration.getProperty();
				toReturn.add(Declaration.getNonVendorProperty(Declaration.getNonHackedProperty(property)));
			}
		}
		return toReturn;
	}
	
}
