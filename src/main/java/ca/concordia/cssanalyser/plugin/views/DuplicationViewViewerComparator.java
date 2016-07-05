package ca.concordia.cssanalyser.plugin.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import ca.concordia.cssanalyser.plugin.utility.DuplicationInfo;

public class DuplicationViewViewerComparator extends ViewerComparator {
	
	private final int columnIndex;
	
	public DuplicationViewViewerComparator(int columnIndex) {
		this.columnIndex = columnIndex;
	}
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		DuplicationInfo duplicationInfo1 = (DuplicationInfo)e1;
		DuplicationInfo duplicationInfo2 = (DuplicationInfo)e2;
		switch (columnIndex) {
			case 0:
				return duplicationInfo1.getDeclarationNames().compareTo(duplicationInfo2.getDeclarationNames());
			case 1:
				return duplicationInfo1.getSelectorNames().compareTo(duplicationInfo2.getSelectorNames());
			case 2:
				return Double.compare(duplicationInfo1.getRank(), duplicationInfo2.getRank());
			default:
				return -1;
		}
		
	}
	
}