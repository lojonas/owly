// code by jph
package ch.ethz.idsc.owly.demo.util;

import java.io.Serializable;

import ch.ethz.idsc.owly.math.TensorUnaryOperator;
import ch.ethz.idsc.tensor.Scalar;

public interface BijectionFamily extends Serializable {
  /** for rendering
   * 
   * @param scalar
   * @return */
  TensorUnaryOperator forward(Scalar scalar);

  /** for collision checking
   * 
   * @param scalar
   * @return */
  TensorUnaryOperator inverse(Scalar scalar);
}
