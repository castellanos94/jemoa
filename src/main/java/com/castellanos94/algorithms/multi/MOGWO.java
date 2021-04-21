package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.components.impl.CrowdingDistance;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.operators.impl.RouletteWheelSelection;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.DoubleSolution;

import com.castellanos94.utils.Tools;

/**
 * Mirjalili, S., Saremi, S., Mirjalili, S. M., & Coelho, L. dos S. (2016).
 * Multi-objective grey wolf optimizer: A novel algorithm for multi-criterion
 * optimization. Expert Systems with Applications, 47, 106–119.
 * doi:10.1016/j.eswa.2015.10.039
 */
public class MOGWO<S extends DoubleSolution> extends AbstractEvolutionaryAlgorithm<S> {
    protected S alphaWolf;
    protected S betaWolf;
    protected S deltaWolf;
    protected int currentIteration;
    protected final int MAX_ITERATIONS;
    protected final int nGrid;
    protected RepairOperator<S> repairOperator;
    protected RouletteWheelSelection<S>
    private DominanceComparator<S> dominanceComparator = new DominanceComparator<>();
    /**
     * Positions (agents) at Matlab code
     */
    protected ArrayList<S> wolves;

    public MOGWO(Problem<S> problem, int populationSize, int MAX_ITERATIONS, int nGrid,
            RepairOperator<S> repairOperator) {
        super(problem);
        this.solutions = new ArrayList<>();
        this.MAX_ITERATIONS = MAX_ITERATIONS;
        this.populationSize = populationSize;
        this.repairOperator = repairOperator;
        this.nGrid = nGrid;
    }

    @Override
    public void execute() {
        this.init_time = System.currentTimeMillis();
        wolves = new ArrayList<>();
        this.solutions = new ArrayList<>();
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
            updateProgress();

        }

        updatePopulation();
        this.computeTime = init_time - System.currentTimeMillis();

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
        }

        if (solutions.isEmpty()) {
            dominanceComparator.computeRanking(solutions);
            for (S s : dominanceComparator.getSubFront(0)) {
                solutions.add(s);
            }
        } else {
            for (int i = 0; i < wolves.size(); i++) {
                Iterator<S> iterator = solutions.iterator();
                boolean wasNonDominated = false;
                while (iterator.hasNext()) {
                    S _solution = iterator.next();
                    int val = dominanceComparator.compare(wolves.get(i), _solution);
                    if (val == -1) {
                        iterator.remove();
                        solutions.add((S) wolves.get(i).copy());
                        break;
                    } else if (val == 1) {
                        wasNonDominated = false;
                        break;
                    }
                    wasNonDominated = true;
                }
                if (wasNonDominated) {
                    if (solutions.size() < this.nGrid) {
                        solutions.add((S) wolves.get(i).copy());
                    } else {
                        // Compute crowding distance
                        CrowdingDistance<S> crowdingDistance = new CrowdingDistance<>();
                        ArrayList<S> tmpList = new ArrayList<>(solutions);
                        tmpList.add(wolves.get(i));
                        crowdingDistance.compute(tmpList);
                        ArrayList<S> sort = crowdingDistance.sort(tmpList);
                        ArrayList<S> vList = new ArrayList<>();
                        Comparator<S> cmp = crowdingDistance.getComparator();
                        for (S s : sort) {
                            boolean toAdd = true;
                            for (S s2 : vList) {
                                if(cmp.compare(s, s2) == 0){
                                    toAdd = false;
                                    break;
                                }
                            }
                        }

                    }
                }
            }
            // Select Leader with roulette
            // TODO : Select leader
        }
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

}
