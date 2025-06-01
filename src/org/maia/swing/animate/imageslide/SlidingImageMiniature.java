package org.maia.swing.animate.imageslide;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import org.maia.graphics2d.image.ImageUtils;
import org.maia.util.ColorUtils;

@SuppressWarnings("serial")
public class SlidingImageMiniature extends JPanel {

	private Insets padding;

	private Image image;

	private Dimension viewportSize;

	private Color viewportColor;

	private SlidingImageState state;

	public SlidingImageMiniature(Dimension size, SlidingImageComponent component) {
		this(size, component.getImage(), component.getUI().getSize());
	}

	public SlidingImageMiniature(Dimension size, Image image, Dimension viewportSize) {
		this(size, new Insets(size.height / 10, size.width / 10, size.height / 10, size.width / 10), image,
				viewportSize);
	}

	public SlidingImageMiniature(Dimension size, Insets padding, Image image, Dimension viewportSize) {
		this(size, Color.BLACK, padding, image, viewportSize, Color.WHITE);
	}

	public SlidingImageMiniature(Dimension size, Color background, Insets padding, Image image, Dimension viewportSize,
			Color viewportColor) {
		super(new BorderLayout(), true);
		setMinimumSize(size);
		setPreferredSize(size);
		setMaximumSize(size);
		setSize(size);
		setBackground(background);
		this.padding = padding;
		this.image = image;
		this.viewportSize = viewportSize;
		this.viewportColor = viewportColor;
	}

	public void update(SlidingImageState state) {
		setState(state);
		refreshUI();
	}

	public void refreshUI() {
		if (isShowing())
			repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		paintBackground(g2);
		paintCorner(g2);
		Image image = getImage();
		if (image != null) {
			paintImage(g2, image);
			paintImageOverlay(g2);
			SlidingImageState state = getState();
			if (state != null) {
				paintViewport(g2, image, state, getViewportColor());
			}
		}
	}

	private void paintBackground(Graphics2D g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	private void paintCorner(Graphics2D g) {
		g.setColor(ColorUtils.adjustBrightness(getBackground(), 0.1f));
		int h = getHeight();
		int t = Math.min((int) Math.round(h * 0.1), 16);
		g.fillPolygon(new int[] { 0, 0, t }, new int[] { h - t, h, h }, 3);
	}

	private void paintImage(Graphics2D g, Image image) {
		double s = getImageScaling(image);
		double cx = ImageUtils.getWidth(image) / 2.0;
		double cy = ImageUtils.getHeight(image) / 2.0;
		Graphics2D g2 = (Graphics2D) g.create();
		g2.translate(getWidth() / 2, getHeight() / 2);
		g2.scale(s, s);
		g2.translate(-cx, -cy);
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
	}

	private void paintImageOverlay(Graphics2D g) {
		Insets pad = getPadding();
		int innerWidth = getWidth() - pad.left - pad.right;
		int innerHeight = getHeight() - pad.top - pad.bottom;
		g.setColor(ColorUtils.setTransparency(getBackground(), 0.4f));
		g.fillRect(pad.left, pad.top, innerWidth, innerHeight);
	}

	private void paintViewport(Graphics2D g, Image image, SlidingImageState state, Color color) {
		double s = getImageScaling(image);
		double cx = ImageUtils.getWidth(image) / 2.0;
		double cy = ImageUtils.getHeight(image) / 2.0;
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setColor(color);
		g2.setStroke(new BasicStroke((float) Math.max(state.getZoomFactor() / s, 1.0)));
		g2.translate(getWidth() / 2, getHeight() / 2);
		g2.scale(s, s);
		g2.translate(-cx, -cy);
		g2.transform(state.getInverseTransform());
		g2.drawLine(-(int) Math.round(getViewportSize().width * 0.5), (int) Math.round(getViewportSize().height * 0.2),
				-(int) Math.round(getViewportSize().width * 0.2), (int) Math.round(getViewportSize().height * 0.5));
		g2.draw(getNormalizedViewportBounds());
		g2.dispose();
	}

	private double getImageScaling(Image image) {
		Insets pad = getPadding();
		int innerWidth = getWidth() - pad.left - pad.right;
		int innerHeight = getHeight() - pad.top - pad.bottom;
		int imageWidth = ImageUtils.getWidth(image);
		int imageHeight = ImageUtils.getHeight(image);
		double sx = innerWidth / (double) imageWidth;
		double sy = innerHeight / (double) imageHeight;
		return Math.min(sx, sy);
	}

	private Rectangle2D getNormalizedViewportBounds() {
		double w = getViewportSize().getWidth();
		double h = getViewportSize().getHeight();
		return new Rectangle2D.Double(-w / 2, -h / 2, w, h);
	}

	public Insets getPadding() {
		return padding;
	}

	public void setPadding(Insets padding) {
		this.padding = padding;
		refreshUI();
	}

	public Image getImage() {
		return image;
	}

	public Dimension getViewportSize() {
		return viewportSize;
	}

	public Color getViewportColor() {
		return viewportColor;
	}

	public void setViewportColor(Color viewportColor) {
		this.viewportColor = viewportColor;
		refreshUI();
	}

	public SlidingImageState getState() {
		return state;
	}

	private void setState(SlidingImageState state) {
		this.state = state;
	}

}