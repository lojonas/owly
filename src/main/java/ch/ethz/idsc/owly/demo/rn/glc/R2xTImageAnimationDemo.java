// code by jph
package ch.ethz.idsc.owly.demo.rn.glc;

import ch.ethz.idsc.owly.demo.rn.R2ImageRegions;
import ch.ethz.idsc.owly.demo.rn.R2xTImageStateTimeRegion;
import ch.ethz.idsc.owly.demo.util.DemoInterface;
import ch.ethz.idsc.owly.glc.adapter.SimpleTrajectoryRegionQuery;
import ch.ethz.idsc.owly.gui.RenderInterface;
import ch.ethz.idsc.owly.gui.ani.AbstractEntity;
import ch.ethz.idsc.owly.gui.ani.OwlyAnimationFrame;
import ch.ethz.idsc.owly.math.region.ImageRegion;
import ch.ethz.idsc.owly.math.region.Region;
import ch.ethz.idsc.owly.math.se2.RigidFamily;
import ch.ethz.idsc.owly.math.se2.Se2Family;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensors;

public class R2xTImageAnimationDemo implements DemoInterface {
  @Override
  public void start() {
    OwlyAnimationFrame owlyAnimationFrame = new OwlyAnimationFrame();
    AbstractEntity abstractEntity = new R2xTEntity(Tensors.vector(1.5, 2), RealScalar.of(1.5));
    owlyAnimationFrame.set(abstractEntity);
    // ---
    RigidFamily rigid1 = Se2Family.rotationAround(Tensors.vectorDouble(1.5, 2), time -> time.multiply(RealScalar.of(.1)));
    ImageRegion imageRegion = R2ImageRegions.inside_circ();
    Region<StateTime> region1 = new R2xTImageStateTimeRegion( //
        imageRegion, rigid1, () -> abstractEntity.getStateTimeNow().time());
    // ---
    owlyAnimationFrame.setObstacleQuery(new SimpleTrajectoryRegionQuery(region1));
    owlyAnimationFrame.addBackground((RenderInterface) region1);
    owlyAnimationFrame.configCoordinateOffset(200, 400);
    owlyAnimationFrame.jFrame.setVisible(true);
  }

  public static void main(String[] args) {
    new R2xTImageAnimationDemo().start();
  }
}
