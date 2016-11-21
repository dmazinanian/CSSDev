package ca.concordia.cssdev.plugin.refactoring.grouping;

import org.w3c.dom.Document;

import com.crawljax.core.CrawlSession;

public interface CrawlerObserver {

	void newDOMVisited(Document document);
	void finishedCrawling(CrawlSession session);

}
