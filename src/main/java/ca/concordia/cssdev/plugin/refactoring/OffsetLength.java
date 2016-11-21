package ca.concordia.cssdev.plugin.refactoring;

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
	
	public int getEnd() {
		return this.offset + this.length - 1;
	}
	
	public boolean overlaps(OffsetLength other) {
		int min = Math.min(this.getOffset(), other.getOffset());
		int max = Math.max(this.getEnd(), other.getEnd());
		boolean[] first = new boolean[max - min + 1];
		boolean[] second = new boolean[max - min + 1];
		for (int i = this.offset; i <= this.getEnd(); i++) {
			first[i - min] = true;
		}
		for (int i = other.offset; i <= other.getEnd(); i++) {
			second[i - min] = true;
		}
		for (int i = 0; i < max - min + 1; i++) {
			if (first[i] && second[i]) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("<%s,%s>", offset, length);
	}
}