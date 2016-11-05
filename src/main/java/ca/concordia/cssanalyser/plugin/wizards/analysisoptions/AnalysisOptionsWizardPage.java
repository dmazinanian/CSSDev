package ca.concordia.cssanalyser.plugin.wizards.analysisoptions;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import ca.concordia.cssanalyser.plugin.utility.AnalysisOptions;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;

public class AnalysisOptionsWizardPage extends WizardPage {
	
	private AnalysisOptions analysisOptions;
	
	private Button shouldAnalyzeDomButton;
	private Text urlText;
	private Button clickDefaultElementsButton;
	private Text clickItemsElementText;
	private Text dontClickElementsText;
	private Text dontClickChildrenOfText;
	private Text outputDirectoryText;
	private Button tempOutputFolder;
	private Spinner maxDepthSpinner;
	private Spinner maxStatesSpinner;
	private Text waitTimeAferReloadText;
	private Text waitTimeAfterEventText;
	private Button randomDataInFormsButton;
	private Button elementsInRandomOrderButton;
	private Button crawlFramesButton;
	private Button clickOnceButton;
	private Button crawlHiddenAnchorsButton;
	
	protected AnalysisOptionsWizardPage(AnalysisOptions analysisOptions) {
		super(LocalizedStrings.get(Keys.ANALYSIS_OPTIONS));
		this.analysisOptions = analysisOptions;
	}

	@Override
	public void createControl(Composite parent) {
		
		parent.setLayout(new GridLayout());
		GridData gridData = new GridData();
		gridData.heightHint = 480;
		parent.setLayoutData(gridData);
		
		GridData spannerGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		spannerGridData.horizontalSpan = 2;
		
		Composite analysisOptionsEnabledGroup = new Composite(parent, SWT.NONE);
		analysisOptionsEnabledGroup.setLayout(new GridLayout(2, false));
		analysisOptionsEnabledGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Composite analysisOptionsGroup = new Composite(parent, SWT.NONE);
		analysisOptionsGroup.setLayout(new GridLayout(2, false));
		analysisOptionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		new Label(analysisOptionsEnabledGroup, SWT.NONE).setText(LocalizedStrings.get(Keys.SHOULD_ANALYZE_DOM));
		Composite shouldAnalyzeDOMsGroup = new Composite(analysisOptionsEnabledGroup, SWT.NONE);
		shouldAnalyzeDOMsGroup.setLayout(new GridLayout(2, true));
		shouldAnalyzeDomButton = new Button(shouldAnalyzeDOMsGroup, SWT.RADIO);
		shouldAnalyzeDomButton.setText(LocalizedStrings.get(Keys.YES));
		shouldAnalyzeDomButton.setSelection(analysisOptions.shouldAnalyzeDoms());
		
		Button shouldNotAnalyzeDomButton = new Button(shouldAnalyzeDOMsGroup, SWT.RADIO);
		shouldNotAnalyzeDomButton.setText(LocalizedStrings.get(Keys.NO));
		shouldNotAnalyzeDomButton.setSelection(!analysisOptions.shouldAnalyzeDoms());
		
		new Label(analysisOptionsGroup, SWT.NONE).setText(LocalizedStrings.get(Keys.URL));
		urlText = new Text(analysisOptionsGroup, SWT.BORDER);
		urlText.setText(analysisOptions.getUrl());
		urlText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		clickDefaultElementsButton = new Button(analysisOptionsGroup, SWT.CHECK);
		clickDefaultElementsButton.setText(LocalizedStrings.get(Keys.CLICK_DEFAULT_ELEMENTS));
		clickDefaultElementsButton.setLayoutData(spannerGridData);
		clickDefaultElementsButton.setSelection(analysisOptions.shouldClickDefaultElements());
		
		new Label(analysisOptionsGroup, SWT.NONE).setText(LocalizedStrings.get(Keys.CLICK_ELEMENTS));
		clickItemsElementText = new Text(analysisOptionsGroup, SWT.BORDER);
		clickItemsElementText.setText(getCommaSeparatedStringFromSet(analysisOptions.getClickElements()));
		clickItemsElementText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		new Label(analysisOptionsGroup, SWT.NONE).setText(LocalizedStrings.get(Keys.DONT_CLICK_ELEMENTS));
		dontClickElementsText = new Text(analysisOptionsGroup, SWT.BORDER);
		dontClickElementsText.setText(getCommaSeparatedStringFromSet(analysisOptions.getDontClickElements()));
		dontClickElementsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(analysisOptionsGroup, SWT.NONE).setText(LocalizedStrings.get(Keys.DONT_CLICK_ELEMENTS_CHILDREN_OF));
		dontClickChildrenOfText = new Text(analysisOptionsGroup, SWT.BORDER);
		dontClickChildrenOfText.setText(getCommaSeparatedStringFromSet(analysisOptions.getDontClickElementsChildrenOf()));
		dontClickChildrenOfText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		new Label(analysisOptionsGroup, SWT.NONE).setText(LocalizedStrings.get(Keys.OUTPUT_DIRECTORY));
		Composite outputFolderComposite = new Composite(analysisOptionsGroup, SWT.NONE);
		outputFolderComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout outputFolderGridLayout = new GridLayout(2, false);
		outputFolderGridLayout.marginHeight = 0;
		outputFolderGridLayout.verticalSpacing = 0;
		outputFolderComposite.setLayout(outputFolderGridLayout);
		tempOutputFolder = new Button(outputFolderComposite, SWT.CHECK);
		tempOutputFolder.setText(LocalizedStrings.get(Keys.TEMP_OUT_FOLDER));
		File outputDirectory = analysisOptions.getOutputDirectory();
		tempOutputFolder.setSelection(outputDirectory == null);
		Composite specifyOutputFolderComposite = new Composite(outputFolderComposite, SWT.NONE);
		specifyOutputFolderComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout specifyOutputFolderCompositeLayout = new GridLayout(3, false);
		specifyOutputFolderCompositeLayout.marginHeight = 0;
		specifyOutputFolderCompositeLayout.verticalSpacing = 0;
		specifyOutputFolderComposite.setLayout(specifyOutputFolderCompositeLayout);
		new Label(specifyOutputFolderComposite, SWT.NONE).setText(LocalizedStrings.get(Keys.SPECIFY) + ":");
		outputDirectoryText = new Text(specifyOutputFolderComposite, SWT.BORDER);
		outputDirectoryText.setText(outputDirectory != null ? outputDirectory.getAbsolutePath() : "");
		GridData gridData2 = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gridData2.widthHint = 100;
		outputDirectoryText.setLayoutData(gridData2);
		Button outputDirectoryButton = new Button(specifyOutputFolderComposite, SWT.PUSH);
		outputDirectoryButton.setText("...");
		outputDirectoryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setFilterPath(outputDirectoryText.getText());
				String directoryPath = dialog.open();
				if (directoryPath != null) {
					outputDirectoryText.setText(directoryPath);
				}
			}
		});
		
		new Label(analysisOptionsGroup, SWT.NONE).setText(LocalizedStrings.get(Keys.MAX_DEPTH));
		maxDepthSpinner = new Spinner(analysisOptionsGroup, SWT.BORDER);
		maxDepthSpinner.setSelection(analysisOptions.getMaxDepth());
		maxDepthSpinner.setMinimum(1);
		
		new Label(analysisOptionsGroup, SWT.NONE).setText(LocalizedStrings.get(Keys.MAX_STATES));
		maxStatesSpinner = new Spinner(analysisOptionsGroup, SWT.BORDER);
		maxStatesSpinner.setSelection(analysisOptions.getMaxStates());
		maxStatesSpinner.setMinimum(1);
		
		GridData numberGridData = new GridData();
		numberGridData.widthHint = 50;
		
		new Label(analysisOptionsGroup, SWT.NONE).setText(LocalizedStrings.get(Keys.WAIT_TIME_AFTER_RELOAD));
		waitTimeAferReloadText = new Text(analysisOptionsGroup, SWT.BORDER);
		waitTimeAferReloadText.setText(String.valueOf(analysisOptions.getWaitTimeAferReload()));
		waitTimeAferReloadText.setLayoutData(numberGridData);
		
		new Label(analysisOptionsGroup, SWT.NONE).setText(LocalizedStrings.get(Keys.WAIT_TIME_AFTER_EVENT));
		waitTimeAfterEventText = new Text(analysisOptionsGroup, SWT.BORDER);
		waitTimeAfterEventText.setText(String.valueOf(analysisOptions.getWaitTimeAfterEvent()));
		waitTimeAfterEventText.setLayoutData(numberGridData);
		
		randomDataInFormsButton = new Button(analysisOptionsGroup, SWT.CHECK);
		randomDataInFormsButton.setText(LocalizedStrings.get(Keys.RANDOM_DATA_IN_FORMS));
		randomDataInFormsButton.setLayoutData(spannerGridData);
		randomDataInFormsButton.setSelection(analysisOptions.shouldPutRandomDataInForms());
		
		elementsInRandomOrderButton = new Button(analysisOptionsGroup, SWT.CHECK);
		elementsInRandomOrderButton.setText(LocalizedStrings.get(Keys.CLICK_RANDOMLY));
		elementsInRandomOrderButton.setLayoutData(spannerGridData);
		elementsInRandomOrderButton.setSelection(analysisOptions.shouldClickElementsInRandomOrder());
		
		crawlFramesButton = new Button(analysisOptionsGroup, SWT.CHECK);
		crawlFramesButton.setText(LocalizedStrings.get(Keys.CRAWL_FRAMES));
		crawlFramesButton.setLayoutData(spannerGridData);
		crawlFramesButton.setSelection(analysisOptions.shouldCrawlFrames());
		
		clickOnceButton = new Button(analysisOptionsGroup, SWT.CHECK);
		clickOnceButton.setText(LocalizedStrings.get(Keys.CLICK_ONCE));
		clickOnceButton.setLayoutData(spannerGridData);
		clickOnceButton.setSelection(analysisOptions.shouldClickOnce());
		
		crawlHiddenAnchorsButton = new Button(analysisOptionsGroup, SWT.CHECK);
		crawlHiddenAnchorsButton.setText(LocalizedStrings.get(Keys.CRAWL_HIDDEN_ANCHORS));
		crawlHiddenAnchorsButton.setLayoutData(spannerGridData);
		crawlHiddenAnchorsButton.setSelection(analysisOptions.shouldCrawlHiddenAnchorsButton());
		
		shouldAnalyzeDomButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				recursiveSetEnabled(analysisOptionsGroup, shouldAnalyzeDomButton.getSelection());
				if (shouldAnalyzeDomButton.getSelection() && tempOutputFolder.getSelection()) {
					recursiveSetEnabled(specifyOutputFolderComposite, false);	
				}
			}
		});
		
		tempOutputFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				recursiveSetEnabled(specifyOutputFolderComposite, !tempOutputFolder.getSelection());
			}
		});
		
		recursiveSetEnabled(analysisOptionsGroup, analysisOptions.shouldAnalyzeDoms());
		
		setControl(shouldAnalyzeDOMsGroup);
		
	}
	
	private String getCommaSeparatedStringFromSet(Set<String> set) {
		StringBuilder toReturn = new StringBuilder();
		for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			String element = iterator.next();
			toReturn.append(element);
			if (iterator.hasNext()) {
				toReturn.append(", ");
			}
		}
		return toReturn.toString();
	}

	public AnalysisOptions getAnalysisOptions() {
		try {
			AnalysisOptions.getBuilder(analysisOptions)
				.withShouldAnalyzeDoms(shouldAnalyzeDomButton.getSelection())
				.withUrl(urlText.getText())
				.withShouldClickDefaultElements(clickDefaultElementsButton.getSelection())
				.withClickElements(clickItemsElementText.getText())
				.withDontClickElements(dontClickElementsText.getText())
				.withDontClickElementsChildrenOf(dontClickChildrenOfText.getText())
				.withOutputDirectory(tempOutputFolder.getSelection() ? "" : outputDirectoryText.getText())
				.withMaxDepth(Integer.valueOf(maxDepthSpinner.getSelection()))
				.withMaxStates(Integer.valueOf(maxStatesSpinner.getSelection()))
				.withWaitTimeAferReload(Long.valueOf(waitTimeAferReloadText.getText()))
				.withWaitTimeAfterEvent(Long.valueOf(waitTimeAfterEventText.getText()))
				.withRandomDataInForms(randomDataInFormsButton.getSelection())
				.withElementsInRandomOrder(elementsInRandomOrderButton.getSelection())
				.withCrawlFrames(crawlFramesButton.getSelection())
				.withShouldClickOnce(clickOnceButton.getSelection())
				.withShouldCrawlHiddenAnchorsButton(crawlHiddenAnchorsButton.getSelection());
		} catch (NumberFormatException | IOException e) {
			setPageComplete(validatePage());
		}
		return analysisOptions;
	}

	private boolean validatePage() {
		return true;
	}

	public void recursiveSetEnabled(Control control, boolean enabled) {
		if (control instanceof Composite) {
			Composite composite = (Composite)control;
			for (Control child : composite.getChildren()) {
				recursiveSetEnabled(child, enabled);
			}
		}
		control.setEnabled(enabled);
	}

}
