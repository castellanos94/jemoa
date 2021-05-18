package com.castellanos94.problems;

import com.castellanos94.datatype.Interval;
import com.castellanos94.preferences.impl.OutrankingModel;
import com.castellanos94.solutions.Solution;

public abstract class GDProblem<S extends Solution<?>> extends Problem<S> {
    protected OutrankingModel[] preference_models;
    protected int dms;

    public OutrankingModel getPreferenceModel(int dm) {
        return this.preference_models[dm];
    }

    public OutrankingModel[] getPreferenceModels() {
        return this.preference_models;
    }

    public abstract Interval[][][] getR2();

    public abstract Interval[][][] getR1();

    public abstract int getNumDMs();

}
