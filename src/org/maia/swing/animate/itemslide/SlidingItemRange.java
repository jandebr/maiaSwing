package org.maia.swing.animate.itemslide;

public class SlidingItemRange {

	private int inclusiveFromIndex;

	private int inclusiveToIndex;

	public SlidingItemRange(int inclusiveFromIndex, int inclusiveToIndex) {
		if (inclusiveFromIndex < 0 || inclusiveToIndex < inclusiveFromIndex)
			throw new IllegalArgumentException("Invalid range: [" + inclusiveFromIndex + "," + inclusiveToIndex + "]");
		this.inclusiveFromIndex = inclusiveFromIndex;
		this.inclusiveToIndex = inclusiveToIndex;
	}

	public int getInclusiveFromIndex() {
		return inclusiveFromIndex;
	}

	public void setInclusiveFromIndex(int index) {
		this.inclusiveFromIndex = index;
	}

	public int getInclusiveToIndex() {
		return inclusiveToIndex;
	}

	public void setInclusiveToIndex(int index) {
		this.inclusiveToIndex = index;
	}

}