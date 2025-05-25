package org.maia.swing.text.pte.search;

import java.awt.Color;

public interface TextSearchMatch extends Comparable<TextSearchMatch> {

	void replaceWith(String str);

	void showInFocus(int preferredLinesBefore, int preferredLinesAfter);

	void highlight(Color color);

	void removeHighlight();

}