// code by jl and jph
package ch.ethz.idsc.owly.demo.rnxt.glc;

import java.util.Collections;

import ch.ethz.idsc.owly.demo.rn.RnHeuristicEllipsoidGoalManager;
import ch.ethz.idsc.owly.glc.adapter.HeuristicQ;
import ch.ethz.idsc.owly.glc.core.GlcNode;
import ch.ethz.idsc.owly.glc.core.GlcNodes;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensors;
import junit.framework.TestCase;

public class R2xtEllipsoidGoalManagerTest extends TestCase {
  public void testMinCostToGoal0() {
    RnHeuristicEllipsoidGoalManager rnxtGoal = new RnHeuristicEllipsoidGoalManager(//
        Tensors.vector(5, 0), Tensors.vector(2, 3));
    assertTrue(HeuristicQ.of(rnxtGoal));
    assertEquals(rnxtGoal.minCostToGoal(Tensors.vector(2, 0)), RealScalar.ONE);
    assertEquals(rnxtGoal.minCostToGoal(Tensors.vector(3, 0)), RealScalar.ZERO);
    assertEquals(rnxtGoal.minCostToGoal(Tensors.vector(4, 0)), RealScalar.ZERO);
  }

  public void testMinCostToGoal1() {
    {
      RnHeuristicEllipsoidGoalManager rnxtGoal = new RnHeuristicEllipsoidGoalManager(//
          Tensors.vector(0, 5), Tensors.vector(2, 3));
      assertEquals(rnxtGoal.minCostToGoal(Tensors.vector(0, 1)), RealScalar.ONE);
      assertEquals(rnxtGoal.minCostToGoal(Tensors.vector(0, 2)), RealScalar.ZERO);
      assertEquals(rnxtGoal.minCostToGoal(Tensors.vector(0, 3)), RealScalar.ZERO);
    }
    {
      RnHeuristicEllipsoidGoalManager rnxtGoal = new RnHeuristicEllipsoidGoalManager(//
          Tensors.vector(0, 5), Tensors.vector(2, 3));
      assertEquals(rnxtGoal.minCostToGoal(Tensors.vector(0, 1)), RealScalar.ONE);
      assertEquals(rnxtGoal.minCostToGoal(Tensors.vector(0, 2)), RealScalar.ZERO);
      assertEquals(rnxtGoal.minCostToGoal(Tensors.vector(0, 3)), RealScalar.ZERO);
      // GoalManager underestimates the time to get to the goal, if goal is only available after x seconds,
      // time of euclidean path with MaxSpeed is still underestimating
    }
  }

  public void testMinCostToGoal2() {
    // GoalManager without Heuristic: Therefore minCostToGoal always ZERO
    RnxtEllipsoidGoalManager rnGoal = new RnxtEllipsoidGoalManager(//
        Tensors.of(RealScalar.of(5), RealScalar.ZERO, DoubleScalar.POSITIVE_INFINITY), RealScalar.of(2));
    assertFalse(HeuristicQ.of(rnGoal));
    assertEquals(rnGoal.minCostToGoal(Tensors.vector(2, 0)), RealScalar.ZERO);
    assertEquals(rnGoal.minCostToGoal(Tensors.vector(3, 0)), RealScalar.ZERO);
    assertEquals(rnGoal.minCostToGoal(Tensors.vector(4, 0)), RealScalar.ZERO);
  }

  public void testCostIncrement1() {
    GlcNode root = GlcNodes.createRoot(new StateTime(Tensors.vector(2, 2, 0), RealScalar.ZERO), x -> RealScalar.ZERO);
    RnHeuristicEllipsoidGoalManager rnxtGoal = new RnHeuristicEllipsoidGoalManager(//
        Tensors.of(RealScalar.of(5), RealScalar.ZERO, DoubleScalar.POSITIVE_INFINITY), RealScalar.of(2));
    Scalar incr = rnxtGoal.costIncrement( //
        root, Collections.singletonList(new StateTime(Tensors.vector(10, 2, 8), RealScalar.of(8))), null);
    assertEquals(incr, RealScalar.of(8));
  }

  public void testCostIncrement2() {
    GlcNode root = GlcNodes.createRoot(new StateTime(Tensors.vector(2, 2, 0), RealScalar.ZERO), x -> RealScalar.ZERO);
    RnxtEllipsoidGoalManager rnGoal = new RnxtEllipsoidGoalManager( //
        Tensors.of(RealScalar.of(5), RealScalar.ZERO, DoubleScalar.POSITIVE_INFINITY), RealScalar.of(2));
    Scalar incr = rnGoal.costIncrement( //
        root, Collections.singletonList(new StateTime(Tensors.vector(10, 2, 8), RealScalar.of(8))), null);
    assertEquals(incr, RealScalar.of(8));
  }
}
