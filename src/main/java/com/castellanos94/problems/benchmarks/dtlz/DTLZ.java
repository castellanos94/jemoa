package com.castellanos94.problems.benchmarks.dtlz;

import java.io.FileNotFoundException;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

public abstract class DTLZ extends Problem {
    protected int k;

    public DTLZ(int numberOfObjectives, int numberOfVariables) {
        this.numberOfObjectives = numberOfObjectives;
        this.numberOfDecisionVars = numberOfVariables;
        numberOfConstrains = 0;
        k = this.numberOfDecisionVars - this.numberOfObjectives + 1;
        lowerBound = new Data[numberOfDecisionVars];
        upperBound = new Data[numberOfDecisionVars];
        objectives_type = new int[numberOfObjectives];
        for (int i = 0; i < numberOfObjectives; i++) {
            objectives_type[i] = Problem.MINIMIZATION;
        }
        for (int i = 0; i < lowerBound.length; i++) {
            lowerBound[i] = new RealData(0);
            upperBound[i] = new RealData(1);
        }
        setName(this.getClass().getSimpleName());
    }

    /**
     * G for problem DTLZ1, DTLZ3
     * 
     * @param x vars array
     * @return g value
     */
    protected double g(double x[]) {
        double g = 0.0;
        for (int i = this.numberOfDecisionVars - k; i < this.numberOfDecisionVars; i++) {
            g += (x[i] - 0.5) * (x[i] - 0.5) - Math.cos(20.0 * Math.PI * (x[i] - 0.5));
        }

        g = 100 * (k + g);
        return g;
    }

    /**
     * G for problem DTLZ2, DTLZ4, DTLZ5
     * 
     * @param x vars array
     * @return g value
     */
    protected double g2(double x[]) {
        double g = 0.0;
        for (int i = numberOfDecisionVars - k; i < numberOfDecisionVars; i++) {
            g += (x[i] - 0.5) * (x[i] - 0.5);
        }
        return g;
    }

    public abstract double[][] getParetoOptimal3Obj() throws FileNotFoundException;

    @Override
    public int evaluateConstraints(Solution solution) {
        int cn = 0;
        double v = 0;
        for (int i = 0; i < numberOfDecisionVars; i++) {
            if (solution.getVariable(i).compareTo(lowerBound[i]) < 0) {
                cn++;
                v += lowerBound[i].doubleValue() - solution.getVariable(i).doubleValue();
            } else if (solution.getVariable(i).compareTo(upperBound[i]) > 0) {
                cn++;
                v += upperBound[i].doubleValue() - solution.getVariable(i).doubleValue();
            }
        }
        solution.setPenalties(new RealData(v));
        solution.setN_penalties(cn);
        return 0;
    }

    public DTLZ setK(int k) {
        this.k = k;
        this.numberOfDecisionVars = this.k + this.numberOfObjectives - 1;
        return this;
    }

    @Override
    public Solution randomSolution() {
        Solution solution = new Solution(this);
        for (int i = 0; i < this.numberOfDecisionVars; i++) {
            solution.setVariable(i, new RealData(Tools.getRandom().nextDouble()));
        }
        return solution;
    }

    @Override
    public String toString() {
        return String.format("%s, number_of_variables = %d, number_of_objectives = %d, k = %d", name,
                numberOfDecisionVars, numberOfObjectives, k);
    }

}