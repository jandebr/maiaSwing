package org.maia.swing.animate.itemslide.impl;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Vector;

import org.maia.swing.animate.itemslide.SlidingItem;

public class SlidingItemList {

	private List<SlidingItemInList> items;

	public SlidingItemList() {
		this.items = new Vector<SlidingItemInList>();
	}

	public void addItem(SlidingItemInList item) {
		if (!contains(item)) {
			getItems().add(item);
		}
	}

	public void insertItem(SlidingItemInList item, int index) {
		if (!contains(item)) {
			getItems().add(index, item);
		}
	}

	public void removeItem(SlidingItemInList item) {
		if (contains(item)) {
			getItems().remove(item);
		}
	}

	public void removeAllItems() {
		if (!isEmpty()) {
			getItems().clear();
		}
	}

	public double getMaxItemWidth(Graphics2D g) {
		double max = 0;
		for (int i = 0; i < getItemCount(); i++) {
			max = Math.max(max, getItem(i).getItem().getWidth(g));
		}
		return max;
	}

	public double getMaxItemHeight(Graphics2D g) {
		double max = 0;
		for (int i = 0; i < getItemCount(); i++) {
			max = Math.max(max, getItem(i).getItem().getHeight(g));
		}
		return max;
	}

	public boolean contains(SlidingItemInList item) {
		return getItems().contains(item);
	}

	public boolean isEmpty() {
		return getItems().isEmpty();
	}

	public int getItemCount() {
		return getItems().size();
	}

	public int getIndexOf(SlidingItemInList item) {
		return getItems().indexOf(item);
	}

	public int getIndexOf(SlidingItem item) {
		for (int i = 0; i < getItemCount(); i++) {
			if (getItem(i).getItem().equals(item))
				return i;
		}
		return -1;
	}

	public SlidingItemInList getItem(int index) {
		return getItems().get(index);
	}

	public SlidingItemInList getFirstItem() {
		if (!isEmpty()) {
			return getItem(0);
		} else {
			return null;
		}
	}

	public SlidingItemInList getLastItem() {
		if (!isEmpty()) {
			return getItem(getItemCount() - 1);
		} else {
			return null;
		}
	}

	private List<SlidingItemInList> getItems() {
		return items;
	}

}