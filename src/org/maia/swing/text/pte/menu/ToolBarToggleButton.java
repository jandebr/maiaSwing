package org.maia.swing.text.pte.menu;

import javax.swing.Action;
import javax.swing.JToggleButton;

@SuppressWarnings("serial")
public class ToolBarToggleButton extends ToolBarButton {

	public ToolBarToggleButton(Action action) {
		super(action);
		setModel(new JToggleButton.ToggleButtonModel());
	}

	@Override
	public void setSelected(boolean b) {
		super.setSelected(b);
		refreshUI();
	}

}