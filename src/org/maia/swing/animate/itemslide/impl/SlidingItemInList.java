package org.maia.swing.animate.itemslide.impl;

import java.util.Objects;

import org.maia.swing.animate.itemslide.SlidingItem;

public class SlidingItemInList {

	private SlidingItem item;

	private SlidingItemList list;

	private double position;

	public SlidingItemInList(SlidingItem item, SlidingItemList list) {
		this.item = item;
		this.list = list;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getItem());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SlidingItemInList other = (SlidingItemInList) obj;
		return Objects.equals(getItem(), other.getItem());
	}

	public double getPosition() {
		return position;
	}

	public void setPosition(double position) {
		this.position = position;
	}

	public SlidingItem getItem() {
		return item;
	}

	public SlidingItemList getList() {
		return list;
	}

}