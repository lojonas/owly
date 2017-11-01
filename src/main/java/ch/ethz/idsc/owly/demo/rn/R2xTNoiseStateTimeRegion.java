// code by jph
package ch.ethz.idsc.owly.demo.rn;

import ch.ethz.idsc.owly.math.noise.ContinuousNoise;
import ch.ethz.idsc.owly.math.noise.ContinuousNoiseUtils;
import ch.ethz.idsc.owly.math.noise.SimplexContinuousNoise;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.owly.math.state.StateTimeRegion;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;

/** the simplex noise function is a continuous bivariate function with values in the interval [-1, 1]
 * https://de.wikipedia.org/wiki/Simplex_Noise
 * 
 * membership in the region for coordinates (x,y,t) that evaluate the noise function above a given threshold */
public class R2xTNoiseStateTimeRegion implements StateTimeRegion {
  private static final ContinuousNoise CONTINUOUS_NOISE = //
      ContinuousNoiseUtils.wrap3D(SimplexContinuousNoise.FUNCTION);
  // ---
  private final Scalar threshold;

  /** @param threshold in the interval [-1, 1] */
  public R2xTNoiseStateTimeRegion(Scalar threshold) {
    this.threshold = threshold;
  }

  @Override
  public boolean isMember(StateTime stateTime) {
    Tensor tensor = stateTime.state().extract(0, 2).append(stateTime.time());
    return Scalars.lessThan(threshold, CONTINUOUS_NOISE.apply(tensor));
  }
}
