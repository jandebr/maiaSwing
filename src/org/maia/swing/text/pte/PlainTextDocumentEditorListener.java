package org.maia.swing.text.pte;

import org.maia.util.GenericListener;

public interface PlainTextDocumentEditorListener extends GenericListener {

	void documentEditableChanged(PlainTextDocumentEditor documentEditor);

	void documentNameChanged(PlainTextDocumentEditor documentEditor);

	void documentHasUnsavedChanges(PlainTextDocumentEditor documentEditor);

	void documentSaved(PlainTextDocumentEditor documentEditor);

	void documentSelectionChanged(PlainTextDocumentEditor documentEditor);

	void documentClosed(PlainTextDocumentEditor documentEditor);

	void documentDiscarded(PlainTextDocumentEditor documentEditor);

	void documentWrapLinesChanged(PlainTextDocumentEditor documentEditor);

	void documentUndoableStateChanged(PlainTextDocumentEditor documentEditor);

}