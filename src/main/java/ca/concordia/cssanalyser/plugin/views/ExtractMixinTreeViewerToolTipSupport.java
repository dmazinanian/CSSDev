package ca.concordia.cssanalyser.plugin.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.MultiValuedDeclaration;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.declaration.SingleValuedDeclaration;
import ca.concordia.cssanalyser.cssmodel.media.MediaQueryList;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinDeclaration;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtility;

public class ExtractMixinTreeViewerToolTipSupport extends ColumnViewerToolTipSupport {

	protected ExtractMixinTreeViewerToolTipSupport(ColumnViewer viewer) {
		super(viewer, ToolTip.NO_RECREATE, false);
	}
	
	@Override
	protected Composite createViewerToolTipContentArea(Event event, ViewerCell cell, Composite parent) {
		
		Composite tooltipContentArea = getTooltipContentArea(parent);
		
		Object element = cell.getElement();
		int columnIndex = cell.getColumnIndex();
		
		if (element != null) {
			if (element instanceof MixinDeclaration) {
				MixinDeclaration mixinDeclaration = (MixinDeclaration) element;
				if (columnIndex == 0) {
					createMixinDeclarationPropertyArea(tooltipContentArea, mixinDeclaration);
				} else {
					createMixinDeclarationValueArea(tooltipContentArea, mixinDeclaration, columnIndex);
				}
			} else if (element instanceof Declaration) {
				Declaration declaration = (Declaration) element;
				if (columnIndex == 0) {
					createDeclarationPropertyArea(tooltipContentArea, declaration);
				} else {
					createDeclarationValueArea(tooltipContentArea, declaration, columnIndex);
				}
			}
		}
		
		return tooltipContentArea;		
	}
	
	private Composite getTooltipContentArea(Composite parent) {
		Composite tooltipArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 5;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 5;
		tooltipArea.setLayout(layout);
		return tooltipArea;
	}
	
	private void createMixinDeclarationPropertyArea(Composite tooltipArea, MixinDeclaration mixinDeclaration) {
		
		new Label(tooltipArea, SWT.NONE).setText(LocalizedStrings.get(Keys.DECLARATION) + ":");
		Label mixinDeclarationLabel = new Label(tooltipArea, SWT.BORDER);
		mixinDeclarationLabel.setText(mixinDeclaration.getMixinDeclarationString());
		mixinDeclarationLabel.setFont(PreferencesUtility.getTextEditorFont());
		mixinDeclarationLabel.setBackground(PreferencesUtility.getTextEditorBackgroundColor());
		mixinDeclarationLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		
		new Label(tooltipArea, SWT.NONE).setText(LocalizedStrings.get(Keys.STYLED_PROPERTIES) + ":");
		new Label(tooltipArea, SWT.NONE).setText(mixinDeclaration.getAllSetPropertyAndLayers().toString());
	
		new Label(tooltipArea, SWT.NONE).setText(LocalizedStrings.get(Keys.DECLARATIONS) + ":");
		Composite tableViewerComposite = new Composite(tooltipArea, SWT.BORDER);
		tableViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		TableViewer declarationsTableViewer = new TableViewer(tableViewerComposite, SWT.MULTI | SWT.FULL_SELECTION);
		Table table = declarationsTableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableColumn column1 = new TableColumn(table, SWT.LEFT, 0);
	    column1.setText(LocalizedStrings.get(Keys.DECLARATION));
	    column1.pack();
	    
	    TableColumn column2 = new TableColumn(table, SWT.LEFT, 1);
	    column2.setText(LocalizedStrings.get(Keys.SELECTOR));
	    column2.pack();
	    
	    TableColumn column3 = new TableColumn(table, SWT.LEFT, 2);
	    column3.setText(LocalizedStrings.get(Keys.MEDIA_QUERY));
	    column3.pack();
	    
	    TableColumnLayout tableLayout = new TableColumnLayout();
	    tableLayout.setColumnData(column1, new ColumnWeightData(100, column1.getWidth()));
	    tableLayout.setColumnData(column2, new ColumnWeightData(0, column2.getWidth()));
	    tableLayout.setColumnData(column3, new ColumnWeightData(0, column3.getWidth()));
	    tableViewerComposite.setLayout(tableLayout);
	     
		declarationsTableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
			
			@Override
			public void dispose() {}
			
			@Override
			public Object[] getElements(Object inputElement) {
				List<Declaration> forDeclarations = new ArrayList<>();
				for (Declaration declaration : mixinDeclaration.getForDeclarations()) {
					forDeclarations.add(declaration);
				}
				return forDeclarations.toArray();
			}
		});
		
		declarationsTableViewer.setLabelProvider(new ITableLabelProvider() {
			
			@Override
			public void removeListener(ILabelProviderListener listener) {}
			
			@Override
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			
			@Override
			public void dispose() {}
			
			@Override
			public void addListener(ILabelProviderListener listener) {}
			
			@Override
			public String getColumnText(Object element, int columnIndex) {
				if (element instanceof Declaration) {
					Declaration declaration = (Declaration) element;
					switch (columnIndex) {
					case 0:
						return declaration.toString();
					case 1:
						return declaration.getSelector().toString();
					case 2:
						Set<MediaQueryList> mediaQueryLists = declaration.getSelector().getMediaQueryLists();
						if (mediaQueryLists != null)
							return mediaQueryLists.toString();
						return null;
					}
				}
				return null;
			}
			
			@Override
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
		});
		
		declarationsTableViewer.setInput("");
	}
	
	private void createMixinDeclarationValueArea(Composite tooltipArea, MixinDeclaration mixinDeclaration, int columnIndex) {
		// TODO Auto-generated method stub
	}

	private void createDeclarationPropertyArea(Composite tooltipArea, Declaration declaration) {
		new Label(tooltipArea, SWT.NONE).setText(LocalizedStrings.get(Keys.SELECTOR) + ":");
		Label mixinDeclarationLabel = new Label(tooltipArea, SWT.BORDER);
		mixinDeclarationLabel.setText(declaration.toString());
		mixinDeclarationLabel.setFont(PreferencesUtility.getTextEditorFont());
		mixinDeclarationLabel.setBackground(PreferencesUtility.getTextEditorBackgroundColor());
		mixinDeclarationLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		
		new Label(tooltipArea, SWT.NONE).setText(LocalizedStrings.get(Keys.STYLED_PROPERTIES) + ":");
		new Label(tooltipArea, SWT.WRAP).setText(declaration.getAllSetPropertyAndLayers().toString());
	
		new Label(tooltipArea, SWT.NONE).setText(LocalizedStrings.get(Keys.DECLARATION_TYPE) + ":");
		new Label(tooltipArea, SWT.NONE).setText(getDeclarationTypeString(declaration));
		
	}
	
	private String getDeclarationTypeString(Declaration declaration) {
		Keys stringKey = null;
		if (declaration.getClass() == SingleValuedDeclaration.class) {
			stringKey = Keys.SINGLE_VALUED_DECLARATION;
		} else if (declaration.getClass() == MultiValuedDeclaration.class) {
			stringKey = Keys.MULTI_VALUED_DECLARATION;
		} else if (declaration.getClass() == ShorthandDeclaration.class) {
			ShorthandDeclaration shorthnad = (ShorthandDeclaration)declaration;
			if (shorthnad.isVirtual()) {
				stringKey = Keys.VIRTUAL_SHORTHAND_DECLARATION;
			} else {
				stringKey = Keys.SHORTHAND_DECLARATION;
			}
		} else {
			return "";
		}
		return LocalizedStrings.get(stringKey);
	}

	private void createDeclarationValueArea(Composite tooltipArea, Declaration declaration, int columnIndex) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isHideOnMouseDown() {
		return false;
	}
	
}
