// code by jph
package ch.ethz.idsc.owly.gui.ani;

import ch.ethz.idsc.owly.glc.core.TrajectoryPlanner;

public enum OwlyGui {
  ;
  public static OwlyFrame start() {
    OwlyFrame owlyFrame = new OwlyFrame();
    owlyFrame.jFrame.setVisible(true);
    return owlyFrame;
  }

  public static OwlyFrame glc(TrajectoryPlanner trajectoryPlanner) {
    OwlyFrame owlyFrame = new OwlyFrame();
    owlyFrame.setGlc(trajectoryPlanner);
    owlyFrame.jFrame.setVisible(true);
    return owlyFrame;
  }
}