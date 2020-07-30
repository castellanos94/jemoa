package com.castellanos94.operators.impl;

import java.util.Random;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

public class PolynomialMutation implements MutationOperator {
    private static final double DEFAULT_PROBABILITY = 0.01;
    private static final double DEFAULT_DISTRIBUTION_INDEX = 20.0;
    private Random randomGenerator = Tools.getRandom();
    private double distributionIndex;
    private double mutationProbability;

    public PolynomialMutation() {
        this.distributionIndex = DEFAULT_DISTRIBUTION_INDEX;
        this.mutationProbability = DEFAULT_PROBABILITY;
    }

    public PolynomialMutation(double distributionIndex, double mutationProbability) {
        this.distributionIndex = distributionIndex;
        this.mutationProbability = mutationProbability;
    }

    @Override
    public void execute(Solution solution) throws CloneNotSupportedException {
        Data rnd, delta1, delta2, deltaq;
        Data y, yl, yu, val, xy;
        double mutPow;

        for (int i = 0; i < solution.getDecision_vars().size(); i++) {
            if (randomGenerator.nextDouble() <= mutationProbability) {
                y = solution.getVariable(i);
                yl = solution.getLowerBound(i);
                yu = solution.getUpperBound(i);
                if (yl.compareTo(yu) == 0) {
                    y = yl;
                } else {
                    // delta1 = (y - yl) / (yu - yl);
                    delta1 = y.subtraction(yl).division(yu.subtraction(yl));
                    // delta2 = (yu - y) / (yu - yl);
                    delta2 = yu.subtraction(y).division(yu.subtraction(yl));
                    rnd = Data.getZeroByType(y).addition(randomGenerator.nextDouble());
                    mutPow = 1.0 / (distributionIndex + 1.0);
                    if (rnd.compareTo(0.5) <= 0) {
                        // xy = 1.0 - delta1;
                        xy = RealData.ONE.subtraction(delta1);
                        // val = 2.0 * rnd + (1.0 - 2.0 * rnd) * (Math.pow(xy, distributionIndex +
                        // 1.0));
                        Data value = RealData.ONE.subtraction(rnd.multiplication(2))
                                .multiplication(xy.pow(distributionIndex + 1.0));
                        val = RealData.ZERO.addition(2).addition(value);
                        // deltaq = Math.pow(val, mutPow) - 1.0;
                        deltaq = val.pow(mutPow).subtraction(RealData.ONE);
                    } else {
                        // xy = 1.0 - delta2;
                        xy = RealData.ONE.subtraction(delta2);
                        // val = 2.0 * (1.0 - rnd) + 2.0 * (rnd - 0.5) * (Math.pow(xy, distributionIndex
                        // + 1.0));
                        val = RealData.ZERO.addition(2).multiplication(RealData.ONE.subtraction(rnd))
                                .addition(RealData.ZERO.addition(2).multiplication(
                                        rnd.subtraction(0.5).multiplication(xy.pow(distributionIndex + 1.0))));

                        // deltaq = 1.0 - Math.pow(val, mutPow);
                        deltaq = RealData.ONE.subtraction(val.pow(mutPow));
                    }
                    // y = y + deltaq * (yu - yl);
                    y = y.addition(deltaq.multiplication(yu.subtraction(yl)));
                    // y = solutionRepair.repairSolutionVariableValue(y, yl, yu);
                }
                solution.setVariable(i, y);
            }
        }
    }

    public double getDistributionIndex() {
        return distributionIndex;
    }

    public double getMutationProbability() {
        return mutationProbability;
    }

    public void setDistributionIndex(double distributionIndex) {
        this.distributionIndex = distributionIndex;
    }

    public void setMutationProbability(double mutationProbability) {
        this.mutationProbability = mutationProbability;
    }

    @Override
    public String toString() {
        return "PolyMutation [distributionIndex=" + distributionIndex + ", mutationProbability=" + mutationProbability
                + "]";
    }

}