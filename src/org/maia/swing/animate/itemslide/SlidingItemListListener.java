package org.maia.swing.animate.itemslide;

import org.maia.util.GenericListener;

public interface SlidingItemListListener extends GenericListener {

	void notifyItemsChanged(SlidingItemListComponent component);

	void notifyItemSelectionChanged(SlidingItemListComponent component, SlidingItem selectedItem,
			int selectedItemIndex);

	void notifyItemSelectionLanded(SlidingItemListComponent component, SlidingItem selectedItem, int selectedItemIndex);

	void notifySlidingStateChanged(SlidingItemListComponent component);

	void notifyStartSliding(SlidingItemListComponent component);

	void notifyStopSliding(SlidingItemListComponent component);

}