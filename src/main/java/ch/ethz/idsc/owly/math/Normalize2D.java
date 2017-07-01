// code by jph
package ch.ethz.idsc.owly.math;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.TensorRuntimeException;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Hypot;
import ch.ethz.idsc.tensor.sca.Chop;

public enum Normalize2D {
  ;
  /** function normalizes a vector {x, y} unless the given vector
   * is {0, 0} in which case the return value is {0, 0}
   * 
   * @param x
   * @param y
   * @return {x, y} / |{x, y}|, or {0, 0} */
  public static Tensor unlessZero(Scalar x, Scalar y) {
    Scalar norm = Hypot.BIFUNCTION.apply(x, y);
    // return Scalars.isZero(norm) ? //
    // Tensors.vector(0, 0) : Tensors.of(x, y).multiply(norm.invert());
    if (Scalars.isZero(norm))
      return Tensors.vector(0, 0);
    Tensor result = Tensors.of(x, y).multiply(norm.invert());
    Scalar actual = Hypot.BIFUNCTION.apply(result.Get(0), result.Get(1));
    // TODO remove check after a long while
    if (!Chop._10.allZero(actual.subtract(RealScalar.ONE)))
      throw TensorRuntimeException.of(result, actual);
    return result;
  }
}
