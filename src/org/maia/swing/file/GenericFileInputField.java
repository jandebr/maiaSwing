package org.maia.swing.file;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import org.maia.graphics2d.image.ImageUtils;

@SuppressWarnings("serial")
public abstract class GenericFileInputField extends JPanel {

	private File file;

	private File currentDirectory;

	private JLabel fileLabel;

	private IconActionButton fileClearButton;

	private IconActionButton fileChooserButton;

	private String fileChooserDialogTitle;

	private FileFilter fileChooserFilter;

	private boolean showAbsolutePath;

	private List<GenericFileInputFieldListener> inputFieldListeners;

	public static int MINIMUM_WIDTH = 240;

	protected GenericFileInputField() {
		this(null);
	}

	protected GenericFileInputField(File file) {
		super(new BorderLayout());
		this.file = file;
		if (file != null) {
			this.currentDirectory = getCurrentDirectoryForSelected(file);
		}
		this.inputFieldListeners = new Vector<GenericFileInputFieldListener>();
		this.fileLabel = createFileLabel();
		this.fileClearButton = createFileClearButton();
		this.fileChooserButton = createFileChooserButton();
		buildUI();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		getFileLabel().setEnabled(enabled);
		getFileChooserButton().setEnabled(enabled);
		setClearEnabled(enabled);
	}

	public void enableClear() {
		setClearEnabled(true);
	}

	public void disableClear() {
		setClearEnabled(false);
	}

	public void setClearEnabled(boolean enabled) {
		getFileClearButton().setEnabled(enabled);
	}

	public void addListener(GenericFileInputFieldListener listener) {
		getInputFieldListeners().add(listener);
	}

	public void removeListener(GenericFileInputFieldListener listener) {
		getInputFieldListeners().remove(listener);
	}

	private void buildUI() {
		updateFileLabel();
		add(getFileLabel(), BorderLayout.CENTER);
		add(buildActionsComponent(), BorderLayout.EAST);
		if (getMinimumSize().width < MINIMUM_WIDTH) {
			setMinimumSize(new Dimension(MINIMUM_WIDTH, getMinimumSize().height));
			setPreferredSize(getMinimumSize());
		}
	}

	private JComponent buildActionsComponent() {
		Box box = Box.createHorizontalBox();
		box.add(getFileClearButton());
		box.add(Box.createHorizontalStrut(4));
		box.add(getFileChooserButton());
		return box;
	}

	private JLabel createFileLabel() {
		JLabel label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setBackground(Color.WHITE);
		label.setOpaque(true);
		label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(4, 4, 4, 4)));
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		return label;
	}

	private IconActionButton createFileClearButton() {
		IconActionButton button = new IconActionButton(ImageUtils.getIcon("org/maia/swing/icons/clear16.png"));
		button.setToolTipText("Clear");
		button.addActionListener(new FileClearActionHandler());
		return button;
	}

	private IconActionButton createFileChooserButton() {
		IconActionButton button = new IconActionButton(getFileChooserIcon());
		button.addActionListener(new FileChooserActionHandler());
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return button;
	}

	private void updateFileLabel() {
		if (isCleared()) {
			getFileLabel().setText("");
			getFileLabel().setToolTipText(null);
		} else {
			String text = getLabelForFile(getFile());
			getFileLabel().setText(text);
			getFileLabel().setToolTipText(text);
		}
	}

	protected String getLabelForFile(File file) {
		if (isShowAbsolutePath()) {
			return file.getAbsolutePath();
		} else {
			return file.getName();
		}
	}

	protected abstract Icon getFileChooserIcon();

	protected JFileChooser buildFileChooser() {
		JFileChooser chooser = new JFileChooser(getCurrentDirectory());
		if (getFileChooserDialogTitle() != null) {
			chooser.setDialogTitle(getFileChooserDialogTitle());
		}
		if (getFileChooserFilter() != null) {
			chooser.setFileFilter(getFileChooserFilter());
		}
		return chooser;
	}

	public void clearFile() {
		setFile(null);
	}

	public boolean isCleared() {
		return getFile() == null;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
		if (file != null) {
			setCurrentDirectory(getCurrentDirectoryForSelected(file));
		}
		updateFileLabel();
		for (GenericFileInputFieldListener listener : getInputFieldListeners()) {
			listener.fileSelectionChanged(this);
		}
	}

	protected abstract File getCurrentDirectoryForSelected(File file);

	public File getCurrentDirectory() {
		return currentDirectory;
	}

	public void setCurrentDirectory(File directory) {
		this.currentDirectory = directory;
	}

	private JLabel getFileLabel() {
		return fileLabel;
	}

	private IconActionButton getFileClearButton() {
		return fileClearButton;
	}

	private IconActionButton getFileChooserButton() {
		return fileChooserButton;
	}

	public String getFileChooserDialogTitle() {
		return fileChooserDialogTitle;
	}

	public void setFileChooserDialogTitle(String title) {
		this.fileChooserDialogTitle = title;
		getFileChooserButton().setToolTipText(title);
	}

	public FileFilter getFileChooserFilter() {
		return fileChooserFilter;
	}

	public void setFileChooserFilter(FileFilter filter) {
		this.fileChooserFilter = filter;
	}

	public boolean isShowAbsolutePath() {
		return showAbsolutePath;
	}

	public void setShowAbsolutePath(boolean absolutePath) {
		this.showAbsolutePath = absolutePath;
		updateFileLabel();
	}

	protected List<GenericFileInputFieldListener> getInputFieldListeners() {
		return inputFieldListeners;
	}

	private static class IconActionButton extends JButton {

		public IconActionButton(Icon icon) {
			super(icon);
			setContentAreaFilled(false);
			setOpaque(false);
			setFocusPainted(false);
			setBorderPainted(false);
			setMargin(new Insets(0, 0, 0, 0));
		}

	}

	private class FileChooserActionHandler implements ActionListener {

		public FileChooserActionHandler() {
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = buildFileChooser();
			int returnValue = fileChooser.showDialog(GenericFileInputField.this, "Select");
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				setFile(fileChooser.getSelectedFile());
			}
		}

	}

	private class FileClearActionHandler implements ActionListener {

		public FileClearActionHandler() {
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			clearFile();
		}

	}

}