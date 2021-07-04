package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.Comparator;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.preferences.impl.IntervalOutrankingRelations;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.ExtraInformation;
import com.castellanos94.utils.HeapSort;
import com.castellanos94.utils.Tools;
import com.castellanos94.problems.GDProblem;

/**
 * Multi-Objective Grey Wolf Optimizer based on Preference Indicator
 */
public class PI_MOGWO<S extends DoubleSolution> extends AbstractEvolutionaryAlgorithm<S> implements ExtraInformation {
    protected S alphaWolf;
    protected S betaWolf;
    protected S deltaWolf;
    protected int currentIteration;
    protected final int MAX_ITERATIONS;
    protected RepairOperator<S> repairOperator;
    protected HeapSort<S> heapSortSolutions;
    protected IntervalOutrankingRelations<S> preferences;

    /**
     * Default ArchiveSelection : AdaptiveGrid
     * 
     * @see com.castellanos94.operators.impl.AdaptiveGrid
     * 
     * @param problem        mop
     * @param populationSize wolf population size
     * @param MAX_ITERATIONS max iteration
     * @param repairOperator repair operator
     */
    public PI_MOGWO(Problem<S> problem, int populationSize, int MAX_ITERATIONS, RepairOperator<S> repairOperator) {
        super(problem);
        this.populationSize = populationSize;
        this.MAX_ITERATIONS = MAX_ITERATIONS;

        this.preferences = new IntervalOutrankingRelations<>(problem.getNumberOfObjectives(),
                problem.getObjectives_type(), ((GDProblem<S>) problem).getPreferenceModel(0));
        Comparator<S> cmp = (a, b) -> {
            double netscore_a = (double) a.getAttribute(getAttributeKey());
            double netscore_b = (double) b.getAttribute(getAttributeKey());
            return Double.compare(netscore_a, netscore_b);
        };
        this.heapSortSolutions = new HeapSort<>(cmp.reversed());
        this.repairOperator = repairOperator;
    }

    @Override
    public void execute() {
        this.init_time = System.currentTimeMillis();
        this.solutions = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            S wolf = problem.randomSolution();
            // Return back the search agents that go beyond the boundaries of the search
            // space
            repairOperator.execute(wolf);
            // Calculate objective function for each search agent
            problem.evaluate(wolf);
            problem.evaluateConstraint(wolf);
            solutions.add(wolf);
        }

        double a, r1, r2, alpha_ij, beta_ij, delta_ij, x_ij;
        double A[] = new double[3];
        double C[] = new double[3];

        while (!isStoppingCriteriaReached()) {
            // Update alpha, beta and delta
            selectLeader();
            // a decreases linearly fron 2 to 0
            a = 2 - currentIteration * ((2.0) / MAX_ITERATIONS);

            for (int i = 0; i < populationSize; i++) {
                S currentWolf = solutions.get(i);
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
                    // Equation (3.5)
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

            // calculate fitness and update alpha, beta and delta
            for (S wolf : solutions) {
                // Return back the search agents that go beyond the boundaries of the search
                // space
                repairOperator.execute(wolf);
                // Calculate objective function for each search agent
                problem.evaluate(wolf);
                problem.evaluateConstraint(wolf);
            }
            updateProgress();

        }
        computeTime = System.currentTimeMillis() - init_time;
    }

    /**
     * Select a leader from archive, this remove the element from the list.
     * 
     * 
     */
    @SuppressWarnings("unchecked")
    protected void selectLeader() {
        // Filter
        calculateNetScore(solutions);
        int index = 0;
        alphaWolf = (S) solutions.get(index++).copy();
        betaWolf = (S) solutions.get(index++).copy();
        deltaWolf = (S) solutions.get(index++).copy();

    }

    protected void calculateNetScore(ArrayList<S> solutions) {
        for (int i = 0; i < solutions.size(); i++) {
            double sigma_out = 0, sigma_in = 0;
            S x = solutions.get(i);
            for (int j = 0; j < solutions.size(); j++) {
                if (i != j) {
                    S y = solutions.get(j);
                    this.preferences.compare(x, y);
                    sigma_out += this.preferences.getSigmaXY().doubleValue();
                    sigma_in += this.preferences.getSigmaYX().doubleValue();
                }
            }
            x.setAttribute(getAttributeKey(), sigma_out - sigma_in);
        }
        heapSortSolutions.sort(solutions);
    }

    @Override
    protected void updateProgress() {
        this.currentIteration += 1;
    }

    @Deprecated
    @Override
    protected ArrayList<S> reproduction(ArrayList<S> wolves) {
        return null;

    }

    @Override
    protected ArrayList<S> replacement(ArrayList<S> population, ArrayList<S> offspring) {
        return null;
    }

    @Override
    protected boolean isStoppingCriteriaReached() {
        return currentIteration >= MAX_ITERATIONS;
    }

    @Override
    public String toString() {
        return "PI-MOGWO [MAX_ITERATIONS=" + MAX_ITERATIONS + ", Problem=" + this.problem.toString() + "]";
    }

    @Override
    public ArrayList<S> getSolutions() {
        return this.solutions;
    }

    @Override
    public String getAttributeKey() {
        return "NET_SCORE_PFN";
    }

    @Override
    public PI_MOGWO<S> copy() {
        return new PI_MOGWO<>(problem, populationSize, MAX_ITERATIONS, repairOperator);
    }
}
