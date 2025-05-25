package org.maia.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import org.maia.swing.dialog.ActionableDialog;
import org.maia.swing.dialog.ActionableDialogListener;
import org.maia.swing.dialog.ActionableDialogOption;

public class ActionableDialogDemo {

	public static void main(String[] args) {
		JFrame frame = new JFrame("Actionable dialog demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(createOpenDialogButton(frame), BorderLayout.CENTER);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private static JButton createOpenDialogButton(final JFrame owner) {
		JButton button = new JButton("Open dialog...");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ActionableDialog dialog = ActionableDialog.createOkCancelModalDialog(owner, "Actionable dialog",
						createMainComponent());
				dialog.addListener(new ActionableDialogListenerImpl());
				dialog.setVisible(true);
			}
		});
		button.setPreferredSize(new Dimension(500, 300));
		return button;
	}

	private static JComponent createMainComponent() {
		JTextArea input = new JTextArea("Type here...", 10, 40);
		return input;
	}

	private static class ActionableDialogListenerImpl implements ActionableDialogListener {

		public ActionableDialogListenerImpl() {
		}

		@Override
		public void dialogClosed(ActionableDialog dialog) {
			System.out.println("Dialog closed");
		}

		@Override
		public void dialogConfirmed(ActionableDialog dialog) {
			System.out.println("Dialog confirmed");
		}

		@Override
		public void dialogCancelled(ActionableDialog dialog) {
			System.out.println("Dialog cancelled");
		}

		@Override
		public void dialogButtonClicked(ActionableDialog dialog, ActionableDialogOption dialogOption) {
			System.out.println(dialogOption);
		}

	}

}