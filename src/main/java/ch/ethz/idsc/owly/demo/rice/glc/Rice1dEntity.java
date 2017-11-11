// code by jph
package ch.ethz.idsc.owly.demo.rice.glc;

import java.util.Collection;

import ch.ethz.idsc.owly.demo.rice.Rice1GoalManager;
import ch.ethz.idsc.owly.demo.rice.Rice2StateSpaceModel;
import ch.ethz.idsc.owly.glc.core.GoalInterface;
import ch.ethz.idsc.owly.glc.core.StandardTrajectoryPlanner;
import ch.ethz.idsc.owly.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.owly.gui.ani.AbstractCircularEntity;
import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.owly.math.flow.Integrator;
import ch.ethz.idsc.owly.math.flow.RungeKutta4Integrator;
import ch.ethz.idsc.owly.math.state.FixedStateIntegrator;
import ch.ethz.idsc.owly.math.state.SimpleEpisodeIntegrator;
import ch.ethz.idsc.owly.math.state.StateIntegrator;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.owly.math.state.TrajectoryRegionQuery;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Norm2Squared;

/* package */ class Rice1dEntity extends AbstractCircularEntity {
  public static final Tensor FALLBACK_CONTROL = Tensors.vectorDouble(0).unmodifiable();
  // ---
  private static final Integrator INTEGRATOR = RungeKutta4Integrator.INSTANCE;
  // ---
  private final Collection<Flow> controls;

  /** @param state initial position of entity */
  public Rice1dEntity(Scalar mu, Tensor state, Collection<Flow> controls) {
    super(new SimpleEpisodeIntegrator(Rice2StateSpaceModel.of(mu), INTEGRATOR, //
        new StateTime(state, RealScalar.ZERO)));
    this.controls = controls;
  }

  @Override
  protected Scalar distance(Tensor x, Tensor y) {
    return Norm2Squared.between(x, y);
  }

  @Override
  protected final Tensor fallbackControl() {
    return FALLBACK_CONTROL;
  }

  @Override
  public Scalar delayHint() {
    return RealScalar.of(0.5);
  }

  @Override
  public TrajectoryPlanner createTrajectoryPlanner(TrajectoryRegionQuery obstacleQuery, Tensor goal) {
    Tensor partitionScale = Tensors.vector(8, 8);
    StateIntegrator stateIntegrator = //
        FixedStateIntegrator.create(INTEGRATOR, RationalScalar.of(1, 12), 4);
    GoalInterface goalInterface = new Rice1GoalManager(goal.extract(0, 2), Tensors.vector(.2, .3));
    return new StandardTrajectoryPlanner( //
        partitionScale, stateIntegrator, controls, obstacleQuery, goalInterface);
  }
}
