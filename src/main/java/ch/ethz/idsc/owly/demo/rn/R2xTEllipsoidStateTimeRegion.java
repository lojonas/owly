// code by jph
package ch.ethz.idsc.owly.demo.rn;

import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.function.Supplier;

import ch.ethz.idsc.owly.gui.GeometricLayer;
import ch.ethz.idsc.owly.gui.RenderInterface;
import ch.ethz.idsc.owly.gui.region.RegionRenders;
import ch.ethz.idsc.owly.math.CirclePoints;
import ch.ethz.idsc.owly.math.TensorUnaryOperator;
import ch.ethz.idsc.owly.math.region.Region;
import ch.ethz.idsc.owly.math.se2.BijectionFamily;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.red.Norm2Squared;

/** check if input tensor is inside a polygon */
public class R2xTEllipsoidStateTimeRegion implements Region<StateTime>, RenderInterface {
  private static final int RESOLUTION = 22;
  // ---
  private final Tensor invert;
  private final Tensor polygon;
  private final BijectionFamily bijectionFamily;
  private final Supplier<Scalar> supplier;

  public R2xTEllipsoidStateTimeRegion(Tensor radius, BijectionFamily bijectionFamily, Supplier<Scalar> supplier) {
    invert = radius.map(Scalar::reciprocal);
    this.bijectionFamily = bijectionFamily;
    this.supplier = supplier;
    polygon = CirclePoints.elliptic(RESOLUTION, radius.Get(0), radius.Get(1));
  }

  @Override
  public boolean isMember(StateTime stateTime) {
    Tensor state = stateTime.state().extract(0, invert.length());
    Scalar time = stateTime.time();
    TensorUnaryOperator rev = bijectionFamily.inverse(time);
    return Scalars.lessEquals(Norm2Squared.ofVector(rev.apply(state).pmul(invert)), RealScalar.ONE);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Scalar time = supplier.get();
    TensorUnaryOperator fwd = bijectionFamily.forward(time);
    Path2D path2D = geometricLayer.toPath2D(Tensor.of(polygon.stream().map(fwd)));
    graphics.setColor(RegionRenders.COLOR);
    graphics.fill(path2D);
    graphics.setColor(RegionRenders.BOUNDARY);
    path2D.closePath();
    graphics.draw(path2D);
  }
}
