// code by jl
package ch.ethz.idsc.owly.demo.se2.any;

import ch.ethz.idsc.owl.gui.ani.OwlyAnimationFrame;
import ch.ethz.idsc.owl.img.ImageRegions;
import ch.ethz.idsc.owl.math.region.ImageRegion;
import ch.ethz.idsc.owl.math.state.StateTime;
import ch.ethz.idsc.owly.demo.util.DemoInterface;
import ch.ethz.idsc.owly.demo.util.RegionRenders;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensors;

public class Se2GlcAnyAnimation2Demo implements DemoInterface {
  @Override
  public void start() {
    try {
      OwlyAnimationFrame owlyAnimationFrame = new OwlyAnimationFrame();
      StateTime root = new StateTime(Tensors.vector(7.5, 6, 1), RealScalar.ZERO);
      Se2AnyEntity se2AnyEntity = new Se2AnyEntity(root, 12);
      se2AnyEntity.trajectoryPlannerCallback = owlyAnimationFrame.trajectoryPlannerCallback;
      // Region obstacleRegion = new InvertedRegion(EmptyRegion.INSTANCE);
      ImageRegion imageRegion = ImageRegions.loadFromRepository("/io/track0_100.png", Tensors.vector(10, 10), false);
      // Region obstacleRegion = new R2NoiseRegion(0.8);
      se2AnyEntity.startLife(imageRegion, root); // (trq, root);
      owlyAnimationFrame.set(se2AnyEntity);
      owlyAnimationFrame.configCoordinateOffset(50, 700);
      owlyAnimationFrame.addBackground(RegionRenders.create(imageRegion));
      owlyAnimationFrame.jFrame.setBounds(100, 50, 800, 800);
      owlyAnimationFrame.jFrame.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws Exception {
    new Se2GlcAnyAnimation2Demo().start();
  }
}
