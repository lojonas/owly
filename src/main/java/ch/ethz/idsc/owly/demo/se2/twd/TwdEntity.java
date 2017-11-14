// code by jph
package ch.ethz.idsc.owly.demo.se2.twd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import ch.ethz.idsc.owly.demo.se2.Se2CarIntegrator;
import ch.ethz.idsc.owly.demo.se2.Se2LateralAcceleration;
import ch.ethz.idsc.owly.demo.se2.Se2MinTimeGoalManager;
import ch.ethz.idsc.owly.demo.se2.Se2StateSpaceModel;
import ch.ethz.idsc.owly.demo.se2.Se2Wrap;
import ch.ethz.idsc.owly.glc.adapter.MultiCostGoalAdapter;
import ch.ethz.idsc.owly.glc.core.GoalInterface;
import ch.ethz.idsc.owly.glc.core.StandardTrajectoryPlanner;
import ch.ethz.idsc.owly.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.owly.gui.GeometricLayer;
import ch.ethz.idsc.owly.gui.ani.AbstractEntity;
import ch.ethz.idsc.owly.gui.ani.PlannerType;
import ch.ethz.idsc.owly.math.StateTimeTensorFunction;
import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.owly.math.se2.Se2Utils;
import ch.ethz.idsc.owly.math.state.FixedStateIntegrator;
import ch.ethz.idsc.owly.math.state.SimpleEpisodeIntegrator;
import ch.ethz.idsc.owly.math.state.StateIntegrator;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.owly.math.state.TrajectoryRegionQuery;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.TensorRuntimeException;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.sca.Sqrt;

/* package */ class TwdEntity extends AbstractEntity {
  public static final Tensor FALLBACK_CONTROL = Array.zeros(3).unmodifiable();
  // triangle
  private static final Tensor SHAPE = Tensors.matrixDouble( //
      new double[][] { { .3, 0, 1 }, { -.1, -.1, 1 }, { -.1, +.1, 1 } }).unmodifiable();
  private static final Se2Wrap SE2WRAP = new Se2Wrap(Tensors.vector(1, 1, 2));
  private static final Tensor PARTITIONSCALE = Tensors.vector(6, 6, 50 / Math.PI); // 50/pi == 15.9155
  // ---
  static {
    if (!PARTITIONSCALE.get(0).equals(PARTITIONSCALE.get(1)))
      throw TensorRuntimeException.of(PARTITIONSCALE);
  }

  public static TwdEntity createDefault(Tensor state) {
    TwdConfig twdControls = new TwdConfig(RealScalar.ONE, RealScalar.ONE);
    return new TwdEntity(twdControls, state);
  }

  // ---
  final Collection<Flow> controls;
  final Scalar goalRadius_xy;
  final Scalar goalRadius_theta;
  TrajectoryRegionQuery obstacleQuery = null;

  public TwdEntity(TwdConfig twdControls, Tensor state) {
    super(new SimpleEpisodeIntegrator( //
        Se2StateSpaceModel.INSTANCE, //
        Se2CarIntegrator.INSTANCE, //
        new StateTime(state, RealScalar.ZERO))); // initial position
    controls = twdControls.createControls(4);
    goalRadius_xy = Sqrt.of(RealScalar.of(2)).divide(PARTITIONSCALE.Get(0));
    goalRadius_theta = Sqrt.of(RealScalar.of(2)).divide(PARTITIONSCALE.Get(2));
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
  public Scalar delayHint() {
    return RealScalar.ONE;
  }

  @Override
  public PlannerType getPlannerType() {
    return PlannerType.STANDARD;
  }

  @Override
  public TrajectoryPlanner createTrajectoryPlanner(TrajectoryRegionQuery obstacleQuery, Tensor goal) {
    this.obstacleQuery = obstacleQuery;
    StateIntegrator stateIntegrator = //
        FixedStateIntegrator.create(Se2CarIntegrator.INSTANCE, RationalScalar.of(1, 10), 4);
    Tensor radiusVector = Tensors.of(goalRadius_xy, goalRadius_xy, goalRadius_theta);
    GoalInterface goalInterface = MultiCostGoalAdapter.of( //
        Se2MinTimeGoalManager.create(goal, radiusVector, controls), //
        Arrays.asList(Se2LateralAcceleration.COSTFUNCTION));
    TrajectoryPlanner trajectoryPlanner = new StandardTrajectoryPlanner( //
        PARTITIONSCALE, stateIntegrator, controls, obstacleQuery, goalInterface);
    trajectoryPlanner.represent = StateTimeTensorFunction.state(SE2WRAP::represent);
    return trajectoryPlanner;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    { // indicate current position
      StateTime stateTime = getStateTimeNow();
      Color color = new Color(64, 64, 64, 128);
      if (Objects.nonNull(obstacleQuery))
        if (!obstacleQuery.isDisjoint(Collections.singletonList(stateTime)))
          color = new Color(255, 64, 64, 128);
      geometricLayer.pushMatrix(Se2Utils.toSE2Matrix(stateTime.state()));
      graphics.setColor(color);
      graphics.fill(geometricLayer.toPath2D(SHAPE));
      geometricLayer.popMatrix();
    }
    { // indicate position delay[s] into the future
      Tensor state = getEstimatedLocationAt(delayHint());
      Point2D point = geometricLayer.toPoint2D(state);
      graphics.setColor(new Color(255, 128, 64, 192));
      graphics.fill(new Rectangle2D.Double(point.getX() - 2, point.getY() - 2, 5, 5));
    }
    {
      geometricLayer.pushMatrix(geometricLayer.getMouseSe2Matrix());
      graphics.setColor(new Color(0, 128, 255, 192));
      graphics.fill(geometricLayer.toPath2D(SHAPE));
      geometricLayer.popMatrix();
    }
  }
}