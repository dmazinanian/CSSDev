package ca.concordia.cssanalyser.plugin.views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;

import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinDeclaration;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinValue;
import ca.concordia.cssanalyser.plugin.refactoring.MixinMigrationRefactoring;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;

public class MixinDiffWizardPage extends UserInputWizardPage {

	private final MixinMigrationRefactoring refactoring;
	private final Set<String> checkedProperties;
	private TreeViewer diffTreeViewer;
	
	public MixinDiffWizardPage(MixinMigrationRefactoring refactoring) {
		super(refactoring.getName());
		this.refactoring = refactoring;
		this.checkedProperties = new HashSet<>();
		for (MixinDeclaration declaration : this.refactoring.getMixinMigrationOpportunity().getAllMixinDeclarations()) {
			this.checkedProperties.add(declaration.getPropertyName());
		}
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite pageContents = new Composite(parent, SWT.NONE);
		setControl(pageContents);
		
		GridLayout gridLayout = new GridLayout(1, false);
		pageContents.setLayout(gridLayout);
		
		createTopBar(pageContents);
		createDiffArea(pageContents);
		createBottomBar(pageContents);
	}

	private void createBottomBar(Composite pageContents) {
		MixinMigrationOpportunity<?> mixinMigrationOpportunity = refactoring.getMixinMigrationOpportunity();
		if (mixinMigrationOpportunity.getNumberOfMixinDeclarations() > 1) {
			Group bottomBar = new Group(pageContents, SWT.NONE);
			bottomBar.setText(LocalizedStrings.get(Keys.PROPERTIES_TO_INCLUDE_IN_MIXIN));
			GridLayout layout = new GridLayout(1, false);
			bottomBar.setLayout(layout);
			bottomBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			for (MixinDeclaration mixinDeclaration : mixinMigrationOpportunity.getAllMixinDeclarations()) {
				Button checkBox = new Button(bottomBar, SWT.CHECK);
				checkBox.setText(mixinDeclaration.getPropertyName());
				checkBox.setSelection(true);
				checkBox.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Button source = (Button)e.getSource();
						if (source.getSelection()) {
							checkedProperties.add(source.getText());
						} else {
							checkedProperties.remove(source.getText());
						}
						setPageComplete(validatePage());
						MixinMigrationOpportunity<?> subOpportunity = mixinMigrationOpportunity
							.getSubOpportunity(checkedProperties, (Set<Selector>) mixinMigrationOpportunity.getInvolvedSelectors());
						refactoring.setMixinMigrationOpportunity(subOpportunity);
						ExtractMixinTreeViewerContentProvider contentProvider = 
								new ExtractMixinTreeViewerContentProvider(subOpportunity);
						diffTreeViewer.setContentProvider(contentProvider);
						diffTreeViewer.expandAll();
					}
				});
			}
		}
	}

	private void createTopBar(Composite pageContents) {
		
		Group topBar = new Group(pageContents, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		topBar.setLayout(layout);
		topBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label label = new Label(topBar, SWT.NONE);
		label.setText(LocalizedStrings.get(Keys.MIXIN_NAME) + ":");
		
		Text mixinNameText = new Text(topBar, SWT.BORDER);
		mixinNameText.setText(refactoring.getMixinMigrationOpportunity().getMixinName());
		mixinNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		mixinNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String newName = mixinNameText.getText();
				String mixinNamePattern = "\\.[a-zA-Z\\$_][a-zA-Z0-9\\$_]*";
				if (!newName.matches(mixinNamePattern)) {
					setPageComplete(false);
					setMessage(String.format(LocalizedStrings.get(Keys.INVALID_MIXIN_NAME), newName), ERROR);
				} else {
					refactoring.setMixinName(newName);
					setPageComplete(true);
					setMessage("", NONE);
				}
			}
		});
		
	}
	
	private void createDiffArea(Composite pageContents) {
		MixinMigrationOpportunity<?> mixinMigrationOpportunity = refactoring.getMixinMigrationOpportunity();
		Group diffArea = new Group(pageContents, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		diffArea.setLayout(layout);
		diffArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		diffTreeViewer = new TreeViewer(diffArea, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		ExtractMixinTreeViewerContentProvider contentProvider = new ExtractMixinTreeViewerContentProvider(mixinMigrationOpportunity);
		diffTreeViewer.setContentProvider(contentProvider);
		
		GridData treeViwereGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		treeViwereGridData.horizontalAlignment = SWT.FILL;
		treeViwereGridData.verticalAlignment = SWT.FILL;
		treeViwereGridData.horizontalSpan = 30;
		treeViwereGridData.verticalSpan = 2;
		diffTreeViewer.getTree().setLayoutData(treeViwereGridData);
		
		TreeViewerColumn column = new TreeViewerColumn(diffTreeViewer, SWT.LEFT);
		column.getColumn().setText(LocalizedStrings.get(Keys.PROPERTY));
		column.setLabelProvider(new ExtractMixinTreeViewerDelegatingStyledCellLabelProvider(contentProvider, 0));
		
		int numberOfValueColumns = 0;
		
		List<MixinDeclaration> mixinDeclarations = new ArrayList<>();
		for (MixinDeclaration mixinDeclaration : mixinMigrationOpportunity.getAllMixinDeclarations()) {
			mixinDeclarations.add(mixinDeclaration);
		}
		
		for (MixinDeclaration mixinDeclaration : mixinDeclarations) {
			int numberOfValues = 0;
			for (MixinValue declarationValue : mixinDeclaration.getMixinValues()) {
				if (mixinDeclaration.getPropertyAndLayerForMixinValue(declarationValue) != null)
					numberOfValues++;
			}
			if (numberOfValues > numberOfValueColumns)
				numberOfValueColumns = numberOfValues;
		}

		for (int i = 1; i <= numberOfValueColumns; i++) {
			TreeViewerColumn variableColumn = new TreeViewerColumn(diffTreeViewer, SWT.LEFT);
			//variableColumn.getColumn().setText("Value " + i);
			variableColumn.setLabelProvider(new ExtractMixinTreeViewerDelegatingStyledCellLabelProvider(contentProvider, i));
		}		
				
		diffTreeViewer.setInput(""); // See getElements for ExtractMixinTreeViewerContentProvider
		diffTreeViewer.getTree().setLinesVisible(true);
		diffTreeViewer.getTree().setHeaderVisible(true);
		diffTreeViewer.expandAll();
		
		for (TreeColumn treeColumn : diffTreeViewer.getTree().getColumns()) {
			treeColumn.pack();
		}
		new ExtractMixinTreeViewerToolTipSupport(diffTreeViewer, contentProvider);

	}
	
	private boolean validatePage() {
		if (checkedProperties.isEmpty()) {
			setErrorMessage(LocalizedStrings.get(Keys.EMPTY_PROPERTIES));
			return false;
		} else {
			setErrorMessage(null);
			return true;
		}
	}
	
}
