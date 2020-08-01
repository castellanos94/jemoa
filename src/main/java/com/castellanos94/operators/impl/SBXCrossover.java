package com.castellanos94.operators.impl;

import java.util.ArrayList;
import java.util.Random;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

public class SBXCrossover implements CrossoverOperator {
    private static final double EPS = 1.0e-14;
    private Random randomGenerator = Tools.getRandom();
    private double distributionIndex;
    private double crossoverProbability;
    private RepairOperator repair;

    @Override
    public ArrayList<Solution> execute(ArrayList<Solution> parents) throws CloneNotSupportedException {
        ArrayList<Solution> offspring = new ArrayList<Solution>(2);

        offspring.add((Solution) parents.get(0).clone());
        offspring.add((Solution) parents.get(0).clone());

        int i;
        Data rand;
        Data y1, y2, lowerBound, upperBound;
        Data c1, c2;
        Data alpha, beta, betaq;
        Data valueX1, valueX2;
        Solution parent1 = parents.get(0);
        Solution parent2 = parents.get(1);
        if (Tools.getRandom().nextDouble() <= crossoverProbability) {
            for (i = 0; i < parent1.getDecision_vars().size(); i++) {
                valueX1 = parent1.getVariable(i);
                valueX2 = parent2.getVariable(i);
                if (randomGenerator.nextDouble() <= 0.5) {
                    if (valueX1.minus(valueX2).abs().compareTo(EPS) > 0) {
                        if (valueX1.compareTo(valueX2) < 0) {
                            y1 = valueX1;
                            y2 = valueX2;
                        } else {
                            y1 = valueX2;
                            y2 = valueX1;
                        }

                        lowerBound = parent1.getLowerBound(i);
                        upperBound = parent1.getUpperBound(i);

                        rand = Data.getZeroByType(lowerBound).plus(randomGenerator.nextDouble());
                        // beta = 1.0 + (2.0 * (y1 - lowerBound) / (y2 - y1));
                        beta = Data.getOneByType(lowerBound).plus(Data.getZeroByType(lowerBound).plus(2)
                                .times(y1.minus(lowerBound)).div(y2.minus(y1)));
                        // alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));
                        alpha = Data.getZeroByType(lowerBound).plus(2)
                                .minus(beta.pow(-(distributionIndex + 1.0)));
                        // beta.pow(Data.getOneByType(lowerBound).multiplication(-(distributionIndex -
                        // 1.0))));

                        if (rand.compareTo(RealData.ONE.div(alpha)) <= 0) {
                            // betaq = Math.pow(rand * alpha, (1.0 / (distributionIndex + 1.0)));
                            betaq = rand.times(alpha).pow(1.0 / (distributionIndex + 1));
                        } else {
                            // betaq = Math.pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex +
                            // 1.0));
                            betaq = RealData.ONE
                                    .div(RealData.ZERO.plus(2).minus(rand.times(alpha)))
                                    .pow(1.0 / (distributionIndex + 1));
                        }
                        // c1 = 0.5 * (y1 + y2 - betaq * (y2 - y1));
                        c1 = RealData.ONE.div(2)
                                .times(y1.plus(y2).minus(betaq.times(y2.minus(y1))));

                        // beta = 1.0 + (2.0 * (upperBound - y2) / (y2 - y1));
                        beta = RealData.ONE.plus(RealData.ZERO.plus(2)
                                .times(upperBound.minus(y2).div(y2.minus(y1))));
                        // alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));
                        alpha = RealData.ONE.plus(2).minus(beta.pow(-(distributionIndex + 1)));

                        // if (rand <= (1.0 / alpha)) {
                        if (rand.compareTo(RealData.ONE.div(alpha)) <= 0) {
                            // betaq = Math.pow((rand * alpha), (1.0 / (distributionIndex + 1.0)));
                            betaq = rand.times(alpha).pow(1.0 / (distributionIndex + 1));
                        } else {
                            // betaq = Math.pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex +
                            // 1.0));
                            betaq = RealData.ONE
                                    .div(RealData.ZERO.plus(2).minus(rand.times(alpha)))
                                    .pow(1.0 / (distributionIndex + 1));
                        }
                        // c2 = 0.5 * (y1 + y2 + betaq * (y2 - y1));
                        c2 = RealData.ONE.div(2)
                                .times(y1.plus(y2).plus(betaq.times(y2.minus(y1))));
                        if (randomGenerator.nextDouble() <= 0.5) {
                            offspring.get(0).setDecisionVar(i, c2);
                            offspring.get(1).setDecisionVar(i, c1);
                        } else {
                            offspring.get(0).setDecisionVar(i, c1);
                            offspring.get(1).setDecisionVar(i, c2);
                        }
                    } else {
                        offspring.get(0).setDecisionVar(i, valueX1);
                        offspring.get(1).setDecisionVar(i, valueX2);
                    }
                } else {
                    offspring.get(0).setDecisionVar(i, valueX2);
                    offspring.get(1).setDecisionVar(i, valueX1);
                }
            }
            repair.repair(offspring.get(0));
            repair.repair(offspring.get(0));
        }

        return offspring;

    }

    public SBXCrossover(double distributionIndex, double crossoverProbability) {
        this.distributionIndex = distributionIndex;
        this.crossoverProbability = crossoverProbability;
        this.repair = new RepairRandomBoundary();
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

    public RepairOperator getRepair() {
        return repair;
    }

    public void setRepair(RepairOperator repair) {
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

}