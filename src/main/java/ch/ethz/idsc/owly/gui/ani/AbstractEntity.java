// code by jph
package ch.ethz.idsc.owly.gui.ani;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.ethz.idsc.owly.data.GlobalAssert;
import ch.ethz.idsc.owly.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.owly.glc.core.TrajectorySample;
import ch.ethz.idsc.owly.gui.RenderInterface;
import ch.ethz.idsc.owly.math.state.EpisodeIntegrator;
import ch.ethz.idsc.owly.math.state.TrajectoryRegionQuery;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.red.ArgMin;

public abstract class AbstractEntity implements RenderInterface, AnimationInterface {
  protected final EpisodeIntegrator episodeIntegrator;
  private List<TrajectorySample> trajectory = null;
  private int trajectory_skip = 0;

  public AbstractEntity(EpisodeIntegrator episodeIntegrator) {
    this.episodeIntegrator = episodeIntegrator;
  }

  synchronized void setTrajectory(List<TrajectorySample> trajectory) {
    this.trajectory = trajectory;
    trajectory_skip = 0;
  }

  @Override
  public final synchronized void integrate(Scalar now) {
    // implementation does not require that current position is perfectly located on trajectory
    Tensor u = fallbackControl(); // default control
    if (Objects.nonNull(trajectory)) {
      int index = trajectory_skip + indexOfClosestTrajectorySample(trajectory.subList(trajectory_skip, trajectory.size()));
      trajectory_skip = index;
      GlobalAssert.that(index != ArgMin.NOINDEX);
      ++index; // next node has flow control
      if (index < trajectory.size()) {
        GlobalAssert.that(trajectory.get(index).getFlow().isPresent());
        u = trajectory.get(index).getFlow().get().getU();
      } else {
        System.err.println("out of trajectory");
        trajectory = null;
      }
    }
    episodeIntegrator.move(u, now);
  }

  /** @param delay
   * @return trajectory until delay[s] in the future of entity,
   * or current position if entity does not have a trajectory */
  final synchronized List<TrajectorySample> getFutureTrajectoryUntil(Scalar delay) {
    if (Objects.isNull(trajectory)) // agent does not have a trajectory
      return Collections.singletonList(TrajectorySample.head(episodeIntegrator.tail()));
    int index = trajectory_skip + indexOfClosestTrajectorySample(trajectory.subList(trajectory_skip, trajectory.size()));
    // <- no update of trajectory_skip here
    Scalar threshold = trajectory.get(index).stateTime().time().add(delay);
    return trajectory.stream().skip(index) //
        .filter(trajectorySample -> Scalars.lessEquals(trajectorySample.stateTime().time(), threshold)) //
        .collect(Collectors.toList());
  }

  /** @param delay
   * @return estimated location of agent after given delay */
  public final Tensor getEstimatedLocationAt(Scalar delay) {
    if (Objects.isNull(trajectory))
      return episodeIntegrator.tail().state();
    List<TrajectorySample> relevant = getFutureTrajectoryUntil(delay);
    return relevant.get(relevant.size() - 1).stateTime().state();
  }

  public abstract int indexOfClosestTrajectorySample(List<TrajectorySample> trajectory);

  public abstract Tensor fallbackControl();

  public abstract Scalar delayHint();

  /** @param obstacleQuery
   * @param goal for instance {px, py, angle}
   * @return */
  public abstract TrajectoryPlanner createTrajectoryPlanner(TrajectoryRegionQuery obstacleQuery, Tensor goal);
}
