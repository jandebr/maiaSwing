package org.maia.swing.dialog;

public abstract class ActionableDialogAdapter implements ActionableDialogListener {

	protected ActionableDialogAdapter() {
	}

	@Override
	public void dialogClosed(ActionableDialog dialog) {
		// Subclasses can override this
	}

	@Override
	public void dialogButtonClicked(ActionableDialog dialog, ActionableDialogOption dialogOption) {
		// Subclasses can override this
	}

	@Override
	public void dialogConfirmed(ActionableDialog dialog) {
		// Subclasses can override this
	}

	@Override
	public void dialogCancelled(ActionableDialog dialog) {
		// Subclasses can override this
	}

}