package com.castellanos94.operators.impl;

import java.util.ArrayList;
import java.util.Random;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

public class SBXCrossover implements CrossoverOperator {
    private static final double EPS = 1.0e-14;
    private Random randomGenerator = Tools.getRandom();
    private double distributionIndex;
    private double crossoverProbability;

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
                    if (valueX1.subtraction(valueX2).abs().compareTo(EPS) > 0) {
                        if (valueX1.compareTo(valueX2) < 0) {
                            y1 = valueX1;
                            y2 = valueX2;
                        } else {
                            y1 = valueX2;
                            y2 = valueX1;
                        }

                        lowerBound = parent1.getLowerBound(i);
                        upperBound = parent1.getUpperBound(i);

                        rand = Data.getZeroByType(lowerBound).addition(randomGenerator.nextDouble());
                        // beta = 1.0 + (2.0 * (y1 - lowerBound) / (y2 - y1));
                        beta = Data.getOneByType(lowerBound).addition(Data.getOneByType(lowerBound).addition(2)
                                .multiplication(y1.subtraction(lowerBound)).division(y2.subtraction(y1)));
                        // alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));
                        alpha = Data.getOneByType(lowerBound).addition(2).subtraction(
                                beta.pow(Data.getOneByType(lowerBound).multiplication(-(distributionIndex - 1.0))));

                        if (rand.compareTo(RealData.ONE.division(alpha)) <= 0) {
                            // betaq = Math.pow(rand * alpha, (1.0 / (distributionIndex + 1.0)));
                            betaq = rand.multiplication(alpha).pow(1.0 / (distributionIndex + 1));
                        } else {
                            // betaq = Math.pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex +
                            // 1.0));
                            betaq = RealData.ONE
                                    .division(RealData.ONE.addition(2).subtraction(rand.multiplication(alpha)))
                                    .pow(1.0 / (distributionIndex + 1));
                        }
                        // c1 = 0.5 * (y1 + y2 - betaq * (y2 - y1));
                        c1 = RealData.ONE.division(2)
                                .multiplication(y1.addition(y2).subtraction(betaq.multiplication(y2.subtraction(y1))));

                        // beta = 1.0 + (2.0 * (upperBound - y2) / (y2 - y1));
                        beta = RealData.ONE.addition(RealData.ONE.addition(2)
                                .multiplication(upperBound.subtraction(y2).division(y2.subtraction(y1))));
                        // alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));
                        alpha = RealData.ONE.addition(2).subtraction(beta.pow(-(distributionIndex + 1)));

                        // if (rand <= (1.0 / alpha)) {
                        if (rand.compareTo(RealData.ONE.division(alpha)) <= 0) {
                            // betaq = Math.pow((rand * alpha), (1.0 / (distributionIndex + 1.0)));
                            betaq = rand.multiplication(alpha).pow(1.0 / (distributionIndex + 1));
                        } else {
                            // betaq = Math.pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex +
                            // 1.0));
                            betaq = RealData.ONE
                                    .division(RealData.ONE.addition(2).subtraction(rand.multiplication(alpha)))
                                    .pow(1.0 / (distributionIndex + 1));
                        }
                        // c2 = 0.5 * (y1 + y2 + betaq * (y2 - y1));
                        c2 = RealData.ONE.division(2)
                                .multiplication(y1.addition(y2).addition(betaq.multiplication(y2.subtraction(y1))));
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
        }
        return offspring;

    }

    public SBXCrossover(double distributionIndex, double crossoverProbability) {
        this.distributionIndex = distributionIndex;
        this.crossoverProbability = crossoverProbability;
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