package com.castellanos94.operators;

import java.util.ArrayList;

public interface CrossoverOperator<Source> extends Operator<ArrayList<Source>, ArrayList<Source>> {
    double getCrossoverProbability();

    int getNumberOfRequiredParents();

    int getNumberOfGeneratedChildren();
}