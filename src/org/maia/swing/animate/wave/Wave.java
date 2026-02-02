package org.maia.swing.animate.wave;

import java.awt.Color;

public class Wave implements Cloneable {

	private float translationX; // normalized units

	private float translationY; // normalized units

	private float length; // normalized units

	private float amplitude; // normalized units

	private Color color;

	public Wave() {
		this(0f, 0.5f, 1f, 0.5f);
	}

	public Wave(float translationX, float translationY, float length, float amplitude) {
		this(translationX, translationY, length, amplitude, Color.BLUE);
	}

	public Wave(float translationX, float translationY, float length, float amplitude, Color color) {
		this.translationX = translationX;
		this.translationY = translationY;
		this.length = length;
		this.amplitude = amplitude;
		this.color = color;
	}

	@Override
	protected Wave clone() {
		return new Wave(getTranslationX(), getTranslationY(), getLength(), getAmplitude(), getColor());
	}

	public void translate(float tx, float ty) {
		setTranslationX(getTranslationX() + tx);
		setTranslationY(getTranslationY() + ty);
	}

	public void scale(float sx, float sy) {
		setLength(getLength() * sx);
		setAmplitude(getAmplitude() * sy);
	}

	public float getTranslationX() {
		return translationX;
	}

	public void setTranslationX(float translationX) {
		this.translationX = translationX;
	}

	public float getTranslationY() {
		return translationY;
	}

	public void setTranslationY(float translationY) {
		this.translationY = translationY;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

	public float getAmplitude() {
		return amplitude;
	}

	public void setAmplitude(float amplitude) {
		this.amplitude = amplitude;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

}