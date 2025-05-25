package org.maia.swing;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;

import javax.swing.JComponent;

public class SwingUtils {

	private SwingUtils() {
	}

	public static void fixSize(JComponent comp, int width, int height) {
		fixSize(comp, new Dimension(width, height));
	}

	public static void fixSize(JComponent comp, Dimension size) {
		comp.setMinimumSize(size);
		comp.setPreferredSize(size);
		comp.setMaximumSize(size);
		comp.setSize(size);
	}

	public static void fixWidth(JComponent comp, int width) {
		comp.setMinimumSize(new Dimension(width, comp.getMinimumSize().height));
		comp.setPreferredSize(new Dimension(width, comp.getPreferredSize().height));
		comp.setMaximumSize(new Dimension(width, comp.getMaximumSize().height));
		comp.setSize(new Dimension(width, comp.getHeight()));
	}

	public static void fixHeight(JComponent comp, int height) {
		comp.setMinimumSize(new Dimension(comp.getMinimumSize().width, height));
		comp.setPreferredSize(new Dimension(comp.getPreferredSize().width, height));
		comp.setMaximumSize(new Dimension(comp.getMaximumSize().width, height));
		comp.setSize(new Dimension(comp.getWidth(), height));
	}

	public static double degreesToRadians(double degrees) {
		return degrees / 180.0 * Math.PI;
	}

	public static double radiansToDegrees(double radians) {
		return radians / Math.PI * 180.0;
	}

	public static GraphicsConfiguration getDefaultGraphicsConfiguration() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	}

	public static Graphics2D getDefaultGraphics() {
		return getDefaultGraphics(1, 1);
	}

	public static Graphics2D getDefaultGraphics(Dimension size) {
		return getDefaultGraphics(size.width, size.height);
	}

	public static Graphics2D getDefaultGraphics(int width, int height) {
		return getDefaultGraphicsConfiguration().createCompatibleImage(width, height).createGraphics();
	}

}