package ca.concordia.cssanalyser.plugin.utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class AnalysisOptions {

	private boolean shouldAnalyzeDoms;
	private String url = "";
	private boolean shouldClickDefaultElements;
	private Set<String> clickElements = new LinkedHashSet<>(Arrays.asList("*"));
	private Set<String> dontClickElements = new LinkedHashSet<>();
	private Set<String> dontClickElementsChildrenOf = new LinkedHashSet<>(Arrays.asList("*"));
	private boolean shouldClickOnce;
	private boolean shouldCrawlHiddenAnchorsButton;
	private File outputDirectory;
	private int maxDepth = 1;
	private int maxStates = 2;
	private long waitTimeAferReload = 500;
	private long waitTimeAfterEvent = 500;
	private boolean randomDataInForms = false;
	private boolean elementsInRandomOrder = false;
	private boolean crawlFrames = false;
	
	public static class AnalysisOptionsBulider {

		private AnalysisOptions options;

		private AnalysisOptionsBulider(AnalysisOptions options) {
			this.options = options;
		}
		
		public AnalysisOptionsBulider withShouldAnalyzeDoms(boolean shouldAnalyzeDoms) {
			options.shouldAnalyzeDoms = shouldAnalyzeDoms;
			return this;
		}

		public AnalysisOptionsBulider withUrl(String url) {
			options.url = url;
			return this;
		}

		public AnalysisOptionsBulider withRandomDataInForms(boolean randomDataInForms) {
			options.randomDataInForms = randomDataInForms;
			return this;
		}

		public AnalysisOptionsBulider withElementsInRandomOrder(boolean elementsInRandomOrder) {
			options.elementsInRandomOrder = elementsInRandomOrder;
			return this;
		}

		public AnalysisOptionsBulider withCrawlFrames(boolean crawlFrames) {
			options.crawlFrames = crawlFrames;
			return this;
		}
		
		public AnalysisOptionsBulider withClickElements(String... click) {
			options.clickElements = new LinkedHashSet<>(Arrays.asList(click));
			return this;
		}
		
		public AnalysisOptionsBulider withDontClickElements(String... dontClick) {
			options.dontClickElements = new LinkedHashSet<>(Arrays.asList(dontClick));
			return this;
		}
		
		public AnalysisOptionsBulider withDontClickElementsChildrenOf(String... click) {
			options.dontClickElementsChildrenOf = new LinkedHashSet<>(Arrays.asList(click));
			return this;
		}
		
		public AnalysisOptionsBulider withOutputDirectory(String directory) throws IOException {
			if ("".equals(directory.trim()) || !(new File(directory).exists())) {
				options.outputDirectory = Files.createTempDirectory("crawl-output-folder").toFile();
			} else {
				options.outputDirectory = new File(directory);
			}
			return this;
		}

		public AnalysisOptionsBulider withMaxDepth(int maxDepth) {
			options.maxDepth = maxDepth;
			return this;
		}

		public AnalysisOptionsBulider withMaxStates(int maxStates) {
			options.maxStates = maxStates;
			return this;
		}

		public AnalysisOptionsBulider withWaitTimeAferReload(long waitTimeAferReload) {
			options.waitTimeAferReload = waitTimeAferReload;
			return this;
		}

		public AnalysisOptionsBulider withWaitTimeAfterEvent(long waitTimeAfterEvent) {
			options.waitTimeAfterEvent = waitTimeAfterEvent;
			return this;
		}
		
		public AnalysisOptionsBulider withShouldClickDefaultElements(boolean shouldClickDefaultElements) {
			options.shouldClickDefaultElements = shouldClickDefaultElements;
			return this;
		}

		public AnalysisOptionsBulider withShouldClickOnce(boolean shouldClickOnce) {
			options.shouldClickOnce = shouldClickOnce;
			return this;
		}

		public AnalysisOptionsBulider withShouldCrawlHiddenAnchorsButton(boolean shouldCrawlHiddenAnchorsButton) {
			options.shouldCrawlHiddenAnchorsButton = shouldCrawlHiddenAnchorsButton;
			return this;
		}
		
		public AnalysisOptions build() {
			return options;
		}

	}
	
	public boolean shouldAnalyzeDoms() {
		return shouldAnalyzeDoms;
	}
	
	public String getUrl() {
		return url;
	}

	public boolean shouldPutRandomDataInForms() {
		return randomDataInForms;
	}

	public boolean shouldClickElementsInRandomOrder() {
		return elementsInRandomOrder;
	}

	public boolean shouldCrawlFrames() {
		return crawlFrames;
	}
	
	public Set<String> getClickElements() {
		return clickElements;
	}
	
	public Set<String> getDontClickElements() {
		return dontClickElements;
	}
	
	public Set<String> getDontClickElementsChildrenOf() {
		return dontClickElementsChildrenOf;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public int getMaxStates() {
		return maxStates;
	}

	public long getWaitTimeAferReload() {
		return waitTimeAferReload;
	}

	public long getWaitTimeAfterEvent() {
		return waitTimeAfterEvent;
	}

	public boolean shouldClickDefaultElements() {
		return shouldClickDefaultElements;
	}

	public boolean shouldClickOnce() {
		return shouldClickOnce;
	}

	public boolean shouldCrawlHiddenAnchorsButton() {
		return shouldCrawlHiddenAnchorsButton;
	}
	
	public static AnalysisOptionsBulider getBuilder() {
		return new AnalysisOptionsBulider(new AnalysisOptions());
	}

	public static AnalysisOptionsBulider getBuilder(AnalysisOptions analysisOptions) {
		return new AnalysisOptionsBulider(analysisOptions);
	}

}


