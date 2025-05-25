package org.maia.swing.dialog;

import java.util.Objects;

public abstract class ActionableDialogOption {

	private String id;

	private String label;

	protected ActionableDialogOption(String id, String label) {
		this.id = id;
		this.label = label;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActionableDialogOption other = (ActionableDialogOption) obj;
		return Objects.equals(getId(), other.getId());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ActionableDialogOption [id=");
		builder.append(getId());
		builder.append(", label=");
		builder.append(getLabel());
		builder.append("]");
		return builder.toString();
	}

	public abstract boolean isConfirmation();

	public abstract boolean isCancellation();

	public boolean isClosingDialog() {
		return true;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

}