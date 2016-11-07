package ca.concordia.cssanalyser.plugin.wizards.filteroptions;

import java.util.List;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.ExpandAdapter;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import ca.concordia.cssanalyser.csshelper.CSSPropertyCategoryHelper;
import ca.concordia.cssanalyser.cssmodel.media.MediaQueryList;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.plugin.utility.Constants;
import ca.concordia.cssanalyser.plugin.utility.DuplicationInfo;
import ca.concordia.cssanalyser.plugin.utility.ImagesUtil;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtil;
import ca.concordia.cssanalyser.plugin.utility.PreferencesUtil.ColorConstants;

public class FilterOptionsWizardPage extends WizardPage {

	private static final int TABLE_VIEWER_FLAGS = SWT.CHECK | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL;
	private static final int PARTS_MAX_HEIGHT = 150;

	private final FilterOptions fileterOptions;
	private final FilterOptions newFilterOptions;
	private final List<DuplicationInfo> duplicationInfoList;
	
	private class CSSColoredLabelProvider extends StyledCellLabelProvider {
		
		private ColorConstants textColor;
		
		public CSSColoredLabelProvider(ColorConstants textColor) {
			this.textColor = textColor;
		}
		
		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			cell.setText(element.toString());
		
			StyleRange styleRange = new StyleRange();
			styleRange.start = 0;
			styleRange.length = cell.getText().length();
			styleRange.foreground = PreferencesUtil.getCSSColor(textColor);
			cell.setStyleRanges(new StyleRange[] { styleRange });
			cell.setFont(PreferencesUtil.getTextEditorFont());
			boolean checked = false;
			if (element instanceof MediaQueryList) {
				checked = (fileterOptions.includes((MediaQueryList) element));
			} else if (element instanceof Selector) {
				checked = (fileterOptions.includes((Selector)element));
			} else if (element instanceof String) {
				checked = (fileterOptions.includes((String)element));
			}
			((TableItem)cell.getItem()).setChecked(checked);
		}
		
		@Override
		protected void paint(Event event, Object element) {
			if (element instanceof String && event.index == 0) {
				DuplicationInfo.drawCategoryMarks(event.gc, event.x, event.y, CSSPropertyCategoryHelper.getCSSCategoryOfProperty(element.toString()));
			} else {
				super.paint(event, element);
			}
		}
		
	}

	public FilterOptionsWizardPage(FilterOptions fileterOptions, List<DuplicationInfo> duplicationInfoList) {
		super(LocalizedStrings.get(Keys.FILTER_RESULTS));
		this.fileterOptions = fileterOptions;
		this.newFilterOptions = fileterOptions.getCopy();
		this.duplicationInfoList = duplicationInfoList;
	}

	@Override
	public void createControl(Composite parent) {
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 350;
		parent.setLayoutData(gridData);
		
		ExpandBar expandBar = new ExpandBar(parent, /*SWT.V_SCROLL*/SWT.NONE);
		expandBar.setBackground(parent.getBackground());
		expandBar.addExpandListener(new ExpandAdapter() {
			@Override
			public void itemExpanded(ExpandEvent expandEvent) {
				for (int i = 0; i < expandBar.getItemCount(); i++) {
					ExpandItem item = expandBar.getItem(i);
					if (expandEvent.item != item) {
						item.setExpanded(false);
					}
				}
			}
		});
		createMediaQueryListsPart(expandBar);
		createSelectorsPart(expandBar);
		createPropertiesPart(expandBar);
		setControl(parent);
	}
	
	private void createMediaQueryListsPart(ExpandBar expandBar) {
		Composite composite = new Composite(expandBar, SWT.NONE);
		composite.setLayout(new FillLayout());
		TableViewer mediaQueriesTableViewer = new TableViewer(composite, TABLE_VIEWER_FLAGS);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(1, false));
		mediaQueriesTableViewer.getTable().setLayout(layout);
		mediaQueriesTableViewer.getTable().setHeaderVisible(true);
		TableViewerColumn mediaQueryViewerColumn = new TableViewerColumn(mediaQueriesTableViewer, SWT.LEFT);
		mediaQueryViewerColumn.getColumn().setText(LocalizedStrings.get(Keys.MEDIA_QUERY));
		mediaQueriesTableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object arg0) {
				return FilterUtil.getUniqueMediaQueryLists(duplicationInfoList).toArray();
			}
		});	
		mediaQueriesTableViewer.getTable().addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.CHECK) {
					if (((TableItem)event.item).getChecked()) {
						newFilterOptions.includeMediaQuery((MediaQueryList) event.item.getData());
					} else {
						newFilterOptions.excludeMediaQuery((MediaQueryList) event.item.getData());
					}
				}
			}
		});
		mediaQueriesTableViewer.setLabelProvider(new CSSColoredLabelProvider(ColorConstants.MEDIA));
		mediaQueriesTableViewer.setInput(this);
		ExpandItem mediaQuerysItem = new ExpandItem(expandBar, SWT.NONE, 0);
		mediaQuerysItem.setText(LocalizedStrings.get(Keys.FILTER_BY) + " " + LocalizedStrings.get(Keys.MEDIA_QUERY));
		mediaQuerysItem.setHeight(Math.min(PARTS_MAX_HEIGHT, composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y));
		mediaQuerysItem.setControl(composite);
		mediaQuerysItem.setImage(ImagesUtil.getImageDescriptor(Constants.FILTER_ICON).createImage());
	}
	
	private void createSelectorsPart(ExpandBar expandBar) {
		Composite selectorsComposite = new Composite(expandBar, SWT.NONE);
		selectorsComposite.setLayout(new FillLayout());
		TableViewer selectorsTableViewer = new TableViewer(selectorsComposite, TABLE_VIEWER_FLAGS);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(1, false));
		selectorsTableViewer.getTable().setLayout(layout);
		selectorsTableViewer.getTable().setHeaderVisible(true);
		TableViewerColumn selectorViewerColumn = new TableViewerColumn(selectorsTableViewer, SWT.LEFT);
		selectorViewerColumn.getColumn().setText(LocalizedStrings.get(Keys.SELECTOR));
		selectorsTableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object arg0) {
				return FilterUtil.getUniqueSelectors(duplicationInfoList).toArray();
			}
		});
		selectorsTableViewer.getTable().addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.CHECK) {
					if (((TableItem)event.item).getChecked()) {
						newFilterOptions.includeSelector((Selector) event.item.getData());
					} else {
						newFilterOptions.excludeSelector((Selector) event.item.getData());
					}
				}
			}
		});
		selectorsTableViewer.setLabelProvider(new CSSColoredLabelProvider(ColorConstants.SELECTOR));
		selectorsTableViewer.setInput(this);
		ExpandItem selectorItem = new ExpandItem(expandBar, SWT.NONE, 1);
		selectorItem.setText(LocalizedStrings.get(Keys.FILTER_BY) + " " + LocalizedStrings.get(Keys.SELECTOR));
		selectorItem.setHeight(Math.min(PARTS_MAX_HEIGHT, selectorsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y));
		selectorItem.setControl(selectorsComposite);
		selectorItem.setImage(ImagesUtil.getImageDescriptor(Constants.FILTER_ICON).createImage());
	}

	private void createPropertiesPart(ExpandBar expandBar) {
		Composite composite = new Composite (expandBar, SWT.NONE);
		composite.setLayout(new FillLayout());
		TableViewer propertiesTableViewer = new TableViewer(composite, TABLE_VIEWER_FLAGS);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(5, false));
		layout.addColumnData(new ColumnWeightData(45, true));
		propertiesTableViewer.getTable().setLayout(layout);
		propertiesTableViewer.getTable().setHeaderVisible(true);
		TableViewerColumn propertyCategoryViewerColumn = new TableViewerColumn(propertiesTableViewer, SWT.CENTER);
		propertyCategoryViewerColumn.getColumn().setText(LocalizedStrings.get(Keys.PROPERTY_CATEGORIES));
		TableViewerColumn propertyViewerColumn = new TableViewerColumn(propertiesTableViewer, SWT.LEFT);
		propertyViewerColumn.getColumn().setText(LocalizedStrings.get(Keys.PROPERTY));
		propertiesTableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object arg0) {
				return FilterUtil.getUniqueProperties(duplicationInfoList).toArray();
			}
		});
		propertiesTableViewer.getTable().addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.CHECK) {
					if (((TableItem)event.item).getChecked()) {
						newFilterOptions.includeProperty((String) event.item.getData());
					} else {
						newFilterOptions.excludeProperty((String) event.item.getData());
					}
				}
			}
		});
		propertiesTableViewer.setLabelProvider(new CSSColoredLabelProvider(ColorConstants.PROPERY));
		propertiesTableViewer.setInput(this);
		ExpandItem propertyItemItem = new ExpandItem(expandBar, SWT.NONE, 2);
		propertyItemItem.setText(LocalizedStrings.get(Keys.FILTER_BY) + " " + LocalizedStrings.get(Keys.PROPERTY));
		propertyItemItem.setHeight(Math.min(PARTS_MAX_HEIGHT, composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y));
		propertyItemItem.setControl(composite);
		propertyItemItem.setImage(ImagesUtil.getImageDescriptor(Constants.FILTER_ICON).createImage());
	}

	public FilterOptions getFilterOptions() {
		return newFilterOptions;
	}
	
}
