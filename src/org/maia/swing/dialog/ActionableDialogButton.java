package org.maia.swing.dialog;

import javax.swing.JButton;

@SuppressWarnings("serial")
public class ActionableDialogButton extends JButton {

	private ActionableDialogOption option;

	public ActionableDialogButton(ActionableDialogOption option) {
		super(option.getLabel());
		this.option = option;
	}

	public ActionableDialogOption getOption() {
		return option;
	}

}