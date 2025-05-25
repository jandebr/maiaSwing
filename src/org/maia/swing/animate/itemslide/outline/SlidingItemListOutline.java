package org.maia.swing.animate.itemslide.outline;

public class SlidingItemListOutline {

	private Range viewportToListRange; // bounded by [0,1]

	private Range cursorToViewportRange; // bounded by [0,1]

	public SlidingItemListOutline(Range viewportToListRange, Range cursorToViewportRange) {
		this.viewportToListRange = viewportToListRange;
		this.cursorToViewportRange = cursorToViewportRange;
	}

	public Range getViewportToListRange() {
		return viewportToListRange;
	}

	public Range getCursorToViewportRange() {
		return cursorToViewportRange;
	}

	public static class Range {

		private double start;

		private double end;

		public Range(double start, double end) {
			this.start = start;
			this.end = end;
		}

		public double getStart() {
			return start;
		}

		public double getEnd() {
			return end;
		}

	}

}