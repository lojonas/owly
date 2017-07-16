// code by jl
package ch.ethz.idsc.owly.demo.delta;

import java.util.List;

import ch.ethz.idsc.owly.glc.adapter.SimpleTrajectoryRegionQuery;
import ch.ethz.idsc.owly.glc.core.GoalInterface;
import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.owly.math.region.Region;
import ch.ethz.idsc.owly.math.region.RegionUnion;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.owly.math.state.TimeInvariantRegion;
import ch.ethz.idsc.owly.math.state.Trajectories;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.red.Norm;
import ch.ethz.idsc.tensor.sca.Ramp;

//TODO extend from DeltaGoalManager extended, so that costIncrement are not double
public class DeltaTrajectoryGoalManager extends SimpleTrajectoryRegionQuery implements GoalInterface {
  private final Tensor heuristicCenter;
  private final Scalar radius;
  private final Scalar maxSpeed;
  private final Scalar costScalingFactor;

  // Constructor with Default value in CostScaling
  public DeltaTrajectoryGoalManager(List<Region> goalRegions, Tensor heuristicCenter, Tensor radius, Scalar maxSpeed) {
    this(goalRegions, heuristicCenter, radius, maxSpeed, RealScalar.ONE);
  }

  public DeltaTrajectoryGoalManager(List<Region> goalRegions, Tensor heuristicCenter, Tensor radius, Scalar maxSpeed, Scalar costScalingFactor) {
    // only for comliling reasons
    super(new TimeInvariantRegion(RegionUnion.wrap(goalRegions)));
    // --
    this.maxSpeed = maxSpeed;
    this.heuristicCenter = heuristicCenter;
    if (!radius.Get(0).equals(radius.Get(1)))
      throw new RuntimeException(); // x-y radius have to be equal
    this.radius = radius.Get(0);
    this.costScalingFactor = costScalingFactor;
  }

  @Override
  public Scalar costIncrement(StateTime from, List<StateTime> trajectory, Flow flow) {
    Scalar sum = Norm._2.of(flow.getU()).add(costScalingFactor);
    // Costfunction: integrate (u^2 +1, t)
    return sum.multiply(Trajectories.timeIncrement(from, trajectory));
  }

  @Override
  public Scalar minCostToGoal(Tensor x) {
    // B. Paden: A Generalized Label Correcting Method for Optimal Kinodynamic Motion Planning
    // p. 79 Eq: 6.4.14
    // Heuristic needs to be underestimating: (Euclideandistance-radius) / (MaxControl+Max(|Vectorfield|)
    // last State from trajectory is the ultimate goal
    return Ramp.of(Norm._2.of(x.subtract(heuristicCenter)).subtract(radius).divide(maxSpeed));
  }
}
