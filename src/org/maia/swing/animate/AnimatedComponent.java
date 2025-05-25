package org.maia.swing.animate;

import javax.swing.JComponent;

public interface AnimatedComponent {

	JComponent getUI();

	void refreshUI();

	int getRefreshRate();

	boolean isAnimating();

	boolean isRepaintClientDriven();

	void setRepaintClientDriven(boolean clientDriven);

	boolean isObscured();

	void notifyObscured(boolean obscured);

}