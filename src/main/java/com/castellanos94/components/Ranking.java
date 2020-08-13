package com.castellanos94.components;

import java.util.ArrayList;

import com.castellanos94.solutions.Solution;

public interface Ranking<S extends Solution<?>> {
    void computeRanking(ArrayList<S> population);

    ArrayList<S> getSubFront(int index);

    int getNumberOfSubFronts();
}