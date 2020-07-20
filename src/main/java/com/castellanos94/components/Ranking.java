package com.castellanos94.components;

import java.util.ArrayList;

import com.castellanos94.solutions.Solution;

public interface Ranking {
    void computeRanking(ArrayList<Solution> population);
    ArrayList<Solution> getSubFront(int index);
    int getNumberOfSubFronts();
}