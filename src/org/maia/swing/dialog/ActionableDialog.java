package org.maia.swing.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

@SuppressWarnings("serial")
public class ActionableDialog extends JDialog {

	public static final ActionableDialogOption OK_OPTION = new ConfirmationOption();

	public static final ActionableDialogOption CANCEL_OPTION = new CancellationOption();

	private JComponent mainComponent;

	private List<ActionableDialogOption> dialogOptions;

	private List<ActionableDialogButton> dialogButtons;

	private List<ActionableDialogListener> dialogListeners;

	public ActionableDialog(Window owner, String title, boolean modal, JComponent mainComponent,
			List<ActionableDialogOption> dialogOptions) {
		super(owner, title, modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
		this.mainComponent = mainComponent;
		this.dialogOptions = dialogOptions;
		this.dialogButtons = new Vector<ActionableDialogButton>(dialogOptions.size());
		this.dialogListeners = new Vector<ActionableDialogListener>();
		buildUI();
		pack();
		setLocationRelativeTo(owner);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new DialogClosingHandler());
	}

	public static ActionableDialog createOkModalDialog(String title, JComponent mainComponent) {
		return createOkModalDialog(null, title, mainComponent);
	}

	public static ActionableDialog createOkModalDialog(Window owner, String title, JComponent mainComponent) {
		List<ActionableDialogOption> dialogOptions = new Vector<ActionableDialogOption>(2);
		dialogOptions.add(OK_OPTION);
		return new ActionableDialog(owner, title, true, mainComponent, dialogOptions);
	}

	public static ActionableDialog createOkCancelModalDialog(String title, JComponent mainComponent) {
		return createOkCancelModalDialog(null, title, mainComponent);
	}

	public static ActionableDialog createOkCancelModalDialog(Window owner, String title, JComponent mainComponent) {
		List<ActionableDialogOption> dialogOptions = new Vector<ActionableDialogOption>(2);
		dialogOptions.add(OK_OPTION);
		dialogOptions.add(CANCEL_OPTION);
		return new ActionableDialog(owner, title, true, mainComponent, dialogOptions);
	}

	private void buildUI() {
		JPanel panel = new JPanel(new BorderLayout(0, 16));
		panel.add(getMainComponent(), BorderLayout.CENTER);
		panel.add(createDialogButtonsPanel(), BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		add(panel);
	}

	private JComponent createDialogButtonsPanel() {
		JPanel panel = new JPanel(new GridLayout(1, 0, 24, 0));
		for (ActionableDialogOption option : getDialogOptions()) {
			ActionableDialogButton button = createDialogButton(option);
			getDialogButtons().add(button);
			panel.add(button);
		}
		Box box = new Box(BoxLayout.X_AXIS);
		box.add(Box.createHorizontalGlue());
		box.add(panel);
		box.add(Box.createHorizontalGlue());
		return box;
	}

	private ActionableDialogButton createDialogButton(final ActionableDialogOption option) {
		ActionableDialogButton button = new ActionableDialogButton(option);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ActionableDialog dialog = ActionableDialog.this;
				for (ActionableDialogListener listener : dialog.getDialogListeners()) {
					listener.dialogButtonClicked(dialog, option);
					if (option.isConfirmation()) {
						listener.dialogConfirmed(dialog);
					}
					if (option.isCancellation()) {
						listener.dialogCancelled(dialog);
					}
				}
				if (option.isClosingDialog()) {
					dialog.dispose();
				}
			}
		});
		return button;
	}

	@Override
	public void setVisible(final boolean visible) {
		if (ModalityType.MODELESS.equals(getModalityType())) {
			super.setVisible(visible);
		} else {
			final JDialog dialog = this;
			new Thread(new Runnable() {

				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					dialog.show(visible); // blocking method when dialog is modal
				}
			}).start();
		}
	}

	public void fitInScreen() {
		Dimension screen = getScreenSize();
		Dimension size = getSize();
		Point p = getLocation();
		try {
			p = getLocationOnScreen();
		} catch (IllegalComponentStateException e) {
			// getLocationOnScreen when dialog not showing on screen
		}
		if (p.x < 0)
			p.x = 0;
		if (p.y < 0)
			p.y = 0;
		if (p.x + size.width > screen.width)
			p.x = Math.max(screen.width - size.width, 0);
		if (p.y + size.height > screen.height)
			p.y = Math.max(screen.height - size.height, 0);
		int width = Math.min(size.width, screen.width);
		int height = Math.min(size.height, screen.height);
		setLocation(p);
		setSize(width, height);
	}

	public void center() {
		if (getOwner() != null) {
			centerOnWindowOwner();
		} else {
			centerOnScreen();
		}
	}

	public void centerOnWindowOwner() {
		Window owner = getOwner();
		if (owner != null) {
			Point p = owner.getLocationOnScreen();
			Dimension os = owner.getSize();
			Dimension size = getSize();
			setLocation(p.x + (os.width - size.width) / 2, p.y + (os.height - size.height) / 2);
		}
	}

	public void centerOnScreen() {
		Dimension screen = getScreenSize();
		Dimension size = getSize();
		setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);
	}

	private Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	public void addListener(ActionableDialogListener listener) {
		getDialogListeners().add(listener);
	}

	public void removeListener(ActionableDialogListener listener) {
		getDialogListeners().remove(listener);
	}

	public void enableConfirmation() {
		setConfirmationEnabled(true);
	}

	public void disableConfirmation() {
		setConfirmationEnabled(false);
	}

	public void setConfirmationEnabled(boolean enabled) {
		for (ActionableDialogButton button : getDialogButtons()) {
			if (button.getOption().isConfirmation()) {
				button.setEnabled(enabled);
			}
		}
	}

	public JComponent getMainComponent() {
		return mainComponent;
	}

	public List<ActionableDialogOption> getDialogOptions() {
		return dialogOptions;
	}

	public List<ActionableDialogButton> getDialogButtons() {
		return dialogButtons;
	}

	private List<ActionableDialogListener> getDialogListeners() {
		return dialogListeners;
	}

	private class DialogClosingHandler extends WindowAdapter {

		public DialogClosingHandler() {
		}

		@Override
		public void windowClosed(WindowEvent event) {
			for (ActionableDialogListener listener : getDialogListeners()) {
				listener.dialogClosed(ActionableDialog.this);
			}
		}

	}

	private static class ConfirmationOption extends ActionableDialogOption {

		public ConfirmationOption() {
			super("OK", "OK");
		}

		@Override
		public boolean isConfirmation() {
			return true;
		}

		@Override
		public boolean isCancellation() {
			return false;
		}

	}

	private static class CancellationOption extends ActionableDialogOption {

		public CancellationOption() {
			super("CANCEL", "Cancel");
		}

		@Override
		public boolean isConfirmation() {
			return false;
		}

		@Override
		public boolean isCancellation() {
			return true;
		}

	}

}