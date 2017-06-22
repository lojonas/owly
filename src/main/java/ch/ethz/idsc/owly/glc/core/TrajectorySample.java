// code by jph
package ch.ethz.idsc.owly.glc.core;

import java.io.Serializable;
import java.util.Optional;

import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.owly.math.state.StateTime;

/** container class that bundles information to follow a trajectory */
public class TrajectorySample implements Serializable {
  private final StateTime stateTime;
  private final Flow flow;

  public TrajectorySample(StateTime stateTime, Flow flow) {
    this.stateTime = stateTime;
    this.flow = flow;
  }

  public StateTime stateTime() {
    return stateTime;
  }

  /** the first or the last state time in a trajectory
   * typically does not have a flow associated
   * (since there may not be history for the sample)
   * 
   * We return an optional type to make the application layer
   * aware of the possibility that flow may be null.
   * 
   * @return Optional.ofNullable(flow) */
  public Optional<Flow> getFlow() {
    return Optional.ofNullable(flow);
  }
}
