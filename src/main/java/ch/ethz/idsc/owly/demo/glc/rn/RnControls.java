// code by jph
package ch.ethz.idsc.owly.demo.glc.rn;

import ch.ethz.idsc.owly.glc.core.Controls;
import ch.ethz.idsc.owly.math.StateSpaceModel;
import ch.ethz.idsc.owly.math.StateSpaceModels;
import ch.ethz.idsc.owly.uni.adapter.IdentityStateSpaceModel;
import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Range;
import ch.ethz.idsc.tensor.sca.Chop;
import ch.ethz.idsc.tensor.sca.Cos;
import ch.ethz.idsc.tensor.sca.Sin;

enum RnControls {
  ;
  // ---
  public static Controls createR2RadialControls(final int num, Scalar amp) {
    StateSpaceModel stateSpaceModel = new IdentityStateSpaceModel();
    Controls controls = new Controls();
    for (Tensor angle : Range.of(0, num).multiply(DoubleScalar.of(2 * Math.PI / num))) {
      Tensor u = Chop.of(Tensors.of(Cos.of(angle), Sin.of(angle)).multiply(amp));
      controls.add(StateSpaceModels.createFlow(stateSpaceModel, u));
    }
    return controls;
  }
}
