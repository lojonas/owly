package ch.ethz.idsc.owly.demo.glc.se2glc;

import ch.ethz.idsc.owly.glc.adapter.Parameters;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.sca.Log;
import ch.ethz.idsc.tensor.sca.Power;

// TODO JONAS class should extend from DefaultParameters
public class Se2Parameters extends Parameters {
  private final Scalar lipschitz;

  public Se2Parameters( //
      RationalScalar resolution, Scalar timeScale, Scalar depthScale, Tensor partitionScale, Scalar dtMax, int maxIter, //
      Scalar lipschitz) {
    super(resolution, timeScale, depthScale, partitionScale, dtMax, maxIter);
    this.lipschitz = lipschitz;
  }

  @Override
  /** @return if Lipschitz == 0: R*log(R)²/partitionScale
   * @return else : R^(5/Pi)/partitionScale */
  public Tensor getEta() {
    if (Scalars.isZero(lipschitz))
      return getPartitionScale().map(Scalar::invert) //
          .multiply(RealScalar.of(getResolution()).multiply(Power.of(Log.function.apply(RealScalar.of(getResolution())), 2)));
    return getPartitionScale().map(Scalar::invert) //
        .multiply(Power.of(RealScalar.of(getResolution()), RealScalar.of(5).divide(RealScalar.of(Math.PI))));
    // TODO change to function depending on Lipschitz
  }
}