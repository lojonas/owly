package ch.ethz.idsc.owly.glc.adapter;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.idsc.owly.data.Stopwatch;
import ch.ethz.idsc.owly.demo.util.UserHome;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class RunCompare {
  private Tensor currentRuntimes;
  private Tensor currentIterations;
  private Tensor currentCosts;
  private final int numberOfPlanners;
  private final Path path = UserHome.file("Comparison.csv").toPath();
  private List<String> lines = new ArrayList<String>();
  private Stopwatch stopwatch;
  private int currentPlannerID = -1;

  public RunCompare(int numberOfPlanners) {
    this.numberOfPlanners = numberOfPlanners;
    String reference = "timeReference, iterationsReference, CostReference";
    String firstLine = "";
    for (int i = 1; i < numberOfPlanners; i++) {
      firstLine = String.join(",", firstLine, //
          String.join("", "absolute Time Difference to Ref of run ", Integer.toString(i + 1)), //
          String.join("", "relative Time Difference to Ref of run ", Integer.toString(i + 1)), //
          String.join("", "absolut iteration Difference to Ref of run ", Integer.toString(i + 1)), //
          String.join("", "relative iteration Difference to Ref of run", Integer.toString(i + 1)), //
          String.join("", "cost Difference to Ref of run ", Integer.toString(i + 1)), //
          String.join("", "relative cost Difference to Ref of run ", Integer.toString(i + 1)));
    }
    newRuns();
    lines.add(String.join("", reference, firstLine));
  }

  /** Resets the recorded data to -1 */
  public void newRuns() {
    List<Integer> list = new ArrayList<>();
    for (int i = 0; i < numberOfPlanners; i++)
      list.add(-1);
    currentRuntimes = Tensors.vector(list);
    currentIterations = Tensors.vector(list);
    currentCosts = Tensors.vector(list);
  }

  /** starts the stopwatch for planner with number:
   * @param plannerID */
  public void startStopwatchFor(int plannerID) {
    if (plannerID > numberOfPlanners || plannerID < 0)
      throw new RuntimeException();
    currentPlannerID = plannerID;
    stopwatch = Stopwatch.started();
  }

  /** Stops and records the data from the stopwatch for planner with number:
   * @param plannerID
   * @return the recorded time */
  public double stopStopwatchFor(int plannerID) {
    if (plannerID != currentPlannerID || plannerID < 0)
      throw new RuntimeException();
    stopwatch.stop();
    currentPlannerID = -1;
    currentRuntimes.set(RealScalar.of(stopwatch.display_seconds()), plannerID);
    return stopwatch.display_seconds();
  }

  public void saveIterations(int iterations, int plannerID) {
    if (plannerID > numberOfPlanners)
      throw new RuntimeException();
    currentIterations.set(RealScalar.of(iterations), plannerID);
  }

  public void saveCost(Scalar cost, int plannerID) {
    if (plannerID > numberOfPlanners)
      throw new RuntimeException();
    currentCosts.set(cost, plannerID);
  }

  public void write2lines() {
    for (int i = 0; i < numberOfPlanners; i++) {
      if (Scalars.lessThan(currentRuntimes.Get(i), RealScalar.ZERO) || //
          Scalars.lessThan(currentIterations.Get(i), RealScalar.ZERO) || //
          Scalars.lessThan(currentCosts.Get(i), RealScalar.ZERO))
        throw new RuntimeException();
    }
    String referenceData = String.join(", ", //
        currentRuntimes.Get(0).toString(), //
        currentIterations.Get(0).toString(), //
        currentCosts.Get(0).toString());
    String compareData = "";
    Tensor currentRunTimeDiff = Tensor.of(currentRuntimes.extract(1, numberOfPlanners).stream()//
        .map(Scalar.class::cast).map(s -> s.subtract(currentRuntimes.Get(0))));
    Tensor currentRunTimeRelative = Tensor.of(currentRunTimeDiff.stream()//
        .map(Scalar.class::cast).map(s -> s.divide(currentRuntimes.Get(0))));
    Tensor currentIterationsDiff = Tensor.of(currentIterations.extract(1, numberOfPlanners).stream()//
        .map(Scalar.class::cast).map(s -> s.subtract(currentIterations.Get(0))));
    Tensor currentIterationsRelative = Tensor.of(currentIterationsDiff.stream()//
        .map(Scalar.class::cast).map(s -> s.divide(currentIterations.Get(0))));
    Tensor currentCostDiff = Tensor.of(currentCosts.extract(1, numberOfPlanners).stream()//
        .map(Scalar.class::cast).map(s -> s.subtract(currentCosts.Get(0))));
    Tensor currentCostRelative = Tensor.of(currentCostDiff.stream()//
        .map(Scalar.class::cast).map(s -> s.divide(currentCosts.Get(0))));
    for (int i = 0; i < numberOfPlanners - 1; i++)
      compareData = String.join(",", compareData, //
          currentRunTimeDiff.Get(i).toString(), //
          currentRunTimeRelative.Get(i).toString(), //
          currentIterationsDiff.Get(i).toString(), //
          currentIterationsRelative.Get(i).toString(), //
          currentCostDiff.Get(i).toString(), //
          currentCostRelative.Get(i).toString());
    lines.add(String.join("", referenceData, compareData));
  }

  public void write2File() throws Exception {
    Files.write(path, lines, Charset.forName("UTF-8"));
  }
}
