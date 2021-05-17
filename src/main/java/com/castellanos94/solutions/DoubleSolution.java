package com.castellanos94.solutions;

import java.util.ArrayList;
import java.util.HashMap;

import com.castellanos94.datatype.Data;
import com.castellanos94.problems.Problem;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class DoubleSolution extends Solution<Double> {
    protected ArrayList<Pair<Data, Data>> bounds;

    public DoubleSolution(Problem<?> problem) {
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

    public DoubleSolution(DoubleSolution doubleSolution) {
        this(doubleSolution.getNumberOfObjectives(), doubleSolution.getNumberOfVariables(),
                doubleSolution.getNumberOfResources(), doubleSolution.getBounds());
        for (int i = 0; i < numberOfVariables; i++) {
            this.variables.set(i, doubleSolution.getVariable(i));
        }
        for (int i = 0; i < numberOfObjectives; i++) {
            this.objectives.set(i, doubleSolution.getObjective(i));
        }
        for (int i = 0; i < numberOfResources; i++) {
            this.resources.set(i, doubleSolution.getResources().get(i));
        }
        this.problem = doubleSolution.getProblem();
        this.attributes = new HashMap<>();
        doubleSolution.attributes.forEach((k, v) -> this.attributes.put(k, v));
        this.penalties = doubleSolution.getPenalties();
        this.numberOfPenalties = doubleSolution.getNumberOfPenalties();
        this.rank = doubleSolution.getRank();
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

    @Override
    public DoubleSolution copy() {
        return new DoubleSolution(this);
    }

}