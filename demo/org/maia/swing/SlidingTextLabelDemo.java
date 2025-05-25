package org.maia.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;

import org.maia.swing.animate.textslide.SlidingTextLabel;
import org.maia.swing.layout.HorizontalAlignment;
import org.maia.swing.text.VerticalTextAlignment;

public class SlidingTextLabelDemo {

	private SlidingTextLabel label;

	public static void main(String[] args) {
		new SlidingTextLabelDemo().startDemo();
	}

	private void startDemo() {
		setLabel(createLabel());
		showFrame();
	}

	private SlidingTextLabel createLabel() {
		Font font = new Font(Font.DIALOG, Font.PLAIN, 72);
		SlidingTextLabel label = SlidingTextLabel.createSized("This is a sliding text label", font,
				new Dimension(400, 120), Color.BLACK, Color.WHITE, HorizontalAlignment.LEFT,
				VerticalTextAlignment.CENTER);
		label.setBorder(BorderFactory.createLineBorder(Color.RED, 8));
		label.setSuspensionAtEndsMillis(1000L);
		return label;
	}

	private void showFrame() {
		JFrame frame = new JFrame("Text label");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setBackground(Color.LIGHT_GRAY);
		frame.add(getLabel().getUI(), BorderLayout.CENTER);
		frame.add(Box.createVerticalStrut(40), BorderLayout.NORTH);
		frame.add(Box.createVerticalStrut(40), BorderLayout.SOUTH);
		frame.add(Box.createHorizontalStrut(80), BorderLayout.WEST);
		frame.add(Box.createHorizontalStrut(80), BorderLayout.EAST);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private SlidingTextLabel getLabel() {
		return label;
	}

	private void setLabel(SlidingTextLabel label) {
		this.label = label;
	}

}