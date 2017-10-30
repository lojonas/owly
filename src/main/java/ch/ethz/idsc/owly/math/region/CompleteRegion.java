// code by jph
package ch.ethz.idsc.owly.math.region;

import ch.ethz.idsc.tensor.Tensor;

/** empty region.
 * no given tensor is a member */
public enum CompleteRegion implements Region {
  INSTANCE;
  // ---
  @Override
  public boolean isMember(Tensor tensor) {
    return true;
  }
}