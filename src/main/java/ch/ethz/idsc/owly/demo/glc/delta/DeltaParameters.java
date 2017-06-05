// code by jl
package ch.ethz.idsc.owly.demo.glc.delta;

import ch.ethz.idsc.owly.glc.adapter.Parameters;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.sca.Power;

public class DeltaParameters extends Parameters {
  private final Scalar lipschitz;

  public DeltaParameters( //
      RationalScalar resolution, Scalar timeScale, Scalar depthScale, Tensor partitionScale, Scalar dtMax, int maxIter, //
      Scalar lipschitz) {
    super(resolution, timeScale, depthScale, partitionScale, dtMax, maxIter);
    this.lipschitz = lipschitz;
  }

  @Override
  /** @return if Lipschitz == 0: R*log(R)²
   * @return else : R^(5/Pi) */
  public Tensor getEta() {
    if (Scalars.isZero(lipschitz))
      return getPartitionScale().map(Scalar::invert) //
          .multiply(Power.of(RealScalar.of(getResolution()), 2));
    return getPartitionScale().map(Scalar::invert) //
        .multiply(Power.of(RealScalar.of(getResolution()), RealScalar.ONE.add(lipschitz)));
    // TODO change to function depending on Lipschitz
  }
}
