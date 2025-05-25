package org.maia.swing.text.pte;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import org.maia.swing.text.pte.model.PlainTextDocument;
import org.maia.swing.text.pte.model.PlainTextDocumentListener;
import org.maia.swing.text.pte.search.StyledTextSearchResult;
import org.maia.swing.text.pte.search.TextSearchCommand;
import org.maia.swing.text.pte.search.TextSearchMatch;
import org.maia.swing.text.pte.search.TextSearchResultPlacement;
import org.maia.swing.text.pte.search.TextSearchResultStyling;

public class PlainTextDocumentEditor implements PlainTextDocumentListener {

	private PlainTextDocument document;

	private DocumentEditorPane documentPane;

	private UndoManager undoManager;

	private List<PlainTextDocumentEditorListener> listeners;

	private PlainTextDocumentEditorActions actions;

	private boolean textChangedSinceLastSave;

	private static final String COMMAND_KEY_SAVE = "__save";

	private static final String COMMAND_KEY_CUT = "__cut";

	private static final String COMMAND_KEY_COPY = "__copy";

	private static final String COMMAND_KEY_PASTE = "__paste";

	private static final String COMMAND_KEY_SELECT_ALL = "__selectAll";

	private static final String COMMAND_KEY_UNDO = "__undo";

	private static final String COMMAND_KEY_REDO = "__redo";

	private static final String COMMAND_KEY_FIND = "__find";

	public PlainTextDocumentEditor(PlainTextDocument document, Dimension preferredSize)
			throws PlainTextDocumentException {
		this.document = document;
		this.documentPane = new DocumentEditorPane(preferredSize);
		this.undoManager = new UndoManager();
		this.listeners = new Vector<PlainTextDocumentEditorListener>();
		getInternalDocument().addUndoableEditListener(getUndoManager());
		getInternalDocument().addUndoableEditListener(new DocumentUndoableEditObserver());
		initLookAndFeel();
	}

	protected void initLookAndFeel() {
		setBackgroundColor(Color.WHITE);
		setTextColor(Color.BLACK);
		setSelectedTextColor(Color.WHITE);
		setSelectionColor(new Color(0, 120, 215));
		setCaretColor(Color.BLACK);
		setFont(getDefaultFont());
	}

	public void installPopupMenu(JPopupMenu popupMenu) {
		getDocumentPane().installPopupMenu(popupMenu);
	}

	public void uninstallPopupMenu() {
		getDocumentPane().uninstallPopupMenu();
	}

	public void grabFocus() {
		getTextArea().grabFocus();
	}

	public void showInFocus(int offset) {
		showInFocus(offset, 1);
	}

	public void showInFocus(int offset, int length) {
		try {
			Rectangle2D viewRect = getTextArea().modelToView2D(offset);
			viewRect.add(getTextArea().modelToView2D(Math.min(offset + length, getTextLength() - 1)));
			getDocumentPane().showInFocus(viewRect.getBounds());
		} catch (BadLocationException e) {
			// do nothing
		}
	}

	public void addListener(PlainTextDocumentEditorListener listener) {
		getListeners().add(listener);
	}

	public void removeListener(PlainTextDocumentEditorListener listener) {
		getListeners().remove(listener);
	}

	public synchronized void save() throws PlainTextDocumentException, PlainTextDocumentCancellation {
		if (isTextChangedSinceLastSave()) {
			getDocument().writeText(getText());
			markClean();
		}
	}

	public synchronized final void close() throws PlainTextDocumentException, PlainTextDocumentCancellation {
		close(true);
	}

	public synchronized void close(boolean promptBeforeSave)
			throws PlainTextDocumentException, PlainTextDocumentCancellation {
		if (isTextChangedSinceLastSave() && promptBeforeSave) {
			String docName = getDocument().getShortDocumentName();
			int optionSelected = JOptionPane.showConfirmDialog(getUI(), "Save '" + docName + "' before closing?",
					"Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (optionSelected == JOptionPane.YES_OPTION) {
				save();
			} else if (optionSelected == JOptionPane.CANCEL_OPTION) {
				throw new PlainTextDocumentCancellation(getDocument());
			}
		} else {
			save();
		}
		clearUndoHistory();
		fireClosed();
	}

	public synchronized void discard() {
		// Use discard with caution... any changes go lost
		clearUndoHistory();
		fireDiscarded();
	}

	public synchronized void revert() throws PlainTextDocumentException {
		revert(true);
	}

	public synchronized void revert(boolean promptBeforeRevert) throws PlainTextDocumentException {
		if (isTextChangedSinceLastSave() && promptBeforeRevert) {
			String docName = getDocument().getShortDocumentName();
			int optionSelected = JOptionPane.showConfirmDialog(getUI(),
					"Revert '" + docName + "' and undo all changes?", "Revert", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (optionSelected == JOptionPane.YES_OPTION) {
				doRevert();
			}
		} else {
			doRevert();
		}
	}

	protected void doRevert() throws PlainTextDocumentException {
		getTextArea().setText(getDocument().readText());
		setCaretPosition(0);
		clearUndoHistory();
		markClean();
	}

	@Override
	public void documentEditableChanged(PlainTextDocument document) {
		getTextArea().setEditable(isEditable());
		fireEditableChanged(); // propagate
		fireUndoableStateChanged(); // impacts can undo/redo
	}

	@Override
	public void documentNameChanged(PlainTextDocument document) {
		fireNameChanged(); // propagate
	}

	public Color getBackgroundColor() {
		return getTextArea().getBackground();
	}

	public void setBackgroundColor(Color color) {
		getTextArea().setBackground(color);
	}

	public Color getTextColor() {
		return getTextArea().getForeground();
	}

	public void setTextColor(Color color) {
		getTextArea().setForeground(color);
	}

	public Color getSelectionColor() {
		return getTextArea().getSelectionColor();
	}

	public void setSelectionColor(Color color) {
		getTextArea().setSelectionColor(color);
	}

	public Color getSelectedTextColor() {
		return getTextArea().getSelectedTextColor();
	}

	public void setSelectedTextColor(Color color) {
		getTextArea().setSelectedTextColor(color);
	}

	public Color getCaretColor() {
		return getTextArea().getCaretColor();
	}

	public void setCaretColor(Color color) {
		getTextArea().setCaretColor(color);
	}

	public int getCaretPosition() {
		return getCaret().getDot();
	}

	public void setCaretPosition(int offset) {
		getCaret().setDot(offset);
	}

	public Font getDefaultFont() {
		return Font.decode("Consolas-14");
	}

	public Font getFont() {
		return getTextArea().getFont();
	}

	public void setFont(Font font) {
		getTextArea().setFont(font);
	}

	public void wrapLines() {
		if (!isWrapLines()) {
			getTextArea().setLineWrap(true);
			fireWrapLinesChanged();
		}
	}

	public void unwrapLines() {
		if (isWrapLines()) {
			getTextArea().setLineWrap(false);
			fireWrapLinesChanged();
		}
	}

	public boolean isWrapLines() {
		return getTextArea().getLineWrap();
	}

	public boolean isEditable() {
		return getDocument().isEditable();
	}

	public void cut() {
		if (canCut()) {
			getTextArea().cut();
			fireUndoableStateChanged();
		}
	}

	public void copy() {
		if (canCopy()) {
			getTextArea().copy();
		}
	}

	public void paste() {
		if (canPaste()) {
			getTextArea().paste();
			fireUndoableStateChanged();
		}
	}

	public boolean canCut() {
		return hasSelectedText() && isEditable();
	}

	public boolean canCopy() {
		return hasSelectedText();
	}

	public boolean canPaste() {
		return isEditable();
	}

	public void clearUndoHistory() {
		getUndoManager().discardAllEdits();
		fireUndoableStateChanged();
	}

	public void undo() {
		if (canUndo()) {
			getUndoManager().undo();
			fireUndoableStateChanged();
		}
	}

	public void redo() {
		if (canRedo()) {
			getUndoManager().redo();
			fireUndoableStateChanged();
		}
	}

	public boolean canUndo() {
		return getUndoManager().canUndo() && isEditable();
	}

	public boolean canRedo() {
		return getUndoManager().canRedo() && isEditable();
	}

	public String getUndoPresentationName() {
		return getUndoManager().getUndoPresentationName();
	}

	public String getRedoPresentationName() {
		return getUndoManager().getRedoPresentationName();
	}

	public int getTextLength() {
		return getInternalDocument().getLength();
	}

	public String getText() {
		return getTextArea().getText();
	}

	public String getText(int offset, int length) {
		try {
			return getTextArea().getText(offset, length);
		} catch (BadLocationException e) {
			return "";
		}
	}

	public void setText(String text) {
		if (removeAllText()) {
			insertString(0, text);
		}
	}

	public boolean hasSelectedText() {
		String sel = getSelectedText();
		return sel != null && sel.length() > 0;
	}

	public String getSelectedText() {
		return getTextArea().getSelectedText();
	}

	public void clearTextSelection() {
		getCaret().setDot(getCaret().getDot());
	}

	public void selectTextSpan(int offset, int length) {
		getTextArea().select(offset, offset + length);
	}

	public void selectAllText() {
		getTextArea().selectAll();
	}

	public boolean type(String str) {
		return insertStringAtCaret(str, true);
	}

	public boolean insertStringAtCaret(String str, boolean advanceCaret) {
		int offset = getCaretPosition();
		boolean success = insertString(offset, str);
		if (success && advanceCaret) {
			setCaretPosition(offset + str.length());
		} else {
			setCaretPosition(offset);
		}
		return success;
	}

	public boolean insertString(int offset, String str) {
		boolean success = false;
		if (isEditable()) {
			try {
				if (str != null && !str.isEmpty()) {
					getInternalDocument().insertString(offset, str, null);
					markDirty();
					fireUndoableStateChanged();
				}
				success = true;
			} catch (BadLocationException e) {
				// returns false
			}
		}
		return success;
	}

	public boolean removeAllText() {
		return removeTextSpan(0, getTextLength());
	}

	public boolean removeTextSpan(int offset, int length) {
		boolean success = false;
		if (isEditable()) {
			try {
				if (length > 0) {
					getInternalDocument().remove(offset, length);
					markDirty();
					fireUndoableStateChanged();
				}
				success = true;
			} catch (BadLocationException e) {
				// returns false
			}
		}
		return success;
	}

	public StyledTextSearchResult search(TextSearchCommand command) {
		return search(command, TextSearchResultPlacement.MATCH_AFTER_CARET);
	}

	public StyledTextSearchResult search(TextSearchCommand command, TextSearchResultPlacement placement) {
		clearTextSelection(); // reserve highlights for matches only
		int caretPosition = getCaretPosition();
		TextSearchMatch initialMatch = null;
		List<TextSearchMatch> matches = new Vector<TextSearchMatch>();
		Matcher matcher = convertToPattern(command).matcher(getText());
		while (matcher.find()) {
			try {
				Position start = getInternalDocument().createPosition(matcher.start());
				Position end = getInternalDocument().createPosition(matcher.end());
				TextSearchMatchImpl match = new TextSearchMatchImpl(start, end);
				matches.add(match);
				if (TextSearchResultPlacement.MATCH_BEFORE_CARET.equals(placement) && matcher.start() < caretPosition) {
					initialMatch = match;
				} else if (TextSearchResultPlacement.MATCH_AFTER_CARET.equals(placement) && initialMatch == null
						&& matcher.start() >= caretPosition) {
					initialMatch = match;
				}
			} catch (BadLocationException e) {
				// should not happen (leave out)
			}
		}
		StyledTextSearchResult result = new StyledTextSearchResult(matches);
		if (initialMatch == null && !matches.isEmpty()) {
			if (TextSearchResultPlacement.MATCH_BEFORE_CARET.equals(placement)
					|| TextSearchResultPlacement.LAST_MATCH.equals(placement)) {
				initialMatch = matches.get(matches.size() - 1);
			} else {
				initialMatch = matches.get(0);
			}
		}
		result.setCurrentMatch(initialMatch);
		return result;
	}

	private Pattern convertToPattern(TextSearchCommand command) {
		int flags = 0;
		if (!command.isRegex())
			flags = flags | Pattern.LITERAL;
		if (!command.isCaseSensitive())
			flags = flags | Pattern.CASE_INSENSITIVE;
		return Pattern.compile(command.getSearchString(), flags);
	}

	public TextSearchResultStyling createDefaultSearchResultStyling() {
		return new TextSearchResultStyling(new Color(69, 176, 98), new Color(200, 247, 213));
	}

	public void installActions(PlainTextDocumentEditorActions actions) {
		setActions(actions);
		registerCustomKeyBindings(actions);
	}

	private void registerCustomKeyBindings(PlainTextDocumentEditorActions actions) {
		InputMap inputMap = getTextArea().getInputMap();
		ActionMap actionMap = getTextArea().getActionMap();
		registerCustomKeyBindings(actions, inputMap, actionMap);
	}

	protected void registerCustomKeyBindings(PlainTextDocumentEditorActions actions, InputMap inputMap,
			ActionMap actionMap) {
		registerSaveKeyBinding(actions, inputMap, actionMap);
		registerCutKeyBinding(actions, inputMap, actionMap);
		registerCopyKeyBinding(actions, inputMap, actionMap);
		registerPasteKeyBinding(actions, inputMap, actionMap);
		registerSelectAllKeyBinding(actions, inputMap, actionMap);
		registerUndoKeyBinding(actions, inputMap, actionMap);
		registerRedoKeyBinding(actions, inputMap, actionMap);
		registerFindKeyBinding(actions, inputMap, actionMap);
	}

	private void registerSaveKeyBinding(PlainTextDocumentEditorActions actions, InputMap inputMap,
			ActionMap actionMap) {
		inputMap.put(getKeyStrokeForSave(), COMMAND_KEY_SAVE);
		actionMap.put(COMMAND_KEY_SAVE, actions.getSaveAction());
	}

	private void registerCutKeyBinding(PlainTextDocumentEditorActions actions, InputMap inputMap, ActionMap actionMap) {
		inputMap.put(getKeyStrokeForCut(), COMMAND_KEY_CUT);
		actionMap.put(COMMAND_KEY_CUT, actions.getCutAction());
	}

	private void registerCopyKeyBinding(PlainTextDocumentEditorActions actions, InputMap inputMap,
			ActionMap actionMap) {
		inputMap.put(getKeyStrokeForCopy(), COMMAND_KEY_COPY);
		actionMap.put(COMMAND_KEY_COPY, actions.getCopyAction());
	}

	private void registerPasteKeyBinding(PlainTextDocumentEditorActions actions, InputMap inputMap,
			ActionMap actionMap) {
		inputMap.put(getKeyStrokeForPaste(), COMMAND_KEY_PASTE);
		actionMap.put(COMMAND_KEY_PASTE, actions.getPasteAction());
	}

	private void registerSelectAllKeyBinding(PlainTextDocumentEditorActions actions, InputMap inputMap,
			ActionMap actionMap) {
		inputMap.put(getKeyStrokeForSelectAll(), COMMAND_KEY_SELECT_ALL);
		actionMap.put(COMMAND_KEY_SELECT_ALL, actions.getSelectAllAction());
	}

	private void registerUndoKeyBinding(PlainTextDocumentEditorActions actions, InputMap inputMap,
			ActionMap actionMap) {
		inputMap.put(getKeyStrokeForUndo(), COMMAND_KEY_UNDO);
		actionMap.put(COMMAND_KEY_UNDO, actions.getUndoAction());
	}

	private void registerRedoKeyBinding(PlainTextDocumentEditorActions actions, InputMap inputMap,
			ActionMap actionMap) {
		inputMap.put(getKeyStrokeForRedo(), COMMAND_KEY_REDO);
		actionMap.put(COMMAND_KEY_REDO, actions.getRedoAction());
	}

	private void registerFindKeyBinding(PlainTextDocumentEditorActions actions, InputMap inputMap,
			ActionMap actionMap) {
		inputMap.put(getKeyStrokeForFind(), COMMAND_KEY_FIND);
		actionMap.put(COMMAND_KEY_FIND, actions.getFindReplaceAction());
	}

	public KeyStroke getKeyStrokeForSave() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
	}

	public KeyStroke getKeyStrokeForCut() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK);
	}

	public KeyStroke getKeyStrokeForCopy() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
	}

	public KeyStroke getKeyStrokeForPaste() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK);
	}

	public KeyStroke getKeyStrokeForSelectAll() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK);
	}

	public KeyStroke getKeyStrokeForUndo() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK);
	}

	public KeyStroke getKeyStrokeForRedo() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK);
	}

	public KeyStroke getKeyStrokeForFind() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);
	}

	protected void markDirty() {
		if (!isTextChangedSinceLastSave()) {
			setTextChangedSinceLastSave(true);
			fireUnsavedChanges();
		}
	}

	protected void markClean() {
		if (isTextChangedSinceLastSave()) {
			setTextChangedSinceLastSave(false);
			fireSaved();
		}
	}

	private void fireEditableChanged() {
		for (PlainTextDocumentEditorListener listener : getListeners()) {
			listener.documentEditableChanged(this);
		}
	}

	private void fireNameChanged() {
		for (PlainTextDocumentEditorListener listener : getListeners()) {
			listener.documentNameChanged(this);
		}
	}

	private void fireUnsavedChanges() {
		for (PlainTextDocumentEditorListener listener : getListeners()) {
			listener.documentHasUnsavedChanges(this);
		}
	}

	private void fireSaved() {
		for (PlainTextDocumentEditorListener listener : getListeners()) {
			listener.documentSaved(this);
		}
	}

	private void fireSelectionChanged() {
		for (PlainTextDocumentEditorListener listener : getListeners()) {
			listener.documentSelectionChanged(this);
		}
	}

	private void fireClosed() {
		for (PlainTextDocumentEditorListener listener : getListeners()) {
			listener.documentClosed(this);
		}
	}

	private void fireDiscarded() {
		for (PlainTextDocumentEditorListener listener : getListeners()) {
			listener.documentDiscarded(this);
		}
	}

	private void fireWrapLinesChanged() {
		for (PlainTextDocumentEditorListener listener : getListeners()) {
			listener.documentWrapLinesChanged(this);
		}
	}

	private void fireUndoableStateChanged() {
		for (PlainTextDocumentEditorListener listener : getListeners()) {
			listener.documentUndoableStateChanged(this);
		}
	}

	public JComponent getUI() {
		return getDocumentPane();
	}

	public PlainTextEditorFrame getParentFrame() {
		Window window = SwingUtilities.getWindowAncestor(getUI());
		if (window != null && window instanceof PlainTextEditorFrame) {
			return (PlainTextEditorFrame) window;
		} else {
			return null;
		}
	}

	public PlainTextDocument getDocument() {
		return document;
	}

	public PlainTextDocumentEditorActions getActions() {
		return actions;
	}

	private void setActions(PlainTextDocumentEditorActions actions) {
		this.actions = actions;
	}

	protected Caret getCaret() {
		return getTextArea().getCaret();
	}

	protected Document getInternalDocument() {
		return getTextArea().getDocument();
	}

	protected JTextArea getTextArea() {
		return getDocumentPane().getTextArea();
	}

	private DocumentEditorPane getDocumentPane() {
		return documentPane;
	}

	protected UndoManager getUndoManager() {
		return undoManager;
	}

	protected List<PlainTextDocumentEditorListener> getListeners() {
		return listeners;
	}

	public boolean isTextChangedSinceLastSave() {
		return textChangedSinceLastSave;
	}

	protected void setTextChangedSinceLastSave(boolean changed) {
		this.textChangedSinceLastSave = changed;
	}

	@SuppressWarnings("serial")
	private class DocumentEditorPane extends JPanel {

		private JTextArea textArea;

		private JScrollPane scrollPane;

		public DocumentEditorPane(Dimension preferredSize) throws PlainTextDocumentException {
			super(new BorderLayout());
			this.textArea = buildTextArea();
			this.scrollPane = buildScrollPane(preferredSize);
			add(getScrollPane(), BorderLayout.CENTER);
		}

		private JTextArea buildTextArea() throws PlainTextDocumentException {
			JTextArea area = new JTextArea(new PlainDocumentImpl(), getDocument().readText(), 0, 0);
			area.setEditable(getDocument().isEditable());
			area.setLineWrap(false);
			area.setWrapStyleWord(true);
			DocumentChangeObserver observer = new DocumentChangeObserver();
			area.getDocument().addDocumentListener(observer);
			area.addCaretListener(observer);
			return area;
		}

		private JScrollPane buildScrollPane(Dimension preferredSize) {
			JScrollPane pane = new JScrollPane(getTextArea());
			pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			pane.setPreferredSize(preferredSize);
			return pane;
		}

		public void installPopupMenu(JPopupMenu popupMenu) {
			getTextArea().setComponentPopupMenu(popupMenu);
		}

		public void uninstallPopupMenu() {
			getTextArea().setComponentPopupMenu(null);
		}

		public void showInFocus(Rectangle viewRect) {
			getTextArea().scrollRectToVisible(viewRect);
		}

		protected JTextArea getTextArea() {
			return textArea;
		}

		private JScrollPane getScrollPane() {
			return scrollPane;
		}

	}

	private class DocumentChangeObserver implements DocumentListener, CaretListener {

		private boolean selectionMade;

		public DocumentChangeObserver() {
		}

		@Override
		public void insertUpdate(DocumentEvent event) {
			documentTextChanged(event);
		}

		@Override
		public void removeUpdate(DocumentEvent event) {
			documentTextChanged(event);
		}

		@Override
		public void changedUpdate(DocumentEvent event) {
			// not interested in attribute changes (plain text)
		}

		@Override
		public synchronized void caretUpdate(CaretEvent event) {
			boolean selection = event.getDot() != event.getMark();
			if (selection || isSelectionMade()) {
				fireSelectionChanged();
			}
			setSelectionMade(selection);
		}

		private void documentTextChanged(DocumentEvent event) {
			markDirty();
		}

		private boolean isSelectionMade() {
			return selectionMade;
		}

		private void setSelectionMade(boolean selectionMade) {
			this.selectionMade = selectionMade;
		}

	}

	private class DocumentUndoableEditObserver implements UndoableEditListener {

		public DocumentUndoableEditObserver() {
		}

		@Override
		public void undoableEditHappened(UndoableEditEvent e) {
			fireUndoableStateChanged();
		}

	}

	@SuppressWarnings("serial")
	private class PlainDocumentImpl extends PlainDocument {

		public PlainDocumentImpl() {
		}

		@Override
		protected void fireUndoableEditUpdate(UndoableEditEvent event) {
			UndoableEdit edit = event.getEdit();
			if (edit instanceof DefaultDocumentEvent) {
				DefaultDocumentEvent docEdit = (DefaultDocumentEvent) edit;
				event = new UndoableEditEvent(event.getSource(), new UndoableEditImpl(this, docEdit));
			}
			super.fireUndoableEditUpdate(event);
		}

	}

	@SuppressWarnings("serial")
	private class UndoableEditImpl extends AbstractUndoableEdit {

		private EventType type;

		private int offset;

		private int length;

		private PlainDocumentImpl source;

		private List<UndoableEdit> subEdits;

		public UndoableEditImpl(PlainDocumentImpl source, DefaultDocumentEvent event) {
			this.type = event.getType();
			this.offset = event.getOffset();
			this.length = event.getLength();
			this.source = source;
			this.subEdits = new Vector<UndoableEdit>();
			this.subEdits.add(event);
		}

		@Override
		public String toString() {
			return "UndoableEdit type:" + getType() + " offset:" + getOffset() + " length:" + getLength();
		}

		@Override
		public boolean addEdit(UndoableEdit anEdit) {
			if (anEdit instanceof UndoableEditImpl) {
				return addTypedEdit((UndoableEditImpl) anEdit);
			} else {
				return super.addEdit(anEdit);
			}
		}

		protected boolean addTypedEdit(UndoableEditImpl anEdit) {
			if (anEdit.getType().equals(getType())) {
				if (isInsert()) {
					return addInsertEdit(anEdit);
				} else if (isRemove()) {
					return addRemoveEdit(anEdit);
				} else {
					return false;
				}
			} else {
				return false;
			}
		}

		protected boolean addInsertEdit(UndoableEditImpl anEdit) {
			if (anEdit.getOffset() == getOffset() + getLength()) {
				if (isNewlineInsertion(anEdit)) {
					return false; // split edits over lines
				} else {
					// e.g., typing
					getSubEdits().add(anEdit);
					setLength(getLength() + anEdit.getLength());
					return true;
				}
			} else {
				return false;
			}
		}

		protected boolean addRemoveEdit(UndoableEditImpl anEdit) {
			if (anEdit.getOffset() + anEdit.getLength() == getOffset()) {
				// e.g., backspace
				getSubEdits().add(anEdit);
				setOffset(anEdit.getOffset());
				setLength(getLength() + anEdit.getLength());
				return true;
			} else if (anEdit.getOffset() == getOffset()) {
				// e.g., delete
				getSubEdits().add(anEdit);
				setLength(getLength() + anEdit.getLength());
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			for (int i = getSubEdits().size() - 1; i >= 0; i--) {
				getSubEdits().get(i).undo();
			}
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			for (int i = 0; i < getSubEdits().size(); i++) {
				getSubEdits().get(i).redo();
			}
		}

		@Override
		public void die() {
			for (int i = getSubEdits().size() - 1; i >= 0; i--) {
				getSubEdits().get(i).die();
			}
			super.die();
		}

		@Override
		public String getPresentationName() {
			if (isInsert()) {
				return UIManager.getString("AbstractDocument.additionText");
			} else if (isRemove()) {
				return UIManager.getString("AbstractDocument.deletionText");
			} else {
				return super.getPresentationName();
			}
		}

		private boolean isNewlineInsertion(UndoableEditImpl anEdit) {
			if (!anEdit.isInsert())
				return false;
			if (anEdit.getLength() != 1)
				return false;
			try {
				char c = getSource().getText(anEdit.getOffset(), 1).charAt(0);
				return c == '\n';
			} catch (BadLocationException e) {
				return false;
			}
		}

		public boolean isInsert() {
			return getType().equals(EventType.INSERT);
		}

		public boolean isRemove() {
			return getType().equals(EventType.REMOVE);
		}

		public EventType getType() {
			return type;
		}

		public int getOffset() {
			return offset;
		}

		private void setOffset(int offset) {
			this.offset = offset;
		}

		public int getLength() {
			return length;
		}

		private void setLength(int length) {
			this.length = length;
		}

		private PlainDocumentImpl getSource() {
			return source;
		}

		private List<UndoableEdit> getSubEdits() {
			return subEdits;
		}

	}

	private class TextSearchMatchImpl implements TextSearchMatch {

		private Position startPosition; // inclusive

		private Position endPosition; // exclusive

		private Object highlightReference;

		public TextSearchMatchImpl(Position startPosition, Position endPosition) {
			this.startPosition = startPosition;
			this.endPosition = endPosition;
		}

		@Override
		public int compareTo(TextSearchMatch other) {
			if (other instanceof TextSearchMatchImpl) {
				return getStartPosition().getOffset() - ((TextSearchMatchImpl) other).getStartPosition().getOffset();
			} else {
				return 0;
			}
		}

		@Override
		public void replaceWith(String str) {
			int offset = getStartPosition().getOffset();
			int length = getEndPosition().getOffset() - offset;
			if (removeTextSpan(offset, length)) {
				insertString(offset, str);
				setCaretPosition(offset);
			}
		}

		@Override
		public void showInFocus(int preferredLinesBefore, int preferredLinesAfter) {
			PlainTextDocumentEditor editor = PlainTextDocumentEditor.this;
			int offset = getStartPosition().getOffset();
			int length = getEndPosition().getOffset() - offset;
			int r0 = findRelativeLineOffset(offset, -1, preferredLinesBefore);
			int r1 = Math.min(findRelativeLineOffset(offset + length, +1, preferredLinesAfter) + 1, getTextLength());
			r0 = Math.max(offset - 40 * preferredLinesBefore, r0);
			r1 = Math.min(offset + length + 40 * preferredLinesAfter, r1);
			editor.showInFocus(r0, r1 - r0);
			editor.showInFocus(offset, length);
		}

		@Override
		public void highlight(Color color) {
			removeHighlight(); // if any
			Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(color);
			try {
				setHighlightReference(getHighlighter().addHighlight(getStartPosition().getOffset(),
						getEndPosition().getOffset(), painter));
			} catch (BadLocationException e) {
				// do nothing
			}
		}

		@Override
		public void removeHighlight() {
			Object ref = getHighlightReference();
			if (ref != null) {
				getHighlighter().removeHighlight(ref);
				setHighlightReference(null);
			}
		}

		private int findRelativeLineOffset(int startPosition, int direction, int lines) {
			String text = getText();
			int p = startPosition;
			int lc = 0;
			if (direction < 0) {
				while (p > 0 && lc < lines) {
					if (text.charAt(--p) == '\n')
						lc++;
				}
			} else if (direction > 0) {
				while (p < text.length() && lc < lines) {
					if (text.charAt(p++) == '\n')
						lc++;
				}
			}
			return p;
		}

		private Highlighter getHighlighter() {
			return getTextArea().getHighlighter();
		}

		public Position getStartPosition() {
			return startPosition;
		}

		public Position getEndPosition() {
			return endPosition;
		}

		private Object getHighlightReference() {
			return highlightReference;
		}

		private void setHighlightReference(Object reference) {
			this.highlightReference = reference;
		}

	}

}