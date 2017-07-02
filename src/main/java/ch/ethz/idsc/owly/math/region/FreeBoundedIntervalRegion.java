// code by jph
package ch.ethz.idsc.owly.math.region;

import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.sca.Abs;

public class FreeBoundedIntervalRegion extends ImplicitFunctionRegion {
  public static final Scalar HALF = RationalScalar.of(1, 2);
  // ---
  private final int index;
  private final Scalar semiwidth;
  private final Scalar center;

  public FreeBoundedIntervalRegion(int index, Scalar lo, Scalar hi) {
    if (!Scalars.lessThan(lo, hi))
      throw new RuntimeException();
    this.index = index;
    semiwidth = hi.subtract(lo).multiply(HALF);
    center = hi.add(lo).multiply(HALF);
  }

  @Override
  public Scalar evaluate(Tensor x) {
    return semiwidth.subtract(Abs.FUNCTION.apply(x.Get(index).subtract(center)));
  }
}
