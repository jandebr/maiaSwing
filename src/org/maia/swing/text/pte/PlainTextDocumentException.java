package org.maia.swing.text.pte;

import org.maia.swing.text.pte.model.PlainTextDocument;

@SuppressWarnings("serial")
public class PlainTextDocumentException extends Exception {

	private PlainTextDocument document;

	public PlainTextDocumentException(PlainTextDocument document) {
		this(document, null, null);
	}

	public PlainTextDocumentException(PlainTextDocument document, String message) {
		this(document, message, null);
	}

	public PlainTextDocumentException(PlainTextDocument document, Throwable cause) {
		this(document, null, cause);
	}

	public PlainTextDocumentException(PlainTextDocument document, String message, Throwable cause) {
		super(message, cause);
		this.document = document;
	}

	public PlainTextDocument getDocument() {
		return document;
	}

}