package org.maia.swing.text.pte;

import org.maia.util.GenericListener;

public interface PlainTextEditorListener extends GenericListener {

	void documentOpened(PlainTextDocumentEditor documentEditor, PlainTextEditor editor);

	void documentClosed(PlainTextDocumentEditor documentEditor, PlainTextEditor editor);

	void documentDiscarded(PlainTextDocumentEditor documentEditor, PlainTextEditor editor);

	void documentActivated(PlainTextDocumentEditor documentEditor, PlainTextEditor editor);

	void documentDeactivated(PlainTextDocumentEditor documentEditor, PlainTextEditor editor);

}