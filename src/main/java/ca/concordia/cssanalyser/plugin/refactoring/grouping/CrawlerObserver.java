package ca.concordia.cssanalyser.plugin.refactoring.grouping;

import org.w3c.dom.Document;

public interface CrawlerObserver {

	void newDOMVisited(Document document);

}
