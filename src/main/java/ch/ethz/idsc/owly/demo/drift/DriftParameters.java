// code by edo
package ch.ethz.idsc.owly.demo.drift;

import java.io.Serializable;

import ch.ethz.idsc.owly.math.car.Pacejka3;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;

public class DriftParameters implements Serializable {
  public static final Scalar GRAVITATION = RealScalar.of(9.81);
  // mass [kg]
  public final Scalar m = RealScalar.of(1412);
  public final Scalar mg = GRAVITATION.multiply(m);
  // yawing moment of inertia [kgm2]
  private final Scalar Iz = RealScalar.of(1536.7 + 427.7084); // sprung mass inertia + unsprung mass inertia
  private final Scalar Iz_reciprocal = Iz.reciprocal(); // sprung mass inertia + unsprung mass inertia
  // front axle distance from COG [m]
  public final Scalar a = RealScalar.of(1.015);
  // rear axle distanc from COG [m]
  public final Scalar b = RealScalar.of(1.895);
  // pacejka model parameters
  // 1 - for frint tires, 2 - rear tires
  // Pacejka3
  // Scalar B1 = 13.8509;par.C1=1.367;par.D1=0.9622;
  // Scalar B2 = 14.1663;par.C2=1.3652;par.D2=0.9744;
  // choose average
  public final Pacejka3 pacejka3 = new Pacejka3( //
      (13.8509 + 14.1663) / 2, //
      (1.367 + 1.3652) / 2, //
      (0.9622 + 0.9744) / 2);
  // gravitational acceleration [m/s2]
  public final Scalar muF = RealScalar.of(0.55);
  public final Scalar muR = RealScalar.of(0.53);

  public Scalar Fz_F() {
    return mg.multiply(b).divide(a.add(b));
  }

  public Scalar Fz_R() {
    return mg.multiply(a).divide(a.add(b));
  }

  public Scalar Iz_invert() {
    return Iz_reciprocal;
  }
}
