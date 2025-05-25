package org.maia.swing.animate.itemslide.outline;

import java.awt.Color;
import java.awt.Graphics2D;

public class SolidFillOutlineRenderer implements SlidingItemListOutlineRenderer {

	private Color fillColor;

	public SolidFillOutlineRenderer(Color fillColor) {
		this.fillColor = fillColor;
	}

	@Override
	public void render(Graphics2D g, int width, int height, SlidingItemListOutlineView outlineView) {
		g.setColor(getFillColor());
		g.fillRect(0, 0, width, height);
	}

	public Color getFillColor() {
		return fillColor;
	}

}