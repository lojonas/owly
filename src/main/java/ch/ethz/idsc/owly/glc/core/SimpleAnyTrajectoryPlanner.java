// code by jl
package ch.ethz.idsc.owly.glc.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.ethz.idsc.owly.data.tree.Nodes;
import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.owly.math.state.CostFunction;
import ch.ethz.idsc.owly.math.state.StateIntegrator;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.owly.math.state.Trajectories;
import ch.ethz.idsc.owly.math.state.TrajectoryRegionQuery;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.ZeroScalar;

/** TODO assumptions in order to use SimpleAnyTrajectoryPlanner */
public class SimpleAnyTrajectoryPlanner extends TrajectoryPlanner {
  private final StateIntegrator stateIntegrator;
  private final Collection<Flow> controls;
  private CostFunction costFunction;
  private TrajectoryRegionQuery goalQuery;
  private final TrajectoryRegionQuery obstacleQuery;
  // private final Map<Tensor, DomainQueue> domainCandidateMap = new HashMap<>();

  // private final Queue<Node> queue = new PriorityQueue<>(NodeMeritComparator.instance);
  public SimpleAnyTrajectoryPlanner( //
      Tensor eta, //
      StateIntegrator stateIntegrator, //
      Collection<Flow> controls, //
      CostFunction costFunction, //
      TrajectoryRegionQuery goalQuery, //
      TrajectoryRegionQuery obstacleQuery //
  ) {
    super(eta);
    this.stateIntegrator = stateIntegrator;
    this.controls = controls;
    this.costFunction = costFunction;
    this.goalQuery = goalQuery;
    this.obstacleQuery = obstacleQuery;
  }

  @Override
  public void expand(final GlcNode node) {
    // TODO count updates in cell based on costs for benchmarking
    Map<GlcNode, List<StateTime>> connectors = //
        SharedUtils.integrate(node, controls, stateIntegrator, costFunction);
    // Set<Tensor> domainsNeedingUpdate = new HashSet<>();
    // Map<Tensor, DomainQueue> candidates = new HashMap<>();
    CandidatePairQueueMap candidates = new CandidatePairQueueMap();
    for (GlcNode next : connectors.keySet()) { // <- order of keys is non-deterministic
      CandidatePair nextCandidate = new CandidatePair(node, next);
      final Tensor domain_key = convertToKey(next.state());
      candidates.insert(domain_key, nextCandidate);
      // ALL Candidates are saved in CandidateList
    }
    // save candidates in CandidateMap for RootSwitchlater
    processCandidates(node, connectors, candidates);
  }

  private void processCandidates( //
      GlcNode node, Map<GlcNode, List<StateTime>> connectors, CandidatePairQueueMap candidates) {
    for (Entry<Tensor, CandidatePairQueue> entry : candidates.map.entrySet()) {
      final Tensor domain_key = entry.getKey();
      final CandidatePairQueue domainCandidateQueue = entry.getValue();
      if (domainCandidateQueue != null && best == null) {
        int Candidatesleft = domainCandidateQueue.size();
        while (Candidatesleft > 0) {
          // while (!domainCandidateQueue.isEmpty()) {
          CandidatePair nextCandidatePair = domainCandidateQueue.element();
          Candidatesleft--;
          final GlcNode former = getNode(domain_key);
          final GlcNode next = nextCandidatePair.getCandidate();
          if (former != null) {
            if (Scalars.lessThan(next.merit(), former.merit())) {
              // collision check only if new node is better
              domainCandidateQueue.remove(); // remove next from DomainQueue
              if (obstacleQuery.isDisjoint(connectors.get(next))) {// better node not collision
                // current label disconnecting,
                // current label back in Candidatelist
                node.insertEdgeTo(next);
                insert(domain_key, next);
                if (!goalQuery.isDisjoint(connectors.get(next)))
                  offerDestination(next);
                break;
              }
            }
          } else {
            domainCandidateQueue.remove();
            if (obstacleQuery.isDisjoint(connectors.get(next))) {
              node.insertEdgeTo(next);
              insert(domain_key, next);
              if (!goalQuery.isDisjoint(connectors.get(next)))
                offerDestination(next);
              break;
            }
          }
        }
      }
    }
  }

  public void switchRootToState(Tensor state) {
    GlcNode newRoot = this.getNode(convertToKey(state));
    // TODO not nice, as we jump from state to startnode
    if (newRoot != null)
      switchRootToNode(newRoot);
    else
      System.out.println("This domain is not labelled yet");
  }

  public void switchRootToNode(GlcNode newRoot) {
    if (newRoot.isRoot()) {
      System.out.println("node is already root");
      return;
    }
    int oldDomainMapSize = domainMap().size();
    int oldQueueSize = queue().size();
    System.out.println("changing to root:" + newRoot.state());
    final GlcNode parent = newRoot.parent();
    parent.removeEdgeTo(newRoot);
    Collection<GlcNode> oldtree = Nodes.ofSubtree(parent);
    if (best != null) {
      if (oldtree.contains(best)) // check if goalnode was deleted
        best = null;
      //
    }
    // removes the new root from the child list of its parent
    if (queue().removeAll(oldtree)) // removing from queue;
      System.out.println("Removed " + (oldQueueSize - queue().size()) + " nodes from Queue");
    int removedNodes = 0;
    int addedNodesToQueue = 0;
    for (GlcNode tempNode : oldtree) { // loop for each domain, where sth was deleted
      Tensor tempDomainKey = convertToKey(tempNode.state());
      if (domainMap().remove(tempDomainKey, tempNode)) // removing from DomainMap
        removedNodes++;
      // --
      // Iterate through DomainQueue to find alternative: RELABELING
      // TODO: Iterate through tree to find goal node?
      //
    }
    System.out.println(removedNodes + " out of " + oldDomainMapSize + " Nodes removed from Tree ");
    System.out.println(addedNodesToQueue + " Nodes added to Queue");
  }

  @Override
  protected GlcNode createRootNode(Tensor x) { // TODO check if time of root node should always be set to 0
    return new GlcNode(null, new StateTime(x, ZeroScalar.get()), ZeroScalar.get(), //
        costFunction.minCostToGoal(x));
  }

  @Override
  public List<StateTime> detailedTrajectoryTo(GlcNode node) {
    return Trajectories.connect(stateIntegrator, Nodes.fromRoot(node));
  }

  @Override
  public TrajectoryRegionQuery getObstacleQuery() {
    return obstacleQuery;
  }

  @Override
  public TrajectoryRegionQuery getGoalQuery() {
    return goalQuery;
  }

  /** @param newGoal is the new RegionQuery for the new Goalregion */
  public void setGoalQuery(CostFunction newCostFunction, TrajectoryRegionQuery newGoal) {
    this.goalQuery = newGoal;
    costFunction = newCostFunction;
    // TODO refactoring as some code is double
    if (best != null) {
      List<StateTime> bestList = new ArrayList<>();
      bestList.add(best.stateTime());
      if (newGoal.isDisjoint(bestList)) { // oldBest not in new Goal
        best = null;
        {
          Collection<GlcNode> TreeCollection = Nodes.ofSubtree(getNodesfromRootToGoal().get(1));
          // TODO more efficient way then going through entire tree?
          Iterator<GlcNode> TreeCollectionIterator = TreeCollection.iterator();
          while (TreeCollectionIterator.hasNext()) {
            GlcNode current = TreeCollectionIterator.next();
            List<StateTime> currentList = new ArrayList<>();
            bestList.add(current.stateTime());
            if (!newGoal.isDisjoint(currentList)) { // current Node in Goal
              System.out.println("New Goal was found in current tree");
              offerDestination(current);
            }
          }
        }
        if (best != null)
          System.out.println("Goal was already found in the existing tree");
        long tic = System.nanoTime();
        // Changing the Merit in Queue for each Node
        List<GlcNode> list = new LinkedList<>(queue());
        queue().clear();
        list.stream().parallel() //
            .forEach(glcNode -> glcNode.setMinCostToGoal(costFunction.minCostToGoal(glcNode.state())));
        queue().addAll(list);
        long toc = System.nanoTime();
        System.out.println("Updated Merit of Queue with " + list.size() + " nodes: " + ((toc - tic) * 1e-9));
      } else {
        System.out.println("Old Goal Node is in New Goal");
        offerDestination(best);
      }
    } else {
      long tic = System.nanoTime();
      // Changing the Merit in Queue for each Node
      List<GlcNode> list = new LinkedList<>(queue());
      queue().clear();
      list.stream().parallel() //
          .forEach(glcNode -> glcNode.setMinCostToGoal(costFunction.minCostToGoal(glcNode.state())));
      queue().addAll(list);
      long toc = System.nanoTime();
      System.out.println("Updated Merit of Queue with " + list.size() + " nodes: " + ((toc - tic) * 1e-9));
    }
  }
}