package org.maia.swing.animate.imageslide.path;

import java.util.List;
import java.util.Vector;

public class WeightedScorePathEvaluator implements SlidingImagePathEvaluator {

	private List<SlidingImagePathEvaluator> evaluators;

	private List<Double> weights;

	public WeightedScorePathEvaluator() {
		this.evaluators = new Vector<SlidingImagePathEvaluator>();
		this.weights = new Vector<Double>();
	}

	@Override
	public synchronized double evaluatePath(SlidingImagePath path) {
		double score = 0;
		double weightSum = 0;
		int n = getEvaluators().size();
		for (int i = 0; i < n; i++) {
			SlidingImagePathEvaluator evaluator = getEvaluators().get(i);
			double s = evaluator.evaluatePath(path);
			if (s < 0) {
				return s;
			} else {
				double w = getWeights().get(i);
				score += w * s;
				weightSum += w;
			}
		}
		if (weightSum > 0)
			score /= weightSum;
		return score;
	}

	public synchronized void addEvaluator(SlidingImagePathEvaluator evaluator, double weight) {
		getEvaluators().add(evaluator);
		getWeights().add(weight);
	}

	private List<SlidingImagePathEvaluator> getEvaluators() {
		return evaluators;
	}

	private List<Double> getWeights() {
		return weights;
	}

}