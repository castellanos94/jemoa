package com.castellanos94.operators.impl;

import com.castellanos94.operators.RepairOperator;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.Tools;

public class RepairBoundary implements RepairOperator<DoubleSolution> {
    @Override
    public Void execute(DoubleSolution solution) {
        for (int i = 0; i < solution.getVariables().size(); i++) {
            Double var = solution.getVariable(i);
            Double l = solution.getLowerBound(i).doubleValue();
            Double u = solution.getUpperBound(i).doubleValue();
            if (Double.isNaN(var)) {
                solution.setVariable(i, Tools.getRandomNumberInRange(l, u).doubleValue());
            } else {
                if (l == 0 && var < 6 * Math.pow(10, -16)) {
                    solution.setVariable(i, l);
                } else if (var < l) {
                    solution.setVariable(i, l);
                } else if (var > 1) {
                    solution.setVariable(i, Tools.getRandomNumberInRange(l, u).doubleValue());
                }
            }
        }
        return null;
    }

}