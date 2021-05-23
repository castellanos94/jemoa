package com.castellanos94.preferences.impl;

import java.util.Arrays;

import com.castellanos94.datatype.Data;
import com.castellanos94.preferences.PreferenceModel;

/**
 * This class represent the preference model associate with a dm using
 * outranking preferences.
 */
public class OutrankingModel extends PreferenceModel {
    /**
     * DM supports utility function?
     */
    protected boolean supportsUtilityFunction;
    /**
     * Weight of objectives
     */
    protected Data[] weights;
    /**
     * veto threshold
     */
    protected Data[] vetos;
    /**
     * dominance threshold
     */
    protected Data alpha;
    /**
     * credibility outranking threshold
     */
    protected Data beta;
    /**
     * mayority threshold
     */
    protected Data lambda;

    /**
     * Contraint threshold
     */
    protected Data chi;
    /**
     * Delta value (0.5, 1]
     */
    protected double delta = 0.51;

    public double getDelta() {
        return delta;
    }

    /**
     * Delta value (0.5, 1]
     * @param delta (0.5, 1]
     */
    public void setDelta(double delta) {
        this.delta = delta;
    }

    public Data getChi() {
        return chi;
    }

    public void setChi(Data chi) {
        this.chi = chi;
    }

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

    public boolean isSupportsUtilityFunction() {
        return supportsUtilityFunction;
    }

    public void setSupportsUtilityFunction(boolean supportsUtilityFunction) {
        this.supportsUtilityFunction = supportsUtilityFunction;
    }

    public OutrankingModel(Data[] weights, Data[] vetos, Data alpha, Data beta, Data lambda, Data chi) {
        this.weights = weights;
        this.vetos = vetos;
        this.alpha = alpha;
        this.beta = beta;
        this.lambda = lambda;
        this.chi = chi;
    }

    public OutrankingModel() {
    }

    @Override
    public String toString() {
        return "OutrankingModel [alpha=" + alpha + ", beta=" + beta + ", chi=" + chi + ", lambda=" + lambda
                + ", supports utility Function=" + supportsUtilityFunction + ", vetos=" + Arrays.toString(vetos)
                + ", weights=" + Arrays.toString(weights) + "]";
    }

}
