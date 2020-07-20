package com.castellanos94.components;

import java.util.ArrayList;

import com.castellanos94.solutions.Solution;

public interface DensityEstimator {
    void compute(ArrayList<Solution> solutions);

    ArrayList<Solution> sort(ArrayList<Solution> solutions);

    String getKey();
}