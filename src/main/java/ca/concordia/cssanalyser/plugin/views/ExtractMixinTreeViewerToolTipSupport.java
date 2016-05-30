package ca.concordia.cssanalyser.plugin.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.FontDescriptor;
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.MultiValuedDeclaration;
import ca.concordia.cssanalyser.cssmodel.declaration.PropertyAndLayer;
import ca.concordia.cssanalyser.cssmodel.declaration.ShorthandDeclaration;
import ca.concordia.cssanalyser.cssmodel.declaration.SingleValuedDeclaration;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.cssmodel.media.MediaQueryList;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinDeclaration;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinLiteral;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinParameter;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinValue;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtility;

public class ExtractMixinTreeViewerToolTipSupport extends ColumnViewerToolTipSupport {

	private final Map<MixinDeclaration, List<PropertyAndLayer>> propertiesAndLayersToDisplay;
	private final MixinMigrationOpportunity<?> mixinMigrationOpportunity;

	protected ExtractMixinTreeViewerToolTipSupport(ColumnViewer viewer, ExtractMixinTreeViewerContentProvider contentProvider) {
		super(viewer, ToolTip.NO_RECREATE, false);
		propertiesAndLayersToDisplay = contentProvider.getPropertiesAndLayersToDisplay();
		mixinMigrationOpportunity = contentProvider.getMixinMigrationOpportunity();
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
	
	@Override
	public boolean isHideOnMouseDown() {
		return false;
	}
		
	private Composite getTooltipContentArea(Composite parent) {
		Composite tooltipArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 10;
		tooltipArea.setLayout(layout);
		return tooltipArea;
	}
	
	private void createMixinDeclarationPropertyArea(Composite tooltipArea, MixinDeclaration mixinDeclaration) {
		
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
	
	private void createMixinDeclarationValueArea(Composite tooltipArea, MixinDeclaration mixinDeclaration, int columnIndex) {
		MixinValue mixinValue = getMixinValueForMixinDeclaration(mixinDeclaration, columnIndex);
		
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.MIXIN_VALUE_NAME));
		createCodeLabel(tooltipArea, mixinValue.toString());
		
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.MIXIN_DECLARATION));
		createCodeLabel(tooltipArea, mixinDeclaration.getMixinDeclarationString());
		
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.MIXIN_VALUE_TYPE));
		createNormalLabel(tooltipArea, getMixinValueType(mixinValue));
		
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.STYLED_PROPERTIES));
		createNormalLabel(tooltipArea, getPropertyAndLayersString(mixinDeclaration.getPropertyAndLayerForMixinValue(mixinValue)));
	}

	private MixinValue getMixinValueForMixinDeclaration(MixinDeclaration mixinDeclaration, int columnIndex) {
		PropertyAndLayer propertyAndLayer = propertiesAndLayersToDisplay.get(mixinDeclaration).get(columnIndex - 1);
		return mixinDeclaration.getMixinValueForPropertyandLayer(propertyAndLayer);
	}

	private String getMixinValueType(MixinValue mixinValue) {
		Keys stringKey = null;
		if (mixinValue instanceof MixinLiteral) {
			stringKey = Keys.MIXIN_LITERAL;
		} else if (mixinValue instanceof MixinParameter) {
			stringKey = Keys.MIXIN_PARAMETER;
		}
		return LocalizedStrings.get(stringKey);
	}

	private void createDeclarationPropertyArea(Composite tooltipArea, Declaration declaration) {
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.SELECTOR));
		createCodeLabel(tooltipArea, declaration.getSelector().toString());
		
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.DECLARATION));
		createCodeLabel(tooltipArea, declaration.toString());
		
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.STYLED_PROPERTIES));
		Set<PropertyAndLayer> allSetPropertyAndLayers = declaration.getAllSetPropertyAndLayers(); 
		String allSetPropertyAndLayersString = getPropertyAndLayersString(allSetPropertyAndLayers);
		createNormalLabel(tooltipArea, allSetPropertyAndLayersString);
	
		createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.DECLARATION_TYPE));
		createNormalLabel(tooltipArea, getDeclarationTypeString(declaration));
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
		Collection<DeclarationValue> values = getDeclarationValue(declaration, columnIndex);
		if (values == null) {
			tooltipArea.dispose();
		} else {
			createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.DECLARATION_VALUE));
			createCodeLabel(tooltipArea, values.toString());

			createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.DECLARATION));
			createCodeLabel(tooltipArea, declaration.toString());

			createDescriptionLabel(tooltipArea, LocalizedStrings.get(Keys.STYLED_PROPERTIES));
			DeclarationValue firstValue = values.iterator().next();
			createNormalLabel(tooltipArea, getPropertyAndLayersString(firstValue.getCorrespondingStylePropertyAndLayer()));
		}
	}
	
	private String getPropertyAndLayersString(PropertyAndLayer correspondingStylePropertyAndLayer) {
		List<PropertyAndLayer> dummyList = new ArrayList<>();
		dummyList.add(correspondingStylePropertyAndLayer);
		return getPropertyAndLayersString(dummyList);
	}

	private String getPropertyAndLayersString(Collection<PropertyAndLayer> allSetPropertyAndLayers) {
		Map<Integer, List<PropertyAndLayer>> propertiesAndLayersMap = new HashMap<>();
		for (PropertyAndLayer propertyAndLayer : allSetPropertyAndLayers) {
			int layer = propertyAndLayer.getPropertyLayer();
			List<PropertyAndLayer> propertiesAndLayersListForThisLayer = propertiesAndLayersMap.get(layer);
			if (propertiesAndLayersListForThisLayer == null) {
				propertiesAndLayersListForThisLayer = new ArrayList<>();
				propertiesAndLayersMap.put(layer, propertiesAndLayersListForThisLayer);
			}
			propertiesAndLayersListForThisLayer.add(propertyAndLayer);
		}
		
		StringBuilder toReturn = new StringBuilder();
		if (propertiesAndLayersMap.keySet().size() == 1) {
			List<PropertyAndLayer> list = propertiesAndLayersMap.get(1);
			getPropertiesForLayerString(toReturn, list);
		} else if (propertiesAndLayersMap.keySet().size() > 1) {
			for (Iterator<Integer> iterator = propertiesAndLayersMap.keySet().iterator(); iterator.hasNext();) {
				int layer = iterator.next();
				toReturn.append(LocalizedStrings.get(Keys.PROPERTY_LAYER) + " " + layer + ":\n");
				List<PropertyAndLayer> list = propertiesAndLayersMap.get(layer);
				getPropertiesForLayerString(toReturn, list);
				if (iterator.hasNext())
					toReturn.append("\n\n");
			}
		}
		
		return toReturn.toString();
	}

	private void getPropertiesForLayerString(StringBuilder builder, List<PropertyAndLayer> list) {
		for (Iterator<PropertyAndLayer> iterator = list.iterator(); iterator.hasNext();) {
			PropertyAndLayer propertyAndLayer = iterator.next();
			builder.append(propertyAndLayer.getPropertyName());
			if (iterator.hasNext())
				builder.append(", ");
		}
	}

	private Collection<DeclarationValue> getDeclarationValue(Declaration declaration, int columnIndex) {
		MixinDeclaration mixinDeclaration = this.mixinMigrationOpportunity.getMixinDeclarationForDeclaration(declaration);
		List<PropertyAndLayer> propertyAndLayers = propertiesAndLayersToDisplay.get(mixinDeclaration);
		if (propertyAndLayers != null && columnIndex - 1 < propertyAndLayers.size()) {
			PropertyAndLayer propertyAndLayer = propertyAndLayers.get(columnIndex - 1);
			return declaration.getDeclarationValuesForStyleProperty(propertyAndLayer);
		}
		return null;
	}

	private Label createCodeLabel(Composite tooltipArea, String labelText) {
		Label label = new Label(tooltipArea, SWT.BORDER);
		label.setText(labelText);
		label.setFont(PreferencesUtility.getTextEditorFont());
		label.setBackground(PreferencesUtility.getTextEditorBackgroundColor());
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		return label;
	}
	
	private Label createNormalLabel(Composite tooltipArea, String labelText) {
		Label label = new Label(tooltipArea, SWT.NONE);
		label.setText(labelText);
		return label;
	}

	private Label createDescriptionLabel(Composite tooltipArea, String labelText) {
		Label label = createNormalLabel(tooltipArea, labelText + ":");
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(label.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(Display.getCurrent());		
		label.setFont(boldFont);
		return label;
	}
	
}
