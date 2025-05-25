package org.maia.swing.text.pte.model;

import org.maia.util.GenericListener;

public interface PlainTextDocumentListener extends GenericListener {

	void documentEditableChanged(PlainTextDocument document);

	void documentNameChanged(PlainTextDocument document);

}