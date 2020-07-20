package com.castellanos94.operators;

import java.util.ArrayList;

import com.castellanos94.solutions.Solution;

public interface CrossoverOperator {
    void  execute(ArrayList<Solution> parents, ArrayList<Solution> children);
}