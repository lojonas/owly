package ch.ethz.idsc.owly.glc;

import ch.ethz.idsc.owly.glc.adapter.SimpleTrajectoryRegionQuery;
import ch.ethz.idsc.owly.glc.adapter.TimeInvariantRegion;
import ch.ethz.idsc.owly.glc.core.StateTime;
import ch.ethz.idsc.owly.glc.core.Trajectory;
import ch.ethz.idsc.owly.glc.core.TrajectoryRegionQuery;
import ch.ethz.idsc.owly.util.rn.RnSphericalRegion;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.ZeroScalar;
import junit.framework.TestCase;

public class TrajectoryTest extends TestCase {
  public void testDisjoint() {
    TrajectoryRegionQuery goalQuery = //
        new SimpleTrajectoryRegionQuery(new TimeInvariantRegion( //
            new RnSphericalRegion(Tensors.vector(10, 5), RealScalar.of(1))));
    Trajectory trajectory = new Trajectory();
    trajectory.add(new StateTime(Tensors.vector(0, 5), ZeroScalar.get()));
    trajectory.add(new StateTime(Tensors.vector(5, 5), ZeroScalar.get()));
    assertEquals(goalQuery.firstMember(trajectory), -1);
    assertTrue(goalQuery.isDisjoint(trajectory));
    // ---
    trajectory.add(new StateTime(Tensors.vector(10, 5), ZeroScalar.get()));
    assertEquals(goalQuery.firstMember(trajectory), 2);
    assertFalse(goalQuery.isDisjoint(trajectory));
  }
}
