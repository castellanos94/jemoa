package com.castellanos94.preferences.impl;

import java.util.Arrays;

import com.castellanos94.datatype.Data;

/**
 * This class represent the preference model associate with a dm using
 * outranking preferences.
 */
public class PreferenceModel {
    protected Data[] weights;
    protected Data[] vetos;
    protected Data alpha;
    protected Data beta;
    protected Data lambda;

    public Data getAlpha() {
        return alpha;
    }

    public Data getBeta() {
        return beta;
    }

    public Data getLambda() {
        return lambda;
    }

    public void setAlpha(Data alpha) {
        this.alpha = alpha;
    }

    public void setBeta(Data beta) {
        this.beta = beta;
    }

    public void setLambda(Data lambda) {
        this.lambda = lambda;
    }

    public PreferenceModel(Data alpha, Data beta, Data lambda) {
        this.alpha = alpha;
        this.beta = beta;
        this.lambda = lambda;
    }

    public PreferenceModel() {
    }

    public Data[] getVetos() {
        return vetos;
    }

    public Data[] getWeights() {
        return weights;
    }

    public void setVetos(Data[] vetos) {
        this.vetos = vetos;
    }

    public void setWeights(Data[] weights) {
        this.weights = weights;
    }

    @Override
    public String toString() {
        return "PreferenceModel [alpha=" + alpha + ", beta=" + beta + ", lambda=" + lambda + ", vetos="
                + Arrays.toString(vetos) + ", weights=" + Arrays.toString(weights) + "]";
    }

}
