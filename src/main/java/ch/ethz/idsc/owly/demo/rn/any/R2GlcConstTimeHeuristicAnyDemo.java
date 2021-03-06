// code by jl
package ch.ethz.idsc.owly.demo.rn.any;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ch.ethz.idsc.owl.glc.adapter.GlcExpand;
import ch.ethz.idsc.owl.glc.adapter.GlcNodes;
import ch.ethz.idsc.owl.glc.adapter.SimpleTrajectoryRegionQuery;
import ch.ethz.idsc.owl.glc.any.AnyPlannerInterface;
import ch.ethz.idsc.owl.glc.any.OptimalAnyTrajectoryPlanner;
import ch.ethz.idsc.owl.glc.core.DebugUtils;
import ch.ethz.idsc.owl.glc.core.GlcNode;
import ch.ethz.idsc.owl.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.owl.glc.par.Parameters;
import ch.ethz.idsc.owl.gui.ani.OwlyFrame;
import ch.ethz.idsc.owl.gui.ani.OwlyGui;
import ch.ethz.idsc.owl.math.flow.EulerIntegrator;
import ch.ethz.idsc.owl.math.flow.Flow;
import ch.ethz.idsc.owl.math.region.EllipsoidRegion;
import ch.ethz.idsc.owl.math.region.Region;
import ch.ethz.idsc.owl.math.state.FixedStateIntegrator;
import ch.ethz.idsc.owl.math.state.StateIntegrator;
import ch.ethz.idsc.owl.math.state.StateTime;
import ch.ethz.idsc.owl.math.state.TrajectoryRegionQuery;
import ch.ethz.idsc.owly.demo.rn.R2Flows;
import ch.ethz.idsc.owly.demo.rn.R2NoiseRegion;
import ch.ethz.idsc.owly.demo.rn.R2Parameters;
import ch.ethz.idsc.owly.demo.rn.RnTrajectoryGoalManager;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.sca.Sin;

enum R2GlcConstTimeHeuristicAnyDemo {
  ;
  public static void main(String[] args) throws Exception {
    RationalScalar resolution = (RationalScalar) RealScalar.of(10);
    Scalar timeScale = RealScalar.of(2.5);
    Scalar depthScale = RealScalar.of(100);
    Tensor partitionScale = Tensors.vector(20, 20);
    Scalar dtMax = RationalScalar.of(1, 6);
    int maxIter = 2000;
    Scalar runTime = RealScalar.of(0.4);
    Scalar lipschitz = RealScalar.ONE;
    Parameters parameters = new R2Parameters( //
        resolution, timeScale, depthScale, partitionScale, dtMax, maxIter, lipschitz);
    StateIntegrator stateIntegrator = FixedStateIntegrator.create(EulerIntegrator.INSTANCE, parameters.getdtMax(), //
        parameters.getTrajectorySize());
    parameters.printResolution();
    System.out.println("DomainSize: 1/Eta: " + parameters.getEta().map(n -> RealScalar.ONE.divide(n)));
    R2Flows r2Config = new R2Flows(RealScalar.ONE);
    Collection<Flow> controls = r2Config.getFlows(parameters.getResolutionInt());
    // Creating Goals
    List<StateTime> precomputedTrajectory = new ArrayList<>();
    List<Region<Tensor>> goalRegions = new ArrayList<>();
    Tensor radius = Tensors.vector(0.2, 0.2);
    System.out.println("Goalstates: ");
    for (int i = 0; i < 8; i++) {
      Tensor goal = Tensors.of(RealScalar.of(1.3 * i), Sin.of(RealScalar.of(2 * Math.PI * i / 10)).multiply(RealScalar.of(i * 0.8)));
      System.out.println(goal);
      precomputedTrajectory.add(new StateTime(goal, RealScalar.ZERO));
      goalRegions.add(new EllipsoidRegion(goal, radius));
    }
    RnTrajectoryGoalManager rnGoal = new RnTrajectoryGoalManager(goalRegions, precomputedTrajectory, radius);
    Region<Tensor> region = new R2NoiseRegion(RealScalar.of(0.1));
    TrajectoryRegionQuery obstacleQuery = SimpleTrajectoryRegionQuery.timeInvariant(region);
    AnyPlannerInterface anyPlannerInterface = new OptimalAnyTrajectoryPlanner( //
        parameters.getEta(), stateIntegrator, controls, obstacleQuery, rnGoal);
    anyPlannerInterface.switchRootToState(new StateTime(Tensors.vector(-3, 0), RealScalar.ZERO));
    GlcExpand.constTime(anyPlannerInterface, runTime, parameters.getDepthLimit());
    // --
    OwlyFrame owlyFrame = OwlyGui.start();
    owlyFrame.configCoordinateOffset(400, 400);
    owlyFrame.jFrame.setBounds(0, 0, 800, 800);
    owlyFrame.setGlc((TrajectoryPlanner) anyPlannerInterface);
    // -- Anytime loop
    boolean finalGoalFound = false;
    while (!finalGoalFound) {
      Thread.sleep(1);
      long tic = System.nanoTime();
      // -- ROOTCHANGE
      long ticTemp = System.nanoTime();
      Optional<GlcNode> finalGoalNode = anyPlannerInterface.getFinalGoalNode();
      List<StateTime> trajectory = null;
      if (finalGoalNode.isPresent()) {
        trajectory = GlcNodes.getPathFromRootTo(finalGoalNode.get());
        System.out.println("trajectorys size: " + trajectory.size());
        if (trajectory.size() > 5) {
          //
          StateTime newRootState = trajectory.get(trajectory.size() > 3 ? 3 : 0);
          int increment = anyPlannerInterface.switchRootToState(newRootState);
          parameters.increaseDepthLimit(increment);
        }
      }
      long tocTemp = System.nanoTime();
      System.out.println("Rootchange took: " + (tocTemp - ticTemp) * 1e-9 + "s");
      // -- GOALCHANGE
      ticTemp = tic;
      Optional<StateTime> furthestState = anyPlannerInterface.getFurthestGoalState();
      if (furthestState.isPresent()) {
        if (rnGoal.getGoalRegionList().get(rnGoal.getGoalRegionList().size() - 1).isMember(furthestState.get().state())) {
          System.out.println("***Last Goal was found***");
          finalGoalFound = true;
        }
      }
      System.out.println("Current size of goal regions list: " + rnGoal.getGoalRegionList().size());
      // only change goal if we are not at the end yet
      // creates new RegionUnin form Regionlist and puts Heuristic to next Goal in RegionList
      rnGoal = new RnTrajectoryGoalManager(rnGoal.deleteRegionsBefore(furthestState), precomputedTrajectory, radius);
      anyPlannerInterface.changeToGoal(rnGoal);
      if (rnGoal.getGoalRegionList().size() < 2)
        System.err.println("changed to single region goal --> Last Change");
      tocTemp = System.nanoTime();
      System.out.println("Goalchange took: " + (tocTemp - ticTemp) * 1e-9 + "s");
      // -- EXPANDING
      ticTemp = System.nanoTime();
      int expandIter = GlcExpand.constTime(anyPlannerInterface, runTime, parameters.getDepthLimit());
      furthestState = anyPlannerInterface.getFurthestGoalState();
      // check if furthest Goal is already in last Region in List
      tocTemp = System.nanoTime();
      System.out.println("Expanding took: " + (tocTemp - ticTemp) * 1e-9 + "s");
      // --
      long toc = System.nanoTime();
      owlyFrame.setGlc((TrajectoryPlanner) anyPlannerInterface);
      System.out.println((toc - tic) * 1e-9 + " Seconds needed to replan");
      System.out.println("After goal switch needed " + expandIter + " iterations");
      System.out.println("*****Finished*****");
      DebugUtils.heuristicConsistencyCheck((TrajectoryPlanner) anyPlannerInterface);
      if (!owlyFrame.jFrame.isVisible() || expandIter < 1)
        break;
    }
    System.out.println("Finished LOOP");
  }
}
