// code by jl
package ch.ethz.idsc.owly.demo.delta.glc;

import ch.ethz.idsc.owly.glc.core.AnyPlannerInterface;
import ch.ethz.idsc.owly.glc.core.Expand;
import ch.ethz.idsc.owly.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.owly.gui.Gui;
import ch.ethz.idsc.owly.gui.OwlyFrame;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;

enum DeltaGlcConstTimeAnyDemo {
  ;
  public static void main(String[] args) throws Exception {
    RationalScalar resolution = (RationalScalar) RationalScalar.of(11, 1);
    AnyPlannerInterface trajectoryPlanner = DeltaHelper.createGlcAny(RealScalar.of(-0.25), resolution);
    int depthLimit = 10000; // TODO HACK
    OwlyFrame owlyFrame = Gui.start();
    owlyFrame.configCoordinateOffset(33, 416);
    owlyFrame.jFrame.setBounds(100, 100, 620, 475);
    Scalar planningTime = RealScalar.of(1);
    while (!trajectoryPlanner.getBest().isPresent() && owlyFrame.jFrame.isVisible()) {
      int expandIter = Expand.constTime(trajectoryPlanner, planningTime, depthLimit);
      owlyFrame.setGlc((TrajectoryPlanner) trajectoryPlanner);
      if (expandIter < 1)
        break;
      Thread.sleep(1);
    }
  }
}
