package org.maia.swing;

import java.io.File;

import org.maia.swing.text.pte.PlainTextEditor;

public class PlainTextEditorDemo {

	public static void main(String[] args) throws Exception {
		PlainTextEditor editor = new PlainTextEditor();
		editor.openDocument(editor.getEditorKit().createFileDocument(new File("document.txt")));
		editor.openDocument(editor.getEditorKit().createNewFileDocument());
		editor.showInFrame("Text Editor", true, true);
	}

}