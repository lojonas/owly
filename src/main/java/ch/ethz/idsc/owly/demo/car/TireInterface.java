// code by jph
package ch.ethz.idsc.owly.demo.car;

import ch.ethz.idsc.owly.math.car.Pacejka3;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

// TODO use
public interface TireInterface {
  /** @return lever from COG */
  Tensor lever();

  /** @return radius of wheel [m] */
  Scalar radius();

  /** @return inverse of wheel moment of inertia [kgm2] */
  Scalar Iw_invert();

  Pacejka3 pacejka();
}
