package com.castellanos94.operators.impl;

import com.castellanos94.datatype.Data;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.solutions.Solution;

public class RepairBoundary implements RepairOperator {

    @Override
    public void repair(Solution solution) {
        for (int i = 0; i < solution.getDecision_vars().size(); i++) {
            Data var = solution.getVariable(i);
            if (var.compareTo(solution.getLowerBound(i)) < 0) {
                solution.setDecisionVar(i, Data.initByRefType(var, solution.getLowerBound(i)));
            } else if (var.compareTo(solution.getUpperBound(i)) > 0) {
                solution.setDecisionVar(i, Data.initByRefType(var, solution.getUpperBound(i)));
            }
        }
    }

}