package com.castellanos94.components;

import java.util.ArrayList;

import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.ExtraInformation;

public interface DensityEstimator<S extends Solution<?>> extends ExtraInformation {
    void compute(ArrayList<S> solutions);

    ArrayList<S> sort(ArrayList<S> solutions);

}