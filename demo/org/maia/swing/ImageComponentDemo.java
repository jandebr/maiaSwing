package org.maia.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;

import org.maia.graphics2d.image.ImageUtils;
import org.maia.swing.image.ImageComponent;
import org.maia.swing.layout.FillMode;

public class ImageComponentDemo {

	private ImageComponent component;

	public static void main(String[] args) {
		new ImageComponentDemo().startDemo();
	}

	private void startDemo() {
		setComponent(createComponent());
		showFrame();
	}

	private ImageComponent createComponent() {
		BufferedImage image = ImageUtils.readFromResource("org/maia/swing/resources/woody.jpg");
		ImageComponent comp = new ImageComponent(image);
		comp.setFillMode(FillMode.FIT);
		comp.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		return comp;
	}

	private void showFrame() {
		JFrame frame = new JFrame("Back buffering");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setBackground(Color.WHITE);
		frame.add(getComponent(), BorderLayout.CENTER);
		frame.add(Box.createVerticalStrut(40), BorderLayout.NORTH);
		frame.add(Box.createVerticalStrut(40), BorderLayout.SOUTH);
		frame.add(Box.createHorizontalStrut(80), BorderLayout.WEST);
		frame.add(Box.createHorizontalStrut(80), BorderLayout.EAST);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private ImageComponent getComponent() {
		return component;
	}

	private void setComponent(ImageComponent component) {
		this.component = component;
	}

}