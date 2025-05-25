package org.maia.swing.text.pte;

import org.maia.swing.text.pte.model.PlainTextDocument;

@SuppressWarnings("serial")
public class PlainTextDocumentCancellation extends Exception {

	private PlainTextDocument document;

	public PlainTextDocumentCancellation(PlainTextDocument document) {
		this.document = document;
	}

	public PlainTextDocument getDocument() {
		return document;
	}

}