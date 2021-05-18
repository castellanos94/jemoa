package com.castellanos94.problems;

import java.util.ArrayList;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.Interval;
import com.castellanos94.datatype.RealData;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.problems.benchmarks.dtlz.*;
import com.castellanos94.solutions.DoubleSolution;

public class DTLZP extends GDProblem<DoubleSolution> {
    protected final DTLZ dtlz;
    protected final int numberOfProblem;

    public DTLZP(int numberOfProblem, DTLZ_Instance instance) {
        this.numberOfProblem = numberOfProblem;
        this.instance = instance;
        this.numberOfObjectives = instance.getNumObjectives();
        this.numberOfDecisionVars = instance.getNumDecisionVariables();
        this.preference_models = instance.getPreferenceModels();
        this.numberOfConstrains = 0;
        switch (numberOfProblem) {
            case 1:
                this.dtlz = new DTLZ1(numberOfObjectives, numberOfDecisionVars);
                break;
            case 2:
                this.dtlz = new DTLZ2(numberOfObjectives, numberOfDecisionVars);
                break;
            case 3:
                this.dtlz = new DTLZ3(numberOfObjectives, numberOfDecisionVars);
                break;
            case 4:
                this.dtlz = new DTLZ4(numberOfObjectives, numberOfDecisionVars);
                break;
            case 5:
                this.dtlz = new DTLZ5(numberOfObjectives, numberOfDecisionVars);
                break;
            case 6:
                this.dtlz = new DTLZ6(numberOfObjectives, numberOfDecisionVars);
                break;
            case 7:
                this.dtlz = new DTLZ7(numberOfObjectives, numberOfDecisionVars);
                break;
            case 8:
                this.dtlz = new DTLZ8(numberOfObjectives, numberOfDecisionVars);
                this.numberOfConstrains = numberOfObjectives;
                break;
            case 9:
                this.dtlz = new DTLZ9(numberOfObjectives, numberOfDecisionVars);
                this.numberOfConstrains = numberOfObjectives;
                break;
            default:
                throw new IllegalArgumentException("Invalid Problem Number " + numberOfProblem);
        }
        objectives_type = new int[numberOfObjectives];
        for (int i = 0; i < numberOfObjectives; i++) {
            objectives_type[i] = Problem.MINIMIZATION;
        }
        loadBoundarys();
        setName(this.dtlz.getName());
    }

    private void loadBoundarys() {
        lowerBound = new Data[numberOfDecisionVars];
        upperBound = new Data[numberOfDecisionVars];
        for (int i = 0; i < lowerBound.length; i++) {
            lowerBound[i] = new RealData(0);
            upperBound[i] = new RealData(1);
        }
    }

    @Override
    public void evaluate(DoubleSolution solution) {
        this.dtlz.evaluate(solution);
    }

    @Override
    public void evaluateConstraint(DoubleSolution solution) {
        this.dtlz.evaluateConstraint(solution);
    }

    public DoubleSolution generate() {
        return this.dtlz.generate();
    }

    public DoubleSolution generateFromVarString(String string) {
        return this.dtlz.generateFromVarString(string);
    }

    public ArrayList<DoubleSolution> generateRandomSample(int size) {
        return this.dtlz.generateRandomSample(size);
    }

    public ArrayList<DoubleSolution> generateSampleNonDominated(int size) {
        return this.dtlz.generateSampleNonDominated(size);
    }

    @Override
    public DoubleSolution randomSolution() {
        return this.dtlz.randomSolution();
    }

    @Override
    public DTLZ_Instance getInstance() {
        return (DTLZ_Instance) this.instance;
    }

    @Override
    public int getNumDMs() {
        return getInstance().getNumDMs();
    }

    @Override
    public Interval[][][] getR1() {
        return getInstance().getR1();
    }

    @Override
    public Interval[][][] getR2() {
        return getInstance().getR2();
    }

    public DTLZ getDTLZProblem() {
        return dtlz;
    }

    @Override
    public String toString() {
        return this.dtlz.toString();
    }

    @Override
    public DoubleSolution getEmptySolution() {
        return this.dtlz.getEmptySolution();
    }
}
