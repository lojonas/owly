// code by jph and jl
package ch.ethz.idsc.owly.demo.twd;

import java.util.List;

import ch.ethz.idsc.owly.glc.adapter.StateTimeTrajectories;
import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.red.Norm;
import ch.ethz.idsc.tensor.sca.Ramp;

/** assumes max forward speed of == 1 */
public class TwdMinTimeGoalManager extends TwdAbstractGoalManager {
  public TwdMinTimeGoalManager(Tensor center, Scalar tolerance_xy, Scalar tolerance_angle) {
    super(center, tolerance_xy, tolerance_angle);
  }

  @Override // from CostFunction
  public Scalar costIncrement(StateTime from, List<StateTime> trajectory, Flow flow) {
    return StateTimeTrajectories.timeIncrement(from, trajectory);
  }

  @Override // from CostFunction
  public Scalar minCostToGoal(Tensor x) {
    Tensor xy = x.extract(0, 2);
    Scalar dxy = Norm._2.of(xy.subtract(center.extract(0, 2))).subtract(tolerance_xy);
    return Ramp.of(dxy);
  }
}