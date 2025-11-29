package org.maia.swing.input;

import java.io.File;

import javax.swing.Icon;
import javax.swing.JFileChooser;

import org.maia.graphics2d.image.ImageUtils;

@SuppressWarnings("serial")
public class FileInputField extends GenericFileInputField {

	public static String DEFAULT_DIALOG_TITLE = "Select a file";

	public FileInputField() {
		this(null);
	}

	public FileInputField(File file) {
		super(file);
		if (file != null && !file.isFile())
			throw new IllegalArgumentException("Not a file: " + file.getAbsolutePath());
		setFileChooserDialogTitle(DEFAULT_DIALOG_TITLE);
	}

	@Override
	protected JFileChooser buildFileChooser() {
		JFileChooser chooser = super.buildFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		return chooser;
	}

	@Override
	protected File getCurrentDirectoryForSelected(File file) {
		return file.getParentFile();
	}

	@Override
	protected Icon getFileChooserIcon() {
		return ImageUtils.getIcon("org/maia/swing/icons/io/file24.png");
	}

	public File getSelectedFile() {
		return getFile();
	}

}