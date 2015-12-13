package ca.concordia.cssanalyser.plugin.views;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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

import ca.concordia.cssanalyser.analyser.duplication.DuplicationDetector;
import ca.concordia.cssanalyser.analyser.duplication.items.Item;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.analyser.duplication.items.ItemSetList;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorType;
import ca.concordia.cssanalyser.parser.CSSParser;
import ca.concordia.cssanalyser.parser.CSSParserFactory;
import ca.concordia.cssanalyser.parser.CSSParserFactory.CSSParserType;
import ca.concordia.cssanalyser.parser.ParseException;
import ca.concordia.cssanalyser.plugin.activator.Activator;
import ca.concordia.cssanalyser.plugin.annotations.CSSAnnotation;
import ca.concordia.cssanalyser.plugin.annotations.CSSAnnotationType;
import ca.concordia.cssanalyser.plugin.refactoring.DuplicationRefactoring;
import ca.concordia.cssanalyser.plugin.refactoring.GroupingRefactoring;
import ca.concordia.cssanalyser.plugin.refactoring.MixinMigrationRefactoring;
import ca.concordia.cssanalyser.plugin.utility.ItemSetUtil;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;
import ca.concordia.cssanalyser.plugin.utility.ResultsStorage;

public class DuplicationRefactoringView extends ViewPart {

	public static final String ID = "ca.concordia.cssanalyser.plugin.activator.views.DuplicationRefactoringView";

	private TableViewer viewer;
	private IAction findDuplicatedDeclarationsAction;
	private IAction mixinMigrationAction;
	private IAction tableRowDoubleClickAction;
	private IAction groupingRefactoringOpportunitiesAction;
	private IAction clearAnnotationsAction;
	private IAction clearResultsAction;
	
	private IFile selectedFile;
	private boolean allowDifferencesInValues;
	private boolean liveDetection;
	private List<CSSAnnotation> currentAnnotations = new ArrayList<>();
	private Map<IFile, ResultsStorage> fileToResultsMap = new HashMap<>();
	
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
		private final List<ItemSet> allItemSets;
		public DuplicationViewContentProvider(List<ItemSet> allItemSets) {
			this.allItemSets = allItemSets;
		}
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {}
		
		public void dispose() {}
		
		public Object[] getElements(Object parent) {
			if (allItemSets == null)
				return new String[] {};
			return allItemSets.toArray();
		}
	}
	
	private static class DuplicationViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			ItemSet entry = (ItemSet)obj;
			switch(index){
			case 0:
				return ItemSetUtil.getDeclarationNames(entry);
			case 1:
				return ItemSetUtil.getSelectorNames(entry);
			default:
				return "";
			}
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
	}
	
	private static class DuplicationViewNameSorter extends ViewerSorter {
		@Override
		public int compare(Viewer viewer, Object obj1, Object obj2) {
			double value1 = ((ItemSet)obj1).getGroupingRefactoringImpact();
			double value2 = ((ItemSet)obj2).getGroupingRefactoringImpact();
			if(value1 > value2) {
				return -1;
			} else {
				return 1;
			}
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
		IEditorReference editorReference = getEditorReferenceForIFileIfOpened(selectedFile);
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
		viewer.setLabelProvider(new DuplicationViewLabelProvider());
		viewer.setSorter(new DuplicationViewNameSorter());
		viewer.setInput(getViewSite());
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(40, true));
		layout.addColumnData(new ColumnWeightData(60, true));
		viewer.getTable().setLayout(layout);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);

		TableColumn column1 = new TableColumn(viewer.getTable(), SWT.LEFT);
		column1.setText(LocalizedStrings.get(Keys.DECLARATIONS));
		column1.setResizable(true);
		column1.pack();
		
		TableColumn column2 = new TableColumn(viewer.getTable(), SWT.LEFT);
		column2.setText(LocalizedStrings.get(Keys.SELECTORS));
		column2.setResizable(true);
		column2.pack();
		
		viewer.setColumnProperties(new String[] {"declarations", "selectors"});
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				viewer.getTable().setMenu(null);
				ItemSet selectedItemSet = (ItemSet)((IStructuredSelection)viewer.getSelection()).getFirstElement();
				if (selectedItemSet != null) {
					getRightClickMenu(viewer, selectedItemSet);
				}
			}

			private void getRightClickMenu(TableViewer viewer, ItemSet selectedItemSet) {
				MenuManager menuMgr = new MenuManager("#PopupMenu");
				menuMgr.setRemoveAllWhenShown(true);
				menuMgr.addMenuListener(new IMenuListener() {
					public void menuAboutToShow(IMenuManager manager) {
						boolean haveDifferences = selectedItemSet.containsDifferencesInValues();
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
		GridLayout bottomBarLayout = new GridLayout();
		bottomBarLayout.numColumns = 6;
		bottomBarLayout.horizontalSpacing = 20;
		bottomBar.setLayout(bottomBarLayout);
		
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
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(findDuplicatedDeclarationsAction);
		manager.add(new Separator());
		manager.add(clearResultsAction);
		manager.add(clearAnnotationsAction);
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(clearAnnotationsAction);
		manager.add(clearResultsAction);
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
				IEditorReference editorReference = getEditorReferenceForIFileIfOpened(selectedFile);
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
				IEditorReference editorReference = getEditorReferenceForIFileIfOpened(selectedFile);
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
		
		tableRowDoubleClickAction = new Action() {
			public void run() {
				ItemSet selectedItemSet = getSelectedItemSet();
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					StructuredTextEditor ste = (StructuredTextEditor)IDE.openEditor(page, selectedFile);
					Set<Declaration> declarationsToAnnotate = new HashSet<>();
					clearAnnotations(ste);
					for(Item item : selectedItemSet) {
						for (Declaration declaration : item) {
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
					
					List<CSSAnnotation> annotations = new ArrayList<>();
					for (Declaration declaration : declarationsToAnnotate) {
						Position position = new Position(declaration.getLocationInfo().getOffset(), declaration.getLocationInfo().getLength());
						CSSAnnotation cssAnnotation = new CSSAnnotation(CSSAnnotationType.DUPLICATION, 
								"Duplicated " + declaration.getProperty(),
								position);
						annotations.add(cssAnnotation);
					}
					setAnnotations(annotations, ste);
				} catch (PartInitException e) {
					showDetailedError(e);
				}				
			}
		};

		mixinMigrationAction = new Action() {
			@Override
			public void run() {
				if (selectedFile != null) {
					if (!checkFileIsSaved()) {
						return;
					}
					ItemSet selectedItemSet = getSelectedItemSet();
					if (selectedItemSet != null) {
						try {
							MixinMigrationRefactoring refactoring = new MixinMigrationRefactoring(selectedItemSet, selectedFile, PreprocessorType.LESS);
							showRefactoringWizard(refactoring);
						} catch (ParseException parseException) {
							showDetailedError(parseException);
						}
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
					ItemSet selectedItemSet = getSelectedItemSet();
					if (selectedItemSet != null) {
						if (!selectedItemSet.containsDifferencesInValues()) {
							DuplicationRefactoring refactoring = new GroupingRefactoring(selectedItemSet, selectedFile);
							showRefactoringWizard(refactoring);
						}
					}
				}
			}
		};
		groupingRefactoringOpportunitiesAction.setText(LocalizedStrings.get(Keys.GROUP_SELECTORS));
		
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
	}
	
	private void fillTableViewer(List<ItemSet> itemSets) {
		viewer.setContentProvider(new DuplicationViewContentProvider(itemSets));
		if (itemSets != null && !itemSets.isEmpty()) {
			clearResultsAction.setEnabled(true);
		} else {
			clearResultsAction.setEnabled(false);
		}
	}
	
	private void clearAnnotations(StructuredTextEditor ste) {
		AnnotationModel annotationModel = (AnnotationModel)ste.getDocumentProvider().getAnnotationModel(ste.getEditorInput());
		for(Annotation annotation : currentAnnotations) {
			annotationModel.removeAnnotation(annotation);
		}
		currentAnnotations = new ArrayList<>();
		clearAnnotationsAction.setEnabled(false);
	}
	
	private void setAnnotations(List<CSSAnnotation> annotations, StructuredTextEditor ste) {
		if (ste != null) {
			AnnotationModel annotationModel = (AnnotationModel)ste.getDocumentProvider().getAnnotationModel(ste.getEditorInput());
			currentAnnotations = annotations;
			for (CSSAnnotation annotation : annotations) {
				annotationModel.addAnnotation(annotation, annotation.getPosition());
				clearAnnotationsAction.setEnabled(true);
			}
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
			IEditorReference editorReference = getEditorReferenceForIFileIfOpened(selectedFile);
			if (editorReference != null) {
				StructuredTextEditor ste = (StructuredTextEditor)editorReference.getEditor(false);
				List<ItemSet> itemSets = ((DuplicationViewContentProvider)viewer.getContentProvider()).allItemSets;
				fileToResultsMap.put(selectedFile, new ResultsStorage(selectedFile, itemSets, currentAnnotations));
				clearResults(ste);
			}
		}
		if (file != null) {
			if (fileToResultsMap.containsKey(file)) {
				ResultsStorage resultsStorage = fileToResultsMap.get(file);
				fillTableViewer(resultsStorage.getItemSets());
				IEditorReference editorReference = getEditorReferenceForIFileIfOpened(file);
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
					public void run(IProgressMonitor monitor)  {
						try {
							CSSParser parser = CSSParserFactory.getCSSParser(CSSParserType.LESS);
							StyleSheet styleSheet = parser.parseExternalCSS(selectedFile.getLocation().toOSString());
							styleSheet = styleSheet.getStyleSheetWithIntraSelectorDependenciesRemoved();
							final DuplicationDetector duplicationDetector = new DuplicationDetector(styleSheet);
							if (allowDifferencesInValues) {
								duplicationDetector.findPropertyDuplications();
							} else {
								duplicationDetector.findDuplications();
							}

							final List<ItemSetList> fpgrowthResults = duplicationDetector.fpGrowth(2, false);
							List<ItemSet> allItemSets = new ArrayList<>();

							for (ItemSetList isl : fpgrowthResults)
								for (ItemSet itemSet : isl)
									allItemSets.add(itemSet);
							
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									if (allItemSets.isEmpty()) {
										String title = LocalizedStrings.get(Keys.NO_DUPLICATIONS_FOUND_TITLE);
										String message = LocalizedStrings.get(Keys.NO_DUPLICATIONS_FOUND);
										if (!allowDifferencesInValues) {
											message += "\n" + LocalizedStrings.get(Keys.CONSIDER_HAVING_DIFFERENCES);
										}
										MessageDialog.openInformation(getSite().getShell(), title, message);
									}
									fillTableViewer(allItemSets);
								}
							});	

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
				showDetailedError(throwable);
			}
		}
	}

	private void showDetailedError(Throwable throwable) {
		MultiStatus info = getStatusFromThrowable(throwable);
		ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
				LocalizedStrings.get(Keys.PARSE_ERROR_IN_FILE_TITLE), 
				LocalizedStrings.get(Keys.PARSE_ERROR_IN_FILE_MESSAGE),
				info);
	}

	private MultiStatus getStatusFromThrowable(Throwable throwable) {
		StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		String message = sw.getBuffer().toString();
		if (message == null) {
			message = throwable.toString();
		}
		String pluginId = Activator.PLUGIN_ID;
		MultiStatus info = new MultiStatus(pluginId, 1, throwable.toString(), throwable);
		for (String s : message.split("\n")) {
			info.add(new Status(IStatus.ERROR, pluginId, 1, s.replace("\t", "    "), throwable));	
		}
		return info;
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
						showDetailedError(ex);
						return false;
					}
				} else {
					return false;
				}
			}
		} catch (PartInitException partInitException) {
			showDetailedError(partInitException);
			return false;
		}
		return true;
	}

	private ItemSet getSelectedItemSet() {
		ISelection selection = viewer.getSelection();
		if (!selection.isEmpty()) {
			ItemSet selectedItemSet = (ItemSet)((IStructuredSelection)selection).getFirstElement();
			return selectedItemSet;
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
			showDetailedError(e);
		}
	}

	private IEditorReference getEditorReferenceForIFileIfOpened(IFile file) {
		IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		for (IEditorReference editorReference : editorReferences) {
			try {
				IEditorInput editorInput = editorReference.getEditorInput();
				if (editorInput instanceof IFileEditorInput) {
					IFileEditorInput iFileEditorInput = (IFileEditorInput) editorInput;
					if (iFileEditorInput.getFile().equals(file)) {
						return editorReference;
					}
				}
			} catch (PartInitException e) {
				showDetailedError(e);
			}
		}
		return null;
	}	
	
	private boolean hasStyleSheetExtension(IFile file) {
		String fileExtension = file.getFileExtension().toLowerCase();
		return "css".equals(fileExtension) || "less".equals(fileExtension);
	}
}