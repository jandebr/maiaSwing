package org.maia.swing.text.pte.model;

import javax.swing.Icon;

import org.maia.swing.text.pte.PlainTextDocumentCancellation;
import org.maia.swing.text.pte.PlainTextDocumentException;

public interface PlainTextDocument {

	void addListener(PlainTextDocumentListener listener);

	void removeListener(PlainTextDocumentListener listener);

	String readText() throws PlainTextDocumentException;

	void writeText(String text) throws PlainTextDocumentException, PlainTextDocumentCancellation;

	Icon getSmallDocumentIcon();

	Icon getLargeDocumentIcon();

	String getShortDocumentName();

	String getLongDocumentName();

	boolean isDraft();

	boolean isEditable();

}