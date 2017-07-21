// code by jl
package ch.ethz.idsc.owly.demo.twd.glc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ch.ethz.idsc.owly.demo.rn.R2NoiseRegion;
import ch.ethz.idsc.owly.demo.twd.TwdControls;
import ch.ethz.idsc.owly.demo.twd.TwdHeuristicGoalManager;
import ch.ethz.idsc.owly.demo.twd.TwdStateSpaceModel;
import ch.ethz.idsc.owly.glc.adapter.Parameters;
import ch.ethz.idsc.owly.glc.adapter.SimpleTrajectoryRegionQuery;
import ch.ethz.idsc.owly.glc.core.Expand;
import ch.ethz.idsc.owly.glc.core.GlcNode;
import ch.ethz.idsc.owly.glc.core.GlcNodes;
import ch.ethz.idsc.owly.glc.core.StandardTrajectoryPlanner;
import ch.ethz.idsc.owly.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.owly.gui.Gui;
import ch.ethz.idsc.owly.gui.OwlyFrame;
import ch.ethz.idsc.owly.math.flow.EulerIntegrator;
import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.owly.math.region.Region;
import ch.ethz.idsc.owly.math.state.FixedStateIntegrator;
import ch.ethz.idsc.owly.math.state.StateIntegrator;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.owly.math.state.TimeInvariantRegion;
import ch.ethz.idsc.owly.math.state.Trajectories;
import ch.ethz.idsc.owly.math.state.TrajectoryRegionQuery;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

enum TwdGlcNoiseDemo {
  ;
  public static void main(String[] args) {
    RationalScalar resolution = (RationalScalar) RealScalar.of(6);
    Scalar timeScale = RealScalar.of(10);
    Scalar depthScale = RealScalar.of(20);
    Tensor partitionScale = Tensors.vector(5, 5, 2 * Math.PI / 360 * 10);
    Scalar dtMax = RationalScalar.of(1, 10);
    int maxIter = 2000;
    Scalar wheelDistance = RealScalar.of(0.2);
    Scalar wheelRadius = RealScalar.of(0.05);
    Scalar wheelspeeds_max = RealScalar.of(2).multiply(RealScalar.of(2 * Math.PI));
    TwdStateSpaceModel stateSpaceModel = new TwdStateSpaceModel(wheelRadius, wheelDistance, wheelspeeds_max);
    Parameters parameters = new TwdParameters( //
        resolution, timeScale, depthScale, partitionScale, dtMax, maxIter, stateSpaceModel.getTensorLipschitz());
    parameters.printResolution();
    System.out.println("DomainSize: 1/Eta: " + parameters.getEta().map(n -> RealScalar.ONE.divide(n)));
    Region region = new R2NoiseRegion(.1);
    StateIntegrator stateIntegrator = //
        FixedStateIntegrator.create(EulerIntegrator.INSTANCE, parameters.getdtMax(), parameters.getTrajectorySize());
    // Controls
    Collection<Flow> controls = TwdControls.createControls2(//
        stateSpaceModel, parameters.getResolutionInt());
    // GoalRegion
    Tensor goalCenter = Tensors.vector(5.5, 0, -1 * Math.PI);
    Tensor radiusVector = Tensors.vector(0.2, 0.2, 2 * Math.PI / 360 * 50);
    TwdHeuristicGoalManager twdGoal = new TwdHeuristicGoalManager(goalCenter, radiusVector);
    // ObstacleRegion
    TrajectoryRegionQuery obstacleQuery = //
        new SimpleTrajectoryRegionQuery(new TimeInvariantRegion( //
            region));
    // Planner init
    TrajectoryPlanner trajectoryPlanner = new StandardTrajectoryPlanner( //
        partitionScale, stateIntegrator, controls, obstacleQuery, twdGoal.getGoalInterface());
    trajectoryPlanner.insertRoot(Tensors.vector(0, 0, 0));
    OwlyFrame owlyFrame = Gui.start();
    owlyFrame.configCoordinateOffset(33, 416);
    owlyFrame.jFrame.setBounds(100, 100, 620, 475);
    owlyFrame.setGlc(trajectoryPlanner);
    // Planning
    while (!trajectoryPlanner.getBest().isPresent() && owlyFrame.jFrame.isVisible()) {
      Expand.maxSteps(trajectoryPlanner, 100, parameters.getDepthLimit());
      owlyFrame.setGlc(trajectoryPlanner);
    }
    Optional<GlcNode> optional = trajectoryPlanner.getBest();
    if (optional.isPresent()) {
      List<StateTime> trajectory = GlcNodes.getPathFromRootTo(optional.get());
      Trajectories.print(trajectory);
    }
  }
}