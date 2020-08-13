package com.castellanos94.operators.impl;

import com.castellanos94.operators.MutationOperator;
import com.castellanos94.solutions.BinarySolution;
import com.castellanos94.utils.Tools;

public class BinaryMutation implements MutationOperator<BinarySolution> {
    protected Double probability;

    public BinaryMutation(Double probability) {
        this.probability = probability;
    }

    @Override
    public BinarySolution execute(BinarySolution solution) {
        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
            if (Tools.getRandom().nextDouble() <= probability) {
                solution.getVariable(0).flip(i);
            }
        }

        return solution;
    }

    public Double getProbability() {
        return probability;
    }

    @Override
    public String toString() {
        return "BinaryMutation [probability=" + probability + "]";
    }

}