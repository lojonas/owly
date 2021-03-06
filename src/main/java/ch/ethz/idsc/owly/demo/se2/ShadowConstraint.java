// code by ynager
package ch.ethz.idsc.owly.demo.se2;

import java.awt.geom.Area;
import java.io.Serializable;
import java.util.List;

import ch.ethz.idsc.owl.data.DontModify;
import ch.ethz.idsc.owl.glc.adapter.GlcNodes;
import ch.ethz.idsc.owl.glc.core.GlcNode;
import ch.ethz.idsc.owl.glc.std.PlannerConstraint;
import ch.ethz.idsc.owl.mapping.ShadowMap;
import ch.ethz.idsc.owl.math.flow.Flow;
import ch.ethz.idsc.owl.math.state.StateTime;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;

@DontModify
public final class ShadowConstraint implements PlannerConstraint, Serializable {
  private final ShadowMap shadowMap;
  // FIXME design error prone because calling isSatisfied(...) alters state of instance
  // ... that means the order in which to call isSatisfied determines the outcome of the test
  private final Scalar gamma;
  private StateTime rootStateTime = null;

  public ShadowConstraint(ShadowMap shadowMap, Scalar gamma) {
    this.shadowMap = shadowMap;
    this.gamma = gamma;
  }

  @Override // from CostIncrementFunction
  public boolean isSatisfied(GlcNode glcNode, List<StateTime> trajectory, Flow flow) {
    //
    // get time at root
    if (glcNode.isRoot()) {
      rootStateTime = glcNode.stateTime();
    }
    StateTime childStateTime = trajectory.get(trajectory.size() - 1);
    double posX = childStateTime.state().Get(0).number().doubleValue();
    double posY = childStateTime.state().Get(1).number().doubleValue();
    Area simShadowArea = (Area) shadowMap.getCurrentMap().clone();
    //
    // check if shadowArea far enough away from node to be unreachable by shadow
    /* double rad = tDelt * shadowMap.vMax;
     * double tDelt = glcNode.stateTime().time().number().doubleValue() //
     * - rootStateTime.time().number().doubleValue();
     * Shape circle = new Ellipse2D.Double(posX - rad, posY + rad, 2*rad, 2*rad);
     * Area circleArea = new Area(circle);
     * circleArea.intersect(simShadowArea);
     * if (circleArea.isEmpty())
     * return true; */
    //
    Scalar vel = flow.getU().Get(0);
    Scalar tStop = vel.multiply(vel).multiply(gamma);
    // find node at t-tStop
    Scalar tMinTStop = (Scalar) childStateTime.time().subtract(tStop).unmodifiable();
    Scalar t = glcNode.stateTime().time();
    GlcNode targetNode = glcNode;
    //
    while (Scalars.lessThan(tMinTStop, t)) {
      if (targetNode.isRoot())
        break;
      targetNode = targetNode.parent();
      // get time at parent node
      t = t.subtract(t.subtract(targetNode.stateTime().time()));
    }
    //
    List<StateTime> path = GlcNodes.getPathFromRootTo(targetNode);
    path.remove(0); // remove root
    if (!path.isEmpty()) {
      Scalar prevTime = rootStateTime.time();
      for (StateTime sT : path) {
        Scalar timeBetweenNodes = sT.time().subtract(prevTime);
        shadowMap.updateMap(simShadowArea, sT, timeBetweenNodes.number().floatValue());
        prevTime = sT.time();
      }
    }
    // simulate shadow map until t with no new sensors info
    shadowMap.updateMap(simShadowArea, targetNode.stateTime(), tStop.number().floatValue());
    // check if node is inside simulated shadow area
    boolean value = !simShadowArea.contains(posX, posY);
    return value;
  }
}