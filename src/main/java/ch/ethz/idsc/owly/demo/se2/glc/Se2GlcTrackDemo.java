// code by jph, ynager
package ch.ethz.idsc.owly.demo.se2.glc;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import ch.ethz.idsc.owl.glc.adapter.SimpleTrajectoryRegionQuery;
import ch.ethz.idsc.owl.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.owl.gui.RenderInterface;
import ch.ethz.idsc.owl.gui.ani.AbstractEntity;
import ch.ethz.idsc.owl.gui.ani.AnimationInterface;
import ch.ethz.idsc.owl.gui.ani.MotionPlanWorker;
import ch.ethz.idsc.owl.gui.ani.OwlyAnimationFrame;
import ch.ethz.idsc.owl.gui.ren.ArrowHeadRender;
import ch.ethz.idsc.owl.math.region.ImageRegion;
import ch.ethz.idsc.owl.math.state.StateTime;
import ch.ethz.idsc.owl.math.state.TrajectoryRegionQuery;
import ch.ethz.idsc.owl.math.state.TrajectorySample;
import ch.ethz.idsc.owl.sim.CameraEmulator;
import ch.ethz.idsc.owl.sim.LidarEmulator;
import ch.ethz.idsc.owly.demo.rn.R2ImageRegionWrap;
import ch.ethz.idsc.owly.demo.rn.R2ImageRegions;
import ch.ethz.idsc.owly.demo.util.RegionRenders;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class Se2GlcTrackDemo extends Se2CarDemo {
  @Override
  void configure(OwlyAnimationFrame owlyAnimationFrame) throws IOException {
    R2ImageRegionWrap r2ImageRegionWrap = R2ImageRegions._EIGHT;
    CarEntity se2Entity = CarEntity.createDefault(new StateTime(Tensors.vector(6, 6, 1), RealScalar.ZERO));
    se2Entity.extraCosts.add(r2ImageRegionWrap.costFunction());
    ImageRegion imageRegion = r2ImageRegionWrap.imageRegion();
    TrajectoryRegionQuery trq = createCarQuery(imageRegion);
    se2Entity.obstacleQuery = trq;
    TrajectoryRegionQuery ray = SimpleTrajectoryRegionQuery.timeInvariant(imageRegion);
    owlyAnimationFrame.set(se2Entity);
    owlyAnimationFrame.setObstacleQuery(trq);
    owlyAnimationFrame.addBackground(RegionRenders.create(imageRegion));
    {
      RenderInterface renderInterface = new CameraEmulator( //
          48, RealScalar.of(10), se2Entity::getStateTimeNow, ray);
      owlyAnimationFrame.addBackground(renderInterface);
    }
    {
      RenderInterface renderInterface = new LidarEmulator( //
          LidarEmulator.DEFAULT, se2Entity::getStateTimeNow, ray);
      owlyAnimationFrame.addBackground(renderInterface);
    }
    //
    // setup motion planning
    MotionPlanWorker mpw;
    AnimationInterface controllable = se2Entity;
    AbstractEntity abstractEntity = (AbstractEntity) controllable;
    List<TrajectorySample> head;
    //
    // define waypoints
    Tensor waypoints = Tensors.of( //
        Tensors.vector(5.5, 6.3, 1.5), //
        Tensors.vector(7.8, 8.5, 0), //
        Tensors.vector(10, 6.1, -1.5), //
        Tensors.vector(7.9, 3.8, -3.14), //
        Tensors.vector(5.5, 6.3, 1.5), //
        Tensors.vector(3.4, 8.4, -3.14), //
        Tensors.vector(1.8, 6.4, -1.5), //
        Tensors.vector(3.5, 4, 0)).unmodifiable();
    //
    {
      RenderInterface renderInterface = new ArrowHeadRender(waypoints, new Color(64, 192, 64, 64));
      owlyAnimationFrame.addBackground(renderInterface);
    }
    //
    // plan to first waypoint
    Tensor goal = waypoints.get(0);
    head = abstractEntity.getFutureTrajectoryUntil(abstractEntity.delayHint());
    TrajectoryPlanner trajectoryPlanner = //
        abstractEntity.createTrajectoryPlanner(trq, goal);
    mpw = new MotionPlanWorker(owlyAnimationFrame.trajectoryPlannerCallback);
    mpw.start(head, trajectoryPlanner);
    //
    // start waypoint tracking loop
    owlyAnimationFrame.jFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        // TODO program should terminate
        System.out.println("window was closed. while loop should terminate.");
      }
    });
    int i = 0;
    while (true) {
      Tensor loc = abstractEntity.getEstimatedLocationAt(abstractEntity.delayHint());
      Scalar dist = se2Entity.distance(loc, goal).abs();
      Scalar distThreshold = RealScalar.of(1);
      if (Scalars.lessThan(dist, distThreshold)) { // if close enough to current waypoint switch to next
        // shut down mpw
        if (Objects.nonNull(mpw)) {
          mpw.flagShutdown();
          mpw = null;
        }
        i = (i + 1) % waypoints.length();
        goal = waypoints.get(i);
        head = abstractEntity.getFutureTrajectoryUntil(abstractEntity.delayHint());
        trajectoryPlanner = abstractEntity.createTrajectoryPlanner(trq, goal);
        mpw = new MotionPlanWorker(owlyAnimationFrame.trajectoryPlannerCallback);
        mpw.start(head, trajectoryPlanner);
      } else {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void main(String[] args) {
    new Se2GlcTrackDemo().start();
  }
}