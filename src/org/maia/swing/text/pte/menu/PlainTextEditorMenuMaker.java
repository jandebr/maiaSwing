package org.maia.swing.text.pte.menu;

import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;

import org.maia.swing.text.pte.PlainTextDocumentEditor;
import org.maia.swing.text.pte.PlainTextEditor;

public interface PlainTextEditorMenuMaker {

	JMenuBar createMenuBar(PlainTextEditor editor);

	JComponent createToolBar(PlainTextEditor editor);

	JPopupMenu createPopupMenu(PlainTextDocumentEditor documentEditor);

}