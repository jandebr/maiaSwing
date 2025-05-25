package org.maia.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.maia.graphics2d.image.GradientImageFactory;
import org.maia.graphics2d.image.ImageUtils;
import org.maia.swing.animate.imageslide.SlidingImageAdapter;
import org.maia.swing.animate.imageslide.SlidingImageComponent;
import org.maia.swing.animate.imageslide.SlidingImageMiniature;
import org.maia.swing.animate.imageslide.SlidingImageState;
import org.maia.util.SystemUtils;

public class SlidingImageDemo extends SlidingImageAdapter implements KeyListener {

	private SlidingImageComponent component;

	private SlidingImageComponent thumbnail;

	private SlidingImageMiniature miniature;

	private double thumbnailScale = 0.05;

	private double thumbnailZoom = 6.0;

	public static void main(String[] args) {
		new SlidingImageDemo().startDemo();
	}

	private void startDemo() {
		setComponent(createSlidingImageComponent());
		setThumbnail(createThumbnail(getComponent()));
		setMiniature(createMiniature(getComponent()));
		showFrame();
		SystemUtils.sleep(100L);
		getComponent().getUI().grabFocus();
		getComponent().getUI().addKeyListener(this);
	}

	private SlidingImageComponent createSlidingImageComponent() {
		Dimension size = new Dimension(600, 400);
		SlidingImageComponent comp = new SlidingImageComponent(size, Color.BLACK);
		comp.setImageOverlay(GradientImageFactory.createGradientBorderImage(size, Color.BLACK, 20));
		comp.changeImage(ImageUtils.readFromResource("org/maia/swing/resources/toystory.jpg"));
		comp.setHigherQualityRenderingEnabled(true);
		comp.addListener(this);
		return comp;
	}

	private SlidingImageComponent createThumbnail(SlidingImageComponent component) {
		double scale = getThumbnailScale();
		double zoom = getThumbnailZoom();
		int width = (int) Math.round(component.getWidth() * scale * zoom);
		int height = (int) Math.round(component.getHeight() * scale * zoom);
		SlidingImageComponent thumbnail = new SlidingImageComponent(new Dimension(width, height),
				component.getBackground());
		thumbnail.changeImage(ImageUtils.scale(getImage(), scale), createThumbnailState());
		thumbnail.setHigherQualityRenderingEnabled(false);
		thumbnail.setRepaintClientDriven(true);
		return thumbnail;
	}

	private SlidingImageMiniature createMiniature(SlidingImageComponent component) {
		SlidingImageMiniature miniature = new SlidingImageMiniature(new Dimension(200, 160), component);
		miniature.setBorder(BorderFactory.createLineBorder(new Color(20, 20, 20), 1));
		miniature.update(component.getState());
		return miniature;
	}

	private void showFrame() {
		JFrame frame = new JFrame("Sliding image");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setBackground(Color.BLACK);
		frame.add(getComponent().getUI(), BorderLayout.CENTER);
		frame.add(Box.createVerticalStrut(40), BorderLayout.NORTH);
		frame.add(Box.createVerticalStrut(40), BorderLayout.SOUTH);
		frame.add(Box.createHorizontalStrut(80), BorderLayout.WEST);
		frame.add(createSidePanel(), BorderLayout.EAST);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private JPanel createSidePanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.BLACK);
		panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.NORTHWEST;
		panel.add(getThumbnail().getUI(), c);
		c.gridy = 1;
		c.anchor = GridBagConstraints.SOUTHWEST;
		panel.add(getMiniature(), c);
		return panel;
	}

	@Override
	public void keyPressed(KeyEvent event) {
		long duration = 2000L;
		int code = event.getKeyCode();
		if (code == KeyEvent.VK_ENTER) {
			getComponent().stopAnimating();
		} else if (code == KeyEvent.VK_LEFT) {
			getComponent().slideToLeftInViewCoordinates(getComponent().getWidth(), duration);
		} else if (code == KeyEvent.VK_RIGHT) {
			getComponent().slideToRightInViewCoordinates(getComponent().getWidth(), duration);
		} else if (code == KeyEvent.VK_UP) {
			getComponent().slideToTopInViewCoordinates(getComponent().getHeight(), duration);
		} else if (code == KeyEvent.VK_DOWN) {
			getComponent().slideToBottomInViewCoordinates(getComponent().getHeight(), duration);
		} else if (code == KeyEvent.VK_A) {
			getComponent().rotateOverCenter(SwingUtils.degreesToRadians(-45), duration);
		} else if (code == KeyEvent.VK_Z) {
			getComponent().rotateOverCenter(SwingUtils.degreesToRadians(45), duration);
		} else if (code == KeyEvent.VK_PAGE_UP) {
			getComponent().zoom(2.0, duration);
		} else if (code == KeyEvent.VK_PAGE_DOWN) {
			getComponent().zoom(0.5, duration);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void notifyStateChanged(SlidingImageComponent component) {
		getThumbnail().moveTo(createThumbnailState());
		getMiniature().update(component.getState());
	}

	private SlidingImageState createThumbnailState() {
		SlidingImageState t = getComponent().getState().clone();
		t.setCenterX(t.getCenterX() * getThumbnailScale());
		t.setCenterY(t.getCenterY() * getThumbnailScale());
		t.setZoomFactor(t.getZoomFactor() * getThumbnailZoom());
		return t;
	}

	private BufferedImage getImage() {
		return (BufferedImage) getComponent().getImage();
	}

	private SlidingImageComponent getComponent() {
		return component;
	}

	private void setComponent(SlidingImageComponent component) {
		this.component = component;
	}

	private SlidingImageComponent getThumbnail() {
		return thumbnail;
	}

	private void setThumbnail(SlidingImageComponent thumbnail) {
		this.thumbnail = thumbnail;
	}

	private SlidingImageMiniature getMiniature() {
		return miniature;
	}

	private void setMiniature(SlidingImageMiniature miniature) {
		this.miniature = miniature;
	}

	private double getThumbnailScale() {
		return thumbnailScale;
	}

	private double getThumbnailZoom() {
		return thumbnailZoom;
	}

}