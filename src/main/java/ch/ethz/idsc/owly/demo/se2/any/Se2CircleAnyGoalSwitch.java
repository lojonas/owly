// code by jl
package ch.ethz.idsc.owly.demo.se2.any;

import ch.ethz.idsc.owly.glc.adapter.Parameters;
import ch.ethz.idsc.owly.glc.core.AbstractAnyTrajectoryPlanner;
import ch.ethz.idsc.owly.math.Degree;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.sca.Cos;
import ch.ethz.idsc.tensor.sca.Sin;

enum Se2CircleAnyGoalSwitch {
  ;
  public static boolean switchToNextCircularGoal(AbstractAnyTrajectoryPlanner trajectoryPlanner, int iter) {
    return switchToNextCircularGoal(trajectoryPlanner, iter, null);
  }

  public static boolean switchToNextCircularGoal(AbstractAnyTrajectoryPlanner trajectoryPlanner, int iter, Parameters parameters) {
    Scalar stepsPerCircle = RealScalar.of(4);
    Scalar circleRadius = RealScalar.of(3);
    Tensor goal = null;
    Tensor radiusVector = Tensors.of(DoubleScalar.of(0.2), DoubleScalar.of(0.2), Degree.of(15));
    do {
      Scalar goalAngle = RealScalar.of(2 * Math.PI).divide(stepsPerCircle).multiply(RealScalar.of(iter)).negate();
      goal = Tensors.of(Cos.of(goalAngle).multiply(circleRadius), //
          Sin.of(goalAngle).multiply(circleRadius), goalAngle.subtract(RealScalar.of(Math.PI * 0.5)));
    } while (trajectoryPlanner.getObstacleQuery().isMember(new StateTime(goal, RealScalar.ZERO)));
    Se2MinCurvatureGoalManager se2GoalManager = new Se2MinCurvatureGoalManager(goal, radiusVector);
    if (parameters != null) { // changeGoal can be conducted quicker, due to GoalHint
      Tensor maxChange = Tensors.of(RealScalar.ONE, RealScalar.ONE, Degree.of(45));
      Tensor possibleGoalReachabilityRegionRadius = radiusVector.add(maxChange);
      Se2NoHeuristicGoalManager possibleGoalReachabilityRegion = new Se2NoHeuristicGoalManager(goal, possibleGoalReachabilityRegionRadius);
      return trajectoryPlanner.changeToGoal(se2GoalManager.getGoalInterface(), possibleGoalReachabilityRegion);
    }
    return trajectoryPlanner.changeToGoal(se2GoalManager.getGoalInterface());
  }
}
