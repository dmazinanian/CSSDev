package ca.concordia.cssdev.plugin.wizards.duplication.declarationsdiffview;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.media.MediaQueryList;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinDeclaration;
import ca.concordia.cssdev.plugin.utility.LocalizedStrings;
import ca.concordia.cssdev.plugin.utility.LocalizedStrings.Keys;

public class ExtractMixinMixinPropertyToolTip extends ExtractMixinAbstractTooltip {

	private final MixinDeclaration mixinDeclaration;

	protected ExtractMixinMixinPropertyToolTip(Control parent, MixinDeclaration mixinDeclaration) {
		super(parent);
		this.mixinDeclaration = mixinDeclaration;
	}
		
	@Override
	protected void createTooltipArea(Composite tooltipArea) {
		
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.DECLARATION));
		createCodeLabel(tooltipArea, mixinDeclaration.getMixinDeclarationString());
		
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.STYLED_PROPERTIES));
		createNormalLabel(tooltipArea, getPropertyAndLayersString(mixinDeclaration.getAllSetPropertyAndLayers()));
	
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.DECLARATIONS));
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
}
