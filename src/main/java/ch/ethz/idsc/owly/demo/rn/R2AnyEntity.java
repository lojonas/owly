// code by jl and jph
package ch.ethz.idsc.owly.demo.rn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import ch.ethz.idsc.owly.glc.core.Expand;
import ch.ethz.idsc.owly.glc.core.GlcNode;
import ch.ethz.idsc.owly.glc.core.GoalInterface;
import ch.ethz.idsc.owly.glc.core.OptimalAnyTrajectoryPlanner;
import ch.ethz.idsc.owly.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.owly.glc.core.TrajectorySample;
import ch.ethz.idsc.owly.gui.OwlyLayer;
import ch.ethz.idsc.owly.gui.ani.AbstractEntity;
import ch.ethz.idsc.owly.gui.ani.PlannerType;
import ch.ethz.idsc.owly.gui.ani.TrajectoryPlannerCallback;
import ch.ethz.idsc.owly.math.SingleIntegratorStateSpaceModel;
import ch.ethz.idsc.owly.math.flow.EulerIntegrator;
import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.owly.math.state.FixedStateIntegrator;
import ch.ethz.idsc.owly.math.state.SimpleEpisodeIntegrator;
import ch.ethz.idsc.owly.math.state.StateIntegrator;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.owly.math.state.TrajectoryRegionQuery;
import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Norm;

/** omni-directional movement with constant speed */
public class R2AnyEntity extends AbstractEntity {
  private static final Tensor FALLBACK_CONTROL = Tensors.vector(0, 0).unmodifiable();
  /** preserve 1[s] of the former trajectory */
  private static final Scalar DELAY_HINT = RealScalar.ONE;
  // ---
  private final Collection<Flow> controls = R2Controls.createRadial(36); // TODO magic const

  /** @param state initial position of entity */
  public R2AnyEntity(Tensor state) {
    super(new SimpleEpisodeIntegrator( //
        SingleIntegratorStateSpaceModel.INSTANCE, //
        EulerIntegrator.INSTANCE, //
        new StateTime(state, RealScalar.ZERO)));
  }

  @Override
  public PlannerType getPlannerType() {
    return PlannerType.ANY;
  }

  @Override
  public Scalar distance(Tensor x, Tensor y) {
    return Norm._2SQUARED.of(x.subtract(y));
  }

  @Override
  public Tensor fallbackControl() {
    return FALLBACK_CONTROL;
  }

  @Override
  public Scalar delayHint() {
    return DELAY_HINT;
  }

  @Override
  public TrajectoryPlanner createTrajectoryPlanner(TrajectoryRegionQuery obstacleQuery, Tensor goal) {
    Tensor partitionScale = Tensors.vector(8, 8);
    StateIntegrator stateIntegrator = //
        FixedStateIntegrator.create(EulerIntegrator.INSTANCE, RationalScalar.of(1, 12), 4);
    RnSimpleCircleGoalManager rnGoal = //
        new RnSimpleCircleGoalManager(goal.extract(0, 2), DoubleScalar.of(.2));
    return new OptimalAnyTrajectoryPlanner( //
        partitionScale, stateIntegrator, controls, obstacleQuery, rnGoal);
  }

  @Override
  public void render(OwlyLayer owlyLayer, Graphics2D graphics) {
    { // indicate current position
      Tensor state = episodeIntegrator.tail().state();
      Point2D point = owlyLayer.toPoint2D(state);
      graphics.setColor(new Color(64, 128, 64, 192));
      graphics.fill(new Ellipse2D.Double(point.getX() - 2, point.getY() - 2, 7, 7));
    }
    { // indicate position 1[s] into the future
      Tensor state = getEstimatedLocationAt(DELAY_HINT);
      Point2D point = owlyLayer.toPoint2D(state);
      graphics.setColor(new Color(255, 128, 128 - 64, 128 + 64));
      graphics.fill(new Rectangle2D.Double(point.getX() - 2, point.getY() - 2, 5, 5));
    }
  }

  Thread thread;
  TrajectoryPlannerCallback trajectoryPlannerCallback;
  List<TrajectorySample> head;
  GlcNode newRoot;
  Tensor goal;
  boolean switchRootRequest = false;

  public void switchToGoal( //
      TrajectoryPlannerCallback trajectoryPlannerCallback, List<TrajectorySample> head, GlcNode newRoot, Tensor goal) { // TODO call in thread
    try {
      while (switchRootRequest) {
        Thread.sleep(500);
        System.out.println("block");
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    this.trajectoryPlannerCallback = trajectoryPlannerCallback;
    this.head = head;
    this.newRoot = newRoot;
    this.goal = goal;
    switchRootRequest = true;
  }

  public OptimalAnyTrajectoryPlanner tp;

  public void startLife(TrajectoryRegionQuery trq, Tensor root) {
    tp = (OptimalAnyTrajectoryPlanner) createTrajectoryPlanner(trq, root);
    head = this.getFutureTrajectoryUntil(delayHint());
    thread = new Thread(() -> {
      while (true) {
        if (switchRootRequest) {
          GlcNode newRoot = getEstimatedNodeAt(DELAY_HINT);
          tp.switchRootToNode(newRoot); // point on trajectory with delay from now
          GoalInterface rnGoal = //
              new RnSimpleCircleGoalManager(goal.extract(0, 2), DoubleScalar.of(.2));
          boolean result = tp.changeToGoal(rnGoal); // <- may take a while
          tp.getBest();
          // Expand.constTime(tp, RealScalar.of(.2), 10000);
          switchRootRequest = false;
        } else
          Expand.constTime(tp, RealScalar.of(0.2), 10000);
        trajectoryPlannerCallback.expandResult(head, tp);
        try {
          Thread.sleep(10);
        } catch (Exception exception) {
          exception.printStackTrace();
        }
      }
    });
    thread.start();
  }

  public final GlcNode getEstimatedNodeAt(Scalar delay) {
    List<TrajectorySample> trajectoryUntil = getFutureTrajectoryUntil(delay);
    ListIterator<TrajectorySample> iterator = trajectoryUntil.listIterator(trajectoryUntil.size() - 1);
    while (iterator.hasPrevious()) { // looks for last past GlcNode
      StateTime temp = iterator.previous().stateTime();
      Optional<GlcNode> optional = tp.existsInTree(temp);
      if (optional.isPresent())
        return optional.get();
    }
    // TODO JONAS: change to next passed GlcNode?
    throw new RuntimeException(); // no StateTime in trajectory corresponds to Node in Tree?
  }
}
