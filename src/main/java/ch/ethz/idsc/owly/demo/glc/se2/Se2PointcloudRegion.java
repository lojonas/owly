// code by jph
package ch.ethz.idsc.owly.demo.glc.se2;

import ch.ethz.idsc.owly.math.region.Region;
import ch.ethz.idsc.tensor.Tensor;

// TODO ?
public class Se2PointcloudRegion implements Region {
  @Override
  public boolean isMember(Tensor tensor) {
    return false;
  }
}
