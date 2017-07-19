// code by bapaden and jph
package ch.ethz.idsc.owly.glc.adapter;

import java.util.Collection;
import java.util.List;

import ch.ethz.idsc.owly.data.LinearRasterMap;
import ch.ethz.idsc.owly.data.RasterMap;
import ch.ethz.idsc.owly.math.state.AbstractTrajectoryRegionQuery;
import ch.ethz.idsc.owly.math.state.StateTime;
import ch.ethz.idsc.owly.math.state.StateTimeRegion;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class SimpleTrajectoryRegionQuery extends AbstractTrajectoryRegionQuery {
  private final StateTimeRegion stateTimeRegion;
  // TODO magic constants of scale are not universal
  private RasterMap<StateTime> discoveredMembers = new LinearRasterMap<>(Tensors.vector(10, 10));
  // TODO make final again?

  public SimpleTrajectoryRegionQuery(StateTimeRegion stateTimeRegion) {
    this.stateTimeRegion = stateTimeRegion;
  }

  // TODO JAN: Real Copy Constructor?
  public SimpleTrajectoryRegionQuery(SimpleTrajectoryRegionQuery simpleTrajectoryRegionQuery) {
    this.discoveredMembers = new LinearRasterMap<>(Tensors.vector(10, 10));
    for (StateTime stateTime : discoveredMembers.values()) {
      Tensor x = stateTime.x();
      if (1 < x.length())
        discoveredMembers.put(x.extract(0, 2), stateTime);
    }
    // this.discoveredMembers = LinearRasterMap<>(simpleTrajectoryRegionQuery.discoveredMembers);
    this.stateTimeRegion = simpleTrajectoryRegionQuery.stateTimeRegion;
  }

  @Override
  public final int firstMember(List<StateTime> trajectory) {
    int index = -1;
    for (StateTime stateTime : trajectory) {
      ++index;
      if (stateTimeRegion.isMember(stateTime)) {
        Tensor x = stateTime.x();
        if (1 < x.length())
          discoveredMembers.put(x.extract(0, 2), stateTime);
        return index;
      }
    }
    return NOMATCH;
  }

  public Collection<StateTime> getDiscoveredMembers() {
    return discoveredMembers.values();
  }
}
