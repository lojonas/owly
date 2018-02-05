// code by jl
package ch.ethz.idsc.owly.demo.delta.glc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import ch.ethz.idsc.owl.data.Stopwatch;
import ch.ethz.idsc.owl.glc.adapter.GlcExpand;
import ch.ethz.idsc.owl.glc.adapter.GlcNodes;
import ch.ethz.idsc.owl.glc.adapter.HeuristicQ;
import ch.ethz.idsc.owl.glc.adapter.StateTimeTrajectories;
import ch.ethz.idsc.owl.glc.any.OptimalAnyTrajectoryPlanner;
import ch.ethz.idsc.owl.glc.core.DebugUtils;
import ch.ethz.idsc.owl.glc.core.GlcNode;
import ch.ethz.idsc.owl.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.owl.gui.ani.OwlyFrame;
import ch.ethz.idsc.owl.gui.ani.OwlyGui;
import ch.ethz.idsc.owl.math.region.EllipsoidRegion;
import ch.ethz.idsc.owl.math.region.Region;
import ch.ethz.idsc.owl.math.state.StateTime;
import ch.ethz.idsc.owly.demo.delta.DeltaAltStateSpaceModel;
import ch.ethz.idsc.owly.demo.delta.DeltaTrajectoryGoalManager;
import ch.ethz.idsc.owly.demo.util.RunCompare;
import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

enum DeltaGlcConstTimeHeuristicAnyDemo {
  ;
  @SuppressWarnings("unused")
  public static void main(String[] args) throws Exception {
    // -- Quick Planner init
    RationalScalar quickResolution = (RationalScalar) RationalScalar.of(9, 1);
    boolean useGui = true;
    Stopwatch quickPlannerStopwatch = Stopwatch.started();
    // Tensor partitionScale = Tensors.vector(120, 120);
    Tensor partitionScale = Tensors.vector(50, 50);
    TrajectoryPlannerContainer quickTrajectoryPlannerContainer = DeltaHelper.createGlc(RealScalar.of(-0.02), quickResolution, partitionScale);
    GlcExpand.maxDepth(quickTrajectoryPlannerContainer.getTrajectoryPlanner(), DoubleScalar.POSITIVE_INFINITY.number().intValue());
    OwlyFrame quickOwlyFrame = OwlyGui.start();
    quickOwlyFrame.configCoordinateOffset(33, 416);
    quickOwlyFrame.jFrame.setBounds(100, 100, 620, 475);
    quickOwlyFrame.setGlc(quickTrajectoryPlannerContainer.getTrajectoryPlanner());
    Optional<GlcNode> optional = quickTrajectoryPlannerContainer.getTrajectoryPlanner().getBest();
    List<StateTime> quickTrajectory = null;
    if (optional.isPresent()) {
      quickTrajectory = GlcNodes.getPathFromRootTo(optional.get());
      StateTimeTrajectories.print(quickTrajectory);
    } else {
      throw new RuntimeException();
    }
    Scalar quickCostFromRoot = quickTrajectoryPlannerContainer.getTrajectoryPlanner().getBest().get().costFromRoot();
    DebugUtils.heuristicConsistencyCheck(quickTrajectoryPlannerContainer.getTrajectoryPlanner());
    System.out.println("Quickplanner took: " + quickPlannerStopwatch.display_seconds());
    System.out.println("***QUICK PLANNER FINISHED***");
    // -- SLOWPLANNER
    partitionScale = Tensors.vector(50, 50);
    RationalScalar resolution = (RationalScalar) RationalScalar.of(12, 1);
    TrajectoryPlannerContainer slowTrajectoryPlannerContainer = DeltaHelper.createGlcAny(RealScalar.of(-0.02), resolution, partitionScale);
    // -- GOALMANAGER
    Iterator<StateTime> iterator = quickTrajectory.iterator();
    List<Region<Tensor>> goalRegions = new ArrayList<>();
    List<Region<Tensor>> goalCheckHelpRegions = new ArrayList<>();
    Tensor radius = Tensors.vector(0.1, 0.1);
    Tensor maxChangePerIterstep = Tensors.vector(1, 1).multiply(slowTrajectoryPlannerContainer.getParameters().getExpandTime()
        .multiply(((DeltaAltStateSpaceModel) slowTrajectoryPlannerContainer.getStateSpaceModel()).getMaxPossibleChange()));
    Tensor GoalCheckHelpRadius = radius.add(maxChangePerIterstep);
    System.out.println("Expandtime: " + slowTrajectoryPlannerContainer.getParameters().getExpandTime());
    // --Trajectory of goals
    // TODO in function/class
    while (iterator.hasNext()) {
      StateTime next = iterator.next();
      goalRegions.add(new EllipsoidRegion(next.state(), radius));
      goalCheckHelpRegions.add(new EllipsoidRegion(next.state(), GoalCheckHelpRadius));
    }
    DeltaTrajectoryGoalManager trajectoryGoalManager = new DeltaTrajectoryGoalManager(goalRegions, quickTrajectory, radius, //
        ((DeltaAltStateSpaceModel) slowTrajectoryPlannerContainer.getStateSpaceModel()).getMaxPossibleChange());
    ((OptimalAnyTrajectoryPlanner) slowTrajectoryPlannerContainer.getTrajectoryPlanner()).changeToGoal(trajectoryGoalManager);
    OwlyFrame owlyFrame = OwlyGui.start();
    owlyFrame.configCoordinateOffset(33, 416);
    owlyFrame.jFrame.setBounds(100, 100, 620, 475);
    Scalar planningTime = RealScalar.of(5);
    RunCompare timingDatabase = new RunCompare(1);
    // -- ANYTIMELOOP
    boolean finalGoalFound = false;
    int iter = 0;
    while (!finalGoalFound && iter < 300) {
      iter++;
      List<StateTime> trajectory = new ArrayList<>();
      Optional<GlcNode> finalGoalNode = null;
      // -- ROOTCHANGE
      Stopwatch stopwatch = Stopwatch.started();
      timingDatabase.startStopwatchFor(0);
      finalGoalNode = slowTrajectoryPlannerContainer.getTrajectoryPlanner().getFinalGoalNode();
      if (finalGoalNode.isPresent())
        trajectory = GlcNodes.getPathFromRootTo(finalGoalNode.get());
      System.out.println("trajectorys size: " + trajectory.size());
      Scalar currentTime = timingDatabase.currentRuntimes.Get(0);
      // if (Scalars.lessThan(trajectory.get(trajectory.size() - 1).time(), currentTime)) {
      // System.err.println("Too slow expansion of Tree");
      // throw new RuntimeException();
      // } else {
      boolean test = trajectory.removeIf(st -> Scalars.lessThan(st.time(), currentTime));
      if (trajectory.size() == 0 && iter != 1)
        throw new RuntimeException("Too slow expansion of tree");
      if (!test)
        System.out.println("Did not move forward a node");
      StateTime newRootState = trajectory.get(0);
      int increment = ((OptimalAnyTrajectoryPlanner) slowTrajectoryPlannerContainer.getTrajectoryPlanner()).switchRootToState(newRootState);
      slowTrajectoryPlannerContainer.getParameters().increaseDepthLimit(increment);
      // }
      stopwatch.stop();
      System.out.println("Rootchange took: " + stopwatch.display_seconds() + "s");
      // -- EXPANDING
      stopwatch.resetToZero();
      stopwatch.start();
      int expandIter = GlcExpand.constTime(slowTrajectoryPlannerContainer.getTrajectoryPlanner(), planningTime,
          slowTrajectoryPlannerContainer.getParameters().getDepthLimit());
      finalGoalNode = slowTrajectoryPlannerContainer.getTrajectoryPlanner().getFinalGoalNode();
      trajectory = GlcNodes.getPathFromRootTo(finalGoalNode.get());
      stopwatch.stop();
      timingDatabase.pauseStopwatchFor(0);
      timingDatabase.saveIterations(expandIter, 0);
      System.out.println("Expanding " + expandIter + " Nodes took: " + stopwatch.display_seconds() + "s");
      if (useGui)
        owlyFrame.setGlc((TrajectoryPlanner) slowTrajectoryPlannerContainer.getTrajectoryPlanner());
      List<StateTime> Trajectory = null;
      if (optional.isPresent()) {
        Trajectory = GlcNodes.getPathFromRootTo(finalGoalNode.get());
        StateTimeTrajectories.print(Trajectory);
      } else {
        throw new RuntimeException();
      }
      System.out.println("*****Finished*****");
      DebugUtils.heuristicConsistencyCheck(slowTrajectoryPlannerContainer.getTrajectoryPlanner());
      if (!owlyFrame.jFrame.isVisible() || expandIter < 1)
        break;
      Scalar slowCostFromRoot = slowTrajectoryPlannerContainer.getTrajectoryPlanner().getFinalGoalNode().get().costFromRoot();
      timingDatabase.saveCost(slowCostFromRoot, 0);
      timingDatabase.printcurrent();
      timingDatabase.write2lines();
      Thread.sleep(1000);
    }
    boolean test = HeuristicQ.of(trajectoryGoalManager);
    String filename = "GLCR" + resolution + (test ? "H" : "noH");
    timingDatabase.write2File(filename);
    System.out.println("Finished LOOP");
  }
}
