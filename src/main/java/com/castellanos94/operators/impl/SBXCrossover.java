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
    private RepairOperator<DoubleSolution> repair;

    @Override
    public ArrayList<DoubleSolution> execute(ArrayList<DoubleSolution> parents) {
        ArrayList<DoubleSolution> offspring = new ArrayList<>(2);

        try {
            offspring.add((DoubleSolution) parents.get(0).clone());
            offspring.add((DoubleSolution) parents.get(0).clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        int i;
        Double rand;
        Double y1, y2, lowerBound, upperBound;
        Double c1, c2;
        Double alpha, beta, betaq;
        Double valueX1, valueX2;
        DoubleSolution parent1 = parents.get(0);
        DoubleSolution parent2 = parents.get(1);
        if (Tools.getRandom().nextDouble() <= crossoverProbability) {
            for (i = 0; i < parent1.getVariables().size(); i++) {
                valueX1 = parent1.getVariable(i);
                valueX2 = parent2.getVariable(i);
                if (randomGenerator.nextDouble() <= 0.5) {
                    if (((Double) Math.abs(valueX1 - valueX2)).compareTo(EPS) > 0) {
                        if (valueX1.compareTo(valueX2) < 0) {
                            y1 = valueX1;
                            y2 = valueX2;
                        } else {
                            y1 = valueX2;
                            y2 = valueX1;
                        }

                        lowerBound = parent1.getLowerBound(i).doubleValue();
                        upperBound = parent1.getUpperBound(i).doubleValue();

                        rand = randomGenerator.nextDouble();
                        beta = 1.0 + (2.0 * (y1 - lowerBound) / (y2 - y1));
                        // beta = Data.getOneByType(lowerBound).plus(
                        // Data.getZeroByType(lowerBound).plus(2).times(y1.minus(lowerBound)).div(y2.minus(y1)));
                        alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));
                        // alpha =
                        // Data.getZeroByType(lowerBound).plus(2).minus(beta.pow(-(distributionIndex +
                        // 1.0)));
                        // beta.pow(Data.getOneByType(lowerBound).multiplication(-(distributionIndex -
                        // 1.0))));

                        if (rand.compareTo(1.0 / alpha) <= 0) {
                            betaq = Math.pow(rand * alpha, (1.0 / (distributionIndex + 1.0)));
                            // betaq = rand.times(alpha).pow(1.0 / (distributionIndex + 1));
                        } else {
                            betaq = Math.pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex + 1.0));
                            // betaq = RealData.ONE.div(RealData.ZERO.plus(2).minus(rand.times(alpha)))
                            // .pow(1.0 / (distributionIndex + 1));
                        }
                        c1 = 0.5 * (y1 + y2 - betaq * (y2 - y1));
                        // c1 = RealData.ONE.div(2).times(y1.plus(y2).minus(betaq.times(y2.minus(y1))));

                        beta = 1.0 + (2.0 * (upperBound - y2) / (y2 - y1));
                        // beta =
                        // RealData.ONE.plus(RealData.ZERO.plus(2).times(upperBound.minus(y2).div(y2.minus(y1))));
                        alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));
                        // alpha = RealData.ONE.plus(2).minus(beta.pow(-(distributionIndex + 1)));

                        if (rand <= (1.0 / alpha)) {
                            // if (rand.compareTo(RealData.ONE.div(alpha)) <= 0) {
                            betaq = Math.pow((rand * alpha), (1.0 / (distributionIndex + 1.0)));
                            // betaq = rand.times(alpha).pow(1.0 / (distributionIndex + 1));
                        } else {
                            betaq = Math.pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex + 1.0));
                            // betaq = RealData.ONE.div(RealData.ZERO.plus(2).minus(rand.times(alpha)))
                            // .pow(1.0 / (distributionIndex + 1));
                        }
                        c2 = 0.5 * (y1 + y2 + betaq * (y2 - y1));
                        // c2 = RealData.ONE.div(2).times(y1.plus(y2).plus(betaq.times(y2.minus(y1))));
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
            repair.execute(offspring.get(0));
            repair.execute(offspring.get(0));
        }

        return offspring;

    }

    public SBXCrossover(double distributionIndex, double crossoverProbability) {
        this.distributionIndex = distributionIndex;
        this.crossoverProbability = crossoverProbability;
        this.repair = new RepairBoundary();
    }

    public SBXCrossover(double distributionIndex) {
        this.distributionIndex = distributionIndex;
        this.crossoverProbability = 1.0;
    }

    public double getCrossoverProbability() {
        return crossoverProbability;
    }

    public double getDistributionIndex() {
        return distributionIndex;
    }

    public RepairOperator<DoubleSolution> getRepair() {
        return repair;
    }

    public void setRepair(RepairOperator<DoubleSolution> repair) {
        this.repair = repair;
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