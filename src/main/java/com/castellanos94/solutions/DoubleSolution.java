package com.castellanos94.solutions;

import java.util.ArrayList;
import java.util.HashMap;

import com.castellanos94.datatype.Data;
import com.castellanos94.problems.Problem;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class DoubleSolution extends Solution<Double> {
    protected ArrayList<Pair<Data, Data>> bounds;

    public DoubleSolution(Problem problem) {
        this(problem.getNumberOfObjectives(), problem.getNumberOfDecisionVars(), problem.getNumberOfConstrains(),
                problem.getLowerBound(), problem.getUpperBound());
        this.problem = problem;
    }

    public DoubleSolution(int numberOfObjectives, int numberOfVariables, int numberOfResources, Data[] lower,
            Data[] upper) {
        super(numberOfObjectives, numberOfVariables, numberOfResources);
        this.bounds = new ArrayList<>();
        for (int i = 0; i < upper.length; i++) {
            bounds.add(new ImmutablePair<Data, Data>(lower[i], upper[i]));
        }
    }

    public DoubleSolution(int numberOfObjectives, int numberOfVariables, int numberOfResources,
            ArrayList<Pair<Data, Data>> bounds) {
        super(numberOfObjectives, numberOfVariables, numberOfResources);
        this.bounds = bounds;
    }

    public ArrayList<Pair<Data, Data>> getBounds() {
        return bounds;
    }

    public Data getLowerBound(int index) {
        return this.bounds.get(index).getLeft();
    }

    public Data getUpperBound(int index) {
        return this.bounds.get(index).getRight();
    }

    public void setBounds(ArrayList<Pair<Data, Data>> bounds) {
        this.bounds = bounds;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() throws CloneNotSupportedException {
        DoubleSolution clone = new DoubleSolution(this.getNumberOfObjectives(), this.getNumberOfVariables(),
                this.getNumberOfResources(), this.bounds);
        if (this.problem != null) {
            clone.setProblem(this.problem);
        }
        clone.setVariables((ArrayList<Double>) this.getVariables().clone());
        clone.setObjectives((ArrayList<Data>) (this.getObjectives().clone()));
        clone.setResources((ArrayList<Data>) this.getResources().clone());
        clone.setRank(this.getRank());
        if (this.getPenalties() != null) {
            clone.setPenalties((Data) this.getPenalties().clone());
        }
        clone.setN_penalties(this.getN_penalties());
        if (this.attributes != null)
            clone.setAttributes((HashMap<String, Object>) this.getAttributes().clone());
        if (this.bounds != null)
            clone.setBounds(this.bounds);
        return clone;
    }

}