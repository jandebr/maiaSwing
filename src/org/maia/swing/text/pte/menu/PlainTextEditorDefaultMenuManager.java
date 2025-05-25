package org.maia.swing.text.pte.menu;

import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;

import org.maia.swing.text.pte.PlainTextDocumentEditor;
import org.maia.swing.text.pte.PlainTextEditor;
import org.maia.swing.text.pte.PlainTextEditorAdapter;
import org.maia.swing.text.pte.PlainTextEditorFrame;

public class PlainTextEditorDefaultMenuManager extends PlainTextEditorAdapter implements PlainTextEditorMenuManager {

	private PlainTextEditor editor;

	private PlainTextEditorMenuMaker menuMaker;

	public PlainTextEditorDefaultMenuManager(PlainTextEditor editor, PlainTextEditorMenuMaker menuMaker) {
		this.editor = editor;
		this.menuMaker = menuMaker;
		editor.addListener(this);
	}

	@Override
	public void documentActivated(PlainTextDocumentEditor documentEditor, PlainTextEditor editor) {
		updateMenus();
	}

	@Override
	public void documentDeactivated(PlainTextDocumentEditor documentEditor, PlainTextEditor editor) {
		if (!editor.hasDocuments()) {
			updateMenus();
		} else {
			// update via some other document's activation
		}
	}

	@Override
	public void updateMenus() {
		updateMenuBar();
		updateToolBar();
	}

	protected void updateMenuBar() {
		PlainTextEditor editor = getEditor();
		PlainTextEditorFrame frame = editor.getParentFrame();
		if (frame != null) {
			JMenuBar menuBar = getMenuMaker().createMenuBar(editor);
			if (menuBar != null) {
				frame.installMenuBar(menuBar);
			} else {
				frame.uninstallMenuBar();
			}
		}
	}

	protected void updateToolBar() {
		PlainTextEditor editor = getEditor();
		JComponent toolBar = getMenuMaker().createToolBar(editor);
		if (toolBar != null) {
			editor.installToolBar(toolBar);
		} else {
			editor.uninstallToolBar();
		}
	}

	@Override
	public void documentOpened(PlainTextDocumentEditor documentEditor, PlainTextEditor editor) {
		updatePopupMenu(documentEditor);
	}

	@Override
	public void documentClosed(PlainTextDocumentEditor documentEditor, PlainTextEditor editor) {
		documentEditor.uninstallPopupMenu();
	}

	protected void updatePopupMenu(PlainTextDocumentEditor documentEditor) {
		JPopupMenu popupMenu = getMenuMaker().createPopupMenu(documentEditor);
		if (popupMenu != null) {
			documentEditor.installPopupMenu(popupMenu);
		} else {
			documentEditor.uninstallPopupMenu();
		}
	}

	public PlainTextEditor getEditor() {
		return editor;
	}

	public PlainTextEditorMenuMaker getMenuMaker() {
		return menuMaker;
	}

}