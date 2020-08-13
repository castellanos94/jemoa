package com.castellanos94.operators.impl;

import java.util.ArrayList;

import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.solutions.BinarySolution;
import com.castellanos94.utils.Tools;

public class HUXCrossover implements CrossoverOperator<BinarySolution> {
    protected Double probability;

    public HUXCrossover() {
        probability = 1.0;
    }

    public HUXCrossover(Double probability) {
        this.probability = probability;
    }

    @Override
    public ArrayList<BinarySolution> execute(ArrayList<BinarySolution> parents) {
        if (parents.size() < 2) {
            throw new IllegalArgumentException("must be two parent at least");
        }
        ArrayList<BinarySolution> child = new ArrayList<>();
        BinarySolution a = parents.get(0);
        BinarySolution b = parents.get(1);
        for (int i = 0; i < 2; i++) {
            BinarySolution c;
            c = a.copy();
            if (Tools.getRandom().nextDouble() <= probability)
                for (int j = 0; j < a.getNumberOfVariables(); j++) {
                    if (Tools.getRandom().nextDouble() < 0.5) {
                        c.getVariable(0).set(j, b.getVariable(0).get(j));
                    }
                }
            child.add(c);

        }
        return child;
    }

    @Override
    public double getCrossoverProbability() {
        return 2;
    }

    @Override
    public int getNumberOfRequiredParents() {
        return 2;
    }

    @Override
    public int getNumberOfGeneratedChildren() {
        return 2;
    }

}