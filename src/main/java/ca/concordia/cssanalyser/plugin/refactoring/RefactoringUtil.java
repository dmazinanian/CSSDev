package ca.concordia.cssanalyser.plugin.refactoring;

import ca.concordia.cssanalyser.cssmodel.LocationInfo;


public class RefactoringUtil {

	public static OffsetLength expandAreaToRemove(String fileContents, LocationInfo locationInfo) {
		int offset = locationInfo.getOffset();
		int length = locationInfo.getLength();
		// Try to delete redundant stuff
		int startFrom = offset;
		while (startFrom > 0) {
			char charAt = fileContents.charAt(--startFrom);
			if (charAt == ' ' || charAt == '\t') {
				offset--;
				length++;
			} else {
				break;
			}
		}
		startFrom = offset + length;
		while (startFrom < fileContents.length()) {
			char charAt = fileContents.charAt(startFrom++);
			if (charAt == ' ' || charAt == '\t' || charAt == '\r' || charAt == ';') {
				length++;
			} else if (charAt == '\n') {
				length++;
				break;
			} else {
				break;
			}
		}
		OffsetLength offsetLength = new OffsetLength(offset, length);
		return offsetLength;
	}

}
