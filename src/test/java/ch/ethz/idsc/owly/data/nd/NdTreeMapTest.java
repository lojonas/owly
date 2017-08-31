// code by Eric Simonton
// adapted by jph
package ch.ethz.idsc.owly.data.nd;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.alg.Flatten;
import ch.ethz.idsc.tensor.pdf.RandomVariate;
import ch.ethz.idsc.tensor.pdf.UniformDistribution;
import ch.ethz.idsc.tensor.red.Tally;
import ch.ethz.idsc.tensor.red.Total;
import junit.framework.TestCase;

public class NdTreeMapTest extends TestCase {
  public void testSome() {
    NdTreeMap<String> ndTreeMap = //
        new NdTreeMap<>(Tensors.vector(-2, -3), Tensors.vector(8, 9), 10, 10);
    ndTreeMap.add(Tensors.vector(1, 1), "d1");
    ndTreeMap.add(Tensors.vector(1, 0), "d2");
    ndTreeMap.add(Tensors.vector(0, 1), "d3");
    ndTreeMap.add(Tensors.vector(1, 1), "d4");
    ndTreeMap.add(Tensors.vector(0.1, 0.1), "d5");
    ndTreeMap.add(Tensors.vector(6, 7), "d6");
    NdDistanceInterface distancer = NdDistanceInterface.EUCLIDEAN;
    {
      NdCluster<String> cluster = ndTreeMap.buildCluster(Tensors.vector(0, 0), 1, distancer);
      assertTrue(cluster.iterator().next().value.equals("d5"));
    }
    {
      NdCluster<String> cluster = ndTreeMap.buildCluster(Tensors.vector(5, 5), 1, distancer);
      assertTrue(cluster.iterator().next().value.equals("d6"));
    }
    {
      NdCluster<String> cluster = ndTreeMap.buildCluster(Tensors.vector(1.1, 0.9), 2, distancer);
      List<String> list = Arrays.asList("d1", "d4");
      for (NdEntry<String> point : cluster)
        assertTrue(list.contains(point.value));
    }
  }

  public void testCornerCase() {
    NdTreeMap<String> ndTreeMap = //
        new NdTreeMap<>(Tensors.vector(-2, -3), Tensors.vector(8, 9), 400, 2);
    Tensor location = Array.zeros(2);
    for (int c = 0; c < 400; ++c)
      ndTreeMap.add(location, "s" + c);
  }

  public void testSimple1() {
    final int n = 10;
    NdTreeMap<String> ndTreeMap = //
        new NdTreeMap<>(Tensors.vector(0, 0), Tensors.vector(1, 1), n, 26);
    for (int c = 0; c < 400; ++c)
      ndTreeMap.add(RandomVariate.of(UniformDistribution.unit(), 2), "s" + c);
    Tensor flatten = Flatten.of(ndTreeMap.binSize());
    assertEquals(Total.of(flatten), RealScalar.of(400));
    NavigableMap<Tensor, Long> map = Tally.sorted(flatten);
    Tensor last = map.lastKey();
    assertEquals(last, RealScalar.of(n));
  }

  public void testPrint() {
    NdTreeMap<String> ndTreeMap = //
        new NdTreeMap<>(Tensors.vector(0, 0), Tensors.vector(1, 1), 3, 3);
    for (int c = 0; c < 12; ++c) {
      Tensor location = RandomVariate.of(UniformDistribution.unit(), 2);
      ndTreeMap.add(location, "s" + c);
    }
    // testTree.print();
    // System.out.println(testTree.binSize());
  }

  public void testFail0() {
    try {
      new NdTreeMap<>(Tensors.vector(-2, -3), Tensors.vector(8, 9, 3), 2, 2);
      assertTrue(false);
    } catch (Exception exception) {
      // ---
    }
  }

  public void testFail1() {
    NdTreeMap<String> ndTreeMap = new NdTreeMap<>( //
        Tensors.vector(-2, -3), Tensors.vector(8, 9), 2, 2);
    Tensor location = Array.zeros(3);
    try {
      ndTreeMap.add(location, "string");
      assertTrue(false);
    } catch (Exception exception) {
      // ---
    }
  }
}
