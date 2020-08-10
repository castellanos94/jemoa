package com.castellanos94.operators.impl;

import com.castellanos94.datatype.Data;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

public class RepairRandomBoundary implements RepairOperator {

    @Override
    public void repair(Solution solution) {
        for (int i = 0; i < solution.getVariables().size(); i++) {
            Data var = solution.getVariable(i);
            if (Data.checkNaN(var)) {
                solution.setVariables(i, Data.initByRefType(var, Data.initByRefType(var,
                        Tools.getRandomNumberInRange(solution.getLowerBound(i), solution.getUpperBound(i)))));
            } else if (var.compareTo(solution.getLowerBound(i)) < 0 || var.compareTo(solution.getUpperBound(i)) > 0) {
                solution.setVariables(i, Data.initByRefType(var, Data.initByRefType(var,
                        Tools.getRandomNumberInRange(solution.getLowerBound(i), solution.getUpperBound(i)))));
            } 
        }
    }

}