package org.maia.swing.text.pte;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.maia.graphics2d.image.ImageUtils;

@SuppressWarnings("serial")
public class PlainTextDocumentEditorActions {

	private PlainTextDocumentEditor documentEditor;

	private PlainTextEditor editor;

	private Action saveAction;

	private Action revertAction;

	private Action cutAction;

	private Action copyAction;

	private Action pasteAction;

	private Action selectAllAction;

	private Action undoAction;

	private Action redoAction;

	private Action findReplaceAction;

	private Action wrapLinesAction;

	public PlainTextDocumentEditorActions(PlainTextDocumentEditor documentEditor, PlainTextEditor editor) {
		this.documentEditor = documentEditor;
		this.editor = editor;
	}

	public final Action getSaveAction() {
		if (saveAction == null) {
			saveAction = createSaveAction();
		}
		return saveAction;
	}

	protected Action createSaveAction() {
		return new SaveAction();
	}

	public final Action getRevertAction() {
		if (revertAction == null) {
			revertAction = createRevertAction();
		}
		return revertAction;
	}

	protected Action createRevertAction() {
		return new RevertAction();
	}

	public final Action getCutAction() {
		if (cutAction == null) {
			cutAction = createCutAction();
		}
		return cutAction;
	}

	protected Action createCutAction() {
		return new CutAction();
	}

	public final Action getCopyAction() {
		if (copyAction == null) {
			copyAction = createCopyAction();
		}
		return copyAction;
	}

	protected Action createCopyAction() {
		return new CopyAction();
	}

	public final Action getPasteAction() {
		if (pasteAction == null) {
			pasteAction = createPasteAction();
		}
		return pasteAction;
	}

	protected Action createPasteAction() {
		return new PasteAction();
	}

	public final Action getSelectAllAction() {
		if (selectAllAction == null) {
			selectAllAction = createSelectAllAction();
		}
		return selectAllAction;
	}

	protected Action createSelectAllAction() {
		return new SelectAllAction();
	}

	public final Action getUndoAction() {
		if (undoAction == null) {
			undoAction = createUndoAction();
		}
		return undoAction;
	}

	protected Action createUndoAction() {
		return new UndoAction();
	}

	public final Action getRedoAction() {
		if (redoAction == null) {
			redoAction = createRedoAction();
		}
		return redoAction;
	}

	protected Action createRedoAction() {
		return new RedoAction();
	}

	public final Action getFindReplaceAction() {
		if (findReplaceAction == null) {
			findReplaceAction = createFindReplaceAction();
		}
		return findReplaceAction;
	}

	protected Action createFindReplaceAction() {
		return new FindReplaceAction();
	}

	public final Action getWrapLinesAction() {
		if (wrapLinesAction == null) {
			wrapLinesAction = createWrapLinesAction();
		}
		return wrapLinesAction;
	}

	protected Action createWrapLinesAction() {
		return new WrapLinesAction();
	}

	public PlainTextDocumentEditor getDocumentEditor() {
		return documentEditor;
	}

	public PlainTextEditor getEditor() {
		return editor;
	}

	protected abstract class DocumentEditorAction extends AbstractAction {

		protected DocumentEditorAction(String name, Icon icon) {
			this(name, icon, icon);
		}

		protected DocumentEditorAction(String name, Icon smallIcon, Icon largeIcon) {
			super(name, smallIcon);
			changeLargeIcon(largeIcon);
			changeToolTipText(name);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			PlainTextDocumentEditor documentEditor = getDocumentEditor();
			doAction(documentEditor);
			documentEditor.grabFocus();
		}

		protected abstract void doAction(PlainTextDocumentEditor documentEditor);

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

	private class SaveAction extends DocumentEditorAction {

		public SaveAction() {
			super("Save", ImageUtils.getIcon("org/maia/swing/icons/text/save16.png"),
					ImageUtils.getIcon("org/maia/swing/icons/text/save32.png"));
			updateEnablement();
			getDocumentEditor().addListener(new SaveActionEnabler());
		}

		@Override
		protected void doAction(PlainTextDocumentEditor documentEditor) {
			try {
				documentEditor.save();
			} catch (PlainTextDocumentCancellation cancellation) {
				// cancelled, do nothing
			} catch (PlainTextDocumentException error) {
				JOptionPane.showMessageDialog(documentEditor.getUI(),
						"Error while saving document\n" + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

		private void updateEnablement() {
			setEnabled(getDocumentEditor().isTextChangedSinceLastSave());
		}

		private class SaveActionEnabler extends PlainTextDocumentEditorAdapter {

			@Override
			public void documentHasUnsavedChanges(PlainTextDocumentEditor documentEditor) {
				updateEnablement();
			}

			@Override
			public void documentSaved(PlainTextDocumentEditor documentEditor) {
				updateEnablement();
			}

		}

	}

	private class RevertAction extends DocumentEditorAction {

		public RevertAction() {
			super("Revert", ImageUtils.getIcon("org/maia/swing/icons/text/revert16.png"),
					ImageUtils.getIcon("org/maia/swing/icons/text/revert32.png"));
			updateEnablement();
			getDocumentEditor().addListener(new RevertActionEnabler());
		}

		@Override
		protected void doAction(PlainTextDocumentEditor documentEditor) {
			try {
				documentEditor.revert();
			} catch (PlainTextDocumentException error) {
				JOptionPane.showMessageDialog(documentEditor.getUI(),
						"Error while reverting document\n" + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

		private void updateEnablement() {
			setEnabled(getDocumentEditor().isTextChangedSinceLastSave());
		}

		private class RevertActionEnabler extends PlainTextDocumentEditorAdapter {

			@Override
			public void documentHasUnsavedChanges(PlainTextDocumentEditor documentEditor) {
				updateEnablement();
			}

			@Override
			public void documentSaved(PlainTextDocumentEditor documentEditor) {
				updateEnablement();
			}

		}

	}

	private class CutAction extends DocumentEditorAction {

		public CutAction() {
			super("Cut", ImageUtils.getIcon("org/maia/swing/icons/text/cut16.png"),
					ImageUtils.getIcon("org/maia/swing/icons/text/cut32.png"));
			updateEnablement();
			getDocumentEditor().addListener(new CutActionEnabler());
		}

		@Override
		protected void doAction(PlainTextDocumentEditor documentEditor) {
			documentEditor.cut();
		}

		private void updateEnablement() {
			setEnabled(getDocumentEditor().canCut());
		}

		private class CutActionEnabler extends PlainTextDocumentEditorAdapter {

			@Override
			public void documentSelectionChanged(PlainTextDocumentEditor documentEditor) {
				updateEnablement();
			}

			@Override
			public void documentEditableChanged(PlainTextDocumentEditor documentEditor) {
				updateEnablement();
			}

		}

	}

	private class CopyAction extends DocumentEditorAction {

		public CopyAction() {
			super("Copy", ImageUtils.getIcon("org/maia/swing/icons/text/copy16.png"),
					ImageUtils.getIcon("org/maia/swing/icons/text/copy32.png"));
			updateEnablement();
			getDocumentEditor().addListener(new CopyActionEnabler());
		}

		@Override
		protected void doAction(PlainTextDocumentEditor documentEditor) {
			documentEditor.copy();
		}

		private void updateEnablement() {
			setEnabled(getDocumentEditor().canCopy());
		}

		private class CopyActionEnabler extends PlainTextDocumentEditorAdapter {

			@Override
			public void documentSelectionChanged(PlainTextDocumentEditor documentEditor) {
				updateEnablement();
			}

		}

	}

	private class PasteAction extends DocumentEditorAction {

		public PasteAction() {
			super("Paste", ImageUtils.getIcon("org/maia/swing/icons/text/paste16.png"),
					ImageUtils.getIcon("org/maia/swing/icons/text/paste32.png"));
			updateEnablement();
			getDocumentEditor().addListener(new PasteActionManager());
		}

		@Override
		protected void doAction(PlainTextDocumentEditor documentEditor) {
			documentEditor.paste();
		}

		private void updateEnablement() {
			setEnabled(getDocumentEditor().canPaste());
		}

		private class PasteActionManager extends PlainTextDocumentEditorAdapter {

			@Override
			public void documentEditableChanged(PlainTextDocumentEditor documentEditor) {
				updateEnablement();
			}

		}

	}

	private class SelectAllAction extends DocumentEditorAction {

		public SelectAllAction() {
			super("Select all", ImageUtils.getIcon("org/maia/swing/icons/text/select16.png"),
					ImageUtils.getIcon("org/maia/swing/icons/text/select32.png"));
			setEnabled(true);
		}

		@Override
		protected void doAction(PlainTextDocumentEditor documentEditor) {
			documentEditor.selectAllText();
		}

	}

	private class UndoAction extends DocumentEditorAction {

		public UndoAction() {
			super("Undo", ImageUtils.getIcon("org/maia/swing/icons/text/undo16.png"),
					ImageUtils.getIcon("org/maia/swing/icons/text/undo32.png"));
			updateEnablement();
			getDocumentEditor().addListener(new UndoActionEnabler());
		}

		@Override
		protected void doAction(PlainTextDocumentEditor documentEditor) {
			getDocumentEditor().undo();
		}

		private void updateEnablement() {
			setEnabled(getDocumentEditor().canUndo());
		}

		private class UndoActionEnabler extends PlainTextDocumentEditorAdapter {

			@Override
			public void documentUndoableStateChanged(PlainTextDocumentEditor documentEditor) {
				updateEnablement();
				changeName(documentEditor.getUndoPresentationName());
			}

			@Override
			public void documentEditableChanged(PlainTextDocumentEditor documentEditor) {
				updateEnablement();
			}

		}

	}

	private class RedoAction extends DocumentEditorAction {

		public RedoAction() {
			super("Redo", ImageUtils.getIcon("org/maia/swing/icons/text/redo16.png"),
					ImageUtils.getIcon("org/maia/swing/icons/text/redo32.png"));
			updateEnablement();
			getDocumentEditor().addListener(new RedoActionEnabler());
		}

		@Override
		protected void doAction(PlainTextDocumentEditor documentEditor) {
			getDocumentEditor().redo();
		}

		private void updateEnablement() {
			setEnabled(getDocumentEditor().canRedo());
		}

		private class RedoActionEnabler extends PlainTextDocumentEditorAdapter {

			@Override
			public void documentUndoableStateChanged(PlainTextDocumentEditor documentEditor) {
				updateEnablement();
				changeName(documentEditor.getRedoPresentationName());
			}

			@Override
			public void documentEditableChanged(PlainTextDocumentEditor documentEditor) {
				updateEnablement();
			}

		}

	}

	private class FindReplaceAction extends DocumentEditorAction {

		public FindReplaceAction() {
			super("Find and replace...", ImageUtils.getIcon("org/maia/swing/icons/text/search16.png"),
					ImageUtils.getIcon("org/maia/swing/icons/text/search32.png"));
			setEnabled(true);
		}

		@Override
		protected void doAction(PlainTextDocumentEditor documentEditor) {
			getEditor().getFindReplaceDialog().show(documentEditor);
		}

	}

	private class WrapLinesAction extends DocumentEditorAction {

		public WrapLinesAction() {
			super("Wrap lines", ImageUtils.getIcon("org/maia/swing/icons/text/wrap16.png"),
					ImageUtils.getIcon("org/maia/swing/icons/text/wrap32.png"));
			setEnabled(true);
		}

		@Override
		protected void doAction(PlainTextDocumentEditor documentEditor) {
			if (documentEditor.isWrapLines()) {
				documentEditor.unwrapLines();
			} else {
				documentEditor.wrapLines();
			}
		}

	}

}