package org.maia.swing.animate.itemslide.impl;

import java.awt.Insets;
import java.util.Objects;

public class SlidingState {

	private double itemTranslation;

	private double cursorPosition;

	private double cursorWidth;

	private double cursorHeight;

	private Insets cursorMargin;

	public SlidingState() {
		this(0, 0, 0, 0, new Insets(0, 0, 0, 0));
	}

	public SlidingState(double itemTranslation, double cursorPosition, double cursorWidth, double cursorHeight,
			Insets cursorMargin) {
		this.itemTranslation = itemTranslation;
		this.cursorPosition = cursorPosition;
		this.cursorWidth = cursorWidth;
		this.cursorHeight = cursorHeight;
		this.cursorMargin = cursorMargin;
	}

	@Override
	public int hashCode() {
		return Objects.hash(cursorHeight, cursorMargin, cursorPosition, cursorWidth, itemTranslation);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SlidingState other = (SlidingState) obj;
		return Double.doubleToLongBits(cursorHeight) == Double.doubleToLongBits(other.cursorHeight)
				&& Objects.equals(cursorMargin, other.cursorMargin)
				&& Double.doubleToLongBits(cursorPosition) == Double.doubleToLongBits(other.cursorPosition)
				&& Double.doubleToLongBits(cursorWidth) == Double.doubleToLongBits(other.cursorWidth)
				&& Double.doubleToLongBits(itemTranslation) == Double.doubleToLongBits(other.itemTranslation);
	}

	public boolean approximateEqualPosition(SlidingState other) {
		return Math.round(getCursorPosition()) == Math.round(other.getCursorPosition())
				&& Math.round(getItemTranslation()) == Math.round(other.getItemTranslation());
	}

	public double getItemTranslation() {
		return itemTranslation;
	}

	public double getCursorPosition() {
		return cursorPosition;
	}

	public double getCursorWidth() {
		return cursorWidth;
	}

	public double getCursorHeight() {
		return cursorHeight;
	}

	public Insets getCursorMargin() {
		return cursorMargin;
	}

}