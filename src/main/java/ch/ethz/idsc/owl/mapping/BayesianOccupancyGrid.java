// code by ynager
package ch.ethz.idsc.owl.mapping;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.HashSet;

import ch.ethz.idsc.owl.data.GlobalAssert;
import ch.ethz.idsc.owl.gui.AffineTransforms;
import ch.ethz.idsc.owl.gui.GeometricLayer;
import ch.ethz.idsc.owl.gui.RenderInterface;
import ch.ethz.idsc.owl.math.map.Se2Utils;
import ch.ethz.idsc.owl.math.region.Region;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.mat.DiagonalMatrix;
import ch.ethz.idsc.tensor.mat.IdentityMatrix;
import ch.ethz.idsc.tensor.sca.Ceiling;
import ch.ethz.idsc.tensor.sca.Floor;
import ch.ethz.idsc.tensor.sca.Sign;

/** all pixels have the same amount of weight or clearance radius attached */
public class BayesianOccupancyGrid implements Region<Tensor>, RenderInterface {
  // private static final Set<Byte> VALUES = new HashSet<>();
  private static final short MASK_OCCUPIED = 0x00;
  private static final short MASK_UNKNOWN = 0xDD;
  @SuppressWarnings("unused")
  private static final short MASK_EMPTY = 0xFF;
  // ---
  private Tensor lbounds;
  private final Scalar cellDim; // [m] per cell
  private final Tensor cellDimHalfVec;
  private final Scalar cellDimInv; // cells per [m]
  private final Tensor gridSize; // grid size in pixels
  private final int dimx;
  private final int dimy;
  // ---
  // lidar2grid = gworld2gpix * world2gworld * gokart2world * lidar2gokart
  private final Tensor grid2cell; // from grid frame to grid cell
  private Tensor world2grid = IdentityMatrix.of(3); // from world frame to grid frame
  private Tensor gokart2world = IdentityMatrix.of(3); // from gokart frame to world frame
  private final static Tensor LIDAR2GOKART = IdentityMatrix.of(3); // from lidar frame to gokart frame
  private final GeometricLayer lidar2cellLayer;
  private final GeometricLayer world2cellLayer;
  @SuppressWarnings("unused")
  private double poseQuality;
  // ---
  /** array containing current log odds of each cell */
  private double[] logOdds;
  /** maximum likelihood obstacle map */
  private final BufferedImage obstacleImage;
  private final byte[] imagePixels;
  private final Graphics2D imageGraphics;
  /** set of occupied cells */
  private final HashSet<Tensor> hset = new HashSet<>();
  // ---
  /** prior */
  private static final double P_M = 0.5; // prior
  private static final double L_M_INV = pToLogOdd(1 - P_M);
  /** inv sensor model p(m|z) */
  private static final double P_M_HIT = 0.85;
  /** cells with p(m|z_1:t) > probThreshold are considered occupied */
  private static final double P_THRESH = 0.5;
  private static final double L_THRESH = pToLogOdd(P_THRESH);
  // ---
  private Scalar obsDilationRadius;
  private final Tensor scaling;
  private final double[] PREDEFINED_P;

  /** Returns an instance of BayesianOccupancyGrid whose grid dimensions are ceiled to
   * fit a whole number of cells per dimension
   * @param lbounds vector of length 2
   * @param range effective size of grid in coordinate space
   * @param cellDim dimension of cell in [m]
   * @return BayesianOccupancyGrid */
  public static BayesianOccupancyGrid of(Tensor lbounds, Tensor range, Scalar cellDim) {
    Tensor sizeCeil = Ceiling.of(range.divide(Sign.requirePositive(cellDim)));
    Tensor rangeCeil = sizeCeil.multiply(cellDim);
    return new BayesianOccupancyGrid(lbounds, rangeCeil, sizeCeil);
  }

  /** @param lbounds vector of length 2
   * @param range effective size of grid in coordinate space
   * @param size size of grid in cell space */
  public BayesianOccupancyGrid(Tensor lbounds, Tensor range, Tensor size) {
    // VectorQ.requireLength(lbounds, 2);
    // VectorQ.requireLength(range, 2);
    // VectorQ.requireLength(size, 2);
    System.out.print("Grid range: " + range + "\n");
    System.out.print("Grid size: " + size + "\n");
    this.lbounds = lbounds;
    gridSize = size;
    dimx = size.Get(0).number().intValue();
    dimy = size.Get(1).number().intValue();
    cellDim = range.Get(0).divide(gridSize.Get(0));
    cellDimInv = cellDim.reciprocal();
    cellDimHalfVec = Tensors.of(cellDim.divide(RealScalar.of(2)), //
        cellDim.divide(RealScalar.of(2)));
    scaling = DiagonalMatrix.of( //
        cellDim.number().doubleValue(), //
        cellDim.number().doubleValue(), 1);
    // ---
    obstacleImage = new BufferedImage( //
        gridSize.Get(0).number().intValue(), //
        gridSize.Get(1).number().intValue(), //
        BufferedImage.TYPE_BYTE_GRAY);
    WritableRaster writableRaster = obstacleImage.getRaster();
    DataBufferByte dataBufferByte = (DataBufferByte) writableRaster.getDataBuffer();
    imagePixels = dataBufferByte.getData();
    imageGraphics = obstacleImage.createGraphics();
    imageGraphics.setColor(new Color(MASK_UNKNOWN, MASK_UNKNOWN, MASK_UNKNOWN));
    imageGraphics.fillRect(0, 0, obstacleImage.getWidth(), obstacleImage.getHeight());
    obsDilationRadius = cellDim.divide(RealScalar.of(2));
    // ---
    PREDEFINED_P = new double[] { 1 - P_M_HIT, P_M_HIT };
    logOdds = new double[dimx * dimy];
    Arrays.fill(logOdds, pToLogOdd(P_M));
    // ---
    double cellDimInvD = cellDimInv.number().doubleValue();
    double lboundX = lbounds.Get(0).number().doubleValue();
    double lboundY = lbounds.Get(1).number().doubleValue();
    grid2cell = Tensors.matrixDouble( //
        new double[][] { { cellDimInvD, 0, 0 }, { 0, cellDimInvD, 0 }, { 0, 0, 1 } });
    world2grid = Tensors.matrixDouble( //
        new double[][] { { 1, 0, -lboundX }, { 0, 1, -lboundY }, { 0, 0, 1 } });
    //  ---
    lidar2cellLayer = new GeometricLayer(grid2cell, Tensors.vector(0, 0, 0)); // grid 2 cell
    lidar2cellLayer.pushMatrix(world2grid); // world to grid
    lidar2cellLayer.pushMatrix(gokart2world); // gokart to world
    lidar2cellLayer.pushMatrix(LIDAR2GOKART); // lidar to gokart
    world2cellLayer = new GeometricLayer(grid2cell, Tensors.vector(0, 0, 0));
    world2cellLayer.pushMatrix(world2grid);
  }

  /** process a new lidar observation and update the occupancy map
   * @param pos 2D position of new lidar observation in gokart coordinates
   * @param type of observation */
  public void processObservation(Tensor pos, int type) {
    Tensor cell = lidarToCell(pos);
    int pix = cell.Get(0).number().intValue();
    if (0 <= pix && pix < dimx) {
      int piy = cell.Get(1).number().intValue();
      if (0 <= piy && piy < dimy) {
        double p_m_z = PREDEFINED_P[type];
        double logOddPrev = logOdds[piy * dimx + pix];
        updateCellLogOdd(pix, piy, p_m_z);
        double logOdd = logOdds[piy * dimx + pix];
        // Max likelihood estimation
        if ((L_THRESH < logOdd) && (logOddPrev <= L_THRESH))
          hset.add(cell);
        else if ((logOdd < L_THRESH) && (L_THRESH <= logOddPrev))
          hset.remove(cell);
      }
    }
  }

  /** set vehicle pose w.r.t world frame */
  public void setPose(Tensor pose, Scalar quality) {
    poseQuality = quality.number().doubleValue();
    gokart2world = Se2Utils.toSE2Matrix(pose);
    lidar2cellLayer.popMatrix();
    lidar2cellLayer.popMatrix();
    lidar2cellLayer.pushMatrix(gokart2world);
    lidar2cellLayer.pushMatrix(LIDAR2GOKART);
  }

  public void genObstacleMap() {
    Graphics graphics = obstacleImage.getGraphics();
    graphics.setColor(new Color(MASK_UNKNOWN, MASK_UNKNOWN, MASK_UNKNOWN));
    graphics.fillRect(0, 0, obstacleImage.getWidth(), obstacleImage.getHeight());
    for (Tensor cell : hset) {
      drawSphere(cell, obsDilationRadius, MASK_OCCUPIED);
    }
  }

  /** cells within this radius of an occupied cell will also be labeled as occupied.
   * If not set or below cellDim, only the occupied cell is labeled as an obstacle
   * @param radius */
  public void setObstacleRadius(Scalar radius) {
    obsDilationRadius = radius;
  }

  /** Updates the grid center. Grid range and size remain unchanged.
   * Overlapping segment is copied.
   * 
   * @param state center of new grid */
  public void setNewlBound(Tensor lbounds) {
    this.lbounds = lbounds;
    double lboundX = lbounds.Get(0).number().doubleValue();
    double lboundY = lbounds.Get(1).number().doubleValue();
    world2grid = Tensors.matrixDouble(new double[][] { { 1, 0, -lboundX }, { 0, 1, -lboundY }, { 0, 0, 1 } });
    // ---
    hset.clear();
    lidar2cellLayer.popMatrix();
    lidar2cellLayer.popMatrix();
    lidar2cellLayer.popMatrix();
    lidar2cellLayer.pushMatrix(world2grid); // updated world to grid
    double[] logOddsNew = new double[dimx * dimy];
    Arrays.fill(logOddsNew, pToLogOdd(P_M));
    double threshold = L_THRESH;
    Tensor trans = lidarToCell(toPos(Tensors.vector(0, 0))); // calculate translation
    for (int i = 0; i < dimx; i++)
      for (int j = 0; j < dimy; j++) {
        double logOdd = logOdds[j * dimx + i];
        int pix = i + trans.Get(0).number().intValue();
        if (0 <= pix && pix < dimx) {
          int piy = j + trans.Get(1).number().intValue();
          if (0 <= piy && piy < dimy) {
            logOddsNew[piy * dimx + pix] = logOdd;
            if (logOdd > threshold)
              hset.add(Tensors.vector(pix, piy));
          }
        }
      }
    logOdds = logOddsNew;
    lidar2cellLayer.pushMatrix(gokart2world); // gokart to world
    lidar2cellLayer.pushMatrix(LIDAR2GOKART); // lidar to gokart
  }

  /** Update the log odds of a cell using the probability of occupation given a new observation.
   * l_t = l_{t-1} + log[ p(m|z_t) / (1 - p(m|z_t)) ] + log[ (1-p(m)) / p(m) ]
   * @param idx of cell to be updated
   * @param p_m_z probability in [0,1] that Cell is occupied given the current observation z */
  private void updateCellLogOdd(int pix, int piy, double p_m_z) {
    double logOddDelta = pToLogOdd(p_m_z) + L_M_INV;
    int idx = piy * dimx + pix;
    logOdds[idx] += logOddDelta;
    if (Double.isInfinite(logOdds[idx]))
      throw new ArithmeticException("Overflow");
  }

  private static double pToLogOdd(double p) {
    GlobalAssert.that((1 - p) > 0);
    return Math.log(p / (1 - p));
  }

  private Tensor lidarToCell(Tensor pos) {
    Point2D point2D = lidar2cellLayer.toPoint2D(pos);
    Tensor point = Tensors.vector(point2D.getX(), point2D.getY());
    return Floor.of(point);
  }

  private Tensor globalToCell(Tensor pos) {
    Point2D point2D = world2cellLayer.toPoint2D(pos);
    Tensor point = Tensors.vector(point2D.getX(), point2D.getY());
    return Floor.of(point);
  }

  private Tensor toPos(Tensor cell) {
    return cell.multiply(cellDim).add(cellDimHalfVec);
  }

  private int cellToIdx(Tensor cell) {
    return cell.Get(1).multiply(gridSize.Get(0)).add(cell.Get(0)).number().intValue();
  }

  private void drawCell(Tensor cell, short grayScale) {
    int idx = cellToIdx(cell);
    if (idx < imagePixels.length)
      imagePixels[idx] = (byte) (grayScale & 0xFF);
  }

  private void drawSphere(Tensor cell, Scalar radius, short grayScale) {
    if (Scalars.lessEquals(obsDilationRadius, cellDim)) {
      drawCell(cell, grayScale);
      return;
    }
    Tensor pos = toPos(cell);
    Scalar radiusScaled = radius.multiply(cellDimInv);
    double dim = radiusScaled.number().doubleValue();
    Ellipse2D sphere = new Ellipse2D.Double( //
        pos.Get(0).multiply(cellDimInv).subtract(radiusScaled).number().doubleValue(), //
        pos.Get(1).multiply(cellDimInv).subtract(radiusScaled).number().doubleValue(), //
        2 * dim, 2 * dim);
    imageGraphics.setColor(new Color(grayScale, grayScale, grayScale));
    imageGraphics.fill(sphere);
  }

  @Override // from Region<Tensor>
  public boolean isMember(Tensor state) {
    Tensor cell = globalToCell(state);
    int pix = cell.Get(0).number().intValue();
    if (0 <= pix && pix < dimx) {
      int piy = cell.Get(1).number().intValue();
      if (0 <= piy && piy < dimy) {
        byte gs = imagePixels[cellToIdx(cell)];
        return gs == MASK_OCCUPIED;
      }
    }
    return true;
  }

  @Override // from Renderinterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor model2pixel = geometricLayer.getMatrix();
    Tensor translate = IdentityMatrix.of(3);
    translate.set(lbounds.get(0).multiply(cellDimInv), 0, 2);
    translate.set(lbounds.get(1).multiply(cellDimInv), 1, 2);
    Tensor matrix = model2pixel.dot(scaling).dot(translate);
    graphics.drawImage(obstacleImage, AffineTransforms.toAffineTransform(matrix), null);
  }
}
