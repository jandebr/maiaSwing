package org.maia.swing.animate.itemslide;

import java.awt.Graphics2D;

import org.maia.swing.animate.itemslide.impl.SlidingItemList;

public interface SlidingShadeDynamics {

	/**
	 * @return Length (in pixels) of the shade that falls outside the viewport
	 */
	int getShadeExteriorLength(SlidingShade shade, SlidingShadeEnd end, SlidingItemListComponent component,
			SlidingItemList itemList, Graphics2D g);

}