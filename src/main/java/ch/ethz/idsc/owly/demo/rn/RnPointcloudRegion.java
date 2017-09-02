// code by jph
package ch.ethz.idsc.owly.demo.rn;

import ch.ethz.idsc.owly.data.nd.NdCenterInterface;
import ch.ethz.idsc.owly.data.nd.NdCluster;
import ch.ethz.idsc.owly.data.nd.NdTreeMap;
import ch.ethz.idsc.owly.math.region.EmptyRegion;
import ch.ethz.idsc.owly.math.region.Region;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.red.Max;
import ch.ethz.idsc.tensor.red.Min;

public class RnPointcloudRegion implements Region {
  private static final String PLACEHOLDER = "PLACEHOLDER";

  /** @param points
   * @param radius
   * @return */
  public static Region of(Tensor points, Scalar radius) {
    return points.length() == 0 ? EmptyRegion.INSTANCE : new RnPointcloudRegion(points, radius);
  }

  // ---
  private final NdTreeMap<String> ndTreeMap;
  private final Scalar radius;

  private RnPointcloudRegion(Tensor points, Scalar radius) {
    Tensor pt = Transpose.of(points);
    Tensor lbounds = Tensors.vector(i -> pt.get(i).flatten(0).reduce(Min::of).get(), pt.length());
    Tensor ubounds = Tensors.vector(i -> pt.get(i).flatten(0).reduce(Max::of).get(), pt.length());
    // System.out.println("---");
    // System.out.println(lbounds);
    // System.out.println(ubounds);
    ndTreeMap = new NdTreeMap<>(lbounds, ubounds, 10, 5); // TODO magic const
    for (Tensor point : points)
      ndTreeMap.add(point, PLACEHOLDER);
    this.radius = radius;
  }

  @Override
  public boolean isMember(Tensor tensor) {
    NdCenterInterface distanceInterface = NdCenterInterface.euclidean(tensor);
    NdCluster<String> ndCluster = ndTreeMap.buildCluster(distanceInterface, 1);
    Scalar distance = ndCluster.iterator().next().distance;
    return Scalars.lessEquals(distance, radius);
  }
}
