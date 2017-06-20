// code by marcello
// code adapted by jph
package ch.ethz.idsc.owly.demo.kart;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Total;

// EXPERIMENTAL API not final
// all values in SI units
class RimoKart {
  // radius of tire
  public static final Scalar r_FL = RealScalar.of((255e-3) / 2);
  public static final Scalar r_FR = RealScalar.of((255e-3) / 2); // assumed to be identical to r_FL
  public static final Scalar r_RL = RealScalar.of((278e-3) / 2);
  public static final Scalar r_RR = RealScalar.of((278e-3) / 2); // assumed to be identical to r_RL
  public static final Scalar m = RealScalar.of(170); // kg
  public static final Scalar length = RealScalar.of(2);
  public static final Scalar l_F = RealScalar.of(2 / 3 * 2); // length
  // TODO why not negative?
  public static final Scalar l_R = RealScalar.of(1 / 3 * 2); // RealScalar.of(
  public static final Scalar width = RealScalar.of(1.4);
  public static final Scalar w_R = RealScalar.of(1.4 / 2);
  public static final Scalar w_L = RealScalar.of(1.4 / 2);
  public static final Scalar I_z = RealScalar.of(25);
  public static final Scalar I_w = RealScalar.of(20);
  // ---
  public static final Scalar g = RealScalar.of(9.81);

  // ---
  // TODO simplify when working
  public static Scalar f0_FLZ() {
    return Total.prod(Tensors.of(m, g, l_R, w_R, inv_area())).Get(); //
    // .divide(l_F.add(l_R).multiply(w_L.add(w_R)));
  }

  public static Scalar f0_FRZ() {
    return Total.prod(Tensors.of(m, g, l_R, w_L, inv_area())).Get(); //
    // .divide(l_F.add(l_R).multiply(w_L.add(w_R)));
  }

  public static Scalar f0_RLZ() {
    return Total.prod(Tensors.of(m, g, l_F, w_R, inv_area())).Get(); //
    // .divide(l_F.add(l_R).multiply(w_L.add(w_R)));
  }

  public static Scalar f0_RRZ() {
    return Total.prod(Tensors.of(m, g, l_F, w_L, inv_area())).Get(); //
    // .divide(l_F.add(l_R).multiply(w_L.add(w_R)));
  }

  public static Scalar inv_area() {
    return width.multiply(length).invert();
  }

  public static Scalar d_flx(Scalar h, Scalar a_x) {
    // TODO why w_R vs l_R
    return Total.prod(Tensors.of(m, h, w_R, a_x, inv_area())).Get();
  }

  public static Scalar d_frx(Scalar h, Scalar a_x) {
    return Total.prod(Tensors.of(m, h, w_L, a_x, inv_area())).Get();
  }
  // ---

  public static Scalar d_ffy(Scalar h, Scalar a_y) {
    return Total.prod(Tensors.of(m, h, l_R, a_y, inv_area())).Get();
  }

  public static Scalar d_fry(Scalar h, Scalar a_y) {
    return Total.prod(Tensors.of(m, h, l_F, a_y, inv_area())).Get();
  }
}
