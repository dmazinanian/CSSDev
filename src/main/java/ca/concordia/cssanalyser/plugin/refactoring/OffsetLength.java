package ca.concordia.cssanalyser.plugin.refactoring;

public class OffsetLength {
	
	private final int offset;
	private final int length;
	
	public OffsetLength(int offset, int length) {
		this.offset = offset;
		this.length = length;
	}

	public int getOffset() {
		return this.offset;
	}

	public int getLength() {
		return this.length;
	}

	@Override
	public String toString() {
		return String.format("<%s,%s>", offset, length);
	}
}