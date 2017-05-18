// code by jph
package ch.ethz.idsc.owly.demo.glc.se2r;

import java.io.File;
import java.util.Collection;

import ch.ethz.idsc.owly.demo.glc.se2.Se2Utils;
import ch.ethz.idsc.owly.glc.adapter.SimpleTrajectoryRegionQuery;
import ch.ethz.idsc.owly.glc.core.DefaultTrajectoryPlanner;
import ch.ethz.idsc.owly.glc.core.Expand;
import ch.ethz.idsc.owly.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.owly.gui.Gui;
import ch.ethz.idsc.owly.gui.OwlyFrame;
import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.owly.math.flow.RungeKutta4Integrator;
import ch.ethz.idsc.owly.math.region.HyperplaneRegion;
import ch.ethz.idsc.owly.math.region.RegionUnion;
import ch.ethz.idsc.owly.math.state.FixedStateIntegrator;
import ch.ethz.idsc.owly.math.state.StateIntegrator;
import ch.ethz.idsc.owly.math.state.TimeInvariantRegion;
import ch.ethz.idsc.owly.math.state.TrajectoryRegionQuery;
import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.GifSequenceWriter;

/** (x,y,theta) */
class Se2rExpandDemo {
  public static void main(String[] args) throws Exception {
    Tensor eta = Tensors.vector(6, 6, 15); // TODO instead of 15 use multiple of PI...
    StateIntegrator stateIntegrator = FixedStateIntegrator.create( //
        new RungeKutta4Integrator(), RationalScalar.of(1, 6), 5);
    Collection<Flow> controls = Se2rControls.createControls(Se2Utils.DEGREE(45), 6);
    // place holder for parameter class
    Se2rGoalManager se2GoalManager = new Se2rGoalManager( //
        Tensors.vector(-1, -1), RealScalar.of(Math.PI * 2), //
        DoubleScalar.of(.1), Se2Utils.DEGREE(10));
    TrajectoryRegionQuery goalQuery = //
        new SimpleTrajectoryRegionQuery(new TimeInvariantRegion(se2GoalManager));
    TrajectoryRegionQuery obstacleQuery = //
        new SimpleTrajectoryRegionQuery(new TimeInvariantRegion( //
            RegionUnion.of( //
                new HyperplaneRegion(Tensors.vector(0, -1, 0), RealScalar.of(1.5)), //
                new HyperplaneRegion(Tensors.vector(0, +1, 0), RealScalar.of(2.0)) //
            )));
    // ---
    TrajectoryPlanner trajectoryPlanner = new DefaultTrajectoryPlanner( //
        eta, stateIntegrator, controls, se2GoalManager, goalQuery, obstacleQuery);
    // ---
    trajectoryPlanner.insertRoot(Tensors.vector(0, 0, 0));
    OwlyFrame owlyFrame = Gui.start();
    owlyFrame.configCoordinateOffset(169, 71);
    owlyFrame.jFrame.setBounds(100, 100, 300, 200);
    // TODO user directory
    GifSequenceWriter gsw = GifSequenceWriter.of(new File("/home/datahaki/se2r.gif"), 250);
    while (trajectoryPlanner.getBest() == null && owlyFrame.jFrame.isVisible()) {
      Expand.maxSteps(trajectoryPlanner, 1);
      owlyFrame.setGlc(trajectoryPlanner);
      gsw.append(owlyFrame.offscreen());
      Thread.sleep(100);
    }
    int repeatLast = 6;
    while (0 < repeatLast--)
      gsw.append(owlyFrame.offscreen());
    gsw.close();
    System.out.println("created gif");
  }
}
