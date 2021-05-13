package com.castellanos94.solutions;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import com.castellanos94.problems.Problem;

public class BinarySolution extends Solution<BitSet> {
    public BinarySolution(Problem<BinarySolution> problem) {
        this(problem.getNumberOfObjectives(), problem.getNumberOfDecisionVars(), problem.getNumberOfConstrains());
        this.problem = problem;
    }

    public BinarySolution(int numberOfObjectives, int numberOfVariables, int numberOfResources) {
        this.attributes = new HashMap<>();
        this.resources = new ArrayList<>();
        this.objectives = new ArrayList<>();
        this.numberOfObjectives = numberOfObjectives;
        this.numberOfVariables = numberOfVariables;
        this.numberOfResources = numberOfResources;
        this.variables = new ArrayList<>();
        this.variables.add(new BitSet(numberOfVariables));

        for (int i = 0; i < numberOfObjectives; i++) {
            objectives.add(null);
        }
        for (int i = 0; i < numberOfResources; i++) {
            resources.add(null);
        }
    }

    @SuppressWarnings("unchecked")
    public BinarySolution(BinarySolution binarySolution) {
        this(binarySolution.getNumberOfObjectives(), binarySolution.getNumberOfVariables(),
                binarySolution.getNumberOfResources());
        for (int i = 0; i < numberOfVariables; i++) {
            this.variables.get(0).set(i, binarySolution.getVariable(0).get(i));
        }
        for (int i = 0; i < numberOfObjectives; i++) {
            this.objectives.set(i, binarySolution.getObjective(i));
        }
        for (int i = 0; i < numberOfResources; i++) {
            this.resources.set(i, binarySolution.getResources().get(i));
        }
        this.problem = binarySolution.getProblem();
        this.penalties = binarySolution.getPenalties();
        this.numberOfPenalties = binarySolution.getNumberOfPenalties();
        this.attributes = (HashMap<String, Object>) binarySolution.getAttributes().clone();
    }

    @Override
    public BitSet getVariable(int index) {
        if (index != 0)
            throw new IllegalArgumentException("Binary solution only has one var.");
        return this.variables.get(index);
    }

    @Override
    public void setVariable(int index, BitSet value) {
        if (index != 0)
            throw new IllegalArgumentException("Binary solution only has one var.");
        super.setVariable(index, value);
    }

    @Override
    public BinarySolution copy() {
        return new BinarySolution(this);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < numberOfVariables; i++) {
            str.append(this.variables.get(0).get(i) ? '1' : '0');
        }
        return String.format("%s * %s * %s * %s * %3d", str.toString(),
                objectives.toString().replace("[", "").replace("]", ""),
                resources.toString().replace("[", "").replace("]", ""), penalties, numberOfPenalties);
    }

    public String varToString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < numberOfVariables; i++) {
            str.append(this.variables.get(0).get(i) ? '1' : '0');
        }
        return str.toString();
    }
}