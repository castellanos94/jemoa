package com.castellanos94.preferences.impl;

import com.castellanos94.datatype.Data;

public class OutrankModel {
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
  

    public OutrankModel(Data alpha, Data beta, Data lambda) {
        this.alpha = alpha;
        this.beta = beta;
        this.lambda = lambda;
    }

    public OutrankModel() {
    }

    @Override
    public String toString() {
        return "OutrankModel [alpha=" + alpha + ", beta=" + beta + ", lambda=" + lambda + "]";
    }
}
