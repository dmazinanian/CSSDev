package ca.concordia.cssdev.plugin.views.duplicationrefactoring;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import ca.concordia.cssdev.plugin.utility.DuplicationInfo;

public class DuplicationViewViewerComparator extends ViewerComparator {
	
	private final int columnIndex;
	private final boolean desc;
	
	public DuplicationViewViewerComparator(int columnIndex, boolean desc) {
		this.columnIndex = columnIndex;
		this.desc = desc;
	}
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		DuplicationInfo duplicationInfo1 = (DuplicationInfo)e1;
		DuplicationInfo duplicationInfo2 = (DuplicationInfo)e2;
		int toReturn = -1;
		switch (columnIndex) {
			case 0:
				toReturn = Integer.compare(duplicationInfo1.getItemSet().size(), duplicationInfo2.getItemSet().size());
				break;
			case 1:
				toReturn = duplicationInfo1.getDeclarationNames().compareTo(duplicationInfo2.getDeclarationNames());
				break;
			case 2:
				toReturn = Integer.compare(duplicationInfo1.getItemSet().getSupportSize(), duplicationInfo2.getItemSet().getSupportSize());
				break;
			case 3:
				toReturn = duplicationInfo1.getSelectorNames().compareTo(duplicationInfo2.getSelectorNames());
				break;
			case 4:
				String s1 = duplicationInfo1.getDuplicationTypes().toString() + duplicationInfo1.hasDifferences();
				String s2 = duplicationInfo2.getDuplicationTypes().toString() + duplicationInfo2.hasDifferences();
				toReturn = s1.compareTo(s2);
				break;
			case 5:
				toReturn = Integer.compare(duplicationInfo1.getPropertyCategories().size(), duplicationInfo2.getPropertyCategories().size());
				break;
			case 6:
				toReturn = Double.compare(duplicationInfo1.getRank(), duplicationInfo2.getRank());
				break;
		}
		return toReturn * (desc ? -1 : 1);
	}
	
}