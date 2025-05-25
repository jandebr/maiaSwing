package org.maia.swing.animate.itemslide;

import java.awt.Graphics2D;
import java.awt.Insets;

public interface SlidingItem {

	void render(Graphics2D g, SlidingItemListComponent component);

	int getWidth(Graphics2D g);

	int getHeight(Graphics2D g);

	Insets getMargin();

}