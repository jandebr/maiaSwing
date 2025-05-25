package org.maia.swing.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Shape;

import javax.swing.border.Border;

public interface ClippingBorder extends Border {

	Shape getInteriorClip(Component c, Graphics g, int x, int y, int width, int height);

}