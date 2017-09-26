// code by jl
package ch.ethz.idsc.owly.demo.rn.any;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.ethz.idsc.owly.demo.rn.R2Controls;
import ch.ethz.idsc.owly.demo.rn.R2NoiseRegion;
import ch.ethz.idsc.owly.demo.rn.R2Parameters;
import ch.ethz.idsc.owly.demo.rn.RnSimpleCircleGoalManager;
import ch.ethz.idsc.owly.glc.adapter.Parameters;
import ch.ethz.idsc.owly.glc.adapter.RunCompare;
import ch.ethz.idsc.owly.glc.adapter.SimpleTrajectoryRegionQuery;
import ch.ethz.idsc.owly.glc.core.AbstractAnyTrajectoryPlanner;
import ch.ethz.idsc.owly.glc.core.AnyPlannerInterface;
import ch.ethz.idsc.owly.glc.core.GlcExpand;
import ch.ethz.idsc.owly.glc.core.OptimalAnyTrajectoryPlanner;
import ch.ethz.idsc.owly.glc.core.StandardTrajectoryPlanner;
import ch.ethz.idsc.owly.gui.Gui;
import ch.ethz.idsc.owly.gui.OwlyFrame;
import ch.ethz.idsc.owly.math.flow.EulerIntegrator;
import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.owly.math.region.EllipsoidRegion;
import ch.ethz.idsc.owly.math.region.InvertedRegion;
import ch.ethz.idsc.owly.math.region.Region;
import ch.ethz.idsc.owly.math.region.RegionUnion;
import ch.ethz.idsc.owly.math.state.FixedStateIntegrator;
import ch.ethz.idsc.owly.math.state.StateIntegrator;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.owly.math.state.TimeInvariantRegion;
import ch.ethz.idsc.owly.math.state.TrajectoryRegionQuery;
import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.lie.AngleVector;

enum R2GlcAnyCircleCompareDemo {
  ;
  public static void main(String[] args) throws Exception {
    RationalScalar resolution = (RationalScalar) RealScalar.of(20);
    Scalar timeScale = RealScalar.of(2);
    Scalar depthScale = RealScalar.of(100);
    Tensor partitionScale = Tensors.vector(30, 30);
    Scalar dtMax = RationalScalar.of(1, 6);
    int maxIter = 2000;
    Scalar lipschitz = RealScalar.ONE;
    Scalar circleRadius = RealScalar.of(6);
    Scalar goalAngle = RealScalar.ZERO;
    Tensor goal = AngleVector.of(goalAngle).multiply(circleRadius);
    Scalar goalRadius = DoubleScalar.of(.25);
    System.out.println("Goal is: " + goal);
    Parameters parameters = new R2Parameters( //
        resolution, timeScale, depthScale, partitionScale, dtMax, maxIter, lipschitz);
    StateIntegrator stateIntegrator = FixedStateIntegrator.create(EulerIntegrator.INSTANCE, parameters.getdtMax(), //
        parameters.getTrajectorySize());
    Collection<Flow> controls = R2Controls.createRadial(parameters.getResolutionInt()); // max (grad(F)) ==1
    RnSimpleCircleGoalManager rnGoal = new RnSimpleCircleGoalManager(goal, goalRadius);
    // performance depends on heuristic: zeroHeuristic vs rnGoal
    // Heuristic heuristic = new ZeroHeuristic(); // rnGoal
    TrajectoryRegionQuery obstacleQuery = //
        new SimpleTrajectoryRegionQuery(new TimeInvariantRegion( //
            RegionUnion.of( //
                new EllipsoidRegion(Tensors.vector(0, 0), Tensors.vector(1, 1).multiply(circleRadius).multiply(RealScalar.of(0.5))) //
                , new InvertedRegion(new EllipsoidRegion(Tensors.vector(0, 0), Tensors.vector(1, 1).multiply(circleRadius).multiply(RealScalar.of(2)))) //
                // , RnPointclouds.createRandomRegion(30, Tensors.vector(12, 12), Tensors.vector(0, 0), RealScalar.of(0.6)) //
                , new R2NoiseRegion(RealScalar.of(.2)) //
            )));
    // ANYPLANNER
    AnyPlannerInterface anyTrajectoryPlanner = new OptimalAnyTrajectoryPlanner( //
        parameters.getEta(), stateIntegrator, controls, obstacleQuery, rnGoal);
    anyTrajectoryPlanner.switchRootToState(Tensors.vector(0, 1).multiply(circleRadius));
    GlcExpand.maxDepth(anyTrajectoryPlanner, parameters.getDepthLimit());
    List<StateTime> anyTrajectory = anyTrajectoryPlanner.trajectoryToBest();
    // StateTimeTrajectories.print(trajectory);
    // AnimationWriter gsw = AnimationWriter.of(UserHome.Pictures("R2_Circle_Gif.gif"), 250);
    OwlyFrame owlyAnyFrame = Gui.start();
    owlyAnyFrame.configCoordinateOffset(400, 400);
    owlyAnyFrame.jFrame.setBounds(0, 0, 800, 800);
    // owlyAnyFrame.setGlc((TrajectoryPlanner) anyTrajectoryPlanner);
    // STANDARD PLANNER
    StandardTrajectoryPlanner standardTrajectoryPlanner = new StandardTrajectoryPlanner( //
        parameters.getEta(), stateIntegrator, controls, obstacleQuery, rnGoal);
    standardTrajectoryPlanner.insertRoot(new StateTime(Tensors.vector(0, 1).multiply(circleRadius), RealScalar.ZERO));
    GlcExpand.maxDepth(standardTrajectoryPlanner, parameters.getDepthLimit());
    // StateTimeTrajectories.print(trajectory);
    // AnimationWriter gsw = AnimationWriter.of(UserHome.Pictures("R2_Circle_Gif.gif"), 250);
    OwlyFrame owlyStandardFrame = Gui.start();
    owlyStandardFrame.configCoordinateOffset(400, 400);
    owlyStandardFrame.jFrame.setBounds(0, 0, 800, 800);
    // owlyStandardFrame.setGlc((TrajectoryPlanner) standardTrajectoryPlanner);
    RunCompare database = new RunCompare(2);
    for (int iter = 1; iter < 30; iter++) {
      Thread.sleep(10000);
      // ANY:
      // -- ROOTCHANGE
      database.startStopwatchFor(1);
      boolean switchingRoot = false;
      StateTime newRootState = null;
      if (anyTrajectory.size() > 0) {
        switchingRoot = true;
        // --
        newRootState = anyTrajectory.get(anyTrajectory.size() > 5 ? 5 : 0);
        int increment = anyTrajectoryPlanner.switchRootToState(newRootState.state());
        parameters.increaseDepthLimit(increment);
      }
      // -- GOALCHANGE
      List<StateTime> goalStateList = new ArrayList<>();
      do {
        goalStateList.clear();
        goalAngle = goalAngle.subtract(RealScalar.of(0.1 * Math.PI));
        goal = AngleVector.of(goalAngle).multiply(circleRadius);
        StateTime goalState = new StateTime(goal, RealScalar.ZERO);
        goalStateList.add(goalState);
      } while (!obstacleQuery.isDisjoint(goalStateList));
      RnSimpleCircleGoalManager rnGoal2 = new RnSimpleCircleGoalManager(goal, goalRadius);
      // System.out.println("Switching to Goal:" + goal);
      Scalar goalSearchHelperRadius = goalRadius.add(RealScalar.ONE).multiply(RealScalar.of(2));
      Region goalSearchHelper = new EllipsoidRegion(goal, Array.of(l -> goalSearchHelperRadius, goal.length()));
      anyTrajectoryPlanner.changeToGoal(rnGoal2, goalSearchHelper);
      // -- EXPANDING
      int itersAny = GlcExpand.maxDepth(anyTrajectoryPlanner, parameters.getDepthLimit());
      database.saveIterations(itersAny, 1);
      anyTrajectory = anyTrajectoryPlanner.trajectoryToBest();
      database.stopStopwatchFor(1);
      // owlyAnyFrame.setGlc((TrajectoryPlanner) anyTrajectoryPlanner);
      // StateTimeTrajectories.print(trajectory);
      // gsw.append(owlyFrame.offscreen());
      // --
      // Standard:
      rnGoal2 = new RnSimpleCircleGoalManager(goal, goalRadius);
      if (switchingRoot) {
        database.startStopwatchFor(0);
        standardTrajectoryPlanner = new StandardTrajectoryPlanner( //
            parameters.getEta(), stateIntegrator, controls, obstacleQuery, rnGoal2);
        standardTrajectoryPlanner.insertRoot(newRootState.state());
        int itersStandard = GlcExpand.maxDepth(standardTrajectoryPlanner, parameters.getDepthLimit());
        database.stopStopwatchFor(0);
        database.saveIterations(itersStandard, 0);
        // owlyStandardFrame.setGlc((TrajectoryPlanner) standardTrajectoryPlanner);
        Scalar anyCost = ((AbstractAnyTrajectoryPlanner) anyTrajectoryPlanner).getTrajectoryCost();
        database.saveCost(anyCost, 1);
        Scalar staCost = standardTrajectoryPlanner.getBest().get().costFromRoot();
        database.saveCost(staCost, 0);
        database.write2lines();
      }
      if (!owlyAnyFrame.jFrame.isVisible())
        break;
    }
    // int repeatLast = 6;
    // while (0 < repeatLast--)
    // gsw.append(owlyFrame.offscreen());
    // gsw.close();
    // System.out.println("created gif");
    System.out.println("Finished LOOP");
    database.write2File();
  }
}
