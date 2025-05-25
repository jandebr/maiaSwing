package org.maia.swing.file;

import java.io.File;

import javax.swing.Icon;
import javax.swing.JFileChooser;

import org.maia.graphics2d.image.ImageUtils;

@SuppressWarnings("serial")
public class FolderInputField extends GenericFileInputField {

	public static String DEFAULT_DIALOG_TITLE = "Select a folder";

	public FolderInputField() {
		this(null);
	}

	public FolderInputField(File file) {
		super(file);
		if (file != null && !file.isDirectory())
			throw new IllegalArgumentException("Not a folder: " + file.getAbsolutePath());
		setFileChooserDialogTitle(DEFAULT_DIALOG_TITLE);
	}

	@Override
	protected JFileChooser buildFileChooser() {
		JFileChooser chooser = super.buildFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		return chooser;
	}

	@Override
	protected File getCurrentDirectoryForSelected(File file) {
		return file;
	}

	@Override
	protected Icon getFileChooserIcon() {
		return ImageUtils.getIcon("org/maia/swing/icons/io/folder24.png");
	}

	public File getSelectedFolder() {
		return getFile();
	}

}