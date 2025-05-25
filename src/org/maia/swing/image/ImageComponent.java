package org.maia.swing.image;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.maia.graphics2d.image.ImageUtils;
import org.maia.swing.layout.FillMode;
import org.maia.swing.layout.HorizontalAlignment;
import org.maia.swing.layout.InnerRegionLayout;
import org.maia.swing.layout.VerticalAlignment;

@SuppressWarnings("serial")
public class ImageComponent extends JComponent {

	private Image image;

	private boolean imageOpaque;

	private InnerRegionLayout imageLayout = new InnerRegionLayout();

	private Insets insetsStandIn = new Insets(0, 0, 0, 0); // reusable object

	public static Color defaultBackground = Color.GRAY;

	public ImageComponent(BufferedImage image) {
		this(image, defaultBackground);
	}

	public ImageComponent(BufferedImage image, Color background) {
		this(image, ImageUtils.isFullyOpaque(image), background);
	}

	public ImageComponent(Image image, boolean imageOpaque) {
		this(image, imageOpaque, defaultBackground);
	}

	public ImageComponent(Image image, boolean imageOpaque, Color background) {
		setImage(image);
		setImageOpaque(imageOpaque);
		setBackground(background);
		setPreferredSize(getImageSize());
		setOpaque(true);
	}

	public void refreshUI() {
		if (isShowing())
			repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		updateLayout();
		if (isBackgroundPaintNecessary()) {
			paintBackground(g);
		}
		paintImage(g);
	}

	protected void paintBackground(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	protected void paintImage(Graphics g) {
		Image image = getImage();
		Rectangle bounds = getImageLayout().getInnerRegionLayoutBounds();
		if (getImageLayout().isUnityScale()) {
			g.drawImage(image, bounds.x, bounds.y, null);
		} else {
			g.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height, null);
		}
	}

	private void updateLayout() {
		InnerRegionLayout layout = getImageLayout();
		layout.setOuterRegionSize(getSize());
		layout.setOuterRegionInsets(getInsets(insetsStandIn));
		layout.setInnerRegionSize(getImageSize());
	}

	private boolean isBackgroundPaintNecessary() {
		if (!isImageOpaque())
			return true;
		if (getBorder() != null && !getBorder().isBorderOpaque())
			return true;
		return !getImageLayout().isAvailableSizeFullyCovered();
	}

	public Dimension getImageSize() {
		return ImageUtils.getSize(getImage());
	}

	public FillMode getFillMode() {
		return getImageLayout().getFillMode();
	}

	public void setFillMode(FillMode fillMode) {
		getImageLayout().setFillMode(fillMode);
		refreshUI();
	}

	public HorizontalAlignment getHorizontalAlignment() {
		return getImageLayout().getHorizontalAlignment();
	}

	public void setHorizontalAlignment(HorizontalAlignment hAlign) {
		getImageLayout().setHorizontalAlignment(hAlign);
		refreshUI();
	}

	public VerticalAlignment getVerticalAlignment() {
		return getImageLayout().getVerticalAlignment();
	}

	public void setVerticalAlignment(VerticalAlignment vAlign) {
		getImageLayout().setVerticalAlignment(vAlign);
		refreshUI();
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		if (image == null)
			throw new NullPointerException("image is null");
		this.image = image;
		refreshUI();
	}

	public boolean isImageOpaque() {
		return imageOpaque;
	}

	public void setImageOpaque(boolean opaque) {
		this.imageOpaque = opaque;
		refreshUI();
	}

	private InnerRegionLayout getImageLayout() {
		return imageLayout;
	}

}