// code by jph
package ch.ethz.idsc.owly.gui.ren;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.ethz.idsc.owly.gui.GeometricLayer;
import ch.ethz.idsc.owly.gui.GraphicsUtil;
import ch.ethz.idsc.owly.gui.RenderInterface;
import ch.ethz.idsc.owly.math.Se2Utils;
import ch.ethz.idsc.owly.math.region.ImageRegion;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.io.ImageFormat;
import ch.ethz.idsc.tensor.mat.DiagonalMatrix;
import ch.ethz.idsc.tensor.mat.IdentityMatrix;

public class ImageRegionRender implements RenderInterface {
  private static final Scalar TFF = RealScalar.of(255);
  private static final Scalar OBS = RealScalar.of(230);
  // ---
  private final BufferedImage bufferedImage;
  private final Tensor scale;

  public ImageRegionRender(ImageRegion imageRegion) {
    Tensor image = imageRegion.image();
    bufferedImage = ImageFormat.of(image.map(ImageRegionRender::color));
    this.scale = imageRegion.scale();
  }

  private static Scalar color(Scalar scalar) {
    if (Scalars.isZero(scalar))
      return TFF;
    return OBS;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor model2pixel = geometricLayer.getMatrix();
    // TODO compute once:
    Tensor image = DiagonalMatrix.of( //
        +1 / scale.Get(0).number().doubleValue(), //
        -1 / scale.Get(1).number().doubleValue(), 1);
    Tensor translate = IdentityMatrix.of(3);
    translate.set(RealScalar.of(0), 0, 2); // pixel of rear axle
    translate.set(RealScalar.of(-bufferedImage.getHeight()), 1, 2); // image width/2
    GraphicsUtil.setQualityHigh(graphics);
    Tensor m = model2pixel.dot(image).dot(translate);
    graphics.drawImage(bufferedImage, Se2Utils.toAffineTransform(m), null);
    GraphicsUtil.setQualityDefault(graphics);
  }
}
