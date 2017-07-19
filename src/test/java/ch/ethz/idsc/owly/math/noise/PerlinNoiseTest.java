// code by jph
package ch.ethz.idsc.owly.math.noise;

import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.pdf.BinCounts;
import junit.framework.TestCase;

public class PerlinNoiseTest extends TestCase {
  public void testSimple() {
    Tensor noise = Tensors.vector(i -> DoubleScalar.of(10 * (1 + PerlinNoise.at(1.6, .1 * i, .1 + i))), 1000);
    Tensor bins = BinCounts.of(noise);
    // System.out.println(bins);
    assertEquals(bins.length(), 18);
    long len = bins.flatten(0) //
        .map(Scalar.class::cast) //
        .filter(scalar -> Scalars.lessThan(DoubleScalar.of(30), scalar)) //
        .count();
    assertTrue(5 < len);
  }
}