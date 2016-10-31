package ca.concordia.cssanalyser.plugin.views.dependenciesvisualization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.Cursors;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.crawljax.core.state.StateVertex;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.plugin.activator.Activator;
import ca.concordia.cssanalyser.plugin.annotations.CSSAnnotation;
import ca.concordia.cssanalyser.plugin.annotations.CSSAnnotationType;
import ca.concordia.cssanalyser.plugin.utility.AnnotationsUtil;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtil;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtil.ColorConstants;
import ca.concordia.cssanalyser.plugin.utility.ViewsUtil;
import ca.concordia.cssanalyser.plugin.views.duplicationrefactoring.DuplicationRefactoringView;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

public class AffectedDOMNodesView extends ViewPart {
	
	public static String ID = "ca.concordia.cssanalyser.plugin.views.dependenciesvisualization.AffectedDOMNodesView";
	
	private TreeViewer affectedDOMNodesTreeViewer;
	private Table propertiesTable; 
	private Label selectorLabel;

	private Map<Declaration, List<CSSValueOverridingDependency>> outgoingDependencies;
	private Map<Declaration, List<CSSValueOverridingDependency>> incomingDependencies;
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		
		Composite nameComposite = new Composite(parent, SWT.NONE);
		nameComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		nameComposite.setLayout(new GridLayout(2, false));
		
		Label titleLabel = new Label(nameComposite, SWT.NONE);
		titleLabel.setText(LocalizedStrings.get(Keys.AFFECTED_DOM_NODES));
		selectorLabel = new Label(nameComposite, SWT.NONE);
		selectorLabel.setFont(PreferencesUtil.getTextEditorFont());
		selectorLabel.setForeground(PreferencesUtil.getCSSColor(ColorConstants.SELECTOR));
		selectorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label selectorNameLabel = new Label(nameComposite, SWT.NONE);
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(titleLabel.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(titleLabel.getDisplay());
		selectorNameLabel.setFont(boldFont);
		
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				
		affectedDOMNodesTreeViewer = new TreeViewer(sashForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		affectedDOMNodesTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		affectedDOMNodesTreeViewer.setContentProvider(new AffectedDOMNodesTreeViewerContentProvider());
		affectedDOMNodesTreeViewer.setLabelProvider(new AffectedDOMNodesTreeViewerLabelProvider());
		affectedDOMNodesTreeViewer.addDoubleClickListener(new AffectedDOMNodesTreeViewerDoubleClickListener());
		affectedDOMNodesTreeViewer.setComparator(new AffectedDOMNodesTreeViewerComparator());
		affectedDOMNodesTreeViewer.setInput(getViewSite());	
		
		Composite propertiesTableParentComposite = new Composite(sashForm, SWT.BORDER);
		propertiesTableParentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout tableLayout = new TableColumnLayout();
		propertiesTableParentComposite.setLayout(tableLayout);
		propertiesTableParentComposite.setBackground(new Color(Display.getCurrent(), new RGB(0,0,0)));
		
		TableViewer propertiesTableViewer = new TableViewer(propertiesTableParentComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.CHECK);
		propertiesTable = propertiesTableViewer.getTable();
		propertiesTable.setHeaderVisible(true);
		TableColumn parameterNameColumn = new TableColumn(propertiesTable, SWT.NONE);
		parameterNameColumn.setText(LocalizedStrings.get(Keys.STYLED_PROPERTIES));
		tableLayout.setColumnData(parameterNameColumn, new ColumnWeightData(1, 100, true));
		
		final Menu menu = new Menu(propertiesTable);
		propertiesTable.setMenu(menu);
	    menu.addMenuListener(new MenuAdapter() {
	        public void menuShown(MenuEvent e) {
	            MenuItem[] items = menu.getItems();
	            for (int i = 0; i < items.length; i++) {
	                items[i].dispose();
	            }
	            if (propertiesTable.getSelection().length == 1) {
	            	Declaration declaration = (Declaration)propertiesTable.getSelection()[0].getData();
	            	MenuItem openDeclarationMenuItem = new MenuItem(menu, SWT.NONE);
	            	openDeclarationMenuItem.setText(LocalizedStrings.get(Keys.GOTO_DEFINITION));
	            	openDeclarationMenuItem.addSelectionListener(new SelectionAdapter() {
	            		@Override
	            		public void widgetSelected(SelectionEvent e) {
	            			openAndAnnotateDeclaration(declaration);
	            		}
					});
	            	if (outgoingDependencies.containsKey(declaration) && outgoingDependencies.get(declaration).size() > 0) {
	            		MenuItem gotoOverridingSelector = new MenuItem(menu, SWT.NONE);
	            		gotoOverridingSelector.setText(LocalizedStrings.get(Keys.GOTO_OVERRIDING_SELECTOR));
	            		gotoOverridingSelector.addSelectionListener(new SelectionAdapter() {
		            		@Override
		            		public void widgetSelected(SelectionEvent e) {
		            			DependenciesVisualizationView dependenciesVisualizationView = 
		            					(DependenciesVisualizationView)ViewsUtil.openView(DependenciesVisualizationView.ID);
		            			Set<Selector> toHighlight = new HashSet<>();
		            			for (CSSValueOverridingDependency dependency : outgoingDependencies.get(declaration)) {		            			
		            				toHighlight.add(dependency.getRealSelector2());
		            			}
		            			dependenciesVisualizationView.highlightSelectors(toHighlight);
		            		}
						});
	            	}
	            	if (incomingDependencies.containsKey(declaration) && incomingDependencies.get(declaration).size() > 0) {
	            		MenuItem gotoOverriddenSelector = new MenuItem(menu, SWT.NONE);
	            		gotoOverriddenSelector.setText(LocalizedStrings.get(Keys.GOTO_OVERRIDDEN_SELECTOR));
	            		gotoOverriddenSelector.addSelectionListener(new SelectionAdapter() {
		            		@Override
		            		public void widgetSelected(SelectionEvent e) {
		            			DependenciesVisualizationView dependenciesVisualizationView = 
		            					(DependenciesVisualizationView)ViewsUtil.openView(DependenciesVisualizationView.ID);
		            			Set<Selector> toHighlight = new HashSet<>();
		            			for (CSSValueOverridingDependency dependency : incomingDependencies.get(declaration)) {		            			
		            				toHighlight.add(dependency.getRealSelector1());
		            			}
		            			dependenciesVisualizationView.highlightSelectors(toHighlight);
		            		}
						});
	            	}
	            }
	        }
	    });

		
		sashForm.setWeights(new int[] { 300, 100 });
		
	}
	
	public void populatePropertiesTable(Selector selector) {
		DependenciesVisualizationView duplicationRefactoringView = (DependenciesVisualizationView)ViewsUtil.getView(DependenciesVisualizationView.ID);
		CSSValueOverridingDependencyList dependencies = duplicationRefactoringView.getOverridingDependencies();
		outgoingDependencies = new HashMap<>();
		incomingDependencies = new HashMap<>();
		propertiesTable.removeAll();
		for (Declaration declaration : selector.getFinalStylingIndividualDeclarations()) {
			outgoingDependencies.put(declaration, dependencies.getDependenciesStartingFrom(declaration));
			incomingDependencies.put(declaration, dependencies.getDependenciesEndingTo(declaration));
			TableItem item = new TableItem(propertiesTable, SWT.NONE);
			item.setData(declaration);
			item.setText(0, declaration.toString());
			item.setChecked(true);
			item.setImage(0, getDeclarationImage(declaration));
			item.addListener(SWT.MouseDoubleClick, new Listener() {
				@Override
				public void handleEvent(Event arg0) {
					openAndAnnotateDeclaration(declaration);
				}
			});
		}
		propertiesTable.pack();
		propertiesTable.getParent().layout();
	}
	
	private void openAndAnnotateDeclaration(Declaration declaration) {
		IEditorPart editorPart = ViewsUtil.openEditor(declaration.getSelector().getParentStyleSheet().getFilePath());
		Position position = new Position(declaration.getLocationInfo().getOffset(), declaration.getLocationInfo().getLength());
		CSSAnnotation annotation = 
				new CSSAnnotation(CSSAnnotationType.DECLARATION, LocalizedStrings.get(Keys.DECLARATION), position);
		List<CSSAnnotation> annotations = new ArrayList<>();
		annotations.add(annotation);
		AnnotationsUtil.setAnnotations(annotations , (StructuredTextEditor) editorPart);
	}

	@Override
	public void setFocus() {
		if (affectedDOMNodesTreeViewer != null) {
			affectedDOMNodesTreeViewer.getControl().setFocus();
		}
	}

	public void showAffectedDOMNodesFor(Selector selector) {
		DuplicationRefactoringView duplicationRefactoringView = (DuplicationRefactoringView)ViewsUtil.getView(DuplicationRefactoringView.ID);
		Set<StateVertex> stateVertices = duplicationRefactoringView.getCrawlSession().getStateFlowGraph().getAllStates();
		affectedDOMNodesTreeViewer.setContentProvider(new AffectedDOMNodesTreeViewerContentProvider(selector, stateVertices));
		selectorLabel.setText(selector.toString());
		selectorLabel.setCursor(Cursors.HAND);
		selectorLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				IEditorPart editorPart = ViewsUtil.openEditor(selector.getParentStyleSheet().getFilePath());
				Position position = new Position(selector.getLocationInfo().getOffset(), selector.getLocationInfo().getLength());
				CSSAnnotation annotation = 
						new CSSAnnotation(CSSAnnotationType.STYLE_RULE, LocalizedStrings.get(Keys.SELECTOR), position);
				List<CSSAnnotation> annotations = new ArrayList<>();
				annotations.add(annotation);
				AnnotationsUtil.setAnnotations(annotations , (StructuredTextEditor) editorPart);
			}
		});
		populatePropertiesTable(selector);
	}
	
	private Image getDeclarationImage(Declaration declaration) {
		ImageDescriptor descriptor = Activator.getImageDescriptor(VisualizationConstants.CSS_PROPERTY_ICON);
		if (outgoingDependencies.containsKey(declaration) && outgoingDependencies.get(declaration).size() > 0) {
			ImageDescriptor overlay = Activator.getImageDescriptor(VisualizationConstants.OVERRIDDEN_OVERLAY);
			Point overlayPosition = new Point(descriptor.getImageData().width - overlay.getImageData().width, 0);
			descriptor = Activator.getOverlayedImagesDescriptor(descriptor, overlay, overlayPosition);
		}
		if (incomingDependencies.containsKey(declaration) && incomingDependencies.get(declaration).size() > 0) {
			ImageDescriptor overlay = Activator.getImageDescriptor(VisualizationConstants.OVERRIDING_OVERLAY);
			Point overlayPosition = new Point(descriptor.getImageData().width - overlay.getImageData().width, 
					descriptor.getImageData().height - overlay.getImageData().height);
			descriptor = Activator.getOverlayedImagesDescriptor(descriptor, overlay, overlayPosition);
		}
		return descriptor.createImage();
	}
	
}
