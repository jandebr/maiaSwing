package org.maia.swing.text.pte;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import org.maia.graphics2d.image.ImageUtils;
import org.maia.swing.text.pte.model.PlainTextFileDocument;

@SuppressWarnings("serial")
public class PlainTextEditorActions {

	private PlainTextEditor editor;

	private Action newAction;

	private Action openAction;

	private Action closeAction;

	private Action closeAllAction;

	private Action quitAction;

	public PlainTextEditorActions(PlainTextEditor editor) {
		this.editor = editor;
	}

	public final Action getNewAction() {
		if (newAction == null) {
			newAction = createNewAction();
		}
		return newAction;
	}

	protected Action createNewAction() {
		return new NewAction();
	}

	public final Action getOpenAction() {
		if (openAction == null) {
			openAction = createOpenAction();
		}
		return openAction;
	}

	protected Action createOpenAction() {
		return new OpenAction();
	}

	public final Action getCloseAction() {
		if (closeAction == null) {
			closeAction = createCloseAction();
		}
		return closeAction;
	}

	protected Action createCloseAction() {
		return new CloseAction();
	}

	public final Action getCloseAllAction() {
		if (closeAllAction == null) {
			closeAllAction = createCloseAllAction();
		}
		return closeAllAction;
	}

	protected Action createCloseAllAction() {
		return new CloseAllAction();
	}

	public final Action getQuitAction() {
		if (quitAction == null) {
			quitAction = createQuitAction();
		}
		return quitAction;
	}

	protected Action createQuitAction() {
		return new QuitAction();
	}

	public PlainTextEditor getEditor() {
		return editor;
	}

	protected abstract class TextEditorAction extends AbstractAction {

		protected TextEditorAction(String name, Icon icon) {
			this(name, icon, icon);
		}

		protected TextEditorAction(String name, Icon smallIcon, Icon largeIcon) {
			super(name, smallIcon);
			changeLargeIcon(largeIcon);
			changeToolTipText(name);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			PlainTextEditor editor = getEditor();
			doAction(editor);
			PlainTextDocumentEditor documentEditor = editor.getActiveDocumentEditor();
			if (documentEditor != null) {
				documentEditor.grabFocus();
			}
		}

		protected abstract void doAction(PlainTextEditor editor);

		protected void changeName(String name) {
			putValue(Action.NAME, name);
			changeToolTipText(name);
		}

		protected void changeSmallIcon(Icon icon) {
			putValue(Action.SMALL_ICON, icon);
		}

		protected void changeLargeIcon(Icon icon) {
			putValue(Action.LARGE_ICON_KEY, icon);
		}

		protected void changeToolTipText(String text) {
			putValue(Action.SHORT_DESCRIPTION, text);
		}

	}

	private class NewAction extends TextEditorAction {

		public NewAction() {
			super("New", ImageUtils.getIcon("org/maia/swing/icons/text/new16.png"),
					ImageUtils.getIcon("org/maia/swing/icons/text/new32.png"));
			setEnabled(true);
		}

		@Override
		protected void doAction(PlainTextEditor editor) {
			try {
				editor.openDocument(editor.getEditorKit().createNewFileDocument());
			} catch (PlainTextDocumentException error) {
				editor.showErrorMessageDialog("Error", "Error while opening document", error);
			}
		}

	}

	private class OpenAction extends TextEditorAction {

		public OpenAction() {
			super("Open", ImageUtils.getIcon("org/maia/swing/icons/text/open16.png"),
					ImageUtils.getIcon("org/maia/swing/icons/text/open32.png"));
			setEnabled(true);
		}

		@Override
		protected void doAction(PlainTextEditor editor) {
			File file = PlainTextFileDocument.selectFileToOpen();
			if (file != null) {
				try {
					editor.openDocument(editor.getEditorKit().createFileDocument(file));
				} catch (PlainTextDocumentException error) {
					editor.showErrorMessageDialog("Error", "Error while opening document", error);
				}
			}
		}

	}

	private class CloseAction extends TextEditorAction {

		public CloseAction() {
			super("Close", ImageUtils.getIcon("org/maia/swing/icons/text/close16.png"),
					ImageUtils.getIcon("org/maia/swing/icons/text/close32.png"));
			updateEnablement();
			getEditor().addListener(new CloseActionEnabler());
		}

		@Override
		protected void doAction(PlainTextEditor editor) {
			editor.closeDocument(editor.getActiveDocument());
		}

		private void updateEnablement() {
			setEnabled(getEditor().getActiveDocument() != null);
		}

		private class CloseActionEnabler extends PlainTextEditorAdapter {

			@Override
			public void documentOpened(PlainTextDocumentEditor documentEditor, PlainTextEditor editor) {
				updateEnablement();
			}

			@Override
			public void documentClosed(PlainTextDocumentEditor documentEditor, PlainTextEditor editor) {
				updateEnablement();
			}

		}

	}

	private class CloseAllAction extends TextEditorAction {

		public CloseAllAction() {
			super("Close all", ImageUtils.getIcon("org/maia/swing/icons/text/closeall16.png"),
					ImageUtils.getIcon("org/maia/swing/icons/text/closeall32.png"));
			updateEnablement();
			getEditor().addListener(new CloseAllActionEnabler());
		}

		@Override
		protected void doAction(PlainTextEditor editor) {
			editor.closeAllDocuments();
		}

		private void updateEnablement() {
			setEnabled(getEditor().hasDocuments());
		}

		private class CloseAllActionEnabler extends PlainTextEditorAdapter {

			@Override
			public void documentOpened(PlainTextDocumentEditor documentEditor, PlainTextEditor editor) {
				updateEnablement();
			}

			@Override
			public void documentClosed(PlainTextDocumentEditor documentEditor, PlainTextEditor editor) {
				updateEnablement();
			}

		}

	}

	private class QuitAction extends TextEditorAction {

		public QuitAction() {
			super("Quit", ImageUtils.getIcon("org/maia/swing/icons/quit16.png"),
					ImageUtils.getIcon("org/maia/swing/icons/quit32.png"));
			setEnabled(true);
		}

		@Override
		protected void doAction(PlainTextEditor editor) {
			PlainTextEditorFrame frame = editor.getParentFrame();
			if (frame != null) {
				frame.close();
			} else {
				editor.closeAllDocuments();
				System.exit(0);
			}
		}

	}

}