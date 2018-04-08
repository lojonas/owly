// code by jph
package ch.ethz.idsc.owly.demo.se2.glc;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import ch.ethz.idsc.owl.glc.adapter.SimpleTrajectoryRegionQuery;
import ch.ethz.idsc.owl.gui.RenderInterface;
import ch.ethz.idsc.owl.gui.ani.OwlyAnimationFrame;
import ch.ethz.idsc.owl.img.ImageRegions;
import ch.ethz.idsc.owl.math.Degree;
import ch.ethz.idsc.owl.math.map.ShadowMap;
import ch.ethz.idsc.owl.math.region.ImageRegion;
import ch.ethz.idsc.owl.math.state.StateTime;
import ch.ethz.idsc.owl.math.state.TrajectoryRegionQuery;
import ch.ethz.idsc.owl.sim.LidarEmulator;
import ch.ethz.idsc.owly.demo.rn.R2ImageRegionWrap;
import ch.ethz.idsc.owly.demo.rn.R2ImageRegions;
import ch.ethz.idsc.owly.demo.se2.CarFlows;
import ch.ethz.idsc.owly.demo.se2.CarStandardFlows;
import ch.ethz.idsc.owly.demo.se2.CarVelocityFlows;
import ch.ethz.idsc.owly.demo.se2.ShadowConstraint;
import ch.ethz.idsc.owly.demo.util.RegionRenders;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensors;

public class Se2ShadowMapDemo extends Se2CarDemo {
  @Override
  void configure(OwlyAnimationFrame owlyAnimationFrame) throws IOException {
    CarFlows carFlows = new CarVelocityFlows(Tensors.vector(0.5,1), Degree.of(60));
    CarEntity se2Entity = new CarEntity(new StateTime(Tensors.vector(3.3, 1.0, 3.14/2), RealScalar.ZERO), carFlows);
    //
    ImageRegion imageRegion = null;
    try {
      imageRegion = ImageRegions.loadFromRepository("/scenarios/streetIntersection1.png", Tensors.vector(8,8), false);
    } catch (Exception e1) {
      e1.printStackTrace();
    }

    TrajectoryRegionQuery trq = createCarQuery(imageRegion);
    se2Entity.obstacleQuery = trq;
    TrajectoryRegionQuery ray = SimpleTrajectoryRegionQuery.timeInvariant(imageRegion);
    owlyAnimationFrame.setObstacleQuery(trq);
    owlyAnimationFrame.addBackground(RegionRenders.create(imageRegion));
    // LIDAR
    RenderInterface lidarEmulator = new LidarEmulator( //
        LidarEmulator.DEFAULT, se2Entity::getStateTimeNow, ray);
    owlyAnimationFrame.addBackground(lidarEmulator);
    // SHADOWMAP
    ShadowMap shadowMap = new ShadowMap((LidarEmulator) lidarEmulator, imageRegion, se2Entity::getStateTimeNow, 10);
    owlyAnimationFrame.addBackground(shadowMap);
    ShadowConstraint shadowConstraint = new ShadowConstraint(shadowMap);
    se2Entity.extraConstraints.add(shadowConstraint);
    owlyAnimationFrame.set(se2Entity);
    
    shadowMap.startNonBlocking();
    //
    owlyAnimationFrame.jFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        System.out.println("window was closed. terminating...");
        shadowMap.flagShutdown();
      }
    });
  }

  public static void main(String[] args) {
    new Se2ShadowMapDemo().start();
  }
}
