package org.maia.swing.animate.itemslide;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.maia.swing.animate.itemslide.impl.SlidingItemInList;
import org.maia.swing.animate.itemslide.impl.SlidingItemList;
import org.maia.swing.animate.itemslide.impl.SlidingState;

public interface SlidingItemLayoutManager {

	abstract void layoutItems(SlidingItemList itemList, Graphics2D g);

	abstract Rectangle2D getItemBounds(SlidingItemInList itemInList, Graphics2D g);

	abstract Rectangle getItemBoundsInViewportCoords(SlidingItemInList itemInList, Graphics2D g);

	abstract SlidingState getItemState(SlidingItemInList itemInList, Graphics2D g);

	abstract Rectangle getCursorInnerBoundsInViewportCoords(SlidingState state, Graphics2D g);

	abstract double getCursorCenterPosition(SlidingState state);

	abstract Rectangle getShadeBoundsInViewportCoords(SlidingShade shade, SlidingShadeEnd end, int shadeExteriorLength);

	abstract AffineTransform getTrailingShadeTransform(SlidingShade shade);

	abstract boolean isHorizontalLayout();

}