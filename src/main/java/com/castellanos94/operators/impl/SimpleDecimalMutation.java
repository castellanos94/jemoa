package com.castellanos94.operators.impl;

import com.castellanos94.operators.MutationOperator;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.Tools;

public class SimpleDecimalMutation implements MutationOperator<DoubleSolution> {
    protected Double probability;

    public SimpleDecimalMutation(Double probability) {
        this.probability = probability;
    }

    @Override
    public DoubleSolution execute(DoubleSolution solution) {
        for (int i = 0; i < solution.getVariables().size(); i++) {
            if (Tools.getRandom().nextDouble() <= probability) {
                double min = solution.getProblem().getLowerBound()[i].doubleValue();
                double max = solution.getProblem().getUpperBound()[i].doubleValue();
                solution.setVariable(i, Tools.getRandomNumberInRange(min, max).doubleValue());
            }
        }
        return solution;
    }

    public Double getProbability() {
        return probability;
    }

}