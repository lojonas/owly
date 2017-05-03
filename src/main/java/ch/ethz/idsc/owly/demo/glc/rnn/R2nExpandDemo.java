// code by jph
package ch.ethz.idsc.owly.demo.glc.rnn;

import java.util.Collection;

import ch.ethz.idsc.owly.demo.glc.rn.R2Controls;
import ch.ethz.idsc.owly.glc.core.DefaultTrajectoryPlanner;
import ch.ethz.idsc.owly.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.owly.glc.gui.ExpandGlcFrame;
import ch.ethz.idsc.owly.math.flow.EulerIntegrator;
import ch.ethz.idsc.owly.math.flow.Flow;
import ch.ethz.idsc.owly.math.state.EmptyTrajectoryRegionQuery;
import ch.ethz.idsc.owly.math.state.FixedStateIntegrator;
import ch.ethz.idsc.owly.math.state.StateIntegrator;
import ch.ethz.idsc.owly.math.state.TrajectoryRegionQuery;
import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

class R2nExpandDemo {
  public static void main(String[] args) {
    Tensor eta = Tensors.vector(4, 4);
    StateIntegrator stateIntegrator = FixedStateIntegrator.create(new EulerIntegrator(), RationalScalar.of(1, 5), 5);
    Collection<Flow> controls = R2Controls.createControls(16);
    RnnGoalManager rnGoal = new RnnGoalManager(Tensors.vector(4, 4), DoubleScalar.of(.25));
    TrajectoryRegionQuery obstacleQuery = new EmptyTrajectoryRegionQuery();
    // ---
    TrajectoryPlanner trajectoryPlanner = new DefaultTrajectoryPlanner( //
        eta, stateIntegrator, controls, rnGoal, rnGoal, obstacleQuery);
    trajectoryPlanner.insertRoot(Tensors.vector(0, 0));
    new ExpandGlcFrame(trajectoryPlanner);
  }
}
