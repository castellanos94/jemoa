package com.castellanos94.operators.impl;

import java.util.Random;

import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.Tools;

public class PolynomialMutation implements MutationOperator<DoubleSolution> {
    private static final double DEFAULT_PROBABILITY = 0.01;
    private static final double DEFAULT_DISTRIBUTION_INDEX = 20.0;
    private Random randomGenerator = Tools.getRandom();
    private double distributionIndex;
    private double mutationProbability;
    private RepairOperator<DoubleSolution> solutionRepair;

    public PolynomialMutation() {
        this.distributionIndex = DEFAULT_DISTRIBUTION_INDEX;
        this.mutationProbability = DEFAULT_PROBABILITY;
        this.solutionRepair = new RepairBoundary();
    }

    public PolynomialMutation(double distributionIndex, double mutationProbability) {
        this.distributionIndex = distributionIndex;
        this.mutationProbability = mutationProbability;
        this.solutionRepair = new RepairBoundary();
    }

    @Override
    public DoubleSolution execute(DoubleSolution solution) {
        double rnd, delta1, delta2, mutPow, deltaq;
        double y, yl, yu, val, xy;

        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
            if (randomGenerator.nextDouble() <= mutationProbability) {
                y = solution.getVariable(i);
                yl = solution.getLowerBound(i).doubleValue();
                yu = solution.getUpperBound(i).doubleValue();
                if (yl == yu) {
                    y = yl;
                } else {
                    delta1 = (y - yl) / (yu - yl);
                    delta2 = (yu - y) / (yu - yl);
                    rnd = randomGenerator.nextDouble();
                    mutPow = 1.0 / (distributionIndex + 1.0);
                    if (rnd <= 0.5) {
                        xy = 1.0 - delta1;
                        val = 2.0 * rnd + (1.0 - 2.0 * rnd) * (Math.pow(xy, distributionIndex + 1.0));
                        deltaq = Math.pow(val, mutPow) - 1.0;
                    } else {
                        xy = 1.0 - delta2;
                        val = 2.0 * (1.0 - rnd) + 2.0 * (rnd - 0.5) * (Math.pow(xy, distributionIndex + 1.0));
                        deltaq = 1.0 - Math.pow(val, mutPow);
                    }
                    y = y + deltaq * (yu - yl);
                }
                solution.setVariable(i, y);
            }
        }
        solutionRepair.execute(solution);
        return solution;
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