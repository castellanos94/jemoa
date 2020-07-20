package com.castellanos94.operators.impl;

import com.castellanos94.datatype.RealData;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

public class SimpleDecimalMutation implements MutationOperator {
    protected Double probability;

    public SimpleDecimalMutation(Double probability) {
        this.probability = probability;
    }

    @Override
    public void execute(Solution solution) throws CloneNotSupportedException {
        if (Tools.getRandom().nextDouble() < probability) {
            for (int i = 0; i < solution.getDecision_vars().size(); i++) {
                if (Tools.getRandom().nextDouble() < 0.5) {
                    double min = solution.getProblem().getLowerBound()[i].doubleValue();
                    double max = solution.getProblem().getUpperBound()[i].doubleValue();
                    solution.setDecisionVar(i, new RealData(Tools.getRandomNumberInRange(min, max)));
                }
            }
        }

    }

}