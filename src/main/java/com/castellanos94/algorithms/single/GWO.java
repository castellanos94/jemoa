package com.castellanos94.algorithms.single;

import java.util.ArrayList;
import java.util.List;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.MatrixArithmetic;
import com.castellanos94.utils.Tools;

/**
 * Mirjalili, S., Mirjalili, S. M., & Lewis, A. (2014). Grey Wolf Optimizer.
 * Advances in Engineering Software, 69, 46–61.
 * https://doi.org/10.1016/j.advengsoft.2013.12.007
 * 
 * @param problem minimization problem (only)
 */
public class GWO<S extends Solution<?>> extends AbstractEvolutionaryAlgorithm<S> {
    protected S alphaWolf;
    protected S betaWolf;
    protected S deltaWolf;
    protected int currentIteration;
    protected final int MAX_ITERATIONS;
    protected static final String FITNESS_KEY = "GWO_FITNESS";
    protected RepairOperator<S> repairOperator;
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
        for (int i = 0; i < populationSize; i++) {
            S wolf = problem.randomSolution();
            wolves.add(wolf);
        }
        double a, r1, r2, A1, C1;
        while (!isStoppingCriteriaReached()) {
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
                if (problem.getObjectives_type()[0] == Problem.MINIMIZATION) {
                    if (alphaWolf.getObjective(0).compareTo(wolf.getObjective(0)) > 0) {
                        alphaWolf = (S) wolf.copy();
                    }
                    if (betaWolf.getObjective(0).compareTo(wolf.getObjective(0)) > 0
                            && wolf.getObjective(0).compareTo(alphaWolf.getObjective(0)) > 0) {
                        betaWolf = (S) wolf.copy();
                    }
                    if (deltaWolf.getObjective(0).compareTo(wolf.getObjective(0)) > 0
                            && wolf.getObjective(0).compareTo(betaWolf.getObjective(0)) > 0) {
                        deltaWolf = (S) wolf.copy();
                    }

                } else {
                    if (alphaWolf.getObjective(0).compareTo(wolf.getObjective(0)) < 0) {
                        alphaWolf = (S) wolf.copy();
                    }
                    if (betaWolf.getObjective(0).compareTo(wolf.getObjective(0)) < 0
                            && wolf.getObjective(0).compareTo(alphaWolf.getObjective(0)) < 0) {
                        betaWolf = (S) wolf.copy();
                    }
                    if (deltaWolf.getObjective(0).compareTo(wolf.getObjective(0)) < 0
                            && wolf.getObjective(0).compareTo(betaWolf.getObjective(0)) < 0) {
                        deltaWolf = (S) wolf.copy();
                    }
                }
            }
            // a decreases linearly fron 2 to 0
            a = 2 - currentIteration * ((2.0) / MAX_ITERATIONS);
            // Update the Position of search agents including omegas
            double pos []= new double[problem.getNumberOfDecisionVars()];
            for (int i = 0; i < populationSize; i++) {
                for (int j = 0; j < populationSize; j++) {
                    r1 = Tools.getRandom().nextDouble();
                    r2 = Tools.getRandom().nextDouble();
                    // Equation (3.3)
                    A1 = 2 * a * r1 - a;
                    // Equation (3.4)
                    C1 = 2 * r2;
                    // Equation (3.5)-part 1
                    //copyVarsToVector()
                    double d_alpha[] = MatrixArithmetic.abs(MatrixArithmetic.entrywiseProduct(C1, pos) ) ;
                }
            }
        }
        this.computeTime = init_time - System.currentTimeMillis();
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
        return currentIteration < MAX_ITERATIONS;
    }

}
