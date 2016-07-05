package ca.concordia.cssanalyser.plugin.wizards.declarationsdiffview;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinDeclaration;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinParameter;
import ca.concordia.cssanalyser.plugin.refactoring.MixinDuplicationInfo;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtil;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;
import ca.concordia.cssanalyser.plugin.wizards.declarationsdiffview.ExtendedCheckBox.ExtendedCheckboxSelectionListener;
import ca.concordia.cssanalyser.plugin.wizards.declarationsdiffview.MixinDeclarationDiffView.MixinDeclarationSelectionListener;

public class MixinDiffWizardPage extends UserInputWizardPage {

	private final Set<String> checkedProperties;
	private final Set<Selector> checkedSelectors;
	private final List<MixinDeclarationDiffView> mixinDeclarationsViews;
	private final MixinDuplicationInfo duplicationInfo;
	
	private Text mixinNameText;
	private Table parametersTable;
	
	
	public MixinDiffWizardPage(MixinDuplicationInfo duplicationInfo) {
		super(LocalizedStrings.get(Keys.EXTRACT_MIXIN));
		this.duplicationInfo = duplicationInfo;
		this.checkedProperties = new HashSet<>();
		this.checkedSelectors = new HashSet<>();
		this.mixinDeclarationsViews = new ArrayList<>();
		MixinMigrationOpportunity<?> mixinMigrationOpportunity = duplicationInfo.getOriginalMixinMigrationOpportunity();
		for (MixinDeclaration mixinDeclaration : mixinMigrationOpportunity.getAllMixinDeclarations()) {
			this.checkedProperties.add(mixinDeclaration.getPropertyName());
		}
		for (Selector selector : mixinMigrationOpportunity.getInvolvedSelectors()) {
			this.checkedSelectors.add(selector);	
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite pageContents = new Composite(parent, SWT.NONE);
		setControl(pageContents);
		
		GridLayout gridLayout = new GridLayout(1, false);
		pageContents.setLayout(gridLayout);
		
		createMixinNameArea(pageContents);
		if (duplicationInfo.getOriginalMixinMigrationOpportunity().getNumberOfParameters() > 0) {
			createParametersTable(pageContents);
		}
		SashForm sashForm = new SashForm(pageContents, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		createSelectorsArea(sashForm);
		createDiffArea(sashForm);
	}

	private void createMixinNameArea(Composite pageContents) {
		Composite mixinNameBar = new Composite(pageContents, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		mixinNameBar.setLayout(layout);
		mixinNameBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		new Label(mixinNameBar, SWT.NONE).setText(LocalizedStrings.get(Keys.MIXIN_NAME) + ":");
		
		mixinNameText = new Text(mixinNameBar, SWT.BORDER);
		mixinNameText.setText(duplicationInfo.getOriginalMixinMigrationOpportunity().getMixinName());
		mixinNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		mixinNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
		});
	}

	private void createParametersTable(Composite pageContents) {		
		Group tableParentGroup = new Group(pageContents, SWT.NONE);
		tableParentGroup.setText(LocalizedStrings.get(Keys.MIXIN_PARAMETERS));
		tableParentGroup.setLayout(new GridLayout());
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.widthHint = 200;
		tableParentGroup.setLayoutData(layoutData);
		
		Composite tableParentComposite = new Composite(tableParentGroup, SWT.NONE);
		tableParentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout tableLayout = new TableColumnLayout();
		tableParentComposite.setLayout(tableLayout);

		TableViewer viewer = new TableViewer(tableParentComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		parametersTable = viewer.getTable();
		parametersTable.setLinesVisible(true);
		//parametersTable.setHeaderVisible(true);
		
		TableColumn parameterNameColumn = new TableColumn(parametersTable, SWT.NONE);
		parameterNameColumn.setText(LocalizedStrings.get(Keys.MIXIN_PARAMETER));
		populateParametersTable();
		tableLayout.setColumnData(parameterNameColumn, new ColumnWeightData(1, 100, true));
		
		viewer.setColumnProperties(new String[]{ LocalizedStrings.get(Keys.MIXIN_PARAMETER) });
		viewer.setCellEditors(new CellEditor[] { new TextCellEditor(parametersTable) });
		viewer.setCellModifier(new ICellModifier() {
			@Override
			public void modify(Object element, String property, Object value) {
				TableItem tableItem = (TableItem)element;
				tableItem.setText(0, value.toString());
				setPageComplete(validatePage());
			}
			@Override
			public Object getValue(Object element, String property) {
				return ((MixinParameter)element).getName();
			}
			
			@Override
			public boolean canModify(Object element, String property) {
				return true;
			}
		});
	}

	private void populateParametersTable() {
		parametersTable.removeAll();
		for (MixinParameter mixinParameter : duplicationInfo.getMixinMigrationOpportunity().getParameters()) {
			TableItem item = new TableItem(parametersTable, SWT.NONE);
			item.setData(mixinParameter);
			item.setText(0, mixinParameter.getName());
		}
		parametersTable.pack();
		parametersTable.getParent().layout();
	}

	private void createSelectorsArea(Composite parent) {
		Group selectorsGroup = new Group(parent, SWT.NONE);
		selectorsGroup.setText(LocalizedStrings.get(Keys.SELECTORS));
		selectorsGroup.setLayout(new GridLayout());
		selectorsGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(selectorsGroup, SWT.H_SCROLL | SWT.V_SCROLL );
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
				
		Composite selectorsComposite = new Composite(scrolledComposite, SWT.NONE);
		selectorsComposite.setLayout(new GridLayout());
		selectorsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		selectorsComposite.setBackground(PreferencesUtil.getTextEditorBackgroundColor());
		
		scrolledComposite.setContent(selectorsComposite);
		
		for (Selector selector : checkedSelectors) {
			SelectorCheckBox selectorCheckBox = new SelectorCheckBox(selectorsComposite, selector.toString(), true);
			selectorCheckBox.setSelection(true);
			selectorCheckBox.setBackground(PreferencesUtil.getTextEditorBackgroundColor());
			selectorCheckBox.addCheckBoxSelectionListener(new ExtendedCheckboxSelectionListener() {
				@Override
				public void selectionChanged(Object source, boolean selected) {
					if (selected) {
						checkedSelectors.add(selector);
					} else {
						checkedSelectors.remove(selector);
					}
					for (MixinDeclarationDiffView mixinDeclarationDiffView : mixinDeclarationsViews) {
						if (checkedProperties.contains(mixinDeclarationDiffView.getProperty())) {
							mixinDeclarationDiffView.setDeclarationSelected(selector, selected);
						}
					}
					setPageComplete(validatePage());
				}
			});
		}
		scrolledComposite.setMinSize(selectorsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	private void createDiffArea(Composite parent) {	
		Group declarationsGroup = new Group(parent, SWT.NONE);
		declarationsGroup.setText(LocalizedStrings.get(Keys.DECLARATIONS));
		declarationsGroup.setLayout(new GridLayout());
		declarationsGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(declarationsGroup, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		Composite declarationsContainer = new Composite(scrolledComposite, SWT.NONE);
		declarationsContainer.setLayout(new GridLayout());
		scrolledComposite.setContent(declarationsContainer);
		for (MixinDeclaration mixinDeclaration : duplicationInfo.getOriginalMixinMigrationOpportunity().getAllMixinDeclarations()) {
			MixinDeclarationDiffView mixinDeclarationDiffView = new MixinDeclarationDiffView(declarationsContainer, mixinDeclaration);
			mixinDeclarationDiffView.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
			mixinDeclarationDiffView.addMixinDeclarationSelectionListener(new MixinDeclarationSelectionListener() {
				@Override
				public void mixinDeclarationSelectionChanged(MixinDeclaration mixinDeclaration, boolean selected) {
					MixinMigrationOpportunity<?> originalMixinMigrationOpportunity = duplicationInfo.getOriginalMixinMigrationOpportunity();
					if (selected) {
						checkedProperties.add(mixinDeclaration.getPropertyName());
						for (Selector selector : originalMixinMigrationOpportunity.getInvolvedSelectors()) {
							mixinDeclarationDiffView.setDeclarationSelected(selector, checkedSelectors.contains(selector));
						}
					} else {
						checkedProperties.remove(mixinDeclaration.getPropertyName());
					}
					duplicationInfo.setMixinMigrationOpportunity(originalMixinMigrationOpportunity.getSubOpportunity(getSelectedProperties(), getSelectedSelectors()));
					populateParametersTable();
					setPageComplete(validatePage());
				}
			});
			mixinDeclarationsViews.add(mixinDeclarationDiffView);
		}
		scrolledComposite.setMinSize(declarationsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private boolean validatePage() {
		
		String newMixinName = mixinNameText.getText();
		try {
			duplicationInfo.getMixinMigrationOpportunity().setMixinName(newMixinName);
		} catch (IllegalArgumentException nameException) {
			setErrorMessage(String.format(LocalizedStrings.get(Keys.INVALID_MIXIN_NAME), newMixinName));
			mixinNameText.forceFocus();
			return false;
		}
		
		if (parametersTable != null) {
			List<MixinParameter> parameters = (List<MixinParameter>)duplicationInfo.getMixinMigrationOpportunity().getParameters();
			TableItem[] tableItems = parametersTable.getItems();
			boolean shouldUpdateTheView = false;
			for (int i = 0; i < tableItems.length; i++) {
				TableItem tableItem = tableItems[i];
				String newValue = tableItem.getText(0).trim();
				MixinParameter parameter = parameters.get(i); 
				Assert.isTrue((MixinParameter)tableItem.getData() == parameter);
				for (int j = i + 1; j < tableItems.length; j++) {
					if (tableItems[j].getText(0).equals(newValue)) {
						setErrorMessage(String.format(LocalizedStrings.get(Keys.DUPLICATE_PARAMETER_NAME), newValue));
						return false;
					}
				}
				if (!parameter.getName().equals(newValue)) { 					
					try {
						parameter.setName(newValue);
						shouldUpdateTheView = true;
					} catch (IllegalArgumentException nameException) {
						setErrorMessage(String.format(LocalizedStrings.get(Keys.INVALID_MIXIN_PARAMETER_NAME), newValue));
						return false;
					}
				}
			}
			if (shouldUpdateTheView) {
				for (MixinDeclarationDiffView mixinDeclarationDiffView : mixinDeclarationsViews) {
					mixinDeclarationDiffView.updateParameterNames();
				}
			}
		}
		
		if (checkedSelectors.isEmpty()) {
			setErrorMessage(LocalizedStrings.get(Keys.EMPTY_SELECTORS));
			return false;
		}
		
		if (checkedProperties.isEmpty()) {
			setErrorMessage(LocalizedStrings.get(Keys.EMPTY_PROPERTIES));
			return false;
		}
			
		
		setErrorMessage(null);
		return true;
	}

	public Set<String> getSelectedProperties() {
		return checkedProperties;
	}
	
	public Set<Selector> getSelectedSelectors() {
		return checkedSelectors;
	}
}
