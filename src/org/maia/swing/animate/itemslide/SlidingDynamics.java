package org.maia.swing.animate.itemslide;

import java.awt.Graphics2D;

import org.maia.swing.animate.itemslide.impl.SlidingItemList;
import org.maia.swing.animate.itemslide.impl.SlidingState;

public interface SlidingDynamics {

	SlidingState getUpdatedStateTowardsTarget(SlidingState currentState, SlidingState targetState,
			SlidingItemList itemList, Graphics2D g, double elapsedTimeMillis);

	double getRelativeVelocity();

}