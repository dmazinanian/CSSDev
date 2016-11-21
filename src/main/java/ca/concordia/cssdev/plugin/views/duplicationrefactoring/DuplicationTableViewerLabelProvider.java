package ca.concordia.cssdev.plugin.views.duplicationrefactoring;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Event;

import ca.concordia.cssdev.plugin.utility.DuplicationInfo;
import ca.concordia.cssdev.plugin.utility.PreferencesUtil;

public class DuplicationTableViewerLabelProvider extends StyledCellLabelProvider {
	
	private final int columnIndex;

	public DuplicationTableViewerLabelProvider(int columnIndex) {
		this.columnIndex = columnIndex;
	}
	
	@Override
	public String getToolTipText(Object element) {
		DuplicationInfo duplicationInfo = (DuplicationInfo) element;
		switch (columnIndex) {
		case 1:
			return duplicationInfo.getDeclarationNames();
		case 3:
			return duplicationInfo.getSelectorNames();
		case 4:
			return getDuplicationTypes(duplicationInfo);
		case 5:
			return duplicationInfo.getPropertyCategories().toString();
		case 6:
			return String.valueOf(duplicationInfo.getRank());
		default:
			break;
		}
		return null;
	}
	
	@Override
	public void update(ViewerCell cell) {
		DuplicationInfo selectedDuplicationInfoObject = (DuplicationInfo) cell.getItem().getData();
		switch (cell.getColumnIndex()) {
		case 0:
			cell.setText(String.valueOf(selectedDuplicationInfoObject.getItemSet().size()));
			break;
		case 1:
			cell.setText(selectedDuplicationInfoObject.getDeclarationNames());
			cell.setFont(PreferencesUtil.getTextEditorFont());
			break;
		case 2:
			cell.setText(String.valueOf(selectedDuplicationInfoObject.getItemSet().getSupportSize()));
			break;
		case 3:
			cell.setText(selectedDuplicationInfoObject.getSelectorNames());
			cell.setFont(PreferencesUtil.getTextEditorFont());
			break;
		case 4:
			String typeText = getDuplicationTypes(selectedDuplicationInfoObject);
			cell.setText(typeText);
			break;
		case 6:
			cell.setText(String.valueOf(selectedDuplicationInfoObject.getRank()));
			break;
		default:
			super.update(cell);
		}
	}

	private String getDuplicationTypes(DuplicationInfo selectedDuplicationInfoObject) {
		String typeText = selectedDuplicationInfoObject.getDuplicationTypes().toString();
		typeText = typeText.substring(1, typeText.length() - 1);
		if (selectedDuplicationInfoObject.hasDifferences()) {
			typeText = (!"".equals(typeText) ? "" : ", ") + "D"; 
		}
		return typeText;
	}
	
	@Override
	protected void paint(Event event, Object element) {
		if (event.index == 5) {
			DuplicationInfo duplicationInfo = (DuplicationInfo)element;
			GC gc = event.gc;
			int startX = event.x;
			int startY = event.y;
			duplicationInfo.drawCategoryMarks(gc, startX, startY);
		} else {
			super.paint(event, element);
		}
	}
	
}
