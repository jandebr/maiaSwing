package org.maia.swing.animate.itemslide.impl;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.maia.swing.animate.itemslide.SlidingCursorMovement;
import org.maia.swing.animate.itemslide.SlidingItem;
import org.maia.swing.animate.itemslide.SlidingItemLayoutManager;
import org.maia.swing.animate.itemslide.SlidingItemListComponent;
import org.maia.swing.animate.itemslide.SlidingShade;
import org.maia.swing.animate.itemslide.SlidingShadeEnd;
import org.maia.swing.layout.HorizontalAlignment;
import org.maia.swing.layout.VerticalAlignment;

public class SlidingItemLayoutManagerFactory {

	private SlidingItemLayoutManagerFactory() {
	}

	public static SlidingItemLayoutManager createHorizontallySlidingLeftAlignedLayout(
			SlidingItemListComponent component, VerticalAlignment verticalAlignment) {
		return new HorizontallySlidingLayoutManager(component, HorizontalAlignment.LEFT, verticalAlignment);
	}

	public static SlidingItemLayoutManager createHorizontallySlidingCenterAlignedLayout(
			SlidingItemListComponent component, VerticalAlignment verticalAlignment) {
		return new HorizontallySlidingLayoutManager(component, HorizontalAlignment.CENTER, verticalAlignment);
	}

	public static SlidingItemLayoutManager createHorizontallySlidingRightAlignedLayout(
			SlidingItemListComponent component, VerticalAlignment verticalAlignment) {
		return new HorizontallySlidingLayoutManager(component, HorizontalAlignment.RIGHT, verticalAlignment);
	}

	public static SlidingItemLayoutManager createVerticallySlidingTopAlignedLayout(SlidingItemListComponent component,
			HorizontalAlignment horizontalAlignment) {
		return new VerticallySlidingLayoutManager(component, horizontalAlignment, VerticalAlignment.TOP);
	}

	public static SlidingItemLayoutManager createVerticallySlidingCenterAlignedLayout(
			SlidingItemListComponent component, HorizontalAlignment horizontalAlignment) {
		return new VerticallySlidingLayoutManager(component, horizontalAlignment, VerticalAlignment.CENTER);
	}

	public static SlidingItemLayoutManager createVerticallySlidingBottomAlignedLayout(
			SlidingItemListComponent component, HorizontalAlignment horizontalAlignment) {
		return new VerticallySlidingLayoutManager(component, horizontalAlignment, VerticalAlignment.BOTTOM);
	}

	private static abstract class AlignedSlidingItemLayoutManager implements SlidingItemLayoutManager {

		private SlidingItemListComponent component;

		private HorizontalAlignment horizontalAlignment;

		private VerticalAlignment verticalAlignment;

		protected AlignedSlidingItemLayoutManager(SlidingItemListComponent component,
				HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
			if (component == null)
				throw new NullPointerException("Component cannot be null");
			if (horizontalAlignment == null)
				throw new NullPointerException("Horizontal alignment cannot be null");
			if (verticalAlignment == null)
				throw new NullPointerException("Vertical alignment cannot be null");
			this.component = component;
			this.horizontalAlignment = horizontalAlignment;
			this.verticalAlignment = verticalAlignment;
		}

		protected SlidingState getItemStateInLeadingAlignment(SlidingItem item, double vlen, double llen, double ipos,
				double ilen, Graphics2D g) {
			double itra = 0, cpos = 0;
			double t = -ipos + ilen / 2.0; // t <= 0
			SlidingCursorMovement cmove = getCursorMovement();
			if (SlidingCursorMovement.NONE.equals(cmove)) {
				itra = t;
			} else if (SlidingCursorMovement.LAZY.equals(cmove) && llen > vlen) {
				itra = t;
				cpos = -Math.min(0, llen - vlen + t);
				itra += cpos;
			} else {
				cpos = -t;
				itra = -Math.max(0, -t + ilen - vlen);
				cpos += itra;
				double delta = getItemTranslation() - itra;
				if (delta < 0) {
					delta = Math.max(delta, -cpos);
					itra += delta;
					cpos += delta;
				}
			}
			return new SlidingState(itra, cpos, item.getWidth(g), item.getHeight(g), item.getMargin());
		}

		protected SlidingState getItemStateInCenterAlignment(SlidingItem item, double vlen, double llen, double ipos,
				double ilen, Graphics2D g) {
			double itra = 0, cpos = 0;
			double t = -ipos;
			SlidingCursorMovement cmove = getCursorMovement();
			if (SlidingCursorMovement.NONE.equals(cmove)) {
				itra = t;
			} else if (SlidingCursorMovement.LAZY.equals(cmove) && llen > vlen) {
				itra = t;
				cpos = Math.signum(t) * Math.min(0, (llen - vlen) / 2.0 - Math.abs(t));
				itra += cpos;
			} else {
				cpos = -t;
				itra = Math.signum(t) * Math.max(0, Math.abs(t) + ilen / 2.0 - vlen / 2.0);
				cpos += itra;
				double delta = getItemTranslation() - itra;
				if (delta != 0) {
					delta -= Math.signum(delta) * Math.max(0, Math.abs(cpos + delta) + ilen / 2.0 - vlen / 2.0);
					itra += delta;
					cpos += delta;
				}
			}
			return new SlidingState(itra, cpos, item.getWidth(g), item.getHeight(g), item.getMargin());
		}

		protected SlidingState getItemStateInTrailingAlignment(SlidingItem item, double vlen, double llen, double ipos,
				double ilen, Graphics2D g) {
			double itra = 0, cpos = 0;
			double t = -ipos - ilen / 2.0; // t >= 0
			SlidingCursorMovement cmove = getCursorMovement();
			if (SlidingCursorMovement.NONE.equals(cmove)) {
				itra = t;
			} else if (SlidingCursorMovement.LAZY.equals(cmove) && llen > vlen) {
				itra = t;
				cpos = Math.min(0, llen - vlen - t);
				itra += cpos;
			} else {
				cpos = -t;
				itra = Math.max(0, t + ilen - vlen);
				cpos += itra;
				double delta = getItemTranslation() - itra;
				if (delta > 0) {
					delta = Math.min(delta, -cpos);
					itra += delta;
					cpos += delta;
				}
			}
			return new SlidingState(itra, cpos, item.getWidth(g), item.getHeight(g), item.getMargin());
		}

		protected SlidingCursorMovement getCursorMovement() {
			return getComponent().getCursorMovement();
		}

		protected double getCursorPosition() {
			return getComponent().getState().getCursorPosition();
		}

		protected double getItemTranslation() {
			return getComponent().getState().getItemTranslation();
		}

		protected double getMaxItemWidth(Graphics2D g) {
			return getComponent().getMaxItemWidth(g);
		}

		protected double getMaxItemHeight(Graphics2D g) {
			return getComponent().getMaxItemHeight(g);
		}

		protected double getViewportWidth() {
			return getComponent().getViewportWidth();
		}

		protected double getViewportHeight() {
			return getComponent().getViewportHeight();
		}

		protected abstract int getLength(SlidingItemList itemList, Graphics2D g);

		protected HorizontalAlignment getHorizontalAlignment() {
			return horizontalAlignment;
		}

		protected VerticalAlignment getVerticalAlignment() {
			return verticalAlignment;
		}

		protected SlidingItemListComponent getComponent() {
			return component;
		}

	}

	private static class HorizontallySlidingLayoutManager extends AlignedSlidingItemLayoutManager {

		public HorizontallySlidingLayoutManager(SlidingItemListComponent component,
				HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
			super(component, horizontalAlignment, verticalAlignment);
		}

		@Override
		public void layoutItems(SlidingItemList itemList, Graphics2D g) {
			double position = computePositionOfFirstItem(itemList, g);
			for (int i = 0; i < itemList.getItemCount(); i++) {
				SlidingItemInList itemInList = itemList.getItem(i);
				SlidingItem item = itemInList.getItem();
				if (i > 0) {
					position += item.getMargin().left;
					position += item.getWidth(g) / 2.0;
				}
				itemInList.setPosition(position);
				position += item.getWidth(g) / 2.0;
				position += item.getMargin().right;
			}
		}

		@Override
		public Rectangle2D getItemBounds(SlidingItemInList itemInList, Graphics2D g) {
			SlidingItem item = itemInList.getItem();
			double xc = itemInList.getPosition() + getItemTranslation();
			double yc = getVerticalShift(item.getHeight(g), g);
			double width = item.getWidth(g);
			double height = item.getHeight(g);
			return new Rectangle2D.Double(xc - width / 2.0, yc - height / 2.0, width, height);
		}

		@Override
		public Rectangle getItemBoundsInViewportCoords(SlidingItemInList itemInList, Graphics2D g) {
			Rectangle2D bounds = getItemBounds(itemInList, g);
			int x = (int) Math.round(getViewportCoordinateOfPositionZero() + bounds.getMinX());
			int y = (int) Math.round(getViewportHeight() / 2.0 + bounds.getMinY());
			int width = itemInList.getItem().getWidth(g);
			int height = itemInList.getItem().getHeight(g);
			return new Rectangle(x, y, width, height);
		}

		@Override
		public SlidingState getItemState(SlidingItemInList itemInList, Graphics2D g) {
			SlidingState state = null;
			SlidingItem item = itemInList.getItem();
			double vlen = getViewportWidth();
			double llen = getLength(itemInList.getList(), g);
			double ipos = itemInList.getPosition();
			double ilen = item.getWidth(g);
			HorizontalAlignment align = getHorizontalAlignment();
			if (HorizontalAlignment.LEFT.equals(align)) {
				state = getItemStateInLeadingAlignment(item, vlen, llen, ipos, ilen, g);
			} else if (HorizontalAlignment.CENTER.equals(align)) {
				state = getItemStateInCenterAlignment(item, vlen, llen, ipos, ilen, g);
			} else if (HorizontalAlignment.RIGHT.equals(align)) {
				state = getItemStateInTrailingAlignment(item, vlen, llen, ipos, ilen, g);
			}
			return state;
		}

		@Override
		public Rectangle getCursorInnerBoundsInViewportCoords(SlidingState state, Graphics2D g) {
			int x = (int) Math.round(getViewportCoordinateOfPositionZero() + getCursorCenterPosition(state)
					- state.getCursorWidth() / 2.0);
			int y = (int) Math.round(getViewportHeight() / 2.0 - state.getCursorHeight() / 2
					+ getVerticalShift(state.getCursorHeight(), g));
			int width = (int) Math.round(state.getCursorWidth());
			int height = (int) Math.round(state.getCursorHeight());
			return new Rectangle(x, y, width, height);
		}

		@Override
		public double getCursorCenterPosition(SlidingState state) {
			double center = state.getCursorPosition();
			HorizontalAlignment align = getHorizontalAlignment();
			if (HorizontalAlignment.LEFT.equals(align)) {
				center += state.getCursorWidth() / 2.0;
			} else if (HorizontalAlignment.RIGHT.equals(align)) {
				center -= state.getCursorWidth() / 2.0;
			}
			return center;
		}

		@Override
		public Rectangle getShadeBoundsInViewportCoords(SlidingShade shade, SlidingShadeEnd end,
				int shadeExteriorLength) {
			int width = shade.getWidth();
			int height = getComponent().getViewportHeight();
			if (SlidingShadeEnd.LEADING.equals(end)) {
				return new Rectangle(-shadeExteriorLength, 0, width, height);
			} else {
				return new Rectangle(getComponent().getViewportWidth() - width + shadeExteriorLength, 0, width, height);
			}
		}

		@Override
		public AffineTransform getTrailingShadeTransform(SlidingShade shade) {
			AffineTransform t = AffineTransform.getTranslateInstance(shade.getWidth(), 0);
			t.concatenate(AffineTransform.getScaleInstance(-1.0, 1.0));
			return t;
		}

		@Override
		public boolean isHorizontalLayout() {
			return true;
		}

		@Override
		protected int getLength(SlidingItemList itemList, Graphics2D g) {
			int length = 0;
			int n = itemList.getItemCount();
			for (int i = 0; i < n; i++) {
				SlidingItem item = itemList.getItem(i).getItem();
				length += item.getWidth(g);
				if (i > 0) {
					length += item.getMargin().left;
				}
				if (i < n - 1) {
					length += item.getMargin().right;
				}
			}
			return length;
		}

		private double computePositionOfFirstItem(SlidingItemList itemList, Graphics2D g) {
			double position = 0;
			if (!itemList.isEmpty()) {
				SlidingItem item = itemList.getFirstItem().getItem();
				double cx = item.getWidth(g) / 2.0;
				HorizontalAlignment align = getHorizontalAlignment();
				if (HorizontalAlignment.LEFT.equals(align)) {
					position = cx;
				} else if (HorizontalAlignment.CENTER.equals(align)) {
					position = cx - getLength(itemList, g) / 2.0;
				} else if (HorizontalAlignment.RIGHT.equals(align)) {
					position = cx - getLength(itemList, g);
				}
			}
			return position;
		}

		private double getVerticalShift(double height, Graphics2D g) {
			double d = 0;
			double maxHeight = getMaxItemHeight(g);
			VerticalAlignment align = getVerticalAlignment();
			if (VerticalAlignment.TOP.equals(align)) {
				d = (height - maxHeight) / 2.0;
			} else if (VerticalAlignment.CENTER.equals(align)) {
				d = 0;
			} else if (VerticalAlignment.BOTTOM.equals(align)) {
				d = (maxHeight - height) / 2.0;
			}
			return d;
		}

		private double getViewportCoordinateOfPositionZero() {
			double c = 0;
			HorizontalAlignment align = getHorizontalAlignment();
			if (HorizontalAlignment.LEFT.equals(align)) {
				c = 0;
			} else if (HorizontalAlignment.CENTER.equals(align)) {
				c = getViewportWidth() / 2.0;
			} else if (HorizontalAlignment.RIGHT.equals(align)) {
				c = getViewportWidth();
			}
			return c;
		}

	}

	private static class VerticallySlidingLayoutManager extends AlignedSlidingItemLayoutManager {

		public VerticallySlidingLayoutManager(SlidingItemListComponent component,
				HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
			super(component, horizontalAlignment, verticalAlignment);
		}

		@Override
		public void layoutItems(SlidingItemList itemList, Graphics2D g) {
			double position = computePositionOfFirstItem(itemList, g);
			for (int i = 0; i < itemList.getItemCount(); i++) {
				SlidingItemInList itemInList = itemList.getItem(i);
				SlidingItem item = itemInList.getItem();
				if (i > 0) {
					position += item.getMargin().top;
					position += item.getHeight(g) / 2.0;
				}
				itemInList.setPosition(position);
				position += item.getHeight(g) / 2.0;
				position += item.getMargin().bottom;
			}
		}

		@Override
		public Rectangle2D getItemBounds(SlidingItemInList itemInList, Graphics2D g) {
			SlidingItem item = itemInList.getItem();
			double xc = getHorizontalShift(item.getWidth(g), g);
			double yc = itemInList.getPosition() + getItemTranslation();
			double width = item.getWidth(g);
			double height = item.getHeight(g);
			return new Rectangle2D.Double(xc - width / 2.0, yc - height / 2.0, width, height);
		}

		@Override
		public Rectangle getItemBoundsInViewportCoords(SlidingItemInList itemInList, Graphics2D g) {
			Rectangle2D bounds = getItemBounds(itemInList, g);
			int x = (int) Math.round(getViewportWidth() / 2.0 + bounds.getMinX());
			int y = (int) Math.round(getViewportCoordinateOfPositionZero() + bounds.getMinY());
			int width = itemInList.getItem().getWidth(g);
			int height = itemInList.getItem().getHeight(g);
			return new Rectangle(x, y, width, height);
		}

		@Override
		public SlidingState getItemState(SlidingItemInList itemInList, Graphics2D g) {
			SlidingState state = null;
			SlidingItem item = itemInList.getItem();
			double vlen = getViewportHeight();
			double llen = getLength(itemInList.getList(), g);
			double ipos = itemInList.getPosition();
			double ilen = item.getHeight(g);
			VerticalAlignment align = getVerticalAlignment();
			if (VerticalAlignment.TOP.equals(align)) {
				state = getItemStateInLeadingAlignment(item, vlen, llen, ipos, ilen, g);
			} else if (VerticalAlignment.CENTER.equals(align)) {
				state = getItemStateInCenterAlignment(item, vlen, llen, ipos, ilen, g);
			} else if (VerticalAlignment.BOTTOM.equals(align)) {
				state = getItemStateInTrailingAlignment(item, vlen, llen, ipos, ilen, g);
			}
			return state;
		}

		@Override
		public Rectangle getCursorInnerBoundsInViewportCoords(SlidingState state, Graphics2D g) {
			int x = (int) Math.round(getViewportWidth() / 2.0 - state.getCursorWidth() / 2
					+ getHorizontalShift(state.getCursorWidth(), g));
			int y = (int) Math.round(getViewportCoordinateOfPositionZero() + getCursorCenterPosition(state)
					- state.getCursorHeight() / 2.0);
			int width = (int) Math.round(state.getCursorWidth());
			int height = (int) Math.round(state.getCursorHeight());
			return new Rectangle(x, y, width, height);
		}

		@Override
		public double getCursorCenterPosition(SlidingState state) {
			double center = state.getCursorPosition();
			VerticalAlignment align = getVerticalAlignment();
			if (VerticalAlignment.TOP.equals(align)) {
				center += state.getCursorHeight() / 2.0;
			} else if (VerticalAlignment.BOTTOM.equals(align)) {
				center -= state.getCursorHeight() / 2.0;
			}
			return center;
		}

		@Override
		public Rectangle getShadeBoundsInViewportCoords(SlidingShade shade, SlidingShadeEnd end,
				int shadeExteriorLength) {
			int width = getComponent().getViewportWidth();
			int height = shade.getHeight();
			if (SlidingShadeEnd.LEADING.equals(end)) {
				return new Rectangle(0, -shadeExteriorLength, width, height);
			} else {
				return new Rectangle(0, getComponent().getViewportHeight() - height + shadeExteriorLength, width,
						height);
			}
		}

		@Override
		public AffineTransform getTrailingShadeTransform(SlidingShade shade) {
			AffineTransform t = AffineTransform.getTranslateInstance(0, shade.getHeight());
			t.concatenate(AffineTransform.getScaleInstance(1.0, -1.0));
			return t;
		}

		@Override
		public boolean isHorizontalLayout() {
			return false;
		}

		@Override
		protected int getLength(SlidingItemList itemList, Graphics2D g) {
			int length = 0;
			int n = itemList.getItemCount();
			for (int i = 0; i < n; i++) {
				SlidingItem item = itemList.getItem(i).getItem();
				length += item.getHeight(g);
				if (i > 0) {
					length += item.getMargin().top;
				}
				if (i < n - 1) {
					length += item.getMargin().bottom;
				}
			}
			return length;
		}

		private double computePositionOfFirstItem(SlidingItemList itemList, Graphics2D g) {
			double position = 0;
			if (!itemList.isEmpty()) {
				SlidingItem item = itemList.getFirstItem().getItem();
				double cy = item.getHeight(g) / 2.0;
				VerticalAlignment align = getVerticalAlignment();
				if (VerticalAlignment.TOP.equals(align)) {
					position = cy;
				} else if (VerticalAlignment.CENTER.equals(align)) {
					position = cy - getLength(itemList, g) / 2.0;
				} else if (VerticalAlignment.BOTTOM.equals(align)) {
					position = cy - getLength(itemList, g);
				}
			}
			return position;
		}

		private double getHorizontalShift(double width, Graphics2D g) {
			double d = 0;
			double maxWidth = getMaxItemWidth(g);
			HorizontalAlignment align = getHorizontalAlignment();
			if (HorizontalAlignment.LEFT.equals(align)) {
				d = (width - maxWidth) / 2.0;
			} else if (HorizontalAlignment.CENTER.equals(align)) {
				d = 0;
			} else if (HorizontalAlignment.RIGHT.equals(align)) {
				d = (maxWidth - width) / 2.0;
			}
			return d;
		}

		private double getViewportCoordinateOfPositionZero() {
			double c = 0;
			VerticalAlignment align = getVerticalAlignment();
			if (VerticalAlignment.TOP.equals(align)) {
				c = 0;
			} else if (VerticalAlignment.CENTER.equals(align)) {
				c = getViewportHeight() / 2.0;
			} else if (VerticalAlignment.BOTTOM.equals(align)) {
				c = getViewportHeight();
			}
			return c;
		}

	}

}