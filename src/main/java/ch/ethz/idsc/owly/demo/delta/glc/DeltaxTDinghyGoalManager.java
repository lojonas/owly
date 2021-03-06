// code by jl
package ch.ethz.idsc.owly.demo.delta.glc;

import java.util.List;

import ch.ethz.idsc.owl.glc.adapter.SimpleTrajectoryRegionQuery;
import ch.ethz.idsc.owl.glc.adapter.StateTimeTrajectories;
import ch.ethz.idsc.owl.glc.core.GlcNode;
import ch.ethz.idsc.owl.glc.core.GoalInterface;
import ch.ethz.idsc.owl.math.flow.Flow;
import ch.ethz.idsc.owl.math.region.Region;
import ch.ethz.idsc.owl.math.state.StateTime;
import ch.ethz.idsc.owly.demo.delta.DeltaStateSpaceModel;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.red.Norm;

class DeltaxTDinghyGoalManager extends SimpleTrajectoryRegionQuery implements GoalInterface {
  private final Scalar timeCostScalingFactor;

  public DeltaxTDinghyGoalManager(Region<StateTime> goalRegion, DeltaStateSpaceModel stateSpaceModel) {
    this(goalRegion, stateSpaceModel, RealScalar.ONE);
  }

  public DeltaxTDinghyGoalManager(Region<StateTime> goalRegion, DeltaStateSpaceModel stateSpaceModel, Scalar timeCostScalingFactor) {
    super(goalRegion);
    this.timeCostScalingFactor = timeCostScalingFactor;
  }

  @Override
  public Scalar costIncrement(GlcNode glcNode, List<StateTime> trajectory, Flow flow) {
    // Costfunction: t
    // return StateTimeTrajectories.timeIncrement(from, trajectory);
    // alternative:
    Scalar sum = Norm._2.ofVector(flow.getU()).add(timeCostScalingFactor);
    // Costfunction: integrate (u^2 +1, t)
    return sum.multiply(StateTimeTrajectories.timeIncrement(glcNode, trajectory));
  }

  @Override
  public Scalar minCostToGoal(Tensor x) {
    // B. Paden: A Generalized Label Correcting Method for Optimal Kinodynamic Motion Planning
    // p. 79 Eq: 6.4.14
    return RealScalar.ZERO;
  }
}
