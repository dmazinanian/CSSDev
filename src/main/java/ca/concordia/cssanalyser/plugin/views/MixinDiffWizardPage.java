package ca.concordia.cssanalyser.plugin.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;

import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinDeclaration;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinMigrationOpportunity;
import ca.concordia.cssanalyser.migration.topreprocessors.mixin.MixinValue;
import ca.concordia.cssanalyser.plugin.refactoring.MixinMigrationRefactoring;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;

public class MixinDiffWizardPage extends UserInputWizardPage {

	private final MixinMigrationRefactoring refactoring;
	private final MixinMigrationOpportunity<?> mixinMigrationOpportunity;
	
	public MixinDiffWizardPage(MixinMigrationRefactoring refactoring) {
		super(refactoring.getName());
		this.refactoring = refactoring;
		this.mixinMigrationOpportunity = refactoring.getMixinMigrationOpportunity();
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite pageContents = new Composite(parent, SWT.NONE);
		setControl(pageContents);
		
		GridLayout gridLayout = new GridLayout(1, false);
		pageContents.setLayout(gridLayout);
		
		createTopBar(pageContents);
		createDiffArea(pageContents);
	}

	private void createTopBar(Composite pageContents) {
		
		Group topBar = new Group(pageContents, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		topBar.setLayout(layout);
		topBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label label = new Label(topBar, SWT.NONE);
		label.setText("Mixin Name:");
		
		Text mixinNameText = new Text(topBar, SWT.BORDER);
		mixinNameText.setText(mixinMigrationOpportunity.getMixinName());
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
		Group diffArea = new Group(pageContents, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		diffArea.setLayout(layout);
		diffArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		TreeViewer treeViewer = new TreeViewer(diffArea, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		treeViewer.setContentProvider(new ExtractMixinTreeViewerContentProvider(mixinMigrationOpportunity));
		
		GridData treeViwereGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		treeViwereGridData.horizontalAlignment = SWT.FILL;
		treeViwereGridData.verticalAlignment = SWT.FILL;
		treeViwereGridData.horizontalSpan = 30;
		treeViwereGridData.verticalSpan = 2;
		treeViewer.getTree().setLayoutData(treeViwereGridData);
		
		TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.LEFT);
		column.getColumn().setText(LocalizedStrings.get(Keys.PROPERTY));
		column.setLabelProvider(new ExtractMixinTreeViewerDelegatingStyledCellLabelProvider(mixinMigrationOpportunity, 0));
		
		int numberOfValueColumns = 0;
		
		List<MixinDeclaration> mixinDeclarations = new ArrayList<>();
		for (MixinDeclaration mixinDeclaration : mixinMigrationOpportunity.getAllMixinDeclarations()) {
			mixinDeclarations.add(mixinDeclaration);
		}
		
		for (MixinDeclaration mixinDeclaration : mixinDeclarations) {
			int numberOfValues = 0;
			for (MixinValue declarationValue : mixinDeclaration.getMixinValues()) {
				if (declarationValue.getAssignedTo() != null)
					numberOfValues++;
			}
			if (numberOfValues > numberOfValueColumns)
				numberOfValueColumns = numberOfValues;
		}

		for (int i = 1; i <= numberOfValueColumns; i++) {
			TreeViewerColumn variableColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
			//variableColumn.getColumn().setText("Value " + i);
			variableColumn.setLabelProvider(new ExtractMixinTreeViewerDelegatingStyledCellLabelProvider(mixinMigrationOpportunity, i));
		}		
				
		treeViewer.setInput(""); // See getElements for ExtractMixinTreeViewerContentProvider
		treeViewer.getTree().setLinesVisible(true);
		treeViewer.getTree().setHeaderVisible(true);
		treeViewer.expandAll();
		
		for (TreeColumn treeColumn : treeViewer.getTree().getColumns()) {
			treeColumn.pack();
		}
		
	}

}
