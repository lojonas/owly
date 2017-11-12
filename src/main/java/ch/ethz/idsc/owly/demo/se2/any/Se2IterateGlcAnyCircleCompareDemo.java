// code by jl
package ch.ethz.idsc.owly.demo.se2.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ch.ethz.idsc.owly.demo.se2.Se2AbstractGoalManager;
import ch.ethz.idsc.owly.demo.se2.Se2CarIntegrator;
import ch.ethz.idsc.owly.demo.se2.Se2Controls;
import ch.ethz.idsc.owly.demo.se2.Se2StateSpaceModel;
import ch.ethz.idsc.owly.demo.se2.glc.Se2Parameters;
import ch.ethz.idsc.owly.glc.adapter.HeuristicQ;
import ch.ethz.idsc.owly.glc.adapter.Parameters;
import ch.ethz.idsc.owly.glc.adapter.RunCompare;
import ch.ethz.idsc.owly.glc.adapter.SimpleTrajectoryRegionQuery;
import ch.ethz.idsc.owly.glc.core.AnyPlannerInterface;
import ch.ethz.idsc.owly.glc.core.DebugUtils;
import ch.ethz.idsc.owly.glc.core.GlcExpand;
import ch.ethz.idsc.owly.glc.core.OptimalAnyTrajectoryPlanner;
import ch.ethz.idsc.owly.glc.core.StandardTrajectoryPlanner;
import ch.ethz.idsc.owly.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.owly.gui.ani.OwlyFrame;
import ch.ethz.idsc.owly.gui.ani.OwlyGui;
import ch.ethz.idsc.owly.math.RotationUtils;
import ch.ethz.idsc.owly.math.StateSpaceModel;
import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.owly.math.region.EllipsoidRegion;
import ch.ethz.idsc.owly.math.region.HyperplaneRegion;
import ch.ethz.idsc.owly.math.region.InvertedRegion;
import ch.ethz.idsc.owly.math.region.RegionUnion;
import ch.ethz.idsc.owly.math.state.FixedStateIntegrator;
import ch.ethz.idsc.owly.math.state.StateIntegrator;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.owly.math.state.TrajectoryRegionQuery;
import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/** (x,y,theta) */
class Se2IterateGlcAnyCircleCompareDemo {
  public static void _run(Scalar resolution) throws Exception {
    RunCompare timingDatabase = new RunCompare(2);
    boolean useGui = false;
    Scalar timeScale = RealScalar.of(4);
    Scalar depthScale = RealScalar.of(10);
    Tensor partitionScale = Tensors.vector(10, 10, 50 / Math.PI);
    Scalar dtMax = RationalScalar.of(1, 12);
    int maxIter = 2000;
    StateSpaceModel stateSpaceModel = Se2StateSpaceModel.INSTANCE;
    Tensor radiusVector = Tensors.of(DoubleScalar.of(0.2), DoubleScalar.of(0.2), RotationUtils.DEGREE(15));
    // --
    Parameters parameters = new Se2Parameters( //
        resolution, timeScale, depthScale, partitionScale, dtMax, maxIter, stateSpaceModel.getLipschitz());
    StateIntegrator stateIntegrator = FixedStateIntegrator.create( //
        Se2CarIntegrator.INSTANCE, parameters.getdtMax(), parameters.getTrajectorySize());
    // ---
    System.out.println("1/Domainsize=" + parameters.getEta());
    parameters.printResolution();
    Collection<Flow> controls = Se2Controls.createControls(RotationUtils.DEGREE(45), parameters.getResolutionInt());
    Se2AbstractGoalManager se2GoalManager = new Se2MinCurvatureGoalManager( //
        Tensors.vector(3, 0, 1.5 * Math.PI), radiusVector);
    TrajectoryRegionQuery obstacleQuery = SimpleTrajectoryRegionQuery.timeInvariant( //
        RegionUnion.wrap(Arrays.asList( //
            new EllipsoidRegion(Tensors.vector(0, 0, 0), Tensors.vector(1, 1, Double.POSITIVE_INFINITY)), //
            new InvertedRegion(new EllipsoidRegion(Tensors.vector(0, 0, 0), Tensors.vector(5, 5, Double.POSITIVE_INFINITY))), //
            new HyperplaneRegion(Tensors.vector(0, -1, 0), RealScalar.of(4)), //
            new HyperplaneRegion(Tensors.vector(0, +1, 0), RealScalar.of(4)) //
        )));
    List<Tensor> goalList = new ArrayList<>();
    goalList.add(Tensors.vector(0, -3, Math.PI));// South
    goalList.add(Tensors.vector(-3, 0, 0.5 * Math.PI));// West
    goalList.add(Tensors.vector(0, 3, 0)); // North
    goalList.add(Tensors.vector(3, 0, -0.5 * Math.PI)); // East
    // ---
    System.out.println("***ANY***");
    // {
    AnyPlannerInterface anyTrajectoryPlanner = new OptimalAnyTrajectoryPlanner( //
        parameters.getEta(), stateIntegrator, controls, obstacleQuery, se2GoalManager.getGoalInterface());
    anyTrajectoryPlanner.switchRootToState(Tensors.vector(0, 3, 0));
    int iters = GlcExpand.maxDepth(anyTrajectoryPlanner, parameters.getDepthLimit());
    System.out.println("After " + iters + " iterations");
    // }
    OwlyFrame owlyFrameAny = OwlyGui.start();
    owlyFrameAny.setGlc((TrajectoryPlanner) anyTrajectoryPlanner);
    // ---
    System.out.println("***DEFAULT**");
    // ---
    OwlyFrame owlyFrameDefault = OwlyGui.start();
    {
      StandardTrajectoryPlanner defaultTrajectoryPlanner = new StandardTrajectoryPlanner( //
          parameters.getEta(), stateIntegrator, controls, obstacleQuery, se2GoalManager.getGoalInterface());
      defaultTrajectoryPlanner.insertRoot(new StateTime(Tensors.vector(0, 3, 0), RealScalar.ZERO));
      iters = GlcExpand.maxDepth(defaultTrajectoryPlanner, parameters.getDepthLimit());
      DebugUtils.nodeAmountCompare(defaultTrajectoryPlanner);
      System.out.println("After " + iters + " iterations");
      owlyFrameDefault.setGlc(defaultTrajectoryPlanner);
    }
    // --
    int iter = 0;
    boolean goalFound = false;
    int expandIter = 0;
    System.out.println("****STARTING COMPARISON****");
    while (owlyFrameAny.jFrame.isVisible() && iter < 20) {
      System.out.println("***NEW SET");
      List<StateTime> anyTrajectory = anyTrajectoryPlanner.trajectoryToBest();
      StateTime newRootState = null;
      if (anyTrajectory != null)
        newRootState = anyTrajectory.get(anyTrajectory.size() > 5 ? 5 : 0);
      int index = iter % 4;
      Se2AbstractGoalManager se2GoalManager2 = new Se2MinCurvatureGoalManager( //
          goalList.get(index), radiusVector);
      // --
      System.out.println("***ANY***");
      timingDatabase.startStopwatchFor(1);
      {
        int increment = anyTrajectoryPlanner.switchRootToState(newRootState.state());
        parameters.increaseDepthLimit(increment);
        // --
        goalFound = anyTrajectoryPlanner.changeToGoal(se2GoalManager2.getGoalInterface());
        // --
        expandIter = 0;
        if (!goalFound)
          expandIter = GlcExpand.maxDepth(anyTrajectoryPlanner, parameters.getDepthLimit());
        timingDatabase.saveIterations(expandIter, 1);
        System.out.println("After " + expandIter + " iterations");
        timingDatabase.stopStopwatchFor(1);
        if (useGui)
          owlyFrameAny.setGlc((TrajectoryPlanner) anyTrajectoryPlanner);
        // ---
      }
      System.out.println("***DEFAULT***");
      timingDatabase.startStopwatchFor(0);
      {
        StandardTrajectoryPlanner defaultTrajectoryPlanner = new StandardTrajectoryPlanner( //
            parameters.getEta(), stateIntegrator, controls, obstacleQuery, se2GoalManager2.getGoalInterface());
        // TODO why not use plan statetime newRootState here !?
        defaultTrajectoryPlanner.insertRoot(new StateTime(newRootState.state(), RealScalar.ZERO));
        iters = GlcExpand.maxDepth(defaultTrajectoryPlanner, parameters.getDepthLimit());
        timingDatabase.saveIterations(iters, 0);
        System.out.println("After " + iters + " iterations");
        timingDatabase.stopStopwatchFor(0);
        if (useGui)
          owlyFrameDefault.setGlc(defaultTrajectoryPlanner);
      }
      iter++;
      timingDatabase.write2lines();
    }
    boolean heuristic = HeuristicQ.of(se2GoalManager);
    timingDatabase.write2File("SE2R" + resolution + (heuristic ? "H" : "noH"));
    owlyFrameDefault.close();
    owlyFrameAny.close();
    System.out.println("Finished loop");
  }

  public static void main(String[] args) throws Exception {
    _run(RealScalar.of(8));
    _run(RealScalar.of(12));
    _run(RealScalar.of(14));
    _run(RealScalar.of(16));
  }
}
