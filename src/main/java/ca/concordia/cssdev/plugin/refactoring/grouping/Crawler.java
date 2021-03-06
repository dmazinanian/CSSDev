package ca.concordia.cssdev.plugin.refactoring.grouping;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.w3c.dom.Document;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawlRules.CrawlRulesBuilder;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;

import ca.concordia.cssdev.plugin.utility.AnalysisOptions;
import ca.concordia.cssdev.plugin.utility.FilesUtil;

public class Crawler {

	private static final String PHANTOM_JS_PATH = FilesUtil.getAbsolutePathForFile("phantomjs/phantomjs.exe");
	
	private final AnalysisOptions options;
	private final List<Document> documents;
	private final List<CrawlerObserver> observers;
	private CrawljaxRunner crawljax;
	private BooleanSupplier cancelSupplier;
	private Server server;
	
	public Crawler(AnalysisOptions options) {
		System.getProperties().setProperty(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, PHANTOM_JS_PATH);
		this.options = options;
		this.documents = new ArrayList<>();
		this.observers = new ArrayList<>();
	}

	public void start() throws Exception {
		CrawljaxConfiguration crawljaxBuilder;
		try {
			crawljaxBuilder = getCrawljaxBuilder();
			crawljax = new CrawljaxRunner(crawljaxBuilder);
			crawljax.call();
		} catch (Exception e) {
			throw e;
		} finally {
			stopServer();	
		}
	}
	
	public void stop() {
		if (crawljax != null) {
			crawljax.stop();
		}
		stopServer();
	}
	
	private void stopServer() {
		if (server != null && server.isRunning()) {
			try {
				server.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private CrawljaxConfiguration getCrawljaxBuilder() throws Exception {
		URI uri = null;
		if (options.getUrl().toLowerCase().startsWith("http://") ||
				options.getUrl().toLowerCase().startsWith("https://")) {
			uri = URI.create(options.getUrl());
		} else {
			server = new Server(1668);
			ResourceHandler handler = new ResourceHandler();
			File fileToCrawl =  new File(options.getUrl());
			handler.setBaseResource(Resource.newResource(fileToCrawl.getParentFile().getAbsolutePath()));
			server.setHandler(handler);
			server.start();
			int port = ((ServerConnector)server.getConnectors()[0]).getLocalPort();
		    String nullFragment = null;
		    uri = new URI("http", nullFragment, "localhost", port, "/" + fileToCrawl.getName(), nullFragment, nullFragment);
		}
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(uri);
		//builder.addPlugin(new CrawlOverview());
		builder.addPlugin(new OnNewStatePlugin() {
			@Override
			public void onNewState(CrawlerContext arg0, StateVertex arg1) {
				if (cancelSupplier != null) {
					if (cancelSupplier.getAsBoolean()) {
						crawljax.stop();
					}
				}
				Document document;
				try {
					for (Eventable eventable : arg0.getCrawlPath()) {
						System.out.println("------------------------------------------------------");
						System.out.println(eventable.getElement());
						System.out.println("------------------------------------------------------");
					}
					document = arg1.getDocument();
					documents.add(document);
					notifyDomVisitedObservers(document);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		builder.addPlugin(new PostCrawlingPlugin() {
			@Override
			public void postCrawling(CrawlSession session, ExitStatus exitStaus) {
				notifyCrawlingFinishedObservers(session);
			}
		});
		builder.setBrowserConfig(new BrowserConfiguration(BrowserType.PHANTOMJS, 1));
		builder.setOutputDirectory(new File(options.getOutputDirectory().getAbsolutePath() + "/crawljax"));
		builder.setMaximumDepth(options.getMaxDepth());
		builder.setMaximumStates(options.getMaxStates());
		CrawlRulesBuilder crawlRules = builder.crawlRules();
		if (options.shouldClickDefaultElements()) {
			crawlRules.clickDefaultElements();
		}
		if (options.getDontClickElements().size() > 0) {
			for (String dontClick : options.getDontClickElements()) {
				crawlRules.dontClick(dontClick);
				//TODO: .withAttribute("value", "I don't recognize");
				//TODO: .underXPath("//*[@id='pageFooter']");
				//		.underXPath("//*[@id='content']/div/div[2]");
			}
		}
		if (options.getClickElements().size() > 0) {
			for (String click : options.getClickElements()) {
				crawlRules.click(click);
				//TODO: .withAttribute("type", "submit");
			}
		}
		if (options.getDontClickElementsChildrenOf().size() > 0) {
			for (String dontClick : options.getDontClickElementsChildrenOf()) {
				crawlRules.dontClickChildrenOf(dontClick);
			}
		}
		crawlRules.insertRandomDataInInputForms(options.shouldPutRandomDataInForms());
		crawlRules.clickElementsInRandomOrder(options.shouldClickElementsInRandomOrder());
		crawlRules.crawlFrames(options.shouldCrawlFrames());
		crawlRules.waitAfterReloadUrl(options.getWaitTimeAferReload(), TimeUnit.MILLISECONDS);
		crawlRules.waitAfterEvent(options.getWaitTimeAfterEvent(), TimeUnit.MILLISECONDS);
		crawlRules.clickOnce(options.shouldClickOnce());
		crawlRules.crawlHiddenAnchors(options.shouldCrawlHiddenAnchorsButton());

		return builder.build();
		
	}
	
	private void notifyDomVisitedObservers(Document document) {
		for (CrawlerObserver observer : observers) {
			observer.newDOMVisited(document);
		}
	}
	
	private void notifyCrawlingFinishedObservers(CrawlSession session) {
		for (CrawlerObserver observer : observers) {
			observer.finishedCrawling(session);
		}
	}
	
	public void addNewDOMObserver(CrawlerObserver observer) {
		observers.add(observer);
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void setCancelMonitor(BooleanSupplier cancelSupplier) {
		this.cancelSupplier = cancelSupplier;
	}

}
