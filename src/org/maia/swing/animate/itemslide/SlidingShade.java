package org.maia.swing.animate.itemslide;

import java.awt.Graphics2D;

public interface SlidingShade {

	void renderAsLeading(Graphics2D g, int width, int height, SlidingItemListComponent component);

	int getWidth();

	int getHeight();

}