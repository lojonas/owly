// code by jph
package ch.ethz.idsc.owly.gui.ani;

import java.util.List;

import ch.ethz.idsc.owly.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.owly.glc.core.TrajectorySample;

/**
 * 
 */
// API not finalized
interface TrajectoryPlannerCallback {
  void hasTrajectoryPlanner(List<TrajectorySample> head, TrajectoryPlanner trajectoryPlanner);
}