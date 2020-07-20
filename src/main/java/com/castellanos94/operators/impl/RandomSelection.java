package com.castellanos94.operators.impl;

import java.util.ArrayList;
import java.util.Collections;

import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.solutions.Solution;

public class RandomSelection extends SelectionOperator {
    private int size;
    public RandomSelection(int size){
        this.size = size;
        this.parents = new ArrayList<>();
    }
    @Override
    public void execute(final ArrayList<Solution> solutions) {
        Collections.shuffle(solutions);
        for (int i = 0; i < size; i++) {
            parents.add(solutions.get(i));
        }
    }

    @Override
    public String toString() {
        return "RandomSelection [size=" + size + "]";
    }

}