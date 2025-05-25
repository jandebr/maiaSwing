package org.maia.swing.animate.itemslide;

import java.awt.Graphics2D;
import java.awt.Rectangle;

public interface SlidingCursor {

	void renderUnderItems(Graphics2D g, Rectangle innerBounds, SlidingItemListComponent component);

	void renderAboveItems(Graphics2D g, Rectangle innerBounds, SlidingItemListComponent component);

}