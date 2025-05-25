package org.maia.swing.text.pte.menu;

import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;

@SuppressWarnings("serial")
public class ToolBarButton extends JButton {

	private static Border DEFAULT_BORDER = BorderFactory.createEmptyBorder(2, 2, 2, 2);

	private static Color ROLLOVER_BACKGROUND = new Color(218, 234, 240);

	private static Color ROLLOVER_BORDER_COLOR = new Color(168, 213, 230);

	private static Color ROLLOVER_PRESSED_BORDER_COLOR = ROLLOVER_BORDER_COLOR.darker();

	private static Border ROLLOVER_BORDER = BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(1, 1, 1, 1, ROLLOVER_BORDER_COLOR),
			BorderFactory.createEmptyBorder(1, 1, 1, 1));

	private static Border ROLLOVER_PRESSED_BORDER = BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(1, 1, 1, 1, ROLLOVER_PRESSED_BORDER_COLOR),
			BorderFactory.createEmptyBorder(1, 2, 1, 0));

	private static Color SELECTED_BACKGROUND = new Color(191, 217, 227);

	private static Color SELECTED_BORDER_COLOR = new Color(147, 181, 194);

	private static Border SELECTED_BORDER = BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(1, 1, 1, 1, SELECTED_BORDER_COLOR),
			BorderFactory.createEmptyBorder(1, 2, 1, 0));

	public ToolBarButton(Action action) {
		super(action);
		setHideActionText(true); // icon only
		setFocusPainted(false);
		setFocusable(false);
		refreshUI();
		addMouseListener(new RolloverEffectHandler());
		addMouseListener(new PressedEffectHandler());
	}

	public void refreshUI() {
		if (isSelected()) {
			setBackground(SELECTED_BACKGROUND);
			setContentAreaFilled(true);
			setBorder(SELECTED_BORDER);
		} else {
			setContentAreaFilled(false);
			setBorder(DEFAULT_BORDER);
		}
	}

	private class RolloverEffectHandler extends MouseInputAdapter {

		public RolloverEffectHandler() {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			if (isEnabled() && !isSelected()) {
				setBackground(ROLLOVER_BACKGROUND);
				setContentAreaFilled(true);
				setBorder(ROLLOVER_BORDER);
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (!isSelected()) {
				setContentAreaFilled(false);
				setBorder(DEFAULT_BORDER);
			}
		}

	}

	private class PressedEffectHandler extends MouseInputAdapter {

		public PressedEffectHandler() {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (isEnabled() && !isSelected()) {
				setBorder(ROLLOVER_PRESSED_BORDER);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (isEnabled()) {
				if (isSelected()) {
					setBackground(SELECTED_BACKGROUND);
					setBorder(SELECTED_BORDER);
				} else {
					setBackground(ROLLOVER_BACKGROUND);
					setBorder(ROLLOVER_BORDER);
				}
			}
		}

	}

}