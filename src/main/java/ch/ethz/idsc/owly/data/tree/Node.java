// code by jph
package ch.ethz.idsc.owly.data.tree;

import java.util.Collection;

public interface Node {
  /** @return parent */
  Node parent();

  /** @return collection of child nodes */
  Collection<? extends Node> children();

  /** disconnects this node from given child.
   * the parent of child becomes null.
   * 
   * @param child */
  void removeEdgeTo(Node child);

  /** given child becomes a child of this node.
   * the parent of child becomes this node.
   * 
   * @param child */
  void insertEdgeTo(Node child);

  /** @return true if this node has parent == null */
  boolean isRoot();

  /** @return true if this node has no children */
  boolean isLeaf();
}
