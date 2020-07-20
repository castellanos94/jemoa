package com.castellanos94.operators.impl;

import java.util.ArrayList;

import com.castellanos94.datatype.Data;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

public class UniformCrossover implements CrossoverOperator {

    @Override
    public ArrayList<Solution> execute(ArrayList<Solution> parents) throws CloneNotSupportedException {
        if (parents.size() < 2) {
            throw new IllegalArgumentException("must be two parent at least");
        }
        ArrayList<Solution> child = new ArrayList<>();
        Solution a = parents.get(0);
        Solution b = parents.get(1);
        for (int i = 0; i < 2; i++) {
            Solution c = (Solution) a.clone();
            for (int j = 0; j < a.getDecision_vars().size(); j++) {
                if (Tools.getRandom().nextDouble() < 0.5) {
                    c.setDecisionVar(i, (Data) b.getDecision_vars().get(i).clone());
                }
            }
            child.add(c);
        }
        return child;
    }

}