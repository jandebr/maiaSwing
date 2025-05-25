package org.maia.swing.text.pte.menu;

import java.awt.Component;
import java.awt.Container;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.maia.graphics2d.image.ImageUtils;
import org.maia.swing.text.pte.PlainTextDocumentEditor;
import org.maia.swing.text.pte.PlainTextDocumentEditorActions;
import org.maia.swing.text.pte.PlainTextDocumentEditorAdapter;
import org.maia.swing.text.pte.PlainTextEditor;
import org.maia.swing.text.pte.PlainTextEditorActions;
import org.maia.swing.text.pte.PlainTextEditorFrame;

public class PlainTextEditorDefaultMenuMaker implements PlainTextEditorMenuMaker {

	private Map<MenuKey, JMenuBar> cachedMenuBars;

	private Map<MenuKey, JComponent> cachedToolBars;

	private Map<MenuKey, JPopupMenu> cachedPopupMenus;

	public static String FILE_MENU_LABEL = "File";

	public static String EDIT_MENU_LABEL = "Edit";

	public static String VIEW_MENU_LABEL = "View";

	public PlainTextEditorDefaultMenuMaker() {
		this.cachedMenuBars = new HashMap<MenuKey, JMenuBar>();
		this.cachedToolBars = new HashMap<MenuKey, JComponent>();
		this.cachedPopupMenus = new HashMap<MenuKey, JPopupMenu>();
	}

	@Override
	public final JMenuBar createMenuBar(PlainTextEditor editor) {
		MenuKey menyKey = createCacheKey(editor, editor.getActiveDocumentEditor());
		JMenuBar menuBar = getCachedMenuBars().get(menyKey);
		if (menuBar == null) {
			PlainTextEditorActions actions = editor.getActions();
			PlainTextDocumentEditorActions docActions = getActiveDocumentActions(editor);
			menuBar = createMenuBar(actions, docActions);
			getCachedMenuBars().put(menyKey, menuBar);
		}
		return menuBar;
	}

	protected JMenuBar createMenuBar(PlainTextEditorActions actions, PlainTextDocumentEditorActions docActions) {
		JMenuBar menuBar = new JMenuBar();
		for (JMenu menu : createMenusForMenuBar(actions, docActions)) {
			menuBar.add(menu);
		}
		return menuBar;
	}

	protected List<JMenu> createMenusForMenuBar(PlainTextEditorActions actions,
			PlainTextDocumentEditorActions docActions) {
		List<JMenu> menus = new Vector<JMenu>();
		menus.add(createFileMenu(actions, docActions));
		menus.add(createEditMenu(docActions));
		menus.add(createViewMenu(docActions));
		return menus;
	}

	protected JMenu createFileMenu(PlainTextEditorActions actions, PlainTextDocumentEditorActions docActions) {
		JMenu menu = new JMenu(FILE_MENU_LABEL);
		menu.add(decorateForMenu(new JMenuItem(actions.getNewAction())));
		menu.add(decorateForMenu(new JMenuItem(actions.getOpenAction())));
		if (docActions != null) {
			menu.add(decorateForMenu(createSaveMenuItem(docActions)));
			menu.add(decorateForMenu(createRevertMenuItem(docActions)));
		}
		menu.add(decorateForMenu(new JMenuItem(actions.getCloseAction())));
		menu.add(decorateForMenu(new JMenuItem(actions.getCloseAllAction())));
		if (isQuitActionAvailable(actions.getEditor())) {
			menu.add(new JSeparator());
			menu.add(decorateForMenu(new JMenuItem(actions.getQuitAction())));
		}
		return menu;
	}

	protected boolean isQuitActionAvailable(PlainTextEditor editor) {
		PlainTextEditorFrame frame = editor.getParentFrame();
		return frame != null && frame.isExitOnClose();
	}

	protected JMenu createEditMenu(PlainTextDocumentEditorActions docActions) {
		JMenu menu = new JMenu(EDIT_MENU_LABEL);
		if (docActions != null) {
			menu.add(decorateForMenu(createCutMenuItem(docActions)));
			menu.add(decorateForMenu(createCopyMenuItem(docActions)));
			menu.add(decorateForMenu(createPasteMenuItem(docActions)));
			menu.add(decorateForMenu(createSelectAllMenuItem(docActions)));
			menu.add(new JSeparator());
			menu.add(decorateForMenu(createUndoMenuItem(docActions)));
			menu.add(decorateForMenu(createRedoMenuItem(docActions)));
			menu.add(new JSeparator());
			menu.add(decorateForMenu(createFindReplaceMenuItem(docActions)));
		}
		return menu;
	}

	protected JMenu createViewMenu(PlainTextDocumentEditorActions docActions) {
		JMenu menu = new JMenu(VIEW_MENU_LABEL);
		if (docActions != null) {
			menu.add(decorateForMenu(createWrapLinesMenuItem(docActions)));
		}
		return menu;
	}

	protected JCheckBoxMenuItem createWrapLinesMenuItem(PlainTextDocumentEditorActions docActions) {
		final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(docActions.getWrapLinesAction());
		menuItem.setSelected(docActions.getDocumentEditor().isWrapLines());
		docActions.getDocumentEditor().addListener(new PlainTextDocumentEditorAdapter() {

			@Override
			public void documentWrapLinesChanged(PlainTextDocumentEditor documentEditor) {
				menuItem.setSelected(documentEditor.isWrapLines());
			}

		});
		return menuItem;
	}

	@Override
	public final JComponent createToolBar(PlainTextEditor editor) {
		MenuKey menyKey = createCacheKey(editor, editor.getActiveDocumentEditor());
		JComponent toolBar = getCachedToolBars().get(menyKey);
		if (toolBar == null) {
			PlainTextEditorActions actions = editor.getActions();
			PlainTextDocumentEditorActions docActions = getActiveDocumentActions(editor);
			toolBar = createToolBar(actions, docActions);
			getCachedToolBars().put(menyKey, toolBar);
		} else {
			prepareToolBarForReuse(toolBar);
		}
		return toolBar;
	}

	protected JComponent createToolBar(PlainTextEditorActions actions, PlainTextDocumentEditorActions docActions) {
		JComponent toolBar = Box.createHorizontalBox();
		toolBar.add(new ToolBarButton(actions.getNewAction()));
		toolBar.add(new ToolBarButton(actions.getOpenAction()));
		if (docActions != null) {
			toolBar.add(new ToolBarButton(docActions.getSaveAction()));
		}
		toolBar.add(new ToolBarButton(actions.getCloseAction()));
		toolBar.add(new ToolBarButton(actions.getCloseAllAction()));
		if (docActions != null) {
			toolBar.add(createToolBarSeparator());
			toolBar.add(new ToolBarButton(docActions.getCutAction()));
			toolBar.add(new ToolBarButton(docActions.getCopyAction()));
			toolBar.add(new ToolBarButton(docActions.getPasteAction()));
			toolBar.add(createToolBarSeparator());
			toolBar.add(new ToolBarButton(docActions.getUndoAction()));
			toolBar.add(new ToolBarButton(docActions.getRedoAction()));
			toolBar.add(createToolBarSeparator());
			toolBar.add(new ToolBarButton(docActions.getFindReplaceAction()));
			toolBar.add(createWrapLinesToolBarButton(docActions));
		}
		return toolBar;
	}

	protected JComponent createToolBarSeparator() {
		return new JLabel(ImageUtils.getIcon("org/maia/swing/icons/separator8x32.png"));
	}

	protected ToolBarToggleButton createWrapLinesToolBarButton(PlainTextDocumentEditorActions docActions) {
		final ToolBarToggleButton button = new ToolBarToggleButton(docActions.getWrapLinesAction());
		button.setSelected(docActions.getDocumentEditor().isWrapLines());
		docActions.getDocumentEditor().addListener(new PlainTextDocumentEditorAdapter() {

			@Override
			public void documentWrapLinesChanged(PlainTextDocumentEditor documentEditor) {
				button.setSelected(documentEditor.isWrapLines());
			}

		});
		return button;
	}

	protected void prepareToolBarForReuse(JComponent toolBar) {
		prepareToolBarComponentForReuse(toolBar);
	}

	protected void prepareToolBarComponentForReuse(Component component) {
		if (component instanceof ToolBarButton) {
			((ToolBarButton) component).refreshUI();
		} else if (component instanceof Container) {
			Container container = (Container) component;
			for (Component child : container.getComponents()) {
				prepareToolBarComponentForReuse(child);
			}
		}
	}

	@Override
	public final JPopupMenu createPopupMenu(PlainTextDocumentEditor documentEditor) {
		MenuKey menyKey = createCacheKey(documentEditor);
		JPopupMenu popupMenu = getCachedPopupMenus().get(menyKey);
		if (popupMenu == null) {
			PlainTextDocumentEditorActions docActions = documentEditor.getActions();
			if (docActions != null) {
				popupMenu = createPopupMenu(docActions);
			}
			getCachedPopupMenus().put(menyKey, popupMenu);
		}
		return popupMenu;
	}

	protected JPopupMenu createPopupMenu(PlainTextDocumentEditorActions docActions) {
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(decorateForMenu(createCutMenuItem(docActions)));
		popupMenu.add(decorateForMenu(createCopyMenuItem(docActions)));
		popupMenu.add(decorateForMenu(createPasteMenuItem(docActions)));
		popupMenu.add(new JSeparator());
		popupMenu.add(decorateForMenu(createUndoMenuItem(docActions)));
		popupMenu.add(decorateForMenu(createRedoMenuItem(docActions)));
		return popupMenu;
	}

	protected JMenuItem createSaveMenuItem(PlainTextDocumentEditorActions docActions) {
		JMenuItem menuItem = new JMenuItem(docActions.getSaveAction());
		menuItem.setAccelerator(docActions.getDocumentEditor().getKeyStrokeForSave());
		return menuItem;
	}

	protected JMenuItem createRevertMenuItem(PlainTextDocumentEditorActions docActions) {
		JMenuItem menuItem = new JMenuItem(docActions.getRevertAction());
		return menuItem;
	}

	protected JMenuItem createCutMenuItem(PlainTextDocumentEditorActions docActions) {
		JMenuItem menuItem = new JMenuItem(docActions.getCutAction());
		menuItem.setAccelerator(docActions.getDocumentEditor().getKeyStrokeForCut());
		return menuItem;
	}

	protected JMenuItem createCopyMenuItem(PlainTextDocumentEditorActions docActions) {
		JMenuItem menuItem = new JMenuItem(docActions.getCopyAction());
		menuItem.setAccelerator(docActions.getDocumentEditor().getKeyStrokeForCopy());
		return menuItem;
	}

	protected JMenuItem createPasteMenuItem(PlainTextDocumentEditorActions docActions) {
		JMenuItem menuItem = new JMenuItem(docActions.getPasteAction());
		menuItem.setAccelerator(docActions.getDocumentEditor().getKeyStrokeForPaste());
		return menuItem;
	}

	protected JMenuItem createSelectAllMenuItem(PlainTextDocumentEditorActions docActions) {
		JMenuItem menuItem = new JMenuItem(docActions.getSelectAllAction());
		menuItem.setAccelerator(docActions.getDocumentEditor().getKeyStrokeForSelectAll());
		return menuItem;
	}

	protected JMenuItem createUndoMenuItem(PlainTextDocumentEditorActions docActions) {
		JMenuItem menuItem = new JMenuItem(docActions.getUndoAction());
		menuItem.setAccelerator(docActions.getDocumentEditor().getKeyStrokeForUndo());
		return menuItem;
	}

	protected JMenuItem createRedoMenuItem(PlainTextDocumentEditorActions docActions) {
		JMenuItem menuItem = new JMenuItem(docActions.getRedoAction());
		menuItem.setAccelerator(docActions.getDocumentEditor().getKeyStrokeForRedo());
		return menuItem;
	}

	protected JMenuItem createFindReplaceMenuItem(PlainTextDocumentEditorActions docActions) {
		JMenuItem menuItem = new JMenuItem(docActions.getFindReplaceAction());
		menuItem.setAccelerator(docActions.getDocumentEditor().getKeyStrokeForFind());
		return menuItem;
	}

	protected JMenuItem decorateForMenu(JMenuItem menuItem) {
		menuItem.setToolTipText(null);
		return menuItem;
	}

	protected PlainTextDocumentEditorActions getActiveDocumentActions(PlainTextEditor editor) {
		PlainTextDocumentEditor documentEditor = editor.getActiveDocumentEditor();
		if (documentEditor != null) {
			return documentEditor.getActions();
		} else {
			return null;
		}
	}

	protected int getIndexOfMenu(List<JMenu> menus, String menuLabel) {
		for (int i = 0; i < menus.size(); i++) {
			if (menus.get(i).getText().equals(menuLabel))
				return i;
		}
		return -1;
	}

	private MenuKey createCacheKey(PlainTextDocumentEditor documentEditor) {
		return createCacheKey(null, documentEditor);
	}

	private MenuKey createCacheKey(PlainTextEditor editor, PlainTextDocumentEditor documentEditor) {
		return new MenuKey(editor, documentEditor);
	}

	private Map<MenuKey, JMenuBar> getCachedMenuBars() {
		return cachedMenuBars;
	}

	private Map<MenuKey, JComponent> getCachedToolBars() {
		return cachedToolBars;
	}

	private Map<MenuKey, JPopupMenu> getCachedPopupMenus() {
		return cachedPopupMenus;
	}

	private static class MenuKey {

		private PlainTextEditor editor;

		private PlainTextDocumentEditor documentEditor;

		public MenuKey(PlainTextEditor editor, PlainTextDocumentEditor documentEditor) {
			this.editor = editor;
			this.documentEditor = documentEditor;
		}

		@Override
		public int hashCode() {
			return Objects.hash(documentEditor, editor);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MenuKey other = (MenuKey) obj;
			return Objects.equals(documentEditor, other.documentEditor) && Objects.equals(editor, other.editor);
		}

		public PlainTextEditor getEditor() {
			return editor;
		}

		public PlainTextDocumentEditor getDocumentEditor() {
			return documentEditor;
		}

	}

}