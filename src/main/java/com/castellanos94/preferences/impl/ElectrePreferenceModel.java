package com.castellanos94.preferences.impl;

import java.util.Arrays;

import com.castellanos94.datatype.Data;
import com.castellanos94.preferences.PreferenceModel;

public class ElectrePreferenceModel extends PreferenceModel {
    /**
     * Preference threshold
     */
    protected Data[] preference;
    /**
     * The importance coefficients k
     */
    protected Data[] weight;
    /**
     * Indifference threshold
     */
    protected Data[] indifference;
    /**
     * Veto threshold
     */
    protected Data[] veto;
    /**
     * Lambda cutting level
     */
    protected Data lambda;

    public ElectrePreferenceModel(Data[] preference, Data[] weight, Data[] indifference, Data[] veto, Data lambda) {
        this.preference = preference;
        this.weight = weight;
        this.indifference = indifference;
        this.veto = veto;
        this.lambda = lambda;
    }

    public Data getWeight(int index) {
        return this.weight[index];
    }

    public Data getIndiference(int index) {
        return this.indifference[index];
    }

    public Data getPreference(int index) {
        return this.preference[index];
    }

    public Data getVeto(int index) {
        return this.veto[index];
    }

    public Data getLambda() {
        return lambda;
    }

    @Override
    public String toString() {
        return "ElectrePreferenceModel [indifference=" + Arrays.toString(indifference) + ", lambda=" + lambda
                + ", preference=" + Arrays.toString(preference) + ", veto=" + Arrays.toString(veto) + "]";
    }

}
