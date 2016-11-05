package ca.concordia.cssanalyser.plugin.views.duplicationrefactoring;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.w3c.dom.Document;

import com.crawljax.core.CrawlSession;

import ca.concordia.cssanalyser.analyser.duplication.DuplicationDetector;
import ca.concordia.cssanalyser.analyser.duplication.items.Item;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSetList;
import ca.concordia.cssanalyser.analyser.progressmonitor.ProgressMonitor;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorType;
import ca.concordia.cssanalyser.parser.CSSParser;
import ca.concordia.cssanalyser.parser.CSSParserFactory;
import ca.concordia.cssanalyser.parser.CSSParserFactory.CSSParserType;
import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.plugin.annotations.CSSAnnotation;
import ca.concordia.cssanalyser.plugin.annotations.CSSAnnotationType;
import ca.concordia.cssanalyser.plugin.refactoring.DuplicationRefactoring;
import ca.concordia.cssanalyser.plugin.refactoring.grouping.Crawler;
import ca.concordia.cssanalyser.plugin.refactoring.grouping.CrawlerObserver;
import ca.concordia.cssanalyser.plugin.refactoring.grouping.GroupingRefactoring;
import ca.concordia.cssanalyser.plugin.refactoring.mixins.MixinDuplicationInfo;
import ca.concordia.cssanalyser.plugin.refactoring.mixins.MixinMigrationRefactoring;
import ca.concordia.cssanalyser.plugin.utility.AnalysisOptions;
import ca.concordia.cssanalyser.plugin.utility.AnalysisResultsStorage;
import ca.concordia.cssanalyser.plugin.utility.AnnotationsUtil;
import ca.concordia.cssanalyser.plugin.utility.DuplicationInfo;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;
import ca.concordia.cssanalyser.plugin.utility.ViewsUtil;
import ca.concordia.cssanalyser.plugin.views.dependenciesvisualization.DependenciesVisualizationView;
import ca.concordia.cssanalyser.plugin.wizards.analysisoptions.AnalysisOptionsWizard;
import ca.concordia.cssanalyser.plugin.wizards.duplication.DuplicationRefactoringWizard;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

public class DuplicationRefactoringView extends ViewPart {

	private static final PreprocessorType PREPROCESSOR_TYPE = PreprocessorType.LESS;

	public static final String ID = "ca.concordia.cssanalyser.plugin.views.duplicationrefactoring.DuplicationRefactoringView";

	private TableViewer viewer;
	private IAction findDuplicatedDeclarationsAction;
	private IAction mixinMigrationAction;
	private IAction tableRowDoubleClickAction;
	private IAction groupingRefactoringOpportunitiesAction;
	private IAction clearAnnotationsAction;
	private IAction clearResultsAction;
	private IAction showSettingsAction;
	private IAction refreshDependenciesAction;
	private IAction showDependenciesAction;
	
	private IFile selectedFile;
	private boolean allowDifferencesInValues;
	private boolean liveDetection;
	private List<CSSAnnotation> currentAnnotations = new ArrayList<>();
	private Map<IFile, AnalysisResultsStorage> fileToResultsMap = new HashMap<>();
	private Label numberOfOpportunitiesLabel;
	private AnalysisOptions analysisOptions = new AnalysisOptions();
	private List<Document> documents = new ArrayList<>();
	private CrawlSession session;
	
	private IPropertyListener propertyListener = new IPropertyListener() {
		@Override
		public void propertyChanged(Object source, int propertyID) {
			if (propertyID == IEditorPart.PROP_DIRTY && source instanceof StructuredTextEditor) {
				StructuredTextEditor ste = (StructuredTextEditor)source;
				if (!ste.isDirty()) {
					setSelectedFile(((IFileEditorInput)ste.getEditorInput()).getFile());
					if (liveDetection) {
						detectDuplications();
					}
				} else {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							if (viewer != null) {
								clearResults(ste);
								setSelectedFile(null);
							}
						}
					});
				}
			}
		}
	};
	
	private IPartListener2 partListener = new IPartListener2() {
		@Override
		public void partActivated(IWorkbenchPartReference ref) {
			IWorkbenchPart part = ref.getPart(false);
			if (part instanceof StructuredTextEditor) {
				StructuredTextEditor structuredTextEditor = (StructuredTextEditor) part;
				IEditorInput editorInput = structuredTextEditor.getEditorInput();
				if (editorInput instanceof IFileEditorInput) {
					IFileEditorInput iFileEditorInput = (IFileEditorInput) editorInput;
					IFile file = iFileEditorInput.getFile();
					if (hasStyleSheetExtension(file)) {
						if (!structuredTextEditor.isDirty()) {
							setSelectedFile(file);
						}
						part.addPropertyListener(propertyListener);
					} else {
						setSelectedFile(null);
					}
				} else {
					setSelectedFile(null);
				}
			}
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference ref) {
			IWorkbenchPart part = ref.getPart(false);
			part.removePropertyListener(propertyListener);
		}
		
		@Override
		public void partClosed(IWorkbenchPartReference ref) {
			IWorkbenchPart part = ref.getPart(false);
			if (part instanceof StructuredTextEditor) {
				StructuredTextEditor structuredTextEditor = (StructuredTextEditor) part;
				IEditorInput editorInput = structuredTextEditor.getEditorInput();
				if (editorInput instanceof IFileEditorInput) {
					IFileEditorInput iFileEditorInput = (IFileEditorInput) editorInput;
					IFile file = iFileEditorInput.getFile();
					fileToResultsMap.remove(file);
				}
			}
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference arg0) {}

		@Override
		public void partHidden(IWorkbenchPartReference arg0) {}

		@Override
		public void partInputChanged(IWorkbenchPartReference arg0) {}

		@Override
		public void partOpened(IWorkbenchPartReference arg0) {}

		@Override
		public void partVisible(IWorkbenchPartReference arg0) {}
	};
	
	private static class DuplicationViewContentProvider implements IStructuredContentProvider {
		private final List<DuplicationInfo> duplicationInfoList;
		public DuplicationViewContentProvider(List<DuplicationInfo> duplicationInfoList) {
			this.duplicationInfoList = duplicationInfoList;
		}
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {}
		
		public void dispose() {}
		
		public Object[] getElements(Object parent) {
			if (duplicationInfoList == null)
				return new String[] {};
			return duplicationInfoList.toArray();
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout gridLayout = new GridLayout();
	    gridLayout.numColumns = 1;		
	    parent.setLayout(gridLayout);
	    
		createTableViewer(parent);
		createBottomBar(parent);		
		makeActions();
		hookDoubleClickAction();
		contributeToActionBars();
		hookListeners();
	}
	
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	@Override
	public void dispose() {
		IEditorReference editorReference = ViewsUtil.getEditorReferenceForIFileIfOpened(selectedFile);
		if (editorReference != null) {
			clearAnnotations((StructuredTextEditor)editorReference.getEditor(false));
		}
		getSite().getWorkbenchWindow().getPartService().removePartListener(partListener);
		viewer = null;
		super.dispose();
	}
	
	private void hookListeners() {
		getSite().getWorkbenchWindow().getPartService().addPartListener(partListener);
	}

	private void createTableViewer(Composite parent) {
		
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		viewer.setContentProvider(new DuplicationViewContentProvider(null));
		viewer.setInput(getViewSite());
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(8, true));
		layout.addColumnData(new ColumnWeightData(25, true));
		layout.addColumnData(new ColumnWeightData(6, true));
		layout.addColumnData(new ColumnWeightData(30, true));
		layout.addColumnData(new ColumnWeightData(5, true));
		layout.addColumnData(new ColumnWeightData(20, true));
		viewer.getTable().setLayout(layout);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true); 
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.RECREATE);

		TableViewerColumn declarationCountViewerColumn = new TableViewerColumn(viewer, SWT.CENTER);
		TableColumn declarationCountColumn = declarationCountViewerColumn.getColumn();
		declarationCountColumn.setText(LocalizedStrings.get(Keys.NUMBER_OF_DECLARATIONS));
		declarationCountColumn.setResizable(true);
		declarationCountColumn.addListener(SWT.Selection, new ColumnSortListener(viewer, 0));
		declarationCountViewerColumn.setLabelProvider(new DuplicationTableViewerLabelProvider(0));
		
		TableViewerColumn declarationsViewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
		TableColumn declarationColumn = declarationsViewerColumn.getColumn();
		declarationColumn.setText(LocalizedStrings.get(Keys.DECLARATIONS));
		declarationColumn.setResizable(true);
		declarationColumn.addListener(SWT.Selection, new ColumnSortListener(viewer, 1));
		declarationsViewerColumn.setLabelProvider(new DuplicationTableViewerLabelProvider(1));
		
		TableViewerColumn selectorCountViewerColumn = new TableViewerColumn(viewer, SWT.CENTER);
		TableColumn selectorCountColumn = selectorCountViewerColumn.getColumn();
		selectorCountColumn.setText(LocalizedStrings.get(Keys.NUMBER_OF_SELECTORS));
		selectorCountColumn.setResizable(true);
		selectorCountColumn.addListener(SWT.Selection, new ColumnSortListener(viewer, 2));
		selectorCountViewerColumn.setLabelProvider(new DuplicationTableViewerLabelProvider(2));
		
		TableViewerColumn selectorsViewerColmumn = new TableViewerColumn(viewer, SWT.LEFT);
		TableColumn column1 = selectorsViewerColmumn.getColumn();
		column1.setText(LocalizedStrings.get(Keys.SELECTORS));
		column1.setResizable(true);
		column1.addListener(SWT.Selection, new ColumnSortListener(viewer, 3));
		selectorsViewerColmumn.setLabelProvider(new DuplicationTableViewerLabelProvider(3));
		
		TableViewerColumn duplicationTypesViewerColumn = new TableViewerColumn(viewer, SWT.CENTER);
		TableColumn duplicationTypesColumn = duplicationTypesViewerColumn.getColumn();
		duplicationTypesColumn.setText(LocalizedStrings.get(Keys.DUPLICATION_TYPES));
		duplicationTypesColumn.setResizable(true);
		duplicationTypesColumn.addListener(SWT.Selection, new ColumnSortListener(viewer, 4));
		duplicationTypesViewerColumn.setLabelProvider(new DuplicationTableViewerLabelProvider(4));
		
		TableViewerColumn declarationCategoriesViewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
		TableColumn declarationCategoriesColumn = declarationCategoriesViewerColumn.getColumn();
		declarationCategoriesColumn.setText(LocalizedStrings.get(Keys.PROPERTY_CATEGORIES));
		declarationCategoriesColumn.setResizable(true);
		declarationCategoriesColumn.addListener(SWT.Selection, new ColumnSortListener(viewer, 5));
		declarationCategoriesViewerColumn.setLabelProvider(new DuplicationTableViewerLabelProvider(5));
		
		viewer.setColumnProperties(new String[] {"declarations", "selectors", "duplicationTypes", "declarationCategories"});
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				viewer.getTable().setMenu(null);
				DuplicationInfo selectedDuplicationInfo = getSelectedDuplicationInfo();
				if (selectedDuplicationInfo != null) {
					getRightClickMenu(viewer, selectedDuplicationInfo);
				}
			}

			private void getRightClickMenu(TableViewer viewer, DuplicationInfo duplicationInfo) {
				MenuManager menuMgr = new MenuManager("#PopupMenu");
				menuMgr.setRemoveAllWhenShown(true);
				menuMgr.addMenuListener(new IMenuListener() {
					public void menuAboutToShow(IMenuManager manager) {
						boolean haveDifferences = duplicationInfo.hasDifferences();
						if (!haveDifferences){ 
							manager.add(groupingRefactoringOpportunitiesAction);
							manager.add(new Separator());
							//manager.add(extendMigrationAction);
						}
						manager.add(mixinMigrationAction);
						manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
					}
				});
				Menu menu = menuMgr.createContextMenu(viewer.getControl());
				viewer.getControl().setMenu(menu);
				getSite().registerContextMenu(menuMgr, viewer);
			}
		});
	}
	
	private void createBottomBar(Composite parent) {
		Composite bottomBar = new Composite(parent, SWT.NONE);
		GridLayout bottomBarLayout = new GridLayout(3, false);
		bottomBarLayout.horizontalSpacing = 20;
		bottomBar.setLayout(bottomBarLayout);
		bottomBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		
		Button allowDifferencesInValuesButton = new Button(bottomBar, SWT.CHECK);
		allowDifferencesInValuesButton.setText(LocalizedStrings.get(Keys.INCLUDE_DIFFERENCES));
		allowDifferencesInValuesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				allowDifferencesInValues = allowDifferencesInValuesButton.getSelection();
			}
		});
		
		Button liveDetectionButton = new Button(bottomBar, SWT.CHECK);
		liveDetectionButton.setText(LocalizedStrings.get(Keys.DETECT_ON_SAVE));
		liveDetectionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				liveDetection = liveDetectionButton.getSelection();
			}
		});
		
		numberOfOpportunitiesLabel = new Label(bottomBar, SWT.NONE);
		numberOfOpportunitiesLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(findDuplicatedDeclarationsAction);
		manager.add(showSettingsAction);
		manager.add(new Separator());
		manager.add(clearResultsAction);
		manager.add(clearAnnotationsAction);
		manager.add(new Separator());
		manager.add(refreshDependenciesAction);
		manager.add(showDependenciesAction);
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(showDependenciesAction);
		manager.add(refreshDependenciesAction);
		manager.add(new Separator());
		manager.add(clearAnnotationsAction);
		manager.add(clearResultsAction);
		manager.add(new Separator());
		manager.add(showSettingsAction);
		manager.add(findDuplicatedDeclarationsAction);
	}

	private void makeActions() {
		
		findDuplicatedDeclarationsAction = new Action() {
			public void run() {				
				detectDuplications();
			}
		};
		findDuplicatedDeclarationsAction.setText(LocalizedStrings.get(Keys.FIND_DUPLICATED_DECLARATIONS));
		findDuplicatedDeclarationsAction.setToolTipText(LocalizedStrings.get(Keys.FIND_DUPLICATED_DECLARATIONS));
		findDuplicatedDeclarationsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		findDuplicatedDeclarationsAction.setEnabled(false);
		
		clearAnnotationsAction = new Action() {
			@Override
			public void run() {
				IEditorReference editorReference = ViewsUtil.getEditorReferenceForIFileIfOpened(selectedFile);
				if (editorReference != null) {
					clearAnnotations((StructuredTextEditor)editorReference.getEditor(false));	
				}
			}
		};
		clearAnnotationsAction.setText(LocalizedStrings.get(Keys.CLEAR_ANNOTATIONS));
		clearAnnotationsAction.setToolTipText(LocalizedStrings.get(Keys.CLEAR_ANNOTATIONS));
		clearAnnotationsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_REMOVEALL));
		clearAnnotationsAction.setEnabled(false);
		
		clearResultsAction = new Action() {
			@Override
			public void run() {
				IEditorReference editorReference = ViewsUtil.getEditorReferenceForIFileIfOpened(selectedFile);
				if (editorReference != null) {
					StructuredTextEditor ste = (StructuredTextEditor)editorReference.getEditor(false);
					clearResults(ste);
				}
			}
		};
		clearResultsAction.setText(LocalizedStrings.get(Keys.CLEAR_RESULTS));
		clearResultsAction.setToolTipText(LocalizedStrings.get(Keys.CLEAR_RESULTS));
		clearResultsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		clearResultsAction.setEnabled(false);
		
		showSettingsAction = new Action() {
			@Override
			public void run() {
				showAnalysisWizard();
			}

		};
		showSettingsAction.setText(LocalizedStrings.get(Keys.SHOW_ANALYSIS_SETTINGS));
		showSettingsAction.setToolTipText(LocalizedStrings.get(Keys.SHOW_ANALYSIS_SETTINGS));
		showSettingsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_DEF_VIEW));
		
		tableRowDoubleClickAction = new Action() {
			public void run() {
				highlightCodeForSelection();				
			}
		};

		mixinMigrationAction = new Action() {
			@Override
			public void run() {
				if (selectedFile != null) {
					if (!checkFileIsSaved()) {
						return;
					}
					DuplicationInfo selectedDuplicationInfo = getSelectedDuplicationInfo();
					if (selectedDuplicationInfo != null) {
						MixinDuplicationInfo selectedMixinDuplicationInfo = new MixinDuplicationInfo(selectedDuplicationInfo, PREPROCESSOR_TYPE);
						MixinMigrationRefactoring refactoring = new MixinMigrationRefactoring(selectedMixinDuplicationInfo);
						showRefactoringWizard(refactoring);
						// showDetailedError(parseException);
					}
				}
			}
		};
		mixinMigrationAction.setText(LocalizedStrings.get(Keys.EXTRACT_MIXIN));

		groupingRefactoringOpportunitiesAction = new Action() {
			@Override
			public void run() {
				if (selectedFile != null) {
					if (!checkFileIsSaved()) {
						return;
					}
					DuplicationInfo selectedDuplicationInfo = getSelectedDuplicationInfo();
					if (selectedDuplicationInfo != null) {
						ItemSet selectedItemSet = selectedDuplicationInfo.getItemSet();
						if (!selectedItemSet.containsDifferencesInValues()) {
							DuplicationRefactoring refactoring;
							if (documents.size() > 0) {
								CSSValueOverridingDependencyList dependencies = getOverridingDependencies();
								refactoring = new GroupingRefactoring(selectedDuplicationInfo, dependencies);
							} else {
								refactoring = new GroupingRefactoring(selectedDuplicationInfo);
							}
							showRefactoringWizard(refactoring);
						}
					}
				}
			}
		};
		groupingRefactoringOpportunitiesAction.setText(LocalizedStrings.get(Keys.GROUP_SELECTORS));

		refreshDependenciesAction = new Action() {
			@Override
			public void run() {
				analyzeDOMs();
			}
		};
		refreshDependenciesAction.setEnabled(false);
		refreshDependenciesAction.setText(LocalizedStrings.get(Keys.REFRESEH_DEPENDENCIES));
		refreshDependenciesAction.setToolTipText(LocalizedStrings.get(Keys.REFRESEH_DEPENDENCIES));
		refreshDependenciesAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));
		
		showDependenciesAction = new Action() {
			@Override
			public void run() {
				showDependencies();
			}
		};
		showDependenciesAction.setEnabled(false);
		showDependenciesAction.setText(LocalizedStrings.get(Keys.VISUALIZE_DEPENDENCIES));
		showDependenciesAction.setToolTipText(LocalizedStrings.get(Keys.VISUALIZE_DEPENDENCIES));
		showDependenciesAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));
		
		IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (activeEditor != null) {
			IEditorInput editorInput = activeEditor.getEditorInput(); 
			if (editorInput instanceof IFileEditorInput) {
				IFileEditorInput iFileEditorInput = (IFileEditorInput) editorInput;
				IFile file = iFileEditorInput.getFile();
				if (hasStyleSheetExtension(file)) {
					setSelectedFile(file);
				}
			}
		}
	}
	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				tableRowDoubleClickAction.run();
			}
		});
	}
	
	private void clearResults(StructuredTextEditor forTextEditor) {
		viewer.setContentProvider(new DuplicationViewContentProvider(null));
		if (forTextEditor != null)
			clearAnnotations(forTextEditor);
		clearResultsAction.setEnabled(false);
		numberOfOpportunitiesLabel.setText("");
	}
	
	private void fillTableViewer(List<DuplicationInfo> duplicationInfo) {
		viewer.setContentProvider(new DuplicationViewContentProvider(duplicationInfo));
		if (duplicationInfo != null && !duplicationInfo.isEmpty()) {
			clearResultsAction.setEnabled(true);
			numberOfOpportunitiesLabel.setText(String.format(LocalizedStrings.get(Keys.NUMBER_OF_OPPORTUNITIES), String.valueOf(duplicationInfo.size())));
		} else {
			clearResultsAction.setEnabled(false);
			numberOfOpportunitiesLabel.setText("");
		}
		numberOfOpportunitiesLabel.getParent().layout(true, true);
	}
	
	private void clearAnnotations(StructuredTextEditor ste) {
		AnnotationsUtil.clearAnnotations(ste);
		currentAnnotations = new ArrayList<>();
		clearAnnotationsAction.setEnabled(false);
	}
	
	private void setAnnotations(List<CSSAnnotation> annotations, StructuredTextEditor ste) {
		if (ste != null && annotations.size() > 0) {
			currentAnnotations = annotations;
			AnnotationsUtil.setAnnotations(annotations, ste);
			if (annotations.size() > 0) {
				clearAnnotationsAction.setEnabled(true);
			} else {
				clearAnnotationsAction.setEnabled(false);
			}
		} else {
			clearAnnotationsAction.setEnabled(false);
		}
	}

	private void setSelectedFile(IFile file) {
		if (file == selectedFile)
			return;
		if (selectedFile != null) {
			IEditorReference editorReference = ViewsUtil.getEditorReferenceForIFileIfOpened(selectedFile);
			if (editorReference != null) {
				StructuredTextEditor ste = (StructuredTextEditor)editorReference.getEditor(false);
				List<DuplicationInfo> duplicationInfoList = ((DuplicationViewContentProvider)viewer.getContentProvider()).duplicationInfoList;
				fileToResultsMap.put(selectedFile, new AnalysisResultsStorage(selectedFile, duplicationInfoList, currentAnnotations));
				clearResults(ste);
			}
		}
		if (file != null) {
			if (fileToResultsMap.containsKey(file)) {
				AnalysisResultsStorage resultsStorage = fileToResultsMap.get(file);
				fillTableViewer(resultsStorage.getDuplicationInfoList());
				IEditorReference editorReference = ViewsUtil.getEditorReferenceForIFileIfOpened(file);
				if (editorReference != null) {
					StructuredTextEditor ste = (StructuredTextEditor)editorReference.getEditor(false);
					setAnnotations(resultsStorage.getAnnotations(), ste);	
				}
			}
		}
		selectedFile = file;
		findDuplicatedDeclarationsAction.setEnabled(file != null);
	}

	private void detectDuplications() {
		if (selectedFile != null) {
			IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
			try {
				progressService.busyCursorWhile(new IRunnableWithProgress() {
					public void run(IProgressMonitor iProgressMonitor) {
						iProgressMonitor.beginTask(LocalizedStrings.get(Keys.FINDING_REFACTORING_OPPORTUNITIES), 100);
						try {
							// Parsing
							iProgressMonitor.subTask(LocalizedStrings.get(Keys.PARSING_CSS_FILE));
							iProgressMonitor.worked(10);
							if (iProgressMonitor.isCanceled())
								return;
							
							// Finding duplications
							iProgressMonitor.subTask(LocalizedStrings.get(Keys.FINDING_DUPLICATIONS));
							final DuplicationDetector duplicationDetector = new DuplicationDetector(getSelectedStyleSheet());
							if (allowDifferencesInValues) {
								duplicationDetector.findPropertyDuplications();
							} else {
								duplicationDetector.findDuplications();
							}
							iProgressMonitor.worked(20);
							if (iProgressMonitor.isCanceled())
								return;
							
							// FPGrowth
							iProgressMonitor.subTask(LocalizedStrings.get(Keys.GETTING_OPPORTUNITIES));
							final List<ItemSetList> fpgrowthResults = duplicationDetector.fpGrowth(2, true, new ProgressMonitor() {
								@Override
								public void progressed(int percent) {
									if (percent % 2 == 0)
										iProgressMonitor.worked(1);
								}
								
								@Override
								public boolean shouldStop() {
									return iProgressMonitor.isCanceled();
								}
							});
							if (iProgressMonitor.isCanceled())
								return;
							
							// Getting info from duplications
							iProgressMonitor.subTask(LocalizedStrings.get(Keys.GETTING_INFO));
							List<DuplicationInfo> duplicationInfo = new ArrayList<>();
							for (ItemSetList isl : fpgrowthResults) {
								if (iProgressMonitor.isCanceled())
									break;
								for (ItemSet itemSet : isl) {
									if (itemSet.size() > 0) {
										duplicationInfo.add(new DuplicationInfo(itemSet, selectedFile));
									}
								}
							}
							iProgressMonitor.worked(10);
							
							if (iProgressMonitor.isCanceled())
								return;
							
							// Populating view
							iProgressMonitor.subTask(LocalizedStrings.get(Keys.POPULATING_VIEW));
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									if (duplicationInfo.isEmpty()) {
										String title = LocalizedStrings.get(Keys.NO_DUPLICATIONS_FOUND_TITLE);
										String message = LocalizedStrings.get(Keys.NO_DUPLICATIONS_FOUND);
										if (!allowDifferencesInValues) {
											message += "\n" + LocalizedStrings.get(Keys.CONSIDER_HAVING_DIFFERENCES);
										}
										MessageDialog.openInformation(getSite().getShell(), title, message);
									}
									fillTableViewer(duplicationInfo);
								}
							});
							iProgressMonitor.worked(10);
							
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				Throwable throwable = e.getCause();
				if (throwable.getCause() != null && 
						throwable.getCause() instanceof ParseException) {
					throwable = throwable.getCause();
				}
				ViewsUtil.showDetailedError(throwable);
			}
		}
	}

	private boolean checkFileIsSaved() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();
		IProgressService progressService = workbench.getProgressService();
		try {
			StructuredTextEditor ste = (StructuredTextEditor)IDE.openEditor(page, selectedFile);
			if (ste.isDirty()) {
				boolean askForSave = MessageDialog.openConfirm(workbenchWindow.getShell(),
						LocalizedStrings.get(Keys.SAVE_FILE_DIALOG_TITLE), 
						String.format(LocalizedStrings.get(Keys.SAVE_FILE_MESSAGE), selectedFile.getName()));
				if (askForSave) {
					try {
						progressService.busyCursorWhile(new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										ste.doSave(monitor);
									}
								});
							}
						});
					} catch (InvocationTargetException | InterruptedException ex){
						ViewsUtil.showDetailedError(ex);
						return false;
					}
				} else {
					return false;
				}
			}
		} catch (PartInitException partInitException) {
			ViewsUtil.showDetailedError(partInitException);
			return false;
		}
		return true;
	}

	private DuplicationInfo getSelectedDuplicationInfo() {
		ISelection selection = viewer.getSelection();
		if (!selection.isEmpty()) {
			DuplicationInfo selectedRowInfo = (DuplicationInfo)((IStructuredSelection)selection).getFirstElement();
			return selectedRowInfo;
		}
		return null;
	}
	
	private StyleSheet getSelectedStyleSheet() {
		try {
			CSSParser parser = CSSParserFactory.getCSSParser(CSSParserType.LESS);
			return parser.parseExternalCSS(selectedFile.getLocation().toOSString());
		} catch (ParseException e) {
			
			ViewsUtil.showDetailedError(e);
		}
		return null;
	}

	private void showRefactoringWizard(DuplicationRefactoring refactoring) {
		DuplicationRefactoringWizard wizard = new DuplicationRefactoringWizard(refactoring);
		RefactoringWizardOpenOperation wizardOpenOperation = new RefactoringWizardOpenOperation(wizard);
		try { 
			String titleForFailedChecks = "";
			wizardOpenOperation.run(getSite().getShell(), titleForFailedChecks); 
		} catch(InterruptedException e) {
			ViewsUtil.showDetailedError(e);
		}
	}

	private boolean hasStyleSheetExtension(IFile file) {
		String fileExtension = file.getFileExtension().toLowerCase();
		return "css".equals(fileExtension) || "less".equals(fileExtension);
	}

	private void highlightCodeForSelection() {
		ItemSet selectedItemSet = getSelectedDuplicationInfo().getItemSet();
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			StructuredTextEditor ste = (StructuredTextEditor)IDE.openEditor(page, selectedFile);
			Set<Declaration> declarationsToAnnotate = new HashSet<>();
			clearAnnotations(ste);
			for(Item item : selectedItemSet) {
				for (Declaration declaration : item) {
					if (selectedItemSet.supportContains(declaration.getSelector())) {
						//TODO: more tests
						if (declaration instanceof ShorthandDeclaration && ((ShorthandDeclaration) declaration).isVirtual()) {
							ShorthandDeclaration shorthandDeclaration = (ShorthandDeclaration) declaration;
							for (Declaration individualDeclaration : shorthandDeclaration.getIndividualDeclarations()) {
								declarationsToAnnotate.add(individualDeclaration);
							}
						} else {
							declarationsToAnnotate.add(declaration);
						}
					}
				}
			}
			
			List<CSSAnnotation> annotations = new ArrayList<>();
			for (Declaration declaration : declarationsToAnnotate) {
				Position position = new Position(declaration.getLocationInfo().getOffset(), declaration.getLocationInfo().getLength());
				CSSAnnotation cssAnnotation = new CSSAnnotation(CSSAnnotationType.DUPLICATION, 
						LocalizedStrings.get(Keys.DUPLICATED) + declaration.getProperty(),
						position);
				annotations.add(cssAnnotation);
			}
			setAnnotations(annotations, ste);
		} catch (PartInitException e) {
			ViewsUtil.showDetailedError(e);
		}
	}
	
	private void showAnalysisWizard() {
		AnalysisOptionsWizard analysisOptionsWizard = new AnalysisOptionsWizard(analysisOptions);
		WizardDialog wizardDialog = new WizardDialog(getViewSite().getShell(), analysisOptionsWizard);
		if (wizardDialog.open() == Window.OK) {
			analysisOptions = analysisOptionsWizard.getAnalysisOptions();
			refreshDependenciesAction.setEnabled(analysisOptions.shouldAnalyzeDoms());
		}
	}

	private void analyzeDOMs() {
		if (analysisOptions.shouldAnalyzeDoms()) {
			Crawler crawler = new Crawler(analysisOptions);
			documents.clear();
			Job job = Job.create(LocalizedStrings.get(Keys.CRAWLING_JOB), new ICoreRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					crawler.setCancelMonitor(() -> monitor.isCanceled());
					crawler.addNewDOMObserver(new CrawlerObserver() {
						@Override
						public void newDOMVisited(Document document) {
							documents.add(document);
							//newDOMFound();
						}
						@Override
						public void finishedCrawling(CrawlSession session) {
							DuplicationRefactoringView.this.session = session;
						}
					});
					crawler.start();
					showDependenciesAction.setEnabled(documents.size() > 0);
					monitor.done();
				}
			});
			job.setUser(false);
			job.schedule();
		} else {
			showAnalysisWizard();
		}
	}
	
	private void showDependencies() {
		if (selectedFile != null && documents.size() > 0) {
			IViewPart overridingDependenciesView = ViewsUtil.openView(DependenciesVisualizationView.ID);
			if (overridingDependenciesView != null) {
				IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
				try {
					progressService.busyCursorWhile(iProgressMonitor -> {
						SubMonitor subMonitor = SubMonitor.convert(iProgressMonitor, 100);
						//iProgressMonitor.beginTask(LocalizedStrings.get(Keys.GETTING_DEPENDENCIES), 100);
						//iProgressMonitor.subTask(LocalizedStrings.get(Keys.PARSING_CSS_FILE));
						SubMonitor parsingMonior = subMonitor.split(10).setWorkRemaining(100);
						parsingMonior.setTaskName(LocalizedStrings.get(Keys.PARSING_CSS_FILE));
						StyleSheet selectedStyleSheet = getSelectedStyleSheet();
						if (selectedStyleSheet != null) {
							SubMonitor gettingDependenciesMonior = subMonitor.split(10).setWorkRemaining(100);
							gettingDependenciesMonior.setTaskName(LocalizedStrings.get(Keys.GETTING_DEPENDENCIES));
							CSSValueOverridingDependencyList dependencies = getOverridingDependencies();
							//gettingDependenciesMonior.worked(100);
							SubMonitor gettingDependenciesVisualizationMonitor = subMonitor.split(80).setWorkRemaining(100);
							gettingDependenciesVisualizationMonitor.setTaskName(LocalizedStrings.get(Keys.GENERATING_DEPENDENCY_VISUALIZATION));
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									//iProgressMonitor.subTask(LocalizedStrings.get(Keys.GENERATING_DEPENDENCY_VISUALIZATION));
									((DependenciesVisualizationView)overridingDependenciesView).showDependenciesGraph(dependencies, subMonitor);									
								}
							});
						}
					});
				} catch (InvocationTargetException | InterruptedException e) {
					ViewsUtil.showDetailedError(e);
				}
			}
		}
	}

	public CSSValueOverridingDependencyList getOverridingDependencies() {
		CSSValueOverridingDependencyList dependencies = new CSSValueOverridingDependencyList();
		StyleSheet styleSheet = getSelectedStyleSheet();
		if (styleSheet != null) {
			for (Document document : documents) {
				dependencies.addAll(styleSheet.getValueOverridingDependencies(document));
			}
			// Also add intra-selector dependencies
			dependencies.addAll(styleSheet.getValueOverridingDependencies());
		}
		return dependencies;
	}

	public List<Document> getDocumens() {
		return documents;
	}
	
	public CrawlSession getCrawlSession() {
		return session;
	}
}