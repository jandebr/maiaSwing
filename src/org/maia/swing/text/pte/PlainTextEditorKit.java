package org.maia.swing.text.pte;

import java.awt.Color;
import java.awt.Image;
import java.io.File;

import javax.swing.Icon;

import org.maia.graphics2d.image.ImageUtils;
import org.maia.swing.text.pte.menu.PlainTextEditorDefaultMenuMaker;
import org.maia.swing.text.pte.menu.PlainTextEditorDefaultMenuManager;
import org.maia.swing.text.pte.menu.PlainTextEditorMenuMaker;
import org.maia.swing.text.pte.menu.PlainTextEditorMenuManager;
import org.maia.swing.text.pte.model.PlainTextDocument;
import org.maia.swing.text.pte.model.PlainTextFileDocument;

public class PlainTextEditorKit {

	private static PlainTextEditorKit defaultKit;

	protected PlainTextEditorKit() {
	}

	public static PlainTextEditorKit getDefaultEditorKit() {
		if (defaultKit == null) {
			defaultKit = new PlainTextEditorKit();
		}
		return defaultKit;
	}

	public PlainTextEditorFrame createEditorFrame(PlainTextEditor editor, String frameTitle,
			boolean closeDocumentsOnClose, boolean exitOnClose) {
		return new PlainTextEditorFrame(editor, frameTitle, closeDocumentsOnClose, exitOnClose);
	}

	public Image getIconForEditorFrame(PlainTextEditorFrame frame) {
		return ImageUtils.getIcon("org/maia/swing/icons/text/textedit32.png").getImage();
	}

	public PlainTextEditorActions createEditorActions(PlainTextEditor editor) {
		return new PlainTextEditorActions(editor);
	}

	public PlainTextEditorMenuManager createEditorMenuManager(PlainTextEditor editor) {
		PlainTextEditorMenuMaker menuMaker = createEditorMenuMaker(editor);
		return new PlainTextEditorDefaultMenuManager(editor, menuMaker);
	}

	protected PlainTextEditorMenuMaker createEditorMenuMaker(PlainTextEditor editor) {
		return new PlainTextEditorDefaultMenuMaker();
	}

	public PlainTextDocument createNewFileDocument() {
		return new PlainTextFileDocument();
	}

	public PlainTextDocument createFileDocument(File file) {
		return new PlainTextFileDocument(file);
	}

	public PlainTextDocumentEditor createDocumentEditor(PlainTextDocument document, PlainTextEditor editor)
			throws PlainTextDocumentException {
		PlainTextDocumentEditor documentEditor = new PlainTextDocumentEditor(document,
				editor.getUI().getPreferredSize());
		documentEditor.installActions(createDocumentEditorActions(documentEditor, editor));
		return documentEditor;
	}

	protected PlainTextDocumentEditorActions createDocumentEditorActions(PlainTextDocumentEditor documentEditor,
			PlainTextEditor editor) {
		return new PlainTextDocumentEditorActions(documentEditor, editor);
	}

	public Icon getIconForDocumentEditor(PlainTextDocumentEditor documentEditor) {
		return documentEditor.getDocument().getSmallDocumentIcon();
	}

	public String getTitleForDocumentEditor(PlainTextDocumentEditor documentEditor) {
		String title = getTitleForUnmodifiedDocumentEditor(documentEditor);
		if (documentEditor.isTextChangedSinceLastSave()) {
			title = "*" + title;
		}
		return title;
	}

	protected String getTitleForUnmodifiedDocumentEditor(PlainTextDocumentEditor documentEditor) {
		return documentEditor.getDocument().getShortDocumentName();
	}

	public String getToolTipForDocumentEditor(PlainTextDocumentEditor documentEditor) {
		String longTitle = documentEditor.getDocument().getLongDocumentName();
		if (longTitle != null && !longTitle.equals(getTitleForUnmodifiedDocumentEditor(documentEditor))) {
			return longTitle;
		} else {
			return null;
		}
	}

	public Color getColorForDocumentEditor(PlainTextDocumentEditor documentEditor) {
		return null;
	}

}