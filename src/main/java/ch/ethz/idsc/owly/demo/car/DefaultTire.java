// code by jph
package ch.ethz.idsc.owly.demo.car;

import ch.ethz.idsc.owly.math.car.Pacejka3;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

/** class holds invariant parameters of tire */
public class DefaultTire implements TireInterface {
  private final Scalar radius;
  private final Scalar iw_invert;
  private final Pacejka3 pacejka3;
  private final Tensor lever;

  public DefaultTire(Scalar radius, Scalar iw, Pacejka3 pacejka3, Tensor lever) {
    this.radius = radius;
    this.iw_invert = iw.invert();
    this.pacejka3 = pacejka3;
    this.lever = lever.unmodifiable();
  }

  @Override
  public Tensor lever() {
    return lever;
  }

  @Override
  public Scalar radius() {
    return radius;
  }

  @Override
  public Scalar Iw_invert() {
    return iw_invert;
  }

  @Override
  public Pacejka3 pacejka() {
    return pacejka3;
  }
}