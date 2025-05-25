package org.maia.swing.text.pte.search;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class StyledTextSearchResult implements TextSearchResult {

	private List<TextSearchMatch> matches;

	private int currentMatchIndex;

	private TextSearchResultStyling styling;

	private int preferredLinesBeforeFocus = PREFERRED_LINES_AROUND_FOCUS;

	private int preferredLinesAfterFocus = PREFERRED_LINES_AROUND_FOCUS;

	public static int PREFERRED_LINES_AROUND_FOCUS = 3;

	public StyledTextSearchResult(Collection<TextSearchMatch> matches) {
		this.matches = new Vector<TextSearchMatch>(matches);
		Collections.sort(this.matches);
	}

	public void applyStyling(TextSearchResultStyling styling) {
		removeAllHighlights();
		setStyling(styling);
		if (styling.isHighlightAnyMatch()) {
			int index = getCurrentMatchIndex();
			for (int i = 0; i < getNumberOfMatches(); i++) {
				TextSearchMatch match = getMatches().get(i);
				if (i == index && styling.isHighlightCurrentMatch()) {
					match.highlight(styling.getCurrentMatchHighlightColor());
				} else {
					match.highlight(styling.getAnyMatchHighlightColor());
				}
			}
		} else if (styling.isHighlightCurrentMatch() && hasMatches()) {
			currentMatch().highlight(styling.getCurrentMatchHighlightColor());
		}
	}

	public boolean hasMatches() {
		return getNumberOfMatches() > 0;
	}

	@Override
	public int getNumberOfMatches() {
		return getMatches().size();
	}

	@Override
	public TextSearchMatch currentMatch() {
		if (hasMatches()) {
			return getMatches().get(getCurrentMatchIndex());
		} else {
			return null;
		}
	}

	@Override
	public TextSearchMatch previousMatch(boolean wrapAround) {
		if (hasPreviousMatch(wrapAround)) {
			updateHighlightOnAbandoningCurrentMatch();
			setCurrentMatchIndex((getCurrentMatchIndex() - 1 + getNumberOfMatches()) % getNumberOfMatches());
			updateHighlightOnCurrentMatch();
			return currentMatch();
		} else {
			return null;
		}
	}

	@Override
	public TextSearchMatch nextMatch(boolean wrapAround) {
		if (hasNextMatch(wrapAround)) {
			updateHighlightOnAbandoningCurrentMatch();
			setCurrentMatchIndex((getCurrentMatchIndex() + 1) % getNumberOfMatches());
			updateHighlightOnCurrentMatch();
			return currentMatch();
		} else {
			return null;
		}
	}

	@Override
	public boolean hasPreviousMatch(boolean wrapAround) {
		return hasMatches() && (getCurrentMatchIndex() > 0 || wrapAround);
	}

	@Override
	public boolean hasNextMatch(boolean wrapAround) {
		return hasMatches() && (getCurrentMatchIndex() < getNumberOfMatches() - 1 || wrapAround);
	}

	@Override
	public void end() {
		removeAllHighlights();
	}

	public void replaceAllMatchesWith(String str) {
		if (hasMatches()) {
			removeAllHighlights();
			for (TextSearchMatch match : getMatches()) {
				match.replaceWith(str);
			}
			getMatches().clear();
			setCurrentMatchIndex(0);
		}
	}

	public void replaceCurrentMatchWith(String str) {
		if (hasMatches()) {
			currentMatch().removeHighlight();
			currentMatch().replaceWith(str);
			getMatches().remove(getCurrentMatchIndex());
			if (getCurrentMatchIndex() == getNumberOfMatches()) {
				setCurrentMatchIndex(0);
			}
			updateHighlightOnCurrentMatch();
		}
	}

	public void showCurrentMatchInFocus() {
		if (hasMatches()) {
			currentMatch().showInFocus(getPreferredLinesBeforeFocus(), getPreferredLinesAfterFocus());
		}
	}

	protected void removeAllHighlights() {
		for (TextSearchMatch match : getMatches()) {
			match.removeHighlight();
		}
	}

	private void updateHighlightOnAbandoningCurrentMatch() {
		TextSearchResultStyling styling = getStyling();
		if (hasMatches() && styling != null) {
			if (styling.isHighlightAnyMatch()) {
				currentMatch().highlight(styling.getAnyMatchHighlightColor());
			} else {
				currentMatch().removeHighlight();
			}
		}
	}

	private void updateHighlightOnCurrentMatch() {
		TextSearchResultStyling styling = getStyling();
		if (hasMatches() && styling != null) {
			if (styling.isHighlightCurrentMatch()) {
				currentMatch().highlight(styling.getCurrentMatchHighlightColor());
			} else if (styling.isHighlightAnyMatch()) {
				currentMatch().highlight(styling.getAnyMatchHighlightColor());
			} else {
				currentMatch().removeHighlight();
			}
		}
	}

	public void setCurrentMatch(TextSearchMatch match) {
		int index = getMatches().indexOf(match);
		if (index >= 0) {
			setCurrentMatchIndex(index);
		}
	}

	@Override
	public List<TextSearchMatch> getMatches() {
		return matches;
	}

	private int getCurrentMatchIndex() {
		return currentMatchIndex;
	}

	private void setCurrentMatchIndex(int index) {
		this.currentMatchIndex = index;
	}

	protected TextSearchResultStyling getStyling() {
		return styling;
	}

	private void setStyling(TextSearchResultStyling styling) {
		this.styling = styling;
	}

	public int getPreferredLinesBeforeFocus() {
		return preferredLinesBeforeFocus;
	}

	public void setPreferredLinesBeforeFocus(int lines) {
		this.preferredLinesBeforeFocus = lines;
	}

	public int getPreferredLinesAfterFocus() {
		return preferredLinesAfterFocus;
	}

	public void setPreferredLinesAfterFocus(int lines) {
		this.preferredLinesAfterFocus = lines;
	}

}