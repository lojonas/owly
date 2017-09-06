// code by jl and jph
package ch.ethz.idsc.owly.demo.se2.any;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ch.ethz.idsc.owly.demo.rn.R2Controls;
import ch.ethz.idsc.owly.demo.rn.RnSimpleCircleHeuristicGoalManager;
import ch.ethz.idsc.owly.demo.se2.Se2Controls;
import ch.ethz.idsc.owly.demo.se2.Se2StateSpaceModel;
import ch.ethz.idsc.owly.demo.se2.Se2TrajectoryGoalManager;
import ch.ethz.idsc.owly.demo.se2.Se2Wrap;
import ch.ethz.idsc.owly.demo.se2.glc.Se2Parameters;
import ch.ethz.idsc.owly.glc.adapter.SimpleTrajectoryRegionQuery;
import ch.ethz.idsc.owly.glc.core.Expand;
import ch.ethz.idsc.owly.glc.core.GlcNode;
import ch.ethz.idsc.owly.glc.core.GlcNodes;
import ch.ethz.idsc.owly.glc.core.GoalInterface;
import ch.ethz.idsc.owly.glc.core.StandardTrajectoryPlanner;
import ch.ethz.idsc.owly.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.owly.gui.OwlyLayer;
import ch.ethz.idsc.owly.gui.ani.AbstractAnyEntity;
import ch.ethz.idsc.owly.gui.ani.PlannerType;
import ch.ethz.idsc.owly.math.CoordinateWrap;
import ch.ethz.idsc.owly.math.RotationUtils;
import ch.ethz.idsc.owly.math.Se2Utils;
import ch.ethz.idsc.owly.math.flow.EulerIntegrator;
import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.owly.math.flow.RungeKutta45Integrator;
import ch.ethz.idsc.owly.math.region.EllipsoidRegion;
import ch.ethz.idsc.owly.math.region.Region;
import ch.ethz.idsc.owly.math.state.FixedStateIntegrator;
import ch.ethz.idsc.owly.math.state.SimpleEpisodeIntegrator;
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
import ch.ethz.idsc.tensor.sca.Sqrt;

/** omni-directional movement with constant speed */
public class Se2AnyEntity extends AbstractAnyEntity {
  // private static final JLabel JLABEL = new JLabel();
  private static final Tensor FALLBACK_CONTROL = Array.zeros(2).unmodifiable(); // {angle=0, vel=0}
  private static final Tensor SHAPE = Tensors.matrixDouble( //
      new double[][] { //
          { .2, +.07, 1 }, //
          { .2, -.07, 1 }, //
          { -.1, -.07, 1 }, //
          { -.1, +.07, 1 } //
      }).unmodifiable();
  private static final Se2Wrap SE2WRAP = new Se2Wrap(Tensors.vector(1, 1, 2));
  // ---
  private final Tensor goalRadius;
  private static final Scalar DELAY_HINT = RealScalar.of(2);
  private static final Scalar EXPAND_TIME = RealScalar.of(1.75);
  private TrajectoryRegionQuery obstacleQuery;

  /** @param state initial position of entity */
  public Se2AnyEntity(Tensor state, int resolution) {
    super(state, //
        // --
        new Se2Parameters( //
            (RationalScalar) RealScalar.of(resolution), // resolution
            RealScalar.of(2), // TimeScale
            RealScalar.of(100), // DepthScale
            Tensors.vector(5, 5, 50 / Math.PI), // PartitionScale 50/pi == 15.9155
            RationalScalar.of(1, 6), // dtMax
            2000, // maxIter
            Se2StateSpaceModel.INSTANCE.getLipschitz()), // Lipschitz
        // --
        Se2Controls.createControlsForwardAndReverse(RotationUtils.DEGREE(60), resolution), //
        // --
        new SimpleEpisodeIntegrator( //
            Se2StateSpaceModel.INSTANCE, //
            RungeKutta45Integrator.INSTANCE, //
            new StateTime(state, RealScalar.ZERO)),
        //
        DELAY_HINT, EXPAND_TIME); //
    final Scalar goalRadius_xy = Sqrt.of(RealScalar.of(2)).divide(parameters.getEta().Get(0));
    final Scalar goalRadius_theta = Sqrt.of(RealScalar.of(2)).divide(parameters.getEta().Get(2));
    goalRadius = Tensors.of(goalRadius_xy, goalRadius_xy, goalRadius_theta);
  }

  @Override
  public PlannerType getPlannerType() {
    return PlannerType.ANY;
  }

  @Override
  protected Scalar distance(Tensor x, Tensor y) {
    return SE2WRAP.distance(x, y);
  }

  @Override
  protected Tensor fallbackControl() {
    return FALLBACK_CONTROL;
  }

  @Override
  public void render(OwlyLayer owlyLayer, Graphics2D graphics) {
    {// indicate current position
      StateTime stateTime = getStateTimeNow();
      Color color = new Color(64, 64, 64, 128);
      graphics.setColor(color);
      Tensor matrix = Se2Utils.toSE2Matrix(stateTime.state());
      Path2D path2d = owlyLayer.toPath2D(Tensor.of(SHAPE.flatten(0).map(matrix::dot)));
      graphics.fill(path2d);
    }
    { // indicate position delay[s] into the future
      Tensor state = getEstimatedLocationAt(delayHint());
      Point2D point = owlyLayer.toPoint2D(state);
      graphics.setColor(new Color(255, 128, 64, 192));
      graphics.fill(new Rectangle2D.Double(point.getX() - 2, point.getY() - 2, 5, 5));
    }
    {
      graphics.setColor(new Color(0, 128, 255, 192));
      Tensor matrix = owlyLayer.getMouseSe2Matrix();
      Path2D path2d = owlyLayer.toPath2D(Tensor.of(SHAPE.flatten(0).map(matrix::dot)));
      graphics.fill(path2d);
    }
  }

  @Override
  protected final GoalInterface createGoal(Tensor goal) {
    return createGoalFromR2(goal);
    // return new Se2MinDistGoalManager(goal, goalRadius).getGoalInterface();
  }

  protected final GoalInterface createGoalFromR2(Tensor goal) {
    Tensor currentState = getEstimatedLocationAt(DELAY_HINT).extract(0, 2);
    Tensor R2goal = goal.extract(0, 2);
    long tic = System.nanoTime();
    // ---
    Tensor eta = Tensors.vector(8, 8);
    StateIntegrator stateIntegrator = FixedStateIntegrator.create(EulerIntegrator.INSTANCE, RationalScalar.of(1, 5), 5);
    Collection<Flow> controls = R2Controls.createRadial(10);
    RnSimpleCircleHeuristicGoalManager rnGoal = new RnSimpleCircleHeuristicGoalManager(R2goal, goalRadius.Get(0));
    // ---
    TrajectoryPlanner trajectoryPlanner = new StandardTrajectoryPlanner( //
        eta, stateIntegrator, controls, obstacleQuery, rnGoal);
    trajectoryPlanner.insertRoot(currentState);
    // int iters =
    Expand.maxTime(trajectoryPlanner, RealScalar.of(1)); // 1 [s]
    Optional<GlcNode> optional = trajectoryPlanner.getFinalGoalNode();
    List<StateTime> trajectory = GlcNodes.getPathFromRootTo(optional.get());
    List<Region> goalRegionsList = new ArrayList<>();
    Tensor goalRadiusR2 = goalRadius.extract(0, 2).append(DoubleScalar.POSITIVE_INFINITY);
    for (StateTime entry : trajectory) {
      Tensor goalTemp = Tensors.of(entry.state().Get(0), entry.state().Get(1), RealScalar.ZERO);
      if (trajectory.indexOf(entry) == (trajectory.size() - 1)) // if last entry of trajectory
        goalRegionsList.add(new EllipsoidRegion(goal, goalRadius));
      else
        goalRegionsList.add(new EllipsoidRegion(goalTemp, goalRadiusR2));
    }
    long toc = System.nanoTime();
    System.err.println("TrajectoryGoalcreation took: " + (toc - tic) * 1e-9 + "s");
    return new Se2TrajectoryGoalManager(goalRegionsList, trajectory, goalRadiusR2);
  }

  @Override
  protected TrajectoryRegionQuery initializeObstacle(Region region, Tensor currentState) {
    obstacleQuery = new SimpleTrajectoryRegionQuery(new TimeInvariantRegion(region));
    return obstacleQuery;
  }

  @Override
  protected StateIntegrator createIntegrator() {
    return FixedStateIntegrator.create(RungeKutta45Integrator.INSTANCE, parameters.getdtMax(), parameters.getTrajectorySize());
  }

  @Override
  protected CoordinateWrap getWrap() {
    return SE2WRAP;
  }
}