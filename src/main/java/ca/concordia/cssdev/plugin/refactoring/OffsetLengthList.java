package ca.concordia.cssdev.plugin.refactoring;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class OffsetLengthList {
	
	// Sort the blocks to remove, based on their start offsets
    private Set<OffsetLength> offsetAndLengths = new TreeSet<>(new Comparator<OffsetLength>() {
		@Override
		public int compare(OffsetLength o1, OffsetLength o2) {
			if (o1 == o2 || (o1.getOffset() == o2.getOffset() && o1.getLength() == o2.getLength())) {
				return 0;
			}
			if (o1.getOffset() > o2.getOffset()) {
				return 1;
			}
			return -1;
		}
	});

	public boolean add(OffsetLength offsetLength) {
		return offsetAndLengths.add(offsetLength);
	}

	public Iterable<OffsetLength> getNonOverlappingOffsetsAndLengths() {
		// Avoid overlapping text edits
	    List<OffsetLength> offsetAndLengthsList = new ArrayList<>(offsetAndLengths);		    
	    for (int i = 0; i < offsetAndLengthsList.size() - 1; i++) {
	    	OffsetLength offsetLength1 = offsetAndLengthsList.get(i);
	    	OffsetLength offsetLength2 = offsetAndLengthsList.get(i + 1);
	    	if (offsetLength1.getOffset() + offsetLength1.getLength() > offsetLength2.getOffset()) {
	    		int length = offsetLength2.getOffset() + offsetLength2.getLength() - offsetLength1.getOffset();
				OffsetLength newOffSetLength = new OffsetLength(offsetLength1.getOffset(), length);
				 offsetAndLengthsList.set(i, newOffSetLength);
				 offsetAndLengthsList.remove(i + 1);
				 i--;
	    	}
	    }
	    return offsetAndLengthsList;
	}
	
}
