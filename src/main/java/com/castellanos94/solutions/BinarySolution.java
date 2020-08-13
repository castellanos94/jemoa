package com.castellanos94.solutions;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import com.castellanos94.datatype.Data;
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

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() throws CloneNotSupportedException {
        BinarySolution clone = new BinarySolution(this.getNumberOfObjectives(), this.getNumberOfVariables(),
                this.getNumberOfResources());
        clone.setVariables((ArrayList<BitSet>) this.getVariables().clone());
        clone.setObjectives((ArrayList<Data>) (this.getObjectives().clone()));
        clone.setResources((ArrayList<Data>) this.getResources().clone());
        clone.setRank(this.getRank());
        clone.setProblem(this.problem);

        if (this.getPenalties() != null) {
            clone.setPenalties((Data) this.getPenalties().clone());
        }
        clone.setN_penalties(this.getN_penalties());
        if (this.attributes != null)
            clone.setAttributes((HashMap<String, Object>) this.getAttributes().clone());
        return clone;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < numberOfVariables; i++) {
            str.append(this.variables.get(0).get(i) ? '1' : '0');
        }
        return String.format("%s * %s * %s * %s * %3d", str.toString(),
                objectives.toString().replace("[", "").replace("]", ""),
                resources.toString().replace("[", "").replace("]", ""), penalties, n_penalties);
    }

}