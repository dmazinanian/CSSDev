package ca.concordia.cssanalyser.plugin.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.declaration.PropertyAndLayer;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinDeclaration;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinValue;

public class ExtractMixinTreeViewerContentProvider implements ITreeContentProvider {

	private final MixinMigrationOpportunity<?> mixinMigrationOpportunity;
	private Map<MixinDeclaration, List<PropertyAndLayer>> propertiesAndLayers;
		
	public ExtractMixinTreeViewerContentProvider(MixinMigrationOpportunity<?> mixinMigrationOpportunity) {
		this.mixinMigrationOpportunity = mixinMigrationOpportunity;
		populatePropertyAndLayersMap();
	}
	
	private void populatePropertyAndLayersMap() {
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
	
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof MixinDeclaration) {
			MixinDeclaration mixinDeclaration = (MixinDeclaration) parentElement;
			List<Declaration> children = new ArrayList<>();
			for (Declaration declaration : mixinDeclaration.getForDeclarations()) {
				children.add(declaration);
			}
			return children.toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof Declaration) {
			this.mixinMigrationOpportunity.getMixinDeclarationForDeclaration((Declaration) element);
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return element instanceof MixinDeclaration;
	}

	@Override
	public void dispose() {}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }

	@Override
	public Object[] getElements(Object inputElement) {
		List<MixinDeclaration> mixinTreeContents = new ArrayList<>();
		for (MixinDeclaration mixinDeclaration : mixinMigrationOpportunity.getAllMixinDeclarations()) {
			mixinTreeContents.add(mixinDeclaration);
		}
		return mixinTreeContents.toArray();
	}
	
	public Map<MixinDeclaration, List<PropertyAndLayer>> getPropertiesAndLayersToDisplay() {
		return this.propertiesAndLayers;
	}

	public MixinMigrationOpportunity<?> getMixinMigrationOpportunity() {
		return this.mixinMigrationOpportunity;
	}

}
