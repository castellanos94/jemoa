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
            Data zero = Data.getZeroByType(solution.getVariables().get(0));
            Data one = Data.getOneByType(solution.getVariables().get(0));

            for (int i = 0; i < solution.getVariables().size(); i++) {
                if (Tools.getRandom().nextDouble() < 0.5) {
                    if (solution.getVariables().get(i).compareTo(1) == 0) {
                        solution.setVariables(i, (Data) zero.clone());
                    } else {
                        solution.setVariables(i, (Data) one.clone());
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