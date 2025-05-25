package org.maia.swing.text.pte.search;

import java.awt.Color;

public class TextSearchResultStyling {

	private Color currentMatchHighlightColor;

	private Color anyMatchHighlightColor;

	public TextSearchResultStyling(Color currentMatchHighlightColor, Color anyMatchHighlightColor) {
		this.currentMatchHighlightColor = currentMatchHighlightColor;
		this.anyMatchHighlightColor = anyMatchHighlightColor;
	}

	public boolean isHighlightCurrentMatch() {
		return getCurrentMatchHighlightColor() != null;
	}

	public boolean isHighlightAnyMatch() {
		return getAnyMatchHighlightColor() != null;
	}

	public Color getCurrentMatchHighlightColor() {
		return currentMatchHighlightColor;
	}

	public Color getAnyMatchHighlightColor() {
		return anyMatchHighlightColor;
	}

}