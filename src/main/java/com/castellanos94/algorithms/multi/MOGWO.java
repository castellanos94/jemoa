package com.castellanos94.algorithms.multi;

import java.util.ArrayList;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.datatype.Data;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

/**
 * 
 * Zapotecas-Martínez, S., García-Nájera, A., & López-Jaimes, A. (2019).
 * Multi-objective grey wolf optimizer based on decomposition. Expert Systems
 * with Applications, 120, 357–371. doi:10.1016/j.eswa.2018.12.003
 */
public class MOGWO<S extends Solution<?>> extends AbstractEvolutionaryAlgorithm<S> {
    protected ArrayList<S> grayWolves;
    protected S alphaX;
    protected S betaX;
    protected S deltaX;

    public MOGWO(Problem<S> problem) {
        super(problem);

    }

    @Override
    public void execute() {
        for (int i = 0; i < populationSize; i++) {
            S wolf = problem.randomSolution();
            problem.evaluate(wolf);
            problem.evaluateConstraint(wolf);
            grayWolves.add(wolf);
        }

        double a = 2;
        double r1[] = generateRandomVector(populationSize);
        double r2[] = generateRandomVector(populationSize);
        double A[] = calculateA(a, r1);
        double c[] = product(2, r2);
        //double d[] = calculateDOptimium(c,);

    }

    private double[] product(int scalar, double[] vector) {
        double c[] = new double[vector.length];
        for (int i = 0; i < c.length; i++) {
            c[i] = scalar * vector[i];
        }
        return c;
    }

    private double[] calculateA(double a, double[] r1) {
        double c[] = new double[r1.length];
        for (int i = 0; i < c.length; i++) {
            c[i] = 2 * a * (r1[i] - a);
        }
        return c;
    }

    protected double[] generateRandomVector(int n) {
        double d[] = new double[n];
        for (int i = 0; i < n; i++) {
            d[i] = Tools.getRandom().nextDouble();
        }
        return d;
    }

    protected double[] entrywiseProduct(double a[], double b[]) {
        double c[] = new double[a.length];
        for (int i = 0; i < c.length; i++) {
            c[i] = a[i] * b[i];
        }
        return c;
    }

    @Override
    protected void updateProgress() {
        // TODO Auto-generated method stub

    }

    @Override
    protected ArrayList<S> reproduction(ArrayList<S> parents) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ArrayList<S> replacement(ArrayList<S> population, ArrayList<S> offspring) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean isStoppingCriteriaReached() {
        // TODO Auto-generated method stub
        return false;
    }

}
