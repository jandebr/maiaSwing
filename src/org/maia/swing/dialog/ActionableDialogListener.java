package org.maia.swing.dialog;

public interface ActionableDialogListener {

	void dialogClosed(ActionableDialog dialog);

	void dialogButtonClicked(ActionableDialog dialog, ActionableDialogOption dialogOption);

	void dialogConfirmed(ActionableDialog dialog);

	void dialogCancelled(ActionableDialog dialog);

}