package org.maia.swing.text.pte.search;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.List;

import org.maia.swing.dialog.ActionableDialog;
import org.maia.swing.dialog.ActionableDialogAdapter;
import org.maia.swing.dialog.ActionableDialogOption;
import org.maia.swing.text.pte.PlainTextDocumentEditor;

public class FindReplaceDialog {

	private FindReplacePanel panel;

	private ActionableDialog dialog;

	private static final ActionableDialogOption CLOSE_OPTION = new CloseOption();

	public FindReplaceDialog() {
		this(null);
	}

	public FindReplaceDialog(Window windowOwner) {
		this(windowOwner, "Find and replace");
	}

	public FindReplaceDialog(Window windowOwner, String windowTitle) {
		this.panel = createPanel();
		this.dialog = createDialog(windowOwner, windowTitle);
	}

	protected FindReplacePanel createPanel() {
		FindReplacePanel panel = new FindReplacePanel();
		panel.addListener(new FindReplacePanelListener() {

			@Override
			public void notifyEscape() {
				close();
			}
		});
		return panel;
	}

	protected ActionableDialog createDialog(Window windowOwner, String windowTitle) {
		List<ActionableDialogOption> dialogOptions = Collections.singletonList(CLOSE_OPTION);
		ActionableDialog dialog = new ActionableDialog(windowOwner, windowTitle, true, getPanel(), dialogOptions);
		dialog.addListener(new ActionableDialogAdapter() {

			@Override
			public void dialogClosed(ActionableDialog dialog) {
				getPanel().end();
			}

		});
		return dialog;
	}

	public void show(PlainTextDocumentEditor documentEditor) {
		getPanel().init(documentEditor);
		getDialog().setVisible(true);
	}

	public void close() {
		getDialog().dispatchEvent(new WindowEvent(getDialog(), WindowEvent.WINDOW_CLOSING));
	}

	protected FindReplacePanel getPanel() {
		return panel;
	}

	public ActionableDialog getDialog() {
		return dialog;
	}

	private static class CloseOption extends ActionableDialogOption {

		public CloseOption() {
			super("CLOSE", "Close");
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