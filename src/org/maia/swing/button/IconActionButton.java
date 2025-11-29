package org.maia.swing.button;

import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class IconActionButton extends JButton {

	public IconActionButton(Icon icon) {
		super(icon);
		setContentAreaFilled(false);
		setOpaque(false);
		setFocusPainted(false);
		setBorderPainted(false);
		setMargin(new Insets(0, 0, 0, 0));
	}

	public IconActionButton(Icon icon, ActionListener actionListener) {
		this(icon);
		addActionListener(actionListener);
	}

}