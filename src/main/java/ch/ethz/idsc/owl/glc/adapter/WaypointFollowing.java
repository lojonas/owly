// code by ynager
package ch.ethz.idsc.owl.glc.adapter;

import java.util.List;
import ch.ethz.idsc.owl.gui.ani.AbstractEntity;
import ch.ethz.idsc.owl.gui.ani.TrajectoryPlannerCallback;
import ch.ethz.idsc.owl.math.state.TrajectorySample;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;

public abstract class WaypointFollowing {
  private final Tensor waypoints;
  protected final AbstractEntity entity;
  protected final TrajectoryPlannerCallback trajectoryPlannerCallback;
  private Scalar distThreshold = RealScalar.of(1);

  public WaypointFollowing(Tensor waypoints, AbstractEntity entity, TrajectoryPlannerCallback trajectoryPlannerCallback) {
    this.waypoints = waypoints;
    this.entity = entity;
    this.trajectoryPlannerCallback = trajectoryPlannerCallback;
  }

  /** sets the distance threshold. When the distance from the current state
   * to the current goal is below this threshold, planning to the next goal
   * is initiated
   * 
   * @param distThreshold */
  public void setDistanceThreshold(Scalar distThreshold) {
    this.distThreshold = distThreshold;
  }

  /** starts planning towards a goal
   * 
   * @param TrajectoryPlannerCallback
   * @param head
   * @param goal */
  protected abstract void planToGoal(List<TrajectorySample> head, Tensor goal);

  /** Start planning through waypoints */
  public void start() {
    List<TrajectorySample> head = entity.getFutureTrajectoryUntil(entity.delayHint());
    Tensor goal = waypoints.get(0);
    // start waypoint tracking loop
    int i = 0;
    boolean init = true;
    while (true) {
      Tensor loc = entity.getEstimatedLocationAt(entity.delayHint());
      Scalar dist = entity.distance(loc, goal).abs();
      //
      if (Scalars.lessThan(dist, distThreshold) || init) { // if close enough to current waypoint switch to next
        i = (i + 1) % waypoints.length();
        goal = waypoints.get(i);
        head = entity.getFutureTrajectoryUntil(entity.delayHint());
        planToGoal(head, goal);
        init = false;
      } else {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
