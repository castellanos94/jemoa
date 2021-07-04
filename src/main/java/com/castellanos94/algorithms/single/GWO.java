package com.castellanos94.algorithms.single;

import java.util.ArrayList;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.DoubleSolution;

import com.castellanos94.utils.Tools;

/**
 * Mirjalili, S., Mirjalili, S. M., & Lewis, A. (2014). Grey Wolf Optimizer.
 * Advances in Engineering Software, 69, 46–61.
 * https://doi.org/10.1016/j.advengsoft.2013.12.007
 * 
 * @param problem minimization problem (only)
 */
public class GWO<S extends DoubleSolution> extends AbstractEvolutionaryAlgorithm<S> {
    protected S alphaWolf;
    protected S betaWolf;
    protected S deltaWolf;
    protected int currentIteration;
    protected final int MAX_ITERATIONS;
    protected RepairOperator<S> repairOperator;
    private DominanceComparator<DoubleSolution> dominanceComparator = new DominanceComparator<>();
    /**
     * Positions (agents) at Matlab code
     */
    protected ArrayList<S> wolves;

    public GWO(Problem<S> problem, int populationSize, int MAX_ITERATIONS, RepairOperator<S> repairOperator) {
        super(problem);
        this.solutions = new ArrayList<>();
        this.MAX_ITERATIONS = MAX_ITERATIONS;
        this.populationSize = populationSize;
        this.repairOperator = repairOperator;
    }

    @Override
    public void execute() {
        this.init_time = System.currentTimeMillis();
        wolves = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            S wolf = problem.randomSolution();
            wolves.add(wolf);
        }
        double a, r1, r2, alpha_ij, beta_ij, delta_ij, x_ij;
        double A[] = new double[3];
        double C[] = new double[3];
        while (!isStoppingCriteriaReached()) {
            // calculate fitness and update alpha, beta and delta
            updatePopulation();
            // a decreases linearly fron 2 to 0
            a = 2 - currentIteration * ((2.0) / MAX_ITERATIONS);

            for (int i = 0; i < populationSize; i++) {
                S currentWolf = wolves.get(i);
                for (int j = 0; j < problem.getNumberOfDecisionVars(); j++) {

                    for (int k = 0; k < A.length; k++) {
                        r1 = Tools.getRandom().nextDouble();
                        r2 = Tools.getRandom().nextDouble();
                        // Equation (3.3)
                        A[k] = 2 * a * r1 - a;
                        // Equation (3.4)
                        C[k] = 2 * r2;
                    }

                    // Auxiliar
                    alpha_ij = alphaWolf.getVariable(j);
                    beta_ij = betaWolf.getVariable(j);
                    delta_ij = deltaWolf.getVariable(j);
                    x_ij = currentWolf.getVariable(j);
                    // Equation (3.5)-part 1
                    double d1 = Math.abs(C[0] * alpha_ij - x_ij);
                    double d2 = Math.abs(C[1] * beta_ij - x_ij);
                    double d3 = Math.abs(C[2] * delta_ij - x_ij);
                    // Equation 3.6
                    double x1 = alpha_ij - A[0] * d1;
                    double x2 = beta_ij - A[1] * d2;
                    double x3 = delta_ij - A[2] * d3;
                    // Equation 3.7
                    currentWolf.setVariable(j, (x1 + x2 + x3) / 3.0);
                }
            }
            updateProgress();
        }

        this.solutions = new ArrayList<>();

        updatePopulation();
        solutions.add(alphaWolf);
        solutions.add(betaWolf);
        solutions.add(deltaWolf);
        computeTime = System.currentTimeMillis() - init_time;

        /*
         * for (S s : wolves) { if (!solutions.contains(s)) solutions.add(s); }
         */
    }

    @SuppressWarnings("unchecked")
    private void updatePopulation() {
        for (S wolf : wolves) {
            // Return back the search agents that go beyond the boundaries of the search
            // space
            repairOperator.execute(wolf);
            // Calculate objective function for each search agent
            problem.evaluate(wolf);
            problem.evaluateConstraint(wolf);
            // Update Alpha, Beta, and Delta
            if (alphaWolf == null) {
                alphaWolf = (S) wolf.copy();
            }
            if (betaWolf == null) {
                betaWolf = (S) wolf.copy();
            }
            if (deltaWolf == null) {
                deltaWolf = (S) wolf.copy();
            }
            if (dominanceComparator.compare(alphaWolf, wolf) == 1) {
                alphaWolf = (S) wolf.copy();
            }
            if (dominanceComparator.compare(betaWolf, wolf) == 1
                    && dominanceComparator.compare(alphaWolf, wolf) == -1) {
                betaWolf = (S) wolf.copy();
            }
            if (dominanceComparator.compare(deltaWolf, wolf) == 1
                    && dominanceComparator.compare(betaWolf, wolf) == -1) {
                deltaWolf = (S) wolf.copy();
            }

        }
        /*
         * problem.evaluate(alphaWolf); problem.evaluate(betaWolf);
         * problem.evaluate(deltaWolf); problem.evaluateConstraint(alphaWolf);
         * problem.evaluateConstraint(betaWolf); problem.evaluateConstraint(deltaWolf);
         */
    }

    @Override
    protected void updateProgress() {
        this.currentIteration += 1;
    }

    @Deprecated
    @Override
    protected ArrayList<S> reproduction(ArrayList<S> parents) {
        return null;
    }

    @Deprecated
    @Override
    protected ArrayList<S> replacement(ArrayList<S> population, ArrayList<S> offspring) {
        return null;
    }

    @Override
    protected boolean isStoppingCriteriaReached() {
        return currentIteration >= MAX_ITERATIONS;
    }

    @Override
    public GWO<S> copy() {
        return new GWO<>(problem, populationSize, MAX_ITERATIONS, repairOperator);
    }

}
