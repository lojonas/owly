// code by jph and jl
package ch.ethz.idsc.owly.glc.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.ethz.idsc.owly.demo.rn.R2Controls;
import ch.ethz.idsc.owly.demo.rn.RnSimpleCircleHeuristicGoalManager;
import ch.ethz.idsc.owly.glc.adapter.SimpleTrajectoryRegionQuery;
import ch.ethz.idsc.owly.math.flow.EulerIntegrator;
import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.owly.math.region.EmptyRegion;
import ch.ethz.idsc.owly.math.state.FixedStateIntegrator;
import ch.ethz.idsc.owly.math.state.StateIntegrator;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.owly.math.state.TimeInvariantRegion;
import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import junit.framework.TestCase;

public class GlcNodeTest extends TestCase {
  public void testCompare() {
    StateTime state1 = new StateTime(Tensors.vector(3, 0), RealScalar.of(3));
    StateTime state2 = new StateTime(Tensors.vector(3, 0), RealScalar.of(3));
    Scalar cost1 = RealScalar.of(1);
    Scalar cost2 = RealScalar.of(1);
    Scalar heuristic1 = RealScalar.of(0);
    Scalar heuristic2 = RealScalar.of(0);
    GlcNode test1 = GlcNode.of(null, state1, cost1, heuristic1);
    GlcNode test2 = GlcNode.of(null, state1, cost1, heuristic1);
    assertTrue(state1.equals(state1));
    assertTrue(state1.equals(state2));
    // reflexiv
    assertFalse(test1.equals(null));
    // Nodes are completely identical
    assertTrue(test1.equals(test1));
    // Symetrie check
    // assertTrue(test1.equals(test2));
    // assertTrue(test2.equals(test1));
    test2.setMinCostToGoal(heuristic2);
    // Nodes are identically except heuristic
    // assertTrue(test1.equals(test2));
    // Cost is different ==> different node
    @SuppressWarnings("unused")
    GlcNode test3 = GlcNode.of(null, state1, cost2, heuristic1);
    // assertFalse(test1.equals(test3));
    // Nodes are different in state ==> different
    @SuppressWarnings("unused")
    GlcNode test4 = GlcNode.of(null, state2, cost1, heuristic1);
    // assertFalse(test1.equals(test4));
  }

  public void testMakeRoot() {
    final Tensor stateRoot = Tensors.vector(-2, -2);
    final Tensor stateGoal = Tensors.vector(2, 2);
    final Scalar radius = DoubleScalar.of(.25);
    // ---
    Tensor eta = Tensors.vector(8, 8);
    StateIntegrator stateIntegrator = FixedStateIntegrator.create(EulerIntegrator.INSTANCE, RationalScalar.of(1, 5), 5);
    Collection<Flow> controls = R2Controls.createRadial(36);
    RnSimpleCircleHeuristicGoalManager rnGoal = new RnSimpleCircleHeuristicGoalManager(stateGoal, radius);
    SimpleTrajectoryRegionQuery obstacleQuery = new SimpleTrajectoryRegionQuery(new TimeInvariantRegion(EmptyRegion.INSTANCE));
    // ---
    TrajectoryPlanner trajectoryPlanner = new StandardTrajectoryPlanner( //
        eta, stateIntegrator, controls, obstacleQuery, rnGoal);
    trajectoryPlanner.insertRoot(new StateTime(stateRoot, RealScalar.ZERO));
    List<GlcNode> nodeList = new ArrayList<>(trajectoryPlanner.getDomainMap().values());
    assertTrue(nodeList.get(0).isRoot());
    nodeList.get(0).makeRoot(); // no error
    assertTrue(nodeList.get(0).isRoot());
    Expand.maxSteps(trajectoryPlanner, 1);
    nodeList = new ArrayList<>(trajectoryPlanner.getDomainMap().values());
    GlcNode test = nodeList.get(nodeList.size() - 1);
    assertFalse(test.isRoot());
    test.makeRoot();
    assertTrue(test.isRoot());
  }
}
