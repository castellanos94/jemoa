package com.castellanos94.operators.impl;

import java.util.ArrayList;
import java.util.Random;

import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.Tools;

public class SBXCrossover implements CrossoverOperator<DoubleSolution> {
  private static final double EPS = 1.0e-14;
  private Random randomGenerator = Tools.getRandom();
  private double distributionIndex;
  private double crossoverProbability;
  private RepairOperator<DoubleSolution> solutionRepair;

  /**
   * 
   * @param distributionIndex
   * @param crossoverProbability
   */
  public SBXCrossover(double distributionIndex, double crossoverProbability) {
    this.distributionIndex = distributionIndex;
    this.crossoverProbability = crossoverProbability;
    this.solutionRepair = new RepairBoundary();
  }

  public SBXCrossover(double distributionIndex) {
    this.distributionIndex = distributionIndex;
    this.crossoverProbability = 1.0;
  }

  /**
   * distributionIndex : 30 & crossoverProbability : 1.0
   */
  public SBXCrossover() {
    this(30, 1.0);
  }

  @Override
  public ArrayList<DoubleSolution> execute(ArrayList<DoubleSolution> parents) {
    ArrayList<DoubleSolution> offspring = new ArrayList<>(2);

    offspring.add(parents.get(0).copy());
    offspring.add(parents.get(0).copy());

    int i;
    double rand;
    double y1, y2, lowerBound, upperBound;
    double c1, c2;
    double alpha, beta, betaq;
    double valueX1, valueX2;

    if (randomGenerator.nextDouble() <= crossoverProbability) {
      for (i = 0; i < parents.get(0).getNumberOfVariables(); i++) {
        valueX1 = parents.get(0).getVariable(i);
        valueX2 = parents.get(1).getVariable(i);
        if (randomGenerator.nextDouble() <= 0.5) {
          if (Math.abs(valueX1 - valueX2) > EPS) {
            if (valueX1 < valueX2) {
              y1 = valueX1;
              y2 = valueX2;
            } else {
              y1 = valueX2;
              y2 = valueX1;
            }

            lowerBound = parents.get(0).getLowerBound(i).doubleValue();
            upperBound = parents.get(1).getUpperBound(i).doubleValue();

            rand = randomGenerator.nextDouble();
            beta = 1.0 + (2.0 * (y1 - lowerBound) / (y2 - y1));
            alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));

            if (rand <= (1.0 / alpha)) {
              betaq = Math.pow(rand * alpha, (1.0 / (distributionIndex + 1.0)));
            } else {
              betaq = Math.pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex + 1.0));
            }
            c1 = 0.5 * (y1 + y2 - betaq * (y2 - y1));

            beta = 1.0 + (2.0 * (upperBound - y2) / (y2 - y1));
            alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));

            if (rand <= (1.0 / alpha)) {
              betaq = Math.pow((rand * alpha), (1.0 / (distributionIndex + 1.0)));
            } else {
              betaq = Math.pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex + 1.0));
            }
            c2 = 0.5 * (y1 + y2 + betaq * (y2 - y1));

            if (randomGenerator.nextDouble() <= 0.5) {
              offspring.get(0).setVariable(i, c2);
              offspring.get(1).setVariable(i, c1);
            } else {
              offspring.get(0).setVariable(i, c1);
              offspring.get(1).setVariable(i, c2);
            }
          } else {
            offspring.get(0).setVariable(i, valueX1);
            offspring.get(1).setVariable(i, valueX2);
          }
        } else {
          offspring.get(0).setVariable(i, valueX2);
          offspring.get(1).setVariable(i, valueX1);
        }
      }
      solutionRepair.execute(offspring.get(0));
      solutionRepair.execute(offspring.get(1));
    }

    return offspring;

  }

  public double getCrossoverProbability() {
    return crossoverProbability;
  }

  public double getDistributionIndex() {
    return distributionIndex;
  }

  public RepairOperator<DoubleSolution> getRepair() {
    return solutionRepair;
  }

  public void setRepair(RepairOperator<DoubleSolution> repair) {
    this.solutionRepair = repair;
  }

  public void setCrossoverProbability(double crossoverProbability) {
    this.crossoverProbability = crossoverProbability;
  }

  public void setDistributionIndex(double distributionIndex) {
    this.distributionIndex = distributionIndex;
  }

  @Override
  public String toString() {
    return "SBXCrossover [crossoverProbability=" + crossoverProbability + ", distributionIndex=" + distributionIndex
        + "]";
  }

  @Override
  public int getNumberOfRequiredParents() {
    return 2;
  }

  @Override
  public int getNumberOfGeneratedChildren() {
    return 2;
  }

}