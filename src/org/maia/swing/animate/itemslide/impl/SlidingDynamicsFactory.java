package org.maia.swing.animate.itemslide.impl;

import java.awt.Graphics2D;
import java.awt.Insets;

import org.maia.swing.animate.itemslide.SlidingDynamics;
import org.maia.swing.animate.itemslide.SlidingItemListComponent;

public class SlidingDynamicsFactory {

	private static double defaultAcceleration = 3.0; // velocity increase per millisecond

	private static double defaultMaximumVelocity = 1.5; // maximum velocity, in pixels per millisecond

	private static double defaultThrottleNearTarget = 0; // throttle, between 0 and 1.0

	private SlidingDynamicsFactory() {
	}

	public static SlidingDynamics createStepwiseDynamics() {
		return new StepwiseDynamics();
	}

	public static SlidingDynamics createAdaptiveSpeedDynamics(SlidingItemListComponent component) {
		return createAdaptiveSpeedDynamics(component, defaultAcceleration, defaultMaximumVelocity,
				defaultThrottleNearTarget);
	}

	public static SlidingDynamics createAdaptiveSpeedDynamics(SlidingItemListComponent component, double acceleration,
			double maximumVelocity, double throttleNearTarget) {
		return new AdaptiveSpeedSlidingDynamics(component, acceleration, maximumVelocity, throttleNearTarget);
	}

	private static class StepwiseDynamics implements SlidingDynamics {

		public StepwiseDynamics() {
		}

		@Override
		public SlidingState getUpdatedStateTowardsTarget(SlidingState currentState, SlidingState targetState,
				SlidingItemList itemList, Graphics2D g, double elapsedTimeMillis) {
			return targetState;
		}

		@Override
		public double getRelativeVelocity() {
			return 0;
		}

	}

	private static abstract class SpeedBasedSlidingDynamics implements SlidingDynamics {

		private SlidingItemListComponent component;

		private SlidingSpeed speed;

		private double maximumVelocity;

		private double throttleNearTarget;

		protected SpeedBasedSlidingDynamics(SlidingItemListComponent component, double acceleration,
				double maximumVelocity, double throttleNearTarget) {
			if (component == null)
				throw new NullPointerException("Component cannot be null");
			this.component = component;
			this.speed = new SlidingSpeed(acceleration);
			this.maximumVelocity = maximumVelocity;
			this.throttleNearTarget = throttleNearTarget;
		}

		@Override
		public double getRelativeVelocity() {
			return getSpeed().getVelocity() / getMaximumVelocity();
		}

		/**
		 * The interpolated <code>SlidingState</code> defines an interpolated <em>cursor width</em>, <em>cursor
		 * height</em> and <em>cursor margin</em>
		 * <p>
		 * The <em>cursor position</em> and <em>item translation</em> are left undefined
		 * </p>
		 */
		protected SlidingState getInterpolatedCursorShape(SlidingState currentState, SlidingState targetState,
				SlidingItemList itemList, Graphics2D g) {
			if (itemList.isEmpty()) {
				return targetState;
			} else {
				// Search items surrounding position
				SlidingItemInList itemBefore = null;
				SlidingItemInList itemAfter = null;
				double position = getComponent().getLayoutManager().getCursorCenterPosition(currentState);
				double itra = currentState.getItemTranslation();
				for (int i = 1; i < itemList.getItemCount(); i++) {
					SlidingItemInList itemInList = itemList.getItem(i);
					double ipos = itemInList.getPosition() + itra;
					if (ipos >= position) {
						itemAfter = itemInList;
						itemBefore = itemList.getItem(i - 1);
						if (i == 1 && itemBefore.getPosition() + itra >= position) {
							itemAfter = itemBefore;
						}
						break;
					}
				}
				if (itemAfter == null) {
					itemAfter = itemList.getItem(itemList.getItemCount() - 1);
					itemBefore = itemAfter;
				}
				// Interpolate
				double distance = itemAfter.getPosition() - itemBefore.getPosition();
				double r = distance == 0 ? 0 : (position - (itemBefore.getPosition() + itra)) / distance;
				double cw = (1.0 - r) * itemBefore.getItem().getWidth(g) + r * itemAfter.getItem().getWidth(g);
				double ch = (1.0 - r) * itemBefore.getItem().getHeight(g) + r * itemAfter.getItem().getHeight(g);
				Insets marginBefore = itemBefore.getItem().getMargin();
				Insets marginAfter = itemAfter.getItem().getMargin();
				Insets cm = marginBefore;
				if (!equalMargin(marginBefore, marginAfter)) {
					int cmtop = (int) Math.round((1.0 - r) * marginBefore.top + r * marginAfter.top);
					int cmleft = (int) Math.round((1.0 - r) * marginBefore.left + r * marginAfter.left);
					int cmbottom = (int) Math.round((1.0 - r) * marginBefore.bottom + r * marginAfter.bottom);
					int cmright = (int) Math.round((1.0 - r) * marginBefore.right + r * marginAfter.right);
					cm = new Insets(cmtop, cmleft, cmbottom, cmright);
				}
				return new SlidingState(0, 0, cw, ch, cm);
			}
		}

		protected boolean equalMargin(Insets marginOne, Insets marginOther) {
			return marginOne.top == marginOther.top && marginOne.left == marginOther.left
					&& marginOne.bottom == marginOther.bottom && marginOne.right == marginOther.right;
		}

		protected SlidingItemListComponent getComponent() {
			return component;
		}

		protected SlidingSpeed getSpeed() {
			return speed;
		}

		protected double getMaximumVelocity() {
			return maximumVelocity;
		}

		protected double getThrottleNearTarget() {
			return throttleNearTarget;
		}

	}

	private static class AdaptiveSpeedSlidingDynamics extends SpeedBasedSlidingDynamics {

		public AdaptiveSpeedSlidingDynamics(SlidingItemListComponent component, double acceleration,
				double maximumVelocity, double throttleNearTarget) {
			super(component, acceleration, maximumVelocity, throttleNearTarget);
		}

		@Override
		public SlidingState getUpdatedStateTowardsTarget(SlidingState currentState, SlidingState targetState,
				SlidingItemList itemList, Graphics2D g, double elapsedTimeMillis) {
			if (currentState.approximateEqualPosition(targetState)) {
				getSpeed().setVelocity(0);
				return targetState;
			} else {
				double idist = targetState.getItemTranslation() - currentState.getItemTranslation();
				double cdist = targetState.getCursorPosition() - currentState.getCursorPosition();
				double ivelo = getUpdatedVelocity(Math.abs(idist), elapsedTimeMillis);
				double cvelo = getUpdatedVelocity(Math.abs(cdist), elapsedTimeMillis);
				double v = Math.max(cvelo, ivelo);
				getSpeed().setVelocity(v);
				double itra = noOvershoot(currentState.getItemTranslation(),
						currentState.getItemTranslation() + Math.signum(idist) * v * elapsedTimeMillis,
						targetState.getItemTranslation());
				double cpos = noOvershoot(currentState.getCursorPosition(),
						currentState.getCursorPosition() + Math.signum(cdist) * v * elapsedTimeMillis,
						targetState.getCursorPosition());
				SlidingState cursorShape = getInterpolatedCursorShape(currentState, targetState, itemList, g);
				SlidingState updatedState = new SlidingState(itra, cpos, cursorShape.getCursorWidth(),
						cursorShape.getCursorHeight(), cursorShape.getCursorMargin());
				if (updatedState.approximateEqualPosition(targetState)) {
					getSpeed().setVelocity(0);
				}
				return updatedState;
			}
		}

		private double getUpdatedVelocity(double distance, double elapsedTimeMillis) {
			double v = (0.005 + 0.02 * (1.0 - getThrottleNearTarget())) * (1.0 + distance);
			double vmax = Math.min(getSpeed().getVelocity() + getSpeed().getAcceleration() * elapsedTimeMillis,
					getMaximumVelocity());
			return Math.min(v, vmax);
		}

		private double noOvershoot(double before, double after, double target) {
			if (before < target && after > target)
				return target;
			if (before > target && after < target)
				return target;
			return after;
		}

	}

	private static class SlidingSpeed {

		private double velocity;

		private double acceleration;

		public SlidingSpeed(double acceleration) {
			this(0, acceleration);
		}

		public SlidingSpeed(double velocity, double acceleration) {
			this.velocity = velocity;
			this.acceleration = acceleration;
		}

		public double getVelocity() {
			return velocity;
		}

		public void setVelocity(double velocity) {
			this.velocity = velocity;
		}

		public double getAcceleration() {
			return acceleration;
		}

		public void setAcceleration(double acceleration) {
			this.acceleration = acceleration;
		}

	}

}