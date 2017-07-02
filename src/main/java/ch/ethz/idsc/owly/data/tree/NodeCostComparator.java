// code by bapaden and jph
package ch.ethz.idsc.owly.data.tree;

import java.util.Comparator;

import ch.ethz.idsc.tensor.Scalars;

/** compare two nodes based on {@link StateCostNode#costFromRoot()} */
public enum NodeCostComparator implements Comparator<StateCostNode> {
  INSTANCE; // not used yet
  // ---
  @Override
  public int compare(StateCostNode o1, StateCostNode o2) {
    return Scalars.compare(o1.costFromRoot(), o2.costFromRoot());
  }
}
