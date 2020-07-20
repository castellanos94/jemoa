package com.castellanos94.operators.impl;

import com.castellanos94.datatype.Data;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

public class BinaryMutation implements MutationOperator {
    protected Double probability;

    public BinaryMutation(Double probability) {
        this.probability = probability;
    }

    @Override
    public void execute(Solution solution) throws CloneNotSupportedException {
        if (Tools.getRandom().nextDouble() < probability) {
            Data zero = Data.getZeroByType(solution.getDecision_vars().get(0));
            Data one = Data.getOneByType(solution.getDecision_vars().get(0));

            for (int i = 0; i < solution.getDecision_vars().size(); i++) {
                if (Tools.getRandom().nextDouble() < 0.5) {
                    if (solution.getDecision_vars().get(i).compareTo(1) == 0) {
                        solution.setDecisionVar(i, (Data) zero.clone());
                    } else {
                        solution.setDecisionVar(i, (Data) one.clone());
                    }
                }
            }
        }

    }

    @Override
    public String toString() {
        return "BinaryMutation [probability=" + probability + "]";
    }

}