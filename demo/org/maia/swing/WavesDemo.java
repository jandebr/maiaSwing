package org.maia.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JFrame;

import org.maia.swing.animate.wave.Wave;
import org.maia.swing.animate.wave.WaveDynamics;
import org.maia.swing.animate.wave.WavesComponent;
import org.maia.swing.animate.wave.impl.AgitatedWaveDynamics;

public class WavesDemo {

	private WavesComponent wavesComponent;

	public static void main(String[] args) {
		new WavesDemo().startDemo();
	}

	public WavesDemo() {
		this.wavesComponent = createWavesComponent();
	}

	private WavesComponent createWavesComponent() {
		int alpha = 160;
		Dimension size = new Dimension(800, 400);
		WavesComponent comp = new WavesComponent(size, new Color(10, 10, 10));
		comp.addWave(new Wave(0f, 0.5f, 1.0f, 0.2f, new Color(10, 19, 26, alpha)));
		comp.addWave(new Wave(0f, 0.5f, 1.0f, 0.2f, new Color(15, 27, 37, alpha)));
		comp.addWave(new Wave(0f, 0.5f, 1.0f, 0.2f, new Color(14, 31, 44, alpha)));
		comp.addWave(new Wave(0f, 0.5f, 1.0f, 0.2f, new Color(14, 46, 83, alpha)));
		comp.addWave(new Wave(0f, 0.5f, 1.0f, 0.2f, new Color(13, 56, 108, alpha)));
		comp.setWaveDynamics(createWaveDynamics(comp));
		return comp;
	}

	private WaveDynamics createWaveDynamics(WavesComponent comp) {
		AgitatedWaveDynamics dynamics = new AgitatedWaveDynamics(comp, 0.7f);
		dynamics.setAgitationLevelProgression(AgitatedWaveDynamics.createRandomLevelProgression());
		dynamics.setPerspectiveLiftMaximum(0.1f);
		dynamics.getWavelengthRange().setRange(3f, 6f);
		return dynamics;
	}

	private void startDemo() {
		showFrame();
	}

	private void showFrame() {
		JFrame frame = new JFrame("Waves");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setBackground(Color.BLACK);
		frame.add(Box.createVerticalStrut(80), BorderLayout.NORTH);
		frame.add(Box.createVerticalStrut(80), BorderLayout.SOUTH);
		frame.add(Box.createHorizontalStrut(160), BorderLayout.WEST);
		frame.add(Box.createHorizontalStrut(160), BorderLayout.EAST);
		frame.add(getWavesComponent().getUI(), BorderLayout.CENTER);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private WavesComponent getWavesComponent() {
		return wavesComponent;
	}

}