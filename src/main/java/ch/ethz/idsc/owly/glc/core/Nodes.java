// code by jph
package ch.ethz.idsc.owly.glc.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

enum Nodes {
  ;
  // ---
  public static List<Node> getNodesFromRoot(Node best) {
    List<Node> list = new ArrayList<>();
    Node node = best;
    while (node != null) {
      list.add(node);
      node = node.getParent();
    }
    Collections.reverse(list);
    return list;
  }
}