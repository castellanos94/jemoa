package com.castellanos94.operators;

import java.util.ArrayList;

import com.castellanos94.solutions.Solution;

public interface SelectionOperator {

    public void execute(ArrayList<Solution> solutions);

    public ArrayList<Solution> getParents();
    public void setPopulaitonSize(int size);
}