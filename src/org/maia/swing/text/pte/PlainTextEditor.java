package org.maia.swing.text.pte;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.maia.graphics2d.image.ImageUtils;
import org.maia.swing.text.pte.menu.PlainTextEditorMenuManager;
import org.maia.swing.text.pte.model.PlainTextDocument;
import org.maia.swing.text.pte.search.FindReplaceDialog;

public class PlainTextEditor {

	private boolean multiDocumentEnabled;

	private PlainTextEditorKit editorKit;

	private TextEditorPane editorPane;

	private List<PlainTextDocumentEditor> documentEditors;

	private List<PlainTextEditorListener> listeners;

	private PlainTextDocument activeDocument;

	private PlainTextEditorActions actions;

	private PlainTextEditorMenuManager menuManager;

	private FindReplaceDialog findReplaceDialog;

	public PlainTextEditor() {
		this(true);
	}

	public PlainTextEditor(boolean multiDocumentEnabled) {
		this(new Dimension(600, 400), multiDocumentEnabled);
	}

	public PlainTextEditor(Dimension preferredSize) {
		this(preferredSize, true);
	}

	public PlainTextEditor(Dimension preferredSize, boolean multiDocumentEnabled) {
		this(PlainTextEditorKit.getDefaultEditorKit(), preferredSize, multiDocumentEnabled);
	}

	public PlainTextEditor(PlainTextEditorKit editorKit, Dimension preferredSize, boolean multiDocumentEnabled) {
		this.multiDocumentEnabled = multiDocumentEnabled;
		this.editorKit = editorKit;
		this.editorPane = new TextEditorPane(preferredSize);
		this.documentEditors = new Vector<PlainTextDocumentEditor>();
		this.listeners = new Vector<PlainTextEditorListener>();
		this.actions = editorKit.createEditorActions(this);
		this.menuManager = editorKit.createEditorMenuManager(this);
		updateMenus();
	}

	public PlainTextEditorFrame showInFrame(String frameTitle, boolean closeDocumentsOnClose, boolean exitOnClose) {
		PlainTextEditorFrame frame = getParentFrame();
		if (frame == null) {
			frame = getEditorKit().createEditorFrame(this, frameTitle, closeDocumentsOnClose, exitOnClose);
			updateMenus();
		} else {
			frame.init(frameTitle, closeDocumentsOnClose, exitOnClose);
		}
		frame.setVisible(true);
		frame.toFront();
		return frame;
	}

	public void installToolBar(JComponent toolBar) {
		getEditorPane().installToolBar(toolBar);
	}

	public void uninstallToolBar() {
		getEditorPane().uninstallToolBar();
	}

	public void addListener(PlainTextEditorListener listener) {
		getListeners().add(listener);
	}

	public void removeListener(PlainTextEditorListener listener) {
		getListeners().remove(listener);
	}

	public synchronized PlainTextDocumentEditor openDocument(PlainTextDocument document)
			throws PlainTextDocumentException {
		PlainTextDocumentEditor documentEditor = getDocumentEditor(document);
		if (documentEditor == null) {
			// Not yet opened in this editor
			documentEditor = getEditorKit().createDocumentEditor(document, this);
			if (!isMultiDocumentEnabled() && hasDocuments()) {
				closeAllDocuments();
			}
			addDocumentEditor(documentEditor);
			activateDocument(document);
			fireDocumentOpened(documentEditor);
		} else {
			// Already opened
			activateDocument(document);
		}
		return documentEditor;
	}

	public synchronized final void closeDocument(PlainTextDocument document) {
		closeDocument(document, true);
	}

	public synchronized void closeDocument(PlainTextDocument document, boolean promptBeforeSave) {
		PlainTextDocumentEditor documentEditor = getDocumentEditor(document);
		if (documentEditor != null) {
			try {
				documentEditor.close(promptBeforeSave);
				takeOut(documentEditor);
				fireDocumentClosed(documentEditor);
			} catch (PlainTextDocumentCancellation cancellation) {
				// cancelled, do nothing
			} catch (PlainTextDocumentException error) {
				showErrorMessageDialog("Error", "Error while closing document", error);
			}
		}
	}

	public synchronized final void closeAllDocuments() {
		closeAllDocuments(true);
	}

	public synchronized void closeAllDocuments(boolean promptBeforeSave) {
		// First clean documents
		for (PlainTextDocumentEditor editor : getCleanDocumentEditors()) {
			closeDocument(editor.getDocument(), promptBeforeSave);
		}
		// Then dirty documents
		for (PlainTextDocumentEditor editor : getDirtyDocumentEditors()) {
			closeDocument(editor.getDocument(), promptBeforeSave);
		}
	}

	public synchronized void discardDocument(PlainTextDocument document) {
		PlainTextDocumentEditor documentEditor = getDocumentEditor(document);
		if (documentEditor != null) {
			documentEditor.discard();
			takeOut(documentEditor);
			fireDocumentDiscarded(documentEditor);
		}
	}

	public synchronized void discardAllDocuments() {
		List<PlainTextDocumentEditor> editors = new Vector<PlainTextDocumentEditor>(getDocumentEditors());
		for (PlainTextDocumentEditor editor : editors) {
			discardDocument(editor.getDocument());
		}
	}

	private void takeOut(PlainTextDocumentEditor documentEditor) {
		PlainTextDocument document = documentEditor.getDocument();
		removeDocumentEditor(documentEditor);
		if (document.equals(getActiveDocument())) {
			PlainTextDocumentEditor activeEditor = getEditorPane().getActiveDocumentEditor();
			if (activeEditor != null) {
				setActiveDocument(activeEditor.getDocument());
				fireDocumentDeactivated(documentEditor);
				fireDocumentActivated(activeEditor);
				activeEditor.grabFocus();
			} else {
				setActiveDocument(null);
				fireDocumentDeactivated(documentEditor);
			}
		}
	}

	public synchronized void activateDocument(PlainTextDocument document) {
		if (!document.equals(getActiveDocument())) {
			PlainTextDocumentEditor documentEditor = getDocumentEditor(document);
			if (documentEditor != null) {
				PlainTextDocumentEditor deactivatedEditor = getActiveDocumentEditor();
				getEditorPane().activateDocumentEditor(documentEditor);
				setActiveDocument(document);
				fireDocumentDeactivated(deactivatedEditor);
				fireDocumentActivated(documentEditor);
				documentEditor.grabFocus();
			}
		}
	}

	protected void updateMenus() {
		getMenuManager().updateMenus();
	}

	public void showInfoMessageDialog(String dialogMessage) {
		JOptionPane.showMessageDialog(getUI(), dialogMessage);
	}

	public void showErrorMessageDialog(String dialogTitle, String dialogMessage) {
		JOptionPane.showMessageDialog(getUI(), dialogMessage, dialogTitle, JOptionPane.ERROR_MESSAGE);
	}

	public void showErrorMessageDialog(String dialogTitle, String dialogMessage, Exception error) {
		showErrorMessageDialog(dialogTitle, dialogMessage + "\n" + error.getMessage());
	}

	private void addDocumentEditor(PlainTextDocumentEditor documentEditor) {
		getEditorPane().addDocumentEditor(documentEditor);
		getDocumentEditors().add(documentEditor);
	}

	private void removeDocumentEditor(PlainTextDocumentEditor documentEditor) {
		getEditorPane().removeDocumentEditor(documentEditor);
		getDocumentEditors().remove(documentEditor);
	}

	private void fireDocumentOpened(PlainTextDocumentEditor documentEditor) {
		for (PlainTextEditorListener listener : getListeners()) {
			listener.documentOpened(documentEditor, this);
		}
	}

	private void fireDocumentClosed(PlainTextDocumentEditor documentEditor) {
		for (PlainTextEditorListener listener : getListeners()) {
			listener.documentClosed(documentEditor, this);
		}
	}

	private void fireDocumentDiscarded(PlainTextDocumentEditor documentEditor) {
		for (PlainTextEditorListener listener : getListeners()) {
			listener.documentDiscarded(documentEditor, this);
		}
	}

	private void fireDocumentActivated(PlainTextDocumentEditor documentEditor) {
		for (PlainTextEditorListener listener : getListeners()) {
			listener.documentActivated(documentEditor, this);
		}
	}

	private void fireDocumentDeactivated(PlainTextDocumentEditor documentEditor) {
		for (PlainTextEditorListener listener : getListeners()) {
			listener.documentDeactivated(documentEditor, this);
		}
	}

	public PlainTextDocumentEditor getActiveDocumentEditor() {
		PlainTextDocument document = getActiveDocument();
		if (document != null) {
			return getDocumentEditor(document);
		} else {
			return null;
		}
	}

	public PlainTextDocumentEditor getDocumentEditor(PlainTextDocument document) {
		for (PlainTextDocumentEditor editor : getDocumentEditors()) {
			if (editor.getDocument().equals(document))
				return editor;
		}
		return null;
	}

	public boolean isOpened(PlainTextDocument document) {
		return getDocumentEditor(document) != null;
	}

	public boolean hasDocuments() {
		return getDocumentCount() > 0;
	}

	public int getDocumentCount() {
		return getDocumentEditors().size();
	}

	public JComponent getUI() {
		return getEditorPane();
	}

	public PlainTextEditorFrame getParentFrame() {
		Window window = SwingUtilities.getWindowAncestor(getUI());
		if (window != null && window instanceof PlainTextEditorFrame) {
			return (PlainTextEditorFrame) window;
		} else {
			return null;
		}
	}

	public FindReplaceDialog getFindReplaceDialog() {
		if (findReplaceDialog == null) {
			findReplaceDialog = createFindReplaceDialog();
		}
		return findReplaceDialog;
	}

	protected FindReplaceDialog createFindReplaceDialog() {
		return new FindReplaceDialog(getParentFrame());
	}

	public JComponent getToolBar() {
		return getEditorPane().getToolBar();
	}

	public Clipboard getClipboard() {
		return Toolkit.getDefaultToolkit().getSystemClipboard();
	}

	public boolean isClipboardEmpty() {
		try {
			return getClipboard().getContents(null) == null;
		} catch (IllegalStateException e) {
			return true; // clipboard unavailable
		}
	}

	public boolean isMultiDocumentEnabled() {
		return multiDocumentEnabled;
	}

	public PlainTextEditorKit getEditorKit() {
		return editorKit;
	}

	private TextEditorPane getEditorPane() {
		return editorPane;
	}

	private List<PlainTextDocumentEditor> getCleanDocumentEditors() {
		List<PlainTextDocumentEditor> editors = new Vector<PlainTextDocumentEditor>();
		for (PlainTextDocumentEditor editor : getDocumentEditors()) {
			if (!editor.isTextChangedSinceLastSave())
				editors.add(editor);
		}
		return editors;
	}

	private List<PlainTextDocumentEditor> getDirtyDocumentEditors() {
		List<PlainTextDocumentEditor> editors = new Vector<PlainTextDocumentEditor>();
		for (PlainTextDocumentEditor editor : getDocumentEditors()) {
			if (editor.isTextChangedSinceLastSave())
				editors.add(editor);
		}
		return editors;
	}

	public Iterator<PlainTextDocumentEditor> getDocumentEditorsIterator() {
		return Collections.unmodifiableList(getDocumentEditors()).iterator();
	}

	protected List<PlainTextDocumentEditor> getDocumentEditors() {
		return documentEditors;
	}

	protected List<PlainTextEditorListener> getListeners() {
		return listeners;
	}

	public PlainTextDocument getActiveDocument() {
		return activeDocument;
	}

	private void setActiveDocument(PlainTextDocument document) {
		this.activeDocument = document;
	}

	public PlainTextEditorActions getActions() {
		return actions;
	}

	public PlainTextEditorMenuManager getMenuManager() {
		return menuManager;
	}

	@SuppressWarnings("serial")
	private class TextEditorPane extends JPanel {

		private JComponent toolBar;

		private JLabel noDocumentsFiller;

		private JTabbedPane tabbedPane;

		private DocumentTabTitleUpdater tabTitleUpdater;

		public TextEditorPane(Dimension preferredSize) {
			super(new BorderLayout());
			this.noDocumentsFiller = buildNoDocumentsFiller();
			this.tabbedPane = buildTabbedPane();
			this.tabTitleUpdater = new DocumentTabTitleUpdater();
			add(getNoDocumentsFiller(), BorderLayout.CENTER);
			setPreferredSize(preferredSize);
		}

		private JLabel buildNoDocumentsFiller() {
			return new JLabel();
		}

		private JTabbedPane buildTabbedPane() {
			JTabbedPane pane = new JTabbedPane();
			pane.setFocusable(false);
			return pane;
		}

		public void installToolBar(JComponent toolBar) {
			uninstallToolBar(); // if any
			setToolBar(toolBar);
			add(toolBar, BorderLayout.NORTH);
			validate();
			repaint();
		}

		public void uninstallToolBar() {
			JComponent toolBar = getToolBar();
			if (toolBar != null) {
				setToolBar(null);
				remove(toolBar);
				validate();
				repaint();
			}
		}

		public synchronized void addDocumentEditor(PlainTextDocumentEditor documentEditor) {
			JComponent docUI = documentEditor.getUI();
			if (!hasDocuments()) {
				remove(getNoDocumentsFiller());
				if (isMultiDocumentEnabled()) {
					add(getTabbedPane(), BorderLayout.CENTER);
				} else {
					add(docUI, BorderLayout.CENTER);
				}
			}
			if (isMultiDocumentEnabled()) {
				getTabbedPane().addTab(null, docUI);
				updateTabTitle(documentEditor);
				documentEditor.addListener(getTabTitleUpdater());
			}
			validate();
			repaint();
		}

		public synchronized void removeDocumentEditor(PlainTextDocumentEditor documentEditor) {
			if (isMultiDocumentEnabled()) {
				int tabIndex = getTabIndex(documentEditor);
				if (tabIndex >= 0) {
					getTabbedPane().removeTabAt(tabIndex);
					documentEditor.removeListener(getTabTitleUpdater());
					if (getTabbedPane().getTabCount() == 0) {
						remove(getTabbedPane());
						add(getNoDocumentsFiller(), BorderLayout.CENTER);
					}
				}
			} else {
				remove(documentEditor.getUI());
				add(getNoDocumentsFiller(), BorderLayout.CENTER);
			}
			validate();
			repaint();
		}

		public synchronized void activateDocumentEditor(PlainTextDocumentEditor documentEditor) {
			if (isMultiDocumentEnabled()) {
				int tabIndex = getTabIndex(documentEditor);
				if (tabIndex >= 0) {
					getTabbedPane().setSelectedIndex(tabIndex);
				}
			}
		}

		private void updateTabTitle(final PlainTextDocumentEditor documentEditor) {
			int tabIndex = getTabIndex(documentEditor);
			if (tabIndex >= 0) {
				DocumentTabTitleComponent comp = new DocumentTabTitleComponent(documentEditor);
				getTabbedPane().setBackgroundAt(tabIndex, getEditorKit().getColorForDocumentEditor(documentEditor));
				getTabbedPane().setTabComponentAt(tabIndex, comp);
				comp.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent e) {
						activateDocument(documentEditor.getDocument());
					}

				});
			}
		}

		public PlainTextDocumentEditor getActiveDocumentEditor() {
			if (!hasDocuments()) {
				return null;
			} else if (isMultiDocumentEnabled()) {
				return getDocumentEditors().get(getTabbedPane().getSelectedIndex());
			} else {
				return getDocumentEditors().get(0);
			}
		}

		private int getTabIndex(PlainTextDocumentEditor documentEditor) {
			return getTabbedPane().indexOfComponent(documentEditor.getUI());
		}

		public JComponent getToolBar() {
			return toolBar;
		}

		private void setToolBar(JComponent toolBar) {
			this.toolBar = toolBar;
		}

		private JLabel getNoDocumentsFiller() {
			return noDocumentsFiller;
		}

		private JTabbedPane getTabbedPane() {
			return tabbedPane;
		}

		private DocumentTabTitleUpdater getTabTitleUpdater() {
			return tabTitleUpdater;
		}

		private class DocumentTabTitleComponent extends Box {

			public DocumentTabTitleComponent(PlainTextDocumentEditor documentEditor) {
				super(BoxLayout.X_AXIS);
				buildUI(documentEditor);
				setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
			}

			private void buildUI(PlainTextDocumentEditor documentEditor) {
				Icon icon = getEditorKit().getIconForDocumentEditor(documentEditor);
				if (icon != null) {
					add(new JLabel(icon));
					add(Box.createHorizontalStrut(4));
				}
				if (!documentEditor.isEditable()) {
					add(new JLabel(ImageUtils.getIcon("org/maia/swing/icons/lockClosed-white16.png")));
					add(Box.createHorizontalStrut(4));
				}
				add(buildTabLabel(documentEditor));
				add(Box.createHorizontalStrut(8));
				add(buildCloseTabButton(documentEditor));
				setToolTipText(getEditorKit().getToolTipForDocumentEditor(documentEditor));
			}

			private JLabel buildTabLabel(PlainTextDocumentEditor documentEditor) {
				JLabel label = new JLabel(getEditorKit().getTitleForDocumentEditor(documentEditor));
				if (documentEditor.getDocument().isDraft()) {
					label.setFont(label.getFont().deriveFont(Font.ITALIC));
				}
				return label;
			}

			private JButton buildCloseTabButton(final PlainTextDocumentEditor documentEditor) {
				final Icon icon = ImageUtils.getIcon("org/maia/swing/icons/closeTab8.png");
				final Icon iconFocus = ImageUtils.getIcon("org/maia/swing/icons/closeTabFocus8.png");
				final JButton button = new JButton(icon);
				button.setContentAreaFilled(false);
				button.setOpaque(false);
				button.setFocusPainted(false);
				button.setBorderPainted(false);
				button.setMargin(new Insets(0, 0, 0, 0));
				button.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseEntered(MouseEvent e) {
						button.setIcon(iconFocus);
					}

					@Override
					public void mouseExited(MouseEvent e) {
						button.setIcon(icon);
					}

				});
				button.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						closeDocument(documentEditor.getDocument());
					}
				});
				return button;
			}

		}

		private class DocumentTabTitleUpdater extends PlainTextDocumentEditorAdapter {

			public DocumentTabTitleUpdater() {
			}

			@Override
			public void documentNameChanged(PlainTextDocumentEditor documentEditor) {
				updateTabTitle(documentEditor);
			}

			@Override
			public void documentEditableChanged(PlainTextDocumentEditor documentEditor) {
				updateTabTitle(documentEditor);
			}

			@Override
			public void documentHasUnsavedChanges(PlainTextDocumentEditor documentEditor) {
				updateTabTitle(documentEditor);
			}

			@Override
			public void documentSaved(PlainTextDocumentEditor documentEditor) {
				updateTabTitle(documentEditor);
			}

		}

	}

}