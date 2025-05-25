package org.maia.swing.animate.itemslide;

public abstract class SlidingItemListAdapter implements SlidingItemListListener {

	protected SlidingItemListAdapter() {
	}

	@Override
	public void notifyItemsChanged(SlidingItemListComponent component) {
		// Subclasses can override this method
	}

	@Override
	public void notifyItemSelectionChanged(SlidingItemListComponent component, SlidingItem selectedItem,
			int selectedItemIndex) {
		// Subclasses can override this method
	}

	@Override
	public void notifyItemSelectionLanded(SlidingItemListComponent component, SlidingItem selectedItem,
			int selectedItemIndex) {
		// Subclasses can override this method
	}

	@Override
	public void notifySlidingStateChanged(SlidingItemListComponent component) {
		// Subclasses can override this method
	}

	@Override
	public void notifyStartSliding(SlidingItemListComponent component) {
		// Subclasses can override this method
	}

	@Override
	public void notifyStopSliding(SlidingItemListComponent component) {
		// Subclasses can override this method
	}

}