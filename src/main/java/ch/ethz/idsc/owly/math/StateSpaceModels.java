// code by jph
package ch.ethz.idsc.owly.math;

import ch.ethz.idsc.tensor.Tensor;

/** utility functions related to {@link StateSpaceModel} */
public enum StateSpaceModels {
  ;
  // ---
  /** @param stateSpaceModel
   * @param u
   * @return flow defined by stateSpaceModel using control parameter u */
  public static Flow createFlow(StateSpaceModel stateSpaceModel, Tensor u) {
    return new Flow() {
      @Override
      public final Tensor at(Tensor x) {
        return stateSpaceModel.createFlow(x, u);
      }

      @Override
      public final Tensor getU() {
        return u;
      }
    };
  }
}
