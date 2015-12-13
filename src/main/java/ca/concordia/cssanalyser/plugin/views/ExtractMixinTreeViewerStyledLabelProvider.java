package ca.concordia.cssanalyser.plugin.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.PropertyAndLayer;
import ca.concordia.cssanalyser.cssmodel.declaration.value.DeclarationValue;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinDeclaration;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinLiteral;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinValue;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtility;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtility.ColorConstants;

public class ExtractMixinTreeViewerStyledLabelProvider implements IStyledLabelProvider, IColorProvider {
	
	private static final Color MIXIN_DECLARATION_BACKGROUND_COLOR = new Color(null, 235, 235, 235);
	private static final Color PROPERTY_FOREGROUND_COLOR = PreferencesUtility.getCSSColor(ColorConstants.PROPERY);
	private static final Color LITERAL_COLOR = PreferencesUtility.getCSSColor(ColorConstants.LITERAL);

	private static final Styler BOLD_STYLER = new Styler() {
	    @Override
	    public void applyStyles(final TextStyle textStyle)
	    {
	        Font textEditorFont = PreferencesUtility.getTextEditorFont();
			FontDescriptor boldDescriptor = FontDescriptor.createFrom(textEditorFont).setStyle(SWT.BOLD);
	        Font boldFont = boldDescriptor.createFont(Display.getCurrent());
	        textStyle.font = boldFont;
	    }
	};

	private static Map<MixinDeclaration, List<PropertyAndLayer>> propertiesAndLayers;
	
	private final int columnIndex;
	private final MixinMigrationOpportunity<?> mixinMigrationOpportunity;

	public ExtractMixinTreeViewerStyledLabelProvider(MixinMigrationOpportunity<?> mixinMigrationOpportunity, int columnIndex) {
		this.mixinMigrationOpportunity = mixinMigrationOpportunity;
		populateMaps(mixinMigrationOpportunity);
		this.columnIndex = columnIndex; 
	}
	
	private static void populateMaps(MixinMigrationOpportunity<?> mixinMigrationOpportunity) {
		if (propertiesAndLayers == null) {
			propertiesAndLayers = new HashMap<>();
			Iterable<MixinDeclaration> allMixinDeclarations = mixinMigrationOpportunity.getAllMixinDeclarations();
			for (MixinDeclaration mixinDeclaration : allMixinDeclarations) {
				List<PropertyAndLayer> propertiesAndLayersForThisMixinDeclaration = new ArrayList<>();
				for (MixinValue mixinValue : mixinDeclaration.getMixinValues()) {
					if (mixinValue.getAssignedTo() != null) {
						propertiesAndLayersForThisMixinDeclaration.add(mixinValue.getAssignedTo());
					}
				}
				propertiesAndLayers.put(mixinDeclaration, propertiesAndLayersForThisMixinDeclaration);
			}
		}
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public StyledString getStyledText(Object element) {
		
		String text = "";
		Styler styler = null;
		
		if (element instanceof Declaration) {
			Declaration declaration = (Declaration) element;
			MixinDeclaration parentMixinDeclaration = mixinMigrationOpportunity.getMixinDeclarationForDeclaration(declaration);
			switch (columnIndex) {
			case 0:
				text = declaration.getProperty();
				break;
			default:
				List<PropertyAndLayer> propertyAndLayersList = propertiesAndLayers.get(parentMixinDeclaration);
				if (propertyAndLayersList != null && columnIndex - 1 < propertyAndLayersList.size()) {
					PropertyAndLayer propertyAndLayer = propertyAndLayersList.get(columnIndex - 1);
					Collection<DeclarationValue> declarationValuesForStyleProperty = 
							declaration.getDeclarationValuesForStyleProperty(propertyAndLayer);
					for (Iterator<DeclarationValue> iterator = declarationValuesForStyleProperty.iterator(); iterator.hasNext();) {
						DeclarationValue declarationValue = iterator.next();
						text += declarationValue;
						if (iterator.hasNext()) {
							text += " ";
						}
					}
				}
				break;
			}
		} else if (element instanceof MixinDeclaration) {
			MixinDeclaration mixinDeclaration = (MixinDeclaration) element;
			styler = BOLD_STYLER;
			switch (columnIndex) {
			case 0:
				text = mixinDeclaration.getPropertyName();
				break;
			default:
				List<PropertyAndLayer> propertyAndLayersList = propertiesAndLayers.get(mixinDeclaration);
				if (propertyAndLayersList != null && columnIndex - 1 < propertyAndLayersList.size()) {
					PropertyAndLayer propertyAndLayer = propertyAndLayersList.get(columnIndex - 1);
					MixinValue mixinValueForPropertyandLayer = mixinDeclaration.getMixinValueForPropertyandLayer(propertyAndLayer);
					text = mixinValueForPropertyandLayer.toString();
				}
				break;
			}
		}
		
		StyledString styledString = new StyledString(text, styler);
		
		return styledString;
					
	}

	@Override
	public void addListener(ILabelProviderListener listener) {}

	@Override
	public void dispose() {}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {}

	@Override
	public Color getForeground(Object element) {
		if (columnIndex == 0) {
			return PROPERTY_FOREGROUND_COLOR;
		} else {
			if (element instanceof Declaration) {
				return LITERAL_COLOR;
			} else if (element instanceof MixinDeclaration) {
				MixinDeclaration mixinDeclaration = (MixinDeclaration) element;
				List<PropertyAndLayer> propertyAndLayersList = propertiesAndLayers.get(mixinDeclaration);
				if (propertyAndLayersList != null && columnIndex - 1 < propertyAndLayersList.size()) {
					PropertyAndLayer propertyAndLayer = propertyAndLayersList.get(columnIndex - 1);
					MixinValue mixinValue = mixinDeclaration.getMixinValueForPropertyandLayer(propertyAndLayer);
					if (mixinValue instanceof MixinLiteral) {
						return LITERAL_COLOR;
					}
				}
			}
		}
		return null;
	}

	@Override
	public Color getBackground(Object element) {
		if (element instanceof MixinDeclaration) {
			return MIXIN_DECLARATION_BACKGROUND_COLOR;
		}
		return null;
	}

}
