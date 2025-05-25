package org.maia.swing.layout;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

public class InnerRegionLayout {

	private Dimension outerRegionSize;

	private Dimension innerRegionSize;

	private Insets outerRegionInsets;

	private FillMode fillMode = FillMode.NONE;

	private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;

	private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;

	private Rectangle innerRegionLayoutBounds; // Computed layout

	public InnerRegionLayout() {
		this(new Dimension(), new Dimension());
	}

	public InnerRegionLayout(Dimension outerRegionSize, Dimension innerRegionSize) {
		this(outerRegionSize, innerRegionSize, new Insets(0, 0, 0, 0));
	}

	public InnerRegionLayout(Dimension outerRegionSize, Dimension innerRegionSize, Insets outerRegionInsets) {
		this.outerRegionSize = outerRegionSize;
		this.innerRegionSize = innerRegionSize;
		this.outerRegionInsets = outerRegionInsets;
	}

	private void invalidateLayout() {
		setInnerRegionLayoutBounds(null);
	}

	private Rectangle computeInnerRegionLayoutBounds() {
		Rectangle bounds = null;
		Dimension size = getAvailableSize();
		if (size.equals(getInnerRegionSize())) {
			bounds = new Rectangle(size);
		} else if (FillMode.STRETCH.equals(getFillMode())) {
			bounds = new Rectangle(size);
		} else {
			Dimension sis = getScaledInnerRegionSize();
			int w = sis.width;
			int h = sis.height;
			int x0 = 0;
			if (HorizontalAlignment.CENTER.equals(getHorizontalAlignment())) {
				x0 = (size.width - w) / 2;
			} else if (HorizontalAlignment.RIGHT.equals(getHorizontalAlignment())) {
				x0 = size.width - w;
			}
			int y0 = 0;
			if (VerticalAlignment.CENTER.equals(getVerticalAlignment())) {
				y0 = (size.height - h) / 2;
			} else if (VerticalAlignment.BOTTOM.equals(getVerticalAlignment())) {
				y0 = size.height - h;
			}
			bounds = new Rectangle(x0, y0, w, h);
		}
		bounds.x += getOuterRegionInsets().left;
		bounds.y += getOuterRegionInsets().top;
		return bounds;
	}

	private Dimension getScaledInnerRegionSize() {
		FillMode mode = getFillMode();
		Dimension size = getAvailableSize();
		if (FillMode.STRETCH.equals(mode)) {
			return size;
		} else {
			Dimension is = getInnerRegionSize();
			int w = is.width;
			int h = is.height;
			if (FillMode.FIT.equals(mode) || FillMode.FIT_DOWNSCALE.equals(mode) || FillMode.FIT_UPSCALE.equals(mode)) {
				double s = Math.min(size.getWidth() / w, size.getHeight() / h);
				if (FillMode.FIT_DOWNSCALE.equals(mode)) {
					s = Math.min(s, 1.0);
				} else if (FillMode.FIT_UPSCALE.equals(mode)) {
					s = Math.max(s, 1.0);
				}
				w = (int) Math.round(s * w);
				h = (int) Math.round(s * h);
			}
			return new Dimension(w, h);
		}
	}

	public Dimension getAvailableSize() {
		Dimension size = getOuterRegionSize();
		Insets insets = getOuterRegionInsets();
		if (insets.top == 0 && insets.bottom == 0 && insets.left == 0 && insets.right == 0) {
			return size;
		} else {
			return new Dimension(size.width - insets.left - insets.right, size.height - insets.top - insets.bottom);
		}
	}

	public boolean isAvailableSizeFullyCovered() {
		Dimension avail = getAvailableSize();
		Dimension region = getInnerRegionLayoutBounds().getSize();
		return region.width >= avail.width && region.height >= avail.height;
	}

	public boolean isUnityScale() {
		return getInnerRegionSize().equals(getInnerRegionLayoutBounds().getSize());
	}

	public Dimension getOuterRegionSize() {
		return outerRegionSize;
	}

	public void setOuterRegionSize(Dimension size) {
		if (size == null)
			size = new Dimension();
		if (!this.outerRegionSize.equals(size))
			invalidateLayout();
		this.outerRegionSize = size;
	}

	public Dimension getInnerRegionSize() {
		return innerRegionSize;
	}

	public void setInnerRegionSize(Dimension size) {
		if (size == null)
			size = new Dimension();
		if (!this.innerRegionSize.equals(size))
			invalidateLayout();
		this.innerRegionSize = size;
	}

	public Insets getOuterRegionInsets() {
		return outerRegionInsets;
	}

	public void setOuterRegionInsets(Insets insets) {
		if (insets == null)
			insets = new Insets(0, 0, 0, 0);
		if (!this.outerRegionInsets.equals(insets))
			invalidateLayout();
		this.outerRegionInsets = insets;
	}

	public FillMode getFillMode() {
		return fillMode;
	}

	public void setFillMode(FillMode fillMode) {
		if (fillMode == null)
			throw new NullPointerException("fill mode is null");
		if (!this.fillMode.equals(fillMode))
			invalidateLayout();
		this.fillMode = fillMode;
	}

	public HorizontalAlignment getHorizontalAlignment() {
		return horizontalAlignment;
	}

	public void setHorizontalAlignment(HorizontalAlignment hAlignment) {
		if (hAlignment == null)
			throw new NullPointerException("horizontal alignment is null");
		if (!this.horizontalAlignment.equals(hAlignment))
			invalidateLayout();
		this.horizontalAlignment = hAlignment;
	}

	public VerticalAlignment getVerticalAlignment() {
		return verticalAlignment;
	}

	public void setVerticalAlignment(VerticalAlignment vAlignment) {
		if (vAlignment == null)
			throw new NullPointerException("vertical alignment is null");
		if (!this.verticalAlignment.equals(vAlignment))
			invalidateLayout();
		this.verticalAlignment = vAlignment;
	}

	public Rectangle getInnerRegionLayoutBounds() {
		if (innerRegionLayoutBounds == null) {
			innerRegionLayoutBounds = computeInnerRegionLayoutBounds();
		}
		return innerRegionLayoutBounds;
	}

	private void setInnerRegionLayoutBounds(Rectangle bounds) {
		this.innerRegionLayoutBounds = bounds;
	}

}