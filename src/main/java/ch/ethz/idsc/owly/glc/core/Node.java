// code by bapaden and jph
package ch.ethz.idsc.owly.glc.core;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

public class Node {
  private final Map<Flow, Node> children = new HashMap<>();
  /** flow is null for root node */
  public final Flow flow;
  public final Tensor x;
  public final Scalar time;
  public final Scalar cost;
  public final Scalar merit;
  private Node parent = null;
  private int depth = 0;

  /** @param flow that got us to this Node from the parent
   * @param x
   * @param time
   * @param cost
   * @param e */
  public Node(Flow flow, Tensor x, Scalar time, Scalar cost, Scalar e) {
    this.flow = flow;
    this.x = x;
    this.time = time;
    this.cost = cost;
    this.merit = cost.add(e);
  }

  public void addChild(Node child) {
    child.parent = this;
    child.depth = depth + 1;
    children.put(child.flow, child);
  }

  public StateTime getStateTime() {
    return new StateTime(x, time);
  }

  public Node getParent() {
    return parent;
  }

  public boolean isRoot() {
    return parent == null;
  }

  public int getDepth() {
    return depth;
  }

  @Override
  public String toString() {
    return "@" + x.toString() + " cost=" + cost + " merit=" + merit;
  }
}