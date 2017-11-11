// code by jph
package ch.ethz.idsc.owly.demo.rice.glc;

import java.util.Arrays;
import java.util.Collection;

import ch.ethz.idsc.owly.demo.rice.Rice2Controls;
import ch.ethz.idsc.owly.demo.rn.R2xTEllipsoidStateTimeRegion;
import ch.ethz.idsc.owly.demo.rn.R2xTPolygonStateTimeRegion;
import ch.ethz.idsc.owly.demo.rn.glc.R2xTEllipsoidsAnimationDemo;
import ch.ethz.idsc.owly.demo.util.DemoInterface;
import ch.ethz.idsc.owly.glc.adapter.SimpleTrajectoryRegionQuery;
import ch.ethz.idsc.owly.gui.RenderInterface;
import ch.ethz.idsc.owly.gui.ani.OwlyAnimationFrame;
import ch.ethz.idsc.owly.math.CogPoints;
import ch.ethz.idsc.owly.math.ScalarTensorFunction;
import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.owly.math.noise.SimplexContinuousNoise;
import ch.ethz.idsc.owly.math.region.Region;
import ch.ethz.idsc.owly.math.region.RegionUnion;
import ch.ethz.idsc.owly.math.se2.BijectionFamily;
import ch.ethz.idsc.owly.math.se2.So2Family;
import ch.ethz.idsc.owly.math.se2.TranslationFamily;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.owly.math.state.TrajectoryRegionQuery;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class Rice2dxTParts1Demo implements DemoInterface {
  @Override
  public void start() {
    OwlyAnimationFrame owlyAnimationFrame = new OwlyAnimationFrame();
    Scalar mu = RealScalar.of(-.5);
    Collection<Flow> controls = Rice2Controls.create2d(mu, 1, 15);
    Rice2dEntity abstractEntity = new Rice2dEntity(mu, Tensors.vector(2, 2, 0, 0), controls);
    abstractEntity.delayHint = RealScalar.of(1.6);
    owlyAnimationFrame.set(abstractEntity);
    // ---
    ScalarTensorFunction stf1 = R2xTEllipsoidsAnimationDemo.wrap1DTensor(SimplexContinuousNoise.FUNCTION, Tensors.vector(4, 2), 0.03, 2.5);
    BijectionFamily noise1 = new TranslationFamily(stf1);
    Region<StateTime> region1 = new R2xTEllipsoidStateTimeRegion( //
        Tensors.vector(0.4, 0.5), noise1, () -> abstractEntity.getStateTimeNow().time());
    ScalarTensorFunction stf2 = R2xTEllipsoidsAnimationDemo.wrap1DTensor(SimplexContinuousNoise.FUNCTION, Tensors.vector(1, 3), 0.03, 2.5);
    BijectionFamily noise2 = new TranslationFamily(stf2);
    Region<StateTime> region2 = new R2xTEllipsoidStateTimeRegion( //
        Tensors.vector(0.5, 0.6), noise2, () -> abstractEntity.getStateTimeNow().time());
    // ---
    BijectionFamily rigid2 = new So2Family(s -> s.multiply(RealScalar.of(.25)));
    Tensor polygon = CogPoints.of(4, RealScalar.of(1.0), RealScalar.of(0.3));
    Region<StateTime> region3 = new R2xTPolygonStateTimeRegion( //
        polygon, rigid2, () -> abstractEntity.getStateTimeNow().time());
    TrajectoryRegionQuery trq = new SimpleTrajectoryRegionQuery( //
        RegionUnion.wrap(Arrays.asList(region1, region2, region3)));
    // abstractEntity.obstacleQuery = trq;
    owlyAnimationFrame.setObstacleQuery(trq);
    // owlyAnimationFrame.addRegionRender(imageRegion);
    owlyAnimationFrame.addBackground((RenderInterface) region1);
    owlyAnimationFrame.addBackground((RenderInterface) region2);
    owlyAnimationFrame.addBackground((RenderInterface) region3);
    // ---
    owlyAnimationFrame.configCoordinateOffset(350, 350);
    owlyAnimationFrame.jFrame.setVisible(true);
  }

  public static void main(String[] args) {
    new Rice2dxTParts1Demo().start();
  }
}
