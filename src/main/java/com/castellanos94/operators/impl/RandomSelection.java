package com.castellanos94.operators.impl;

import java.util.ArrayList;
import java.util.Collections;

import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.solutions.Solution;

public class RandomSelection implements SelectionOperator {
    private ArrayList<Solution> parents;
    private int size;

    public RandomSelection(int size) {
        this.size = size;
    }

    @Override
    public void execute(final ArrayList<Solution> solutions) {
        Collections.shuffle(solutions);
        this.parents = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            parents.add(solutions.get(i));
        }
    }

    @Override
    public void setPopulaitonSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "RandomSelection [size=" + size + "]";
    }

    @Override
    public ArrayList<Solution> getParents() {
        return parents;
    }

}