// code by jph
package ch.ethz.idsc.owly.math.region;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensors;
import junit.framework.TestCase;

public class SphericalRegionTest extends TestCase {
  public void testSimple() {
    Region region = new SphericalRegion(Tensors.vector(1, 1), RealScalar.ONE);
    assertTrue(region.isMember(Tensors.vector(1, 0)));
    assertTrue(region.isMember(Tensors.vector(0, 1)));
    assertFalse(region.isMember(Tensors.vector(2, 0)));
    assertFalse(region.isMember(Tensors.vector(0, 2)));
  }

  public void testPoint() {
    Region region = new SphericalRegion(Tensors.vector(1, 1), RealScalar.ZERO);
    assertTrue(region.isMember(Tensors.vector(1, 1)));
  }

  public void testDistance() {
    SphericalRegion region = new SphericalRegion(Tensors.vector(1, 1), RealScalar.ZERO);
    assertEquals(region.evaluate(Tensors.vector(11, 1)), RealScalar.of(10));
  }
}