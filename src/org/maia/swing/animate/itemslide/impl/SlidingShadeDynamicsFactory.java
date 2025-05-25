package org.maia.swing.animate.itemslide.impl;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.maia.swing.animate.itemslide.SlidingItemListComponent;
import org.maia.swing.animate.itemslide.SlidingShade;
import org.maia.swing.animate.itemslide.SlidingShadeDynamics;
import org.maia.swing.animate.itemslide.SlidingShadeEnd;

public class SlidingShadeDynamicsFactory {

	private SlidingShadeDynamicsFactory() {
	}

	public static SlidingShadeDynamics createOverflowToggleShadeDynamics() {
		return new OverflowToggleShadeDynamics();
	}

	public static SlidingShadeDynamics createOverflowOffsetShadeDynamics() {
		return new OverflowOffsetShadeDynamics();
	}

	private static abstract class OverflowShadeDynamics implements SlidingShadeDynamics {

		protected OverflowShadeDynamics() {
		}

		@Override
		public int getShadeExteriorLength(SlidingShade shade, SlidingShadeEnd end, SlidingItemListComponent component,
				SlidingItemList itemList, Graphics2D g) {
			if (component.isHorizontalLayout()) {
				return getHorizontalShadeExteriorLength(shade,
						getHorizontalItemListOverflow(end, component, itemList, g));
			} else {
				return getVerticalShadeExteriorLength(shade, getVerticalItemListOverflow(end, component, itemList, g));
			}
		}

		protected abstract int getHorizontalShadeExteriorLength(SlidingShade shade, int itemListOverflow);

		protected abstract int getVerticalShadeExteriorLength(SlidingShade shade, int itemListOverflow);

		private int getHorizontalItemListOverflow(SlidingShadeEnd end, SlidingItemListComponent component,
				SlidingItemList itemList, Graphics2D g) {
			int overflow = 0;
			if (component.hasItems()) {
				if (SlidingShadeEnd.LEADING.equals(end)) {
					Rectangle bounds = component.getLayoutManager()
							.getItemBoundsInViewportCoords(itemList.getFirstItem(), g);
					overflow = Math.max(-bounds.x, 0);
				} else {
					Rectangle bounds = component.getLayoutManager()
							.getItemBoundsInViewportCoords(itemList.getLastItem(), g);
					overflow = Math.max(bounds.x + bounds.width - component.getViewportWidth(), 0);
				}
			}
			return overflow;
		}

		private int getVerticalItemListOverflow(SlidingShadeEnd end, SlidingItemListComponent component,
				SlidingItemList itemList, Graphics2D g) {
			int overflow = 0;
			if (component.hasItems()) {
				if (SlidingShadeEnd.LEADING.equals(end)) {
					Rectangle bounds = component.getLayoutManager()
							.getItemBoundsInViewportCoords(itemList.getFirstItem(), g);
					overflow = Math.max(-bounds.y, 0);
				} else {
					Rectangle bounds = component.getLayoutManager()
							.getItemBoundsInViewportCoords(itemList.getLastItem(), g);
					overflow = Math.max(bounds.y + bounds.height - component.getViewportHeight(), 0);
				}
			}
			return overflow;
		}

	}

	private static class OverflowToggleShadeDynamics extends OverflowShadeDynamics {

		public OverflowToggleShadeDynamics() {
		}

		@Override
		protected int getHorizontalShadeExteriorLength(SlidingShade shade, int itemListOverflow) {
			if (itemListOverflow > 0) {
				return 0; // show
			} else {
				return shade.getWidth(); // hide
			}
		}

		@Override
		protected int getVerticalShadeExteriorLength(SlidingShade shade, int itemListOverflow) {
			if (itemListOverflow > 0) {
				return 0; // show
			} else {
				return shade.getHeight(); // hide
			}
		}

	}

	private static class OverflowOffsetShadeDynamics extends OverflowShadeDynamics {

		public OverflowOffsetShadeDynamics() {
		}

		@Override
		protected int getHorizontalShadeExteriorLength(SlidingShade shade, int itemListOverflow) {
			return Math.max(shade.getWidth() - itemListOverflow, 0);
		}

		@Override
		protected int getVerticalShadeExteriorLength(SlidingShade shade, int itemListOverflow) {
			return Math.max(shade.getHeight() - itemListOverflow, 0);
		}

	}

}