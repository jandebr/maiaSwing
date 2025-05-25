package org.maia.swing.text.pte;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;

import org.maia.swing.text.pte.model.PlainTextDocument;

@SuppressWarnings("serial")
public class PlainTextEditorFrame extends JFrame {

	private PlainTextEditor editor;

	private String baseTitle;

	private boolean includeActiveDocumentShortNameInTitle;

	private boolean includeActiveDocumentLongNameInTitle = true;

	private boolean closeDocumentsOnClose;

	public PlainTextEditorFrame(PlainTextEditor editor) {
		this(editor, true, true);
	}

	public PlainTextEditorFrame(PlainTextEditor editor, boolean closeDocumentsOnClose, boolean exitOnClose) {
		this(editor, "Text Editor", closeDocumentsOnClose, exitOnClose);
	}

	public PlainTextEditorFrame(PlainTextEditor editor, String title, boolean closeDocumentsOnClose,
			boolean exitOnClose) {
		super(title);
		this.editor = editor;
		add(editor.getUI());
		pack();
		setLocationRelativeTo(null);
		setIconImage(editor.getEditorKit().getIconForEditorFrame(this));
		addWindowListener(new FrameOpenHandler());
		addWindowListener(new FrameCloseHandler());
		editor.addListener(new FrameTitleUpdater());
		init(title, closeDocumentsOnClose, exitOnClose);
	}

	public void init(String title, boolean closeDocumentsOnClose, boolean exitOnClose) {
		setCloseDocumentsOnClose(closeDocumentsOnClose);
		setDefaultCloseOperation(exitOnClose ? WindowConstants.EXIT_ON_CLOSE : WindowConstants.DISPOSE_ON_CLOSE);
		setBaseTitle(title);
		updateTitle();
	}

	public void installMenuBar(JMenuBar menuBar) {
		setJMenuBar(menuBar);
		revalidate();
	}

	public void uninstallMenuBar() {
		setJMenuBar(null);
		revalidate();
	}

	public void close() {
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	protected void updateTitle() {
		setTitle(deriveTitle());
	}

	protected String deriveTitle() {
		String title = getBaseTitle();
		if (isIncludeActiveDocumentShortNameInTitle() || isIncludeActiveDocumentLongNameInTitle()) {
			PlainTextDocument document = getEditor().getActiveDocument();
			if (document != null) {
				if (title.length() > 0) {
					title += " - ";
				}
				if (isIncludeActiveDocumentLongNameInTitle()) {
					title += document.getLongDocumentName();
				} else if (isIncludeActiveDocumentShortNameInTitle()) {
					title += document.getShortDocumentName();
				}
			}
		}
		return title;
	}

	public boolean isExitOnClose() {
		return getDefaultCloseOperation() == WindowConstants.EXIT_ON_CLOSE;
	}

	public PlainTextEditor getEditor() {
		return editor;
	}

	protected String getBaseTitle() {
		return baseTitle;
	}

	private void setBaseTitle(String baseTitle) {
		this.baseTitle = baseTitle;
	}

	public boolean isIncludeActiveDocumentShortNameInTitle() {
		return includeActiveDocumentShortNameInTitle;
	}

	public void setIncludeActiveDocumentShortNameInTitle(boolean include) {
		this.includeActiveDocumentShortNameInTitle = include;
		updateTitle();
	}

	public boolean isIncludeActiveDocumentLongNameInTitle() {
		return includeActiveDocumentLongNameInTitle;
	}

	public void setIncludeActiveDocumentLongNameInTitle(boolean include) {
		this.includeActiveDocumentLongNameInTitle = include;
		updateTitle();
	}

	public boolean isCloseDocumentsOnClose() {
		return closeDocumentsOnClose;
	}

	public void setCloseDocumentsOnClose(boolean closeDocuments) {
		this.closeDocumentsOnClose = closeDocuments;
	}

	private class FrameOpenHandler extends WindowAdapter {

		public FrameOpenHandler() {
		}

		@Override
		public void windowOpened(WindowEvent e) {
			if (getEditor().hasDocuments()) {
				getEditor().getActiveDocumentEditor().grabFocus();
			}
		}

	}

	private class FrameCloseHandler extends WindowAdapter {

		public FrameCloseHandler() {
		}

		@Override
		public void windowClosing(WindowEvent e) {
			if (isCloseDocumentsOnClose()) {
				getEditor().closeAllDocuments();
			}
		}

	}

	private class FrameTitleUpdater extends PlainTextEditorAdapter {

		public FrameTitleUpdater() {
		}

		@Override
		public void documentActivated(PlainTextDocumentEditor documentEditor, PlainTextEditor editor) {
			updateTitle();
		}

		@Override
		public void documentDeactivated(PlainTextDocumentEditor documentEditor, PlainTextEditor editor) {
			updateTitle();
		}

	}

}