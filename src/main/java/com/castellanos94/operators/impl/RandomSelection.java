package com.castellanos94.operators.impl;

import java.util.ArrayList;
import java.util.Collections;

import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.solutions.Solution;

public class RandomSelection<S extends Solution<?>> implements SelectionOperator<S> {
    private ArrayList<S> parents;
    private int size;

    public RandomSelection(int size) {
        this.size = size;
    }

    @Override
    public Void execute(ArrayList<S> solutions) {
        Collections.shuffle(solutions);
        this.parents = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            parents.add(solutions.get(i));
        }
        return null;
    }

    @Override
    public void setPopulationSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "RandomSelection [size=" + size + "]";
    }

    @Override
    public ArrayList<S> getParents() {
        return parents;
    }

}