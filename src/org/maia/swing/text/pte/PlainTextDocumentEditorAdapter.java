package org.maia.swing.text.pte;

public class PlainTextDocumentEditorAdapter implements PlainTextDocumentEditorListener {

	protected PlainTextDocumentEditorAdapter() {
	}

	@Override
	public void documentEditableChanged(PlainTextDocumentEditor documentEditor) {
		// Subclasses can override
	}

	@Override
	public void documentNameChanged(PlainTextDocumentEditor documentEditor) {
		// Subclasses can override
	}

	@Override
	public void documentHasUnsavedChanges(PlainTextDocumentEditor documentEditor) {
		// Subclasses can override
	}

	@Override
	public void documentSaved(PlainTextDocumentEditor documentEditor) {
		// Subclasses can override
	}

	@Override
	public void documentSelectionChanged(PlainTextDocumentEditor documentEditor) {
		// Subclasses can override
	}

	@Override
	public void documentClosed(PlainTextDocumentEditor documentEditor) {
		// Subclasses can override
	}

	@Override
	public void documentDiscarded(PlainTextDocumentEditor documentEditor) {
		// Subclasses can override
	}

	@Override
	public void documentWrapLinesChanged(PlainTextDocumentEditor documentEditor) {
		// Subclasses can override
	}

	@Override
	public void documentUndoableStateChanged(PlainTextDocumentEditor documentEditor) {
		// Subclasses can override
	}

}