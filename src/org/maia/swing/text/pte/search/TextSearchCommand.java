package org.maia.swing.text.pte.search;

import java.util.Objects;

public class TextSearchCommand {

	private String searchString;

	private boolean caseSensitive;

	private boolean regex;

	public TextSearchCommand(String searchString) {
		this(searchString, false);
	}

	public TextSearchCommand(String searchString, boolean caseSensitive) {
		this(searchString, caseSensitive, false);
	}

	public TextSearchCommand(String searchString, boolean caseSensitive, boolean regex) {
		this.searchString = searchString;
		this.caseSensitive = caseSensitive;
		this.regex = regex;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TextSearchCommand [search=");
		builder.append(getSearchString());
		builder.append(", caseSensitive=");
		builder.append(isCaseSensitive());
		builder.append(", regex=");
		builder.append(isRegex());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(isCaseSensitive(), isRegex(), getSearchString());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextSearchCommand other = (TextSearchCommand) obj;
		return isCaseSensitive() == other.isCaseSensitive() && isRegex() == other.isRegex()
				&& Objects.equals(getSearchString(), other.getSearchString());
	}

	public String getSearchString() {
		return searchString;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public boolean isRegex() {
		return regex;
	}

}