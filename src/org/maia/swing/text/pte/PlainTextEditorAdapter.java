package org.maia.swing.text.pte;

public class PlainTextEditorAdapter implements PlainTextEditorListener {

	protected PlainTextEditorAdapter() {
	}

	@Override
	public void documentOpened(PlainTextDocumentEditor documentEditor, PlainTextEditor editor) {
		// Subclasses can override
	}

	@Override
	public void documentClosed(PlainTextDocumentEditor documentEditor, PlainTextEditor editor) {
		// Subclasses can override
	}

	@Override
	public void documentDiscarded(PlainTextDocumentEditor documentEditor, PlainTextEditor editor) {
		// Subclasses can override
	}

	@Override
	public void documentActivated(PlainTextDocumentEditor documentEditor, PlainTextEditor editor) {
		// Subclasses can override
	}

	@Override
	public void documentDeactivated(PlainTextDocumentEditor documentEditor, PlainTextEditor editor) {
		// Subclasses can override
	}

}