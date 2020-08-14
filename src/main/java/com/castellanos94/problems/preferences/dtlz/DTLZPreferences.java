package com.castellanos94.problems.preferences.dtlz;

import com.castellanos94.datatype.RealData;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.preferences.Classifier;
import com.castellanos94.problems.benchmarks.dtlz.DTLZ;
import com.castellanos94.solutions.DoubleSolution;

public abstract class DTLZPreferences extends DTLZ {
    private Classifier<DoubleSolution> classifier;

    public DTLZPreferences(DTLZ_Instance instance, Classifier<DoubleSolution> classifier) {
        super(instance.getNumObjectives(), instance.getNumDecisionVariables());
        this.instance = instance;
    }

    @Override
    public void evaluateConstraint(DoubleSolution solution) {
        solution.setPenalties(RealData.ZERO);
        // classifier.classify(solution);
    }

    @Override
    public DTLZ_Instance getInstance() {
        return (DTLZ_Instance) super.getInstance();
    }
}