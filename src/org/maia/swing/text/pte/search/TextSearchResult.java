package org.maia.swing.text.pte.search;

import java.util.List;

public interface TextSearchResult {

	int getNumberOfMatches();

	List<TextSearchMatch> getMatches();

	TextSearchMatch currentMatch();

	TextSearchMatch previousMatch(boolean wrapAround);

	TextSearchMatch nextMatch(boolean wrapAround);

	boolean hasPreviousMatch(boolean wrapAround);

	boolean hasNextMatch(boolean wrapAround);

	void end();

}