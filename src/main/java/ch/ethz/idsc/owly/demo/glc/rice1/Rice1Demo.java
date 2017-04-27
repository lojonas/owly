// code by jph
package ch.ethz.idsc.owly.demo.glc.rice1;

import java.util.Collection;
import java.util.List;

import ch.ethz.idsc.owly.glc.adapter.EllipsoidRegion;
import ch.ethz.idsc.owly.glc.adapter.SimpleTrajectoryRegionQuery;
import ch.ethz.idsc.owly.glc.adapter.TimeInvariantRegion;
import ch.ethz.idsc.owly.glc.core.DefaultTrajectoryPlanner;
import ch.ethz.idsc.owly.glc.core.StateTime;
import ch.ethz.idsc.owly.glc.core.Trajectory;
import ch.ethz.idsc.owly.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.owly.glc.core.TrajectoryRegionQuery;
import ch.ethz.idsc.owly.glc.gui.GlcFrame;
import ch.ethz.idsc.owly.math.Flow;
import ch.ethz.idsc.owly.math.RegionUnion;
import ch.ethz.idsc.owly.math.integrator.Integrator;
import ch.ethz.idsc.owly.math.integrator.MidpointIntegrator;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/** "Mobility and Autonomous Reconfiguration of Marsokhod" */
public class Rice1Demo {
  public static void main(String[] args) {
    Scalar timeStep = RationalScalar.of(1, 5);
    Tensor partitionScale = Tensors.vector(5, 8);
    Collection<Flow> controls = Rice1Controls.createControls(RealScalar.of(.5), 15); //
    int trajectorySize = 5;
    Rice1GoalManager rice1Goal = new Rice1GoalManager(Tensors.vector(6, -.7), Tensors.vector(.4, .3));
    TrajectoryRegionQuery obstacleQuery = //
        new SimpleTrajectoryRegionQuery(new TimeInvariantRegion( //
            RegionUnion.of( //
                new EllipsoidRegion(Tensors.vector(+3, +1), Tensors.vector(1.75, .75)),
                // , // speed limit along the way
                new EllipsoidRegion(Tensors.vector(-2, +0), Tensors.vector(1, 1)) // block to the left
            )));
    // ---
    Integrator integrator = new MidpointIntegrator();
    TrajectoryPlanner trajectoryPlanner = new DefaultTrajectoryPlanner( //
        integrator, timeStep, partitionScale, controls, trajectorySize, //
        rice1Goal, rice1Goal, rice1Goal, obstacleQuery);
    // ---
    trajectoryPlanner.insertRoot(Tensors.vector(0, 0));
    int iters = trajectoryPlanner.plan(1000);
    System.out.println(iters);
    List<StateTime> trajectory = trajectoryPlanner.getPathFromRootToGoal();
    Trajectory.print(trajectory);
    GlcFrame glcFrame = new GlcFrame();
    glcFrame.glcComponent.setTrajectoryPlanner(trajectoryPlanner);
  }
}
