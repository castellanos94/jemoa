package com.castellanos94.operators;

import java.util.ArrayList;

import com.castellanos94.solutions.Solution;

public interface CrossoverOperator {
    ArrayList<Solution>  execute(ArrayList<Solution> parents);
}