// code by Eric Simonton
// adapted by jph and clruch
package ch.ethz.idsc.owly.tree;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;

public class Point<V> implements Comparable<Point<V>> {
  public final Tensor location;
  public final V value;
  public Scalar distanceToCenter;

  Point(Tensor location, V value) {
    this.location = location;
    this.value = value;
  }

  void setDistanceToCenter(Distance distancer, Tensor center) {
    distanceToCenter = distancer.getDistance(center, location);
  }

  boolean isFartherThan(Tensor testPoint, Tensor center, Distance distancer) {
    return Scalars.lessThan(distancer.getDistance(testPoint, center), distanceToCenter);
  }

  @Override
  public int compareTo(Point<V> other) {
    return Scalars.compare(other.distanceToCenter, distanceToCenter);
  }

  @Override
  public int hashCode() {
    return location.hashCode() ^ value.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Point) {
      @SuppressWarnings("unchecked")
      Point<V> other = (Point<V>) object;
      return location.equals(other.location) && value.equals(other.value);
    }
    return false;
  }
}