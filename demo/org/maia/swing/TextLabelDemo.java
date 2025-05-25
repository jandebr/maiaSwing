package org.maia.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JFrame;

import org.maia.swing.border.CompoundClippingBorder;
import org.maia.swing.border.CurvedCornerLineBorder;
import org.maia.swing.text.TextLabel;
import org.maia.swing.text.TextLabel.TextOverflowMode;

public class TextLabelDemo {

	private TextLabel label;

	public static void main(String[] args) {
		new TextLabelDemo().startDemo();
	}

	private void startDemo() {
		setLabel(createLabel());
		showFrame();
	}

	private TextLabel createLabel() {
		Font font = new Font(Font.DIALOG, Font.PLAIN, 72);
		TextLabel label = TextLabel.createCompactLabel("Great Waves", font, new Insets(20, 40, 20, 40));
		label.setBackground(Color.BLACK);
		label.setForeground(Color.WHITE);
		label.setBorder(new CompoundClippingBorder(new CurvedCornerLineBorder.Circular(Color.RED, 0.5f, 1f),
				new CompoundClippingBorder(new CurvedCornerLineBorder.Circular(Color.WHITE, 0.5f, 4f),
						new CurvedCornerLineBorder.Circular(Color.RED, 0.5f, 6f))));
		label.setTextOverflowMode(TextOverflowMode.FADE);
		return label;
	}

	private void showFrame() {
		JFrame frame = new JFrame("Text label");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setBackground(Color.LIGHT_GRAY);
		frame.add(getLabel(), BorderLayout.CENTER);
		frame.add(Box.createVerticalStrut(40), BorderLayout.NORTH);
		frame.add(Box.createVerticalStrut(40), BorderLayout.SOUTH);
		frame.add(Box.createHorizontalStrut(80), BorderLayout.WEST);
		frame.add(Box.createHorizontalStrut(80), BorderLayout.EAST);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private TextLabel getLabel() {
		return label;
	}

	private void setLabel(TextLabel label) {
		this.label = label;
	}

}