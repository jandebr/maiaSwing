package org.maia.swing.animate.itemslide.outline;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.border.Border;

import org.maia.swing.animate.itemslide.SlidingItemListAdapter;
import org.maia.swing.animate.itemslide.SlidingItemListComponent;
import org.maia.swing.animate.itemslide.outline.SlidingItemListOutline.Range;
import org.maia.swing.border.ClippingBorder;
import org.maia.util.ColorUtils;

@SuppressWarnings("serial")
public class SlidingItemListOutlineView extends JComponent {

	private SlidingItemListComponent component;

	private Insets margin;

	private Color slidingLaneColor;

	private Insets extentMargin;

	private Border extentBorder;

	private Insets cursorMargin;

	private Border cursorBorder;

	private SlidingItemListOutlineRenderer extentRenderer;

	private SlidingItemListOutlineRenderer cursorRenderer;

	public SlidingItemListOutlineView(SlidingItemListComponent component, int thickness, Color slidingLaneColor) {
		this(component, component.isHorizontalLayout() ? component.getWidth() : component.getHeight(), thickness,
				deriveMargin(component), slidingLaneColor);
	}

	public SlidingItemListOutlineView(SlidingItemListComponent component, int length, int thickness,
			Color slidingLaneColor) {
		this(component, length, thickness, new Insets(0, 0, 0, 0), slidingLaneColor);
	}

	public SlidingItemListOutlineView(SlidingItemListComponent component, int length, int thickness, Insets margin,
			Color slidingLaneColor) {
		this.component = component;
		this.margin = margin;
		this.slidingLaneColor = slidingLaneColor;
		this.extentMargin = new Insets(0, 0, 0, 0);
		this.cursorMargin = new Insets(0, 0, 0, 0);
		this.extentRenderer = createDefaultExtentRenderer();
		this.cursorRenderer = createDefaultCursorRenderer();
		component.addListener(new SlidingItemListObserver());
		Dimension size = deriveSize(component, length, thickness);
		setMinimumSize(size);
		setPreferredSize(size);
		setMaximumSize(size);
		setSize(size);
		setBackground(component.getBackground());
		setOpaque(true);
	}

	private static Dimension deriveSize(SlidingItemListComponent component, int length, int thickness) {
		int width = 0, height = 0;
		if (component.isHorizontalLayout()) {
			width = length;
			height = thickness;
		} else {
			width = thickness;
			height = length;
		}
		return new Dimension(width, height);
	}

	private static Insets deriveMargin(SlidingItemListComponent component) {
		Insets margin = (Insets) component.getPadding().clone();
		if (component.isHorizontalLayout()) {
			margin.top = 0;
			margin.bottom = 0;
		} else {
			margin.left = 0;
			margin.right = 0;
		}
		return margin;
	}

	protected SlidingItemListOutlineRenderer createDefaultExtentRenderer() {
		return new SolidFillOutlineRenderer(ColorUtils.adjustBrightness(getComponent().getBackground(), 0.2));
	}

	protected SlidingItemListOutlineRenderer createDefaultCursorRenderer() {
		return new SolidFillOutlineRenderer(SlidingItemListComponent.defaultCursorColor);
	}

	public void refreshUI() {
		if (isShowing())
			repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		paintBackground(g);
		Insets margin = getMarginAdjustedByBorder();
		int width = getWidth() - margin.left - margin.right;
		int height = getHeight() - margin.top - margin.bottom;
		Graphics2D g2 = (Graphics2D) g.create(margin.left, margin.top, width, height);
		clipGraphics(g2, getBorder(), 0, 0, width, height);
		paintSlidingLane(g2, width, height);
		SlidingItemListOutline outline = getComponent().getOutline();
		if (outline != null) {
			paintOutline(g2, outline, width, height);
		}
		g2.dispose();
	}

	protected void paintBackground(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	protected void paintSlidingLane(Graphics2D g, int width, int height) {
		g.setColor(getSlidingLaneColor());
		g.fillRect(0, 0, width, height);
	}

	protected void paintOutline(Graphics2D g, SlidingItemListOutline outline, int width, int height) {
		Rectangle outlineBounds = new Rectangle(width, height);
		Rectangle extentBounds = createSubRegion(
				subtractInsets(subtractBorderInsets(outlineBounds, getBorder()), getExtentMargin()),
				outline.getViewportToListRange());
		Rectangle cursorBounds = createSubRegion(
				subtractInsets(subtractBorderInsets(extentBounds, getExtentBorder()), getCursorMargin()),
				outline.getCursorToViewportRange());
		paintOutline(g, extentBounds, cursorBounds);
	}

	protected void paintOutline(Graphics2D g, Rectangle extentBounds, Rectangle cursorBounds) {
		int extentWidth = extentBounds.width;
		int extentHeight = extentBounds.height;
		int cursorWidth = cursorBounds.width;
		int cursorHeight = cursorBounds.height;
		Graphics2D gExtent = (Graphics2D) g.create(extentBounds.x, extentBounds.y, extentWidth, extentHeight);
		Graphics2D gExtentClip = (Graphics2D) gExtent.create();
		clipGraphics(gExtentClip, getExtentBorder(), 0, 0, extentWidth, extentHeight);
		paintExtent(gExtentClip, extentWidth, extentHeight);
		Graphics2D gCursor = (Graphics2D) gExtentClip.create(); // cursor inherits extent clip
		gCursor.translate(cursorBounds.x - extentBounds.x, cursorBounds.y - extentBounds.y);
		Graphics2D gCursorClip = (Graphics2D) gCursor.create();
		if (getCursorBorder() != null) {
			clipGraphics(gCursorClip, getCursorBorder(), 0, 0, cursorWidth, cursorHeight);
		}
		paintCursor(gCursorClip, cursorWidth, cursorHeight);
		paintCursorBorder(gCursor, cursorWidth, cursorHeight);
		paintExtentBorder(gExtent, extentWidth, extentHeight);
		gCursorClip.dispose();
		gCursor.dispose();
		gExtentClip.dispose();
		gExtent.dispose();
	}

	protected void paintExtent(Graphics2D g, int width, int height) {
		if (getExtentRenderer() != null) {
			getExtentRenderer().render(g, width, height, this);
		}
	}

	protected void paintCursor(Graphics2D g, int width, int height) {
		if (getCursorRenderer() != null) {
			getCursorRenderer().render(g, width, height, this);
		}
	}

	protected void paintExtentBorder(Graphics2D g, int width, int height) {
		paintBorder(g, getExtentBorder(), width, height);
	}

	protected void paintCursorBorder(Graphics2D g, int width, int height) {
		paintBorder(g, getCursorBorder(), width, height);
	}

	@Override
	protected void paintBorder(Graphics g) {
		Border border = getBorder();
		if (border != null) {
			Insets margin = getMarginAdjustedByBorder();
			int width = getWidth() - margin.left - margin.right;
			int height = getHeight() - margin.top - margin.bottom;
			Graphics2D g2 = (Graphics2D) g.create(margin.left, margin.top, width, height);
			paintBorder(g2, border, width, height);
			g2.dispose();
		}
	}

	protected void paintBorder(Graphics2D g, Border border, int width, int height) {
		if (border != null) {
			border.paintBorder(this, g, 0, 0, width, height);
		}
	}

	private void clipGraphics(Graphics2D g, Border border, int x, int y, int width, int height) {
		if (border != null) {
			if (border instanceof ClippingBorder) {
				g.clip(((ClippingBorder) border).getInteriorClip(this, g, x, y, width, height));
			} else {
				Insets insets = border.getBorderInsets(this);
				g.clipRect(x + insets.left, y + insets.top, width - insets.left - insets.right,
						height - insets.top - insets.bottom);
			}
		} else {
			g.clipRect(x, y, width, height);
		}
	}

	private Rectangle createSubRegion(Rectangle region, Range range) {
		if (isHorizontalLayout()) {
			int x0 = region.x + (int) Math.round(region.width * range.getStart());
			int x1 = region.x + (int) Math.round(region.width * range.getEnd());
			return new Rectangle(x0, region.y, x1 - x0, region.height);
		} else {
			int y0 = region.y + (int) Math.round(region.height * range.getStart());
			int y1 = region.y + (int) Math.round(region.height * range.getEnd());
			return new Rectangle(region.x, y0, region.width, y1 - y0);
		}
	}

	private Rectangle subtractBorderInsets(Rectangle region, Border border) {
		if (border != null) {
			return subtractInsets(region, border.getBorderInsets(this));
		} else {
			return region;
		}
	}

	private Rectangle subtractInsets(Rectangle region, Insets insets) {
		return new Rectangle(region.x + insets.left, region.y + insets.top, region.width - insets.left - insets.right,
				region.height - insets.top - insets.bottom);
	}

	private Insets getMarginAdjustedByBorder() {
		Insets margin = getMargin();
		Border border = getBorder();
		if (border != null) {
			Insets insets = border.getBorderInsets(this);
			return new Insets(Math.max(margin.top - insets.top, 0), Math.max(margin.left - insets.left, 0),
					Math.max(margin.bottom - insets.bottom, 0), Math.max(margin.right - insets.right, 0));
		} else {
			return margin;
		}
	}

	public boolean isHorizontalLayout() {
		return getComponent().isHorizontalLayout();
	}

	public boolean isVerticalLayout() {
		return !isHorizontalLayout();
	}

	public SlidingItemListComponent getComponent() {
		return component;
	}

	public Insets getMargin() {
		return margin;
	}

	public Color getSlidingLaneColor() {
		return slidingLaneColor;
	}

	public void setSlidingLaneColor(Color color) {
		this.slidingLaneColor = color;
		refreshUI();
	}

	public Insets getExtentMargin() {
		return extentMargin;
	}

	public void setExtentMargin(Insets margin) {
		this.extentMargin = margin;
		refreshUI();
	}

	public Border getExtentBorder() {
		return extentBorder;
	}

	public void setExtentBorder(Border border) {
		this.extentBorder = border;
		refreshUI();
	}

	public Insets getCursorMargin() {
		return cursorMargin;
	}

	public void setCursorMargin(Insets margin) {
		this.cursorMargin = margin;
		refreshUI();
	}

	public Border getCursorBorder() {
		return cursorBorder;
	}

	public void setCursorBorder(Border border) {
		this.cursorBorder = border;
		refreshUI();
	}

	public SlidingItemListOutlineRenderer getExtentRenderer() {
		return extentRenderer;
	}

	public void setExtentRenderer(SlidingItemListOutlineRenderer extentRenderer) {
		this.extentRenderer = extentRenderer;
		refreshUI();
	}

	public SlidingItemListOutlineRenderer getCursorRenderer() {
		return cursorRenderer;
	}

	public void setCursorRenderer(SlidingItemListOutlineRenderer cursorRenderer) {
		this.cursorRenderer = cursorRenderer;
		refreshUI();
	}

	private class SlidingItemListObserver extends SlidingItemListAdapter {

		public SlidingItemListObserver() {
		}

		@Override
		public void notifyItemsChanged(SlidingItemListComponent component) {
			refreshUI();
		}

		@Override
		public void notifySlidingStateChanged(SlidingItemListComponent component) {
			refreshUI();
		}

	}

}