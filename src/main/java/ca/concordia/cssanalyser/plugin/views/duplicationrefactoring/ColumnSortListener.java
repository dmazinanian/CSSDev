package ca.concordia.cssanalyser.plugin.views.duplicationrefactoring;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ColumnSortListener implements Listener {
	
	private final TableViewer tableViewer;
	private final int columnIndex;
	
	public ColumnSortListener(TableViewer tableViewer, int columnIndex) {
		this.tableViewer = tableViewer;
		this.columnIndex = columnIndex;
	}

	@Override
	public void handleEvent(Event arg0) {
		Table table = tableViewer.getTable();
		TableColumn sortColumn = table.getSortColumn();
		TableColumn currentColumn = (TableColumn) arg0.widget;
		int dir = table.getSortDirection();
		if (sortColumn == currentColumn) {
			dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
		} else {
			table.setSortColumn(currentColumn);
			dir = SWT.UP;
		}
		
		table.setSortDirection(dir);
		tableViewer.setComparator(new DuplicationViewViewerComparator(columnIndex, dir == SWT.DOWN));
	}

}
