package ca.concordia.cssdev.plugin.refactoring.mixins;

import org.eclipse.core.resources.IFile;

import ca.concordia.cssanalyser.analyser.duplication.items.ItemSet;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorMigrationOpportunitiesDetectorFactory;
import ca.concordia.cssanalyser.migration.topreprocessors.PreprocessorType;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssdev.plugin.utility.DuplicationInfo;

public class MixinDuplicationInfo extends DuplicationInfo {
	
	private final PreprocessorType preprocessorType;
	private MixinMigrationOpportunity<?> mixinMigrationOpportunity;
	private MixinMigrationOpportunity<?> originalMixinMigrationOpportunity;

	public MixinDuplicationInfo(ItemSet itemSet, IFile iFile, MixinMigrationOpportunity<?> mixinMigrationOpportunity, PreprocessorType preprocessorType) {
		super(itemSet, iFile);
		this.mixinMigrationOpportunity = mixinMigrationOpportunity;
		this.preprocessorType = preprocessorType;
	}
	
	public MixinDuplicationInfo(DuplicationInfo duplicationInfo, PreprocessorType preprocessorType) {
		super(duplicationInfo.getItemSet(), duplicationInfo.getSourceIFile());
		this.preprocessorType = preprocessorType;
		this.mixinMigrationOpportunity = getOriginalMixinMigrationOpportunity();
	}

	public MixinMigrationOpportunity<?> getMixinMigrationOpportunity() {
		return this.mixinMigrationOpportunity;
	}
	
	public void setMixinMigrationOpportunity(MixinMigrationOpportunity<?> mixinMigrationOpportunity) {
		this.mixinMigrationOpportunity = mixinMigrationOpportunity;
	}
	
	/**
	 * Because a {@link MixinMigrationOpportunity} object may have been changed,
	 * we can retrieve the original one created from the given ItemSet
	 * using this method.
	 * @return
	 */
	public MixinMigrationOpportunity<?> getOriginalMixinMigrationOpportunity() {
		if (originalMixinMigrationOpportunity == null) {
			StyleSheet styleSheet = getItemSet().getSupport().iterator().next().getParentStyleSheet();
			originalMixinMigrationOpportunity =
					PreprocessorMigrationOpportunitiesDetectorFactory.get(preprocessorType, styleSheet).getMixinOpportunityFromItemSet(getItemSet());
		}
		return originalMixinMigrationOpportunity; 
		
	}

}
