// code by bapaden and jph
package ch.ethz.idsc.owly.math.state;

import java.io.Serializable;
import java.util.List;

import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

public interface CostFunction extends Serializable {
  /** @param from
   * @param trajectory
   * @param flow
   * @return cost of trajectory along flow */
  Scalar costIncrement(StateTime from, List<StateTime> trajectory, Flow flow);

  /** if a lower bound of the cost to goal is unknown,
   * the function should return {@link RealScalar#ZERO}.
   * 
   * <p>it is imperative that the function does not return a greater number than
   * is absolutely necessary to reach the goal.
   * 
   * <p>if instance encodes a non-trivial heuristic, i.e. a return value not
   * always equals to zero, the function should throw an exception if x == null
   * 
   * @param x
   * @return lower bound of cost to goal */
  Scalar minCostToGoal(Tensor x);
}
