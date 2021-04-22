package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

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
        this.selectionOperator = new RouletteWheelSelection<>(nGrid);
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
                    alpha_ij = (double) alphaWolf.getVariable(j);
                    beta_ij = (double) betaWolf.getVariable(j);
                    delta_ij = (double) deltaWolf.getVariable(j);
                    x_ij = (double) currentWolf.getVariable(j);
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
        }

        if (solutions.isEmpty()) {
            dominanceComparator.computeRanking(wolves);
            for (S s : dominanceComparator.getSubFront(0)) {
                if (!solutions.contains(s) && solutions.size() < this.nGrid)
                    solutions.add(s);
            }
            // Select alfa and remove to exclude
            selectLeader(solutions);
        } else {
            for (int i = 0; i < wolves.size(); i++) {
                Iterator<S> iterator = solutions.iterator();
                boolean wasNonDominated = false;
                boolean wasAdded = false;
                int index = 0;
                while (iterator.hasNext()) {
                    S _solution = iterator.next();
                    int val = dominanceComparator.compare(wolves.get(i), _solution);
                    if (val == -1) {
                        this.solutions.set(index, wolves.get(i));
                        wasAdded = true;
                    } else if (val == 1) {
                        wasNonDominated = false;
                        if (wasAdded)
                            this.solutions.remove(wolves.get(i));
                        break;
                    }
                    wasNonDominated = true;
                    index++;
                }
                if (wasNonDominated) {
                    boolean isNotPresent = solutions.contains(wolves.get(i)) == false;
                    if (solutions.size() < this.nGrid && isNotPresent) {
                        solutions.add((S) wolves.get(i).copy());
                    } else if (isNotPresent) {
                        this.solutions = new ArrayList<>(
                                this.solutions.stream().distinct().collect(Collectors.toList()));
                        ArrayList<S> tmpList = new ArrayList<>(solutions);
                        tmpList.add(wolves.get(i));
                        if (tmpList.size() > nGrid) {
                            // Compute crowding distance
                            CrowdingDistance<S> crowdingDistance = new CrowdingDistance<>();
                            crowdingDistance.compute(tmpList);
                            ArrayList<S> sorted = crowdingDistance.sort(tmpList);
                            this.solutions = new ArrayList<>(sorted.subList(0, this.nGrid));
                        } else {
                            this.solutions.add(wolves.get(i));
                        }
                    }
                }
            }
            // Filter uniques
            this.solutions = new ArrayList<>(this.solutions.stream().distinct().collect(Collectors.toList()));
            selectLeader(solutions);

        }
    }

    /**
     * Select a leader from archive, this remove the element from the list.
     * 
     * @param solutions
     * 
     */
    protected void selectLeader(ArrayList<S> solutions) {
        // Select Leader with roulette
        this.selectionOperator.execute(solutions);
        ArrayList<S> parents = this.selectionOperator.getParents();
        Iterator<S> iterator = parents.iterator();

        // Select alfa and remove to exclude
        alphaWolf = (S) iterator.next().copy();
        iterator.remove();

        // Select beta and remove to exclude

        this.selectionOperator.execute(parents);
        parents = this.selectionOperator.getParents();
        iterator = parents.iterator();
        boolean isBetaWolf = false, isDeltaWolf = false;
        if (iterator.hasNext()) {
            betaWolf = (S) iterator.next().copy();
            iterator.remove();
        } else {
            int index = -1;
            do {
                index = Tools.getRandomNumberInRange(0, wolves.size()).intValue();
            } while (wolves.get(index).equals(alphaWolf));
            betaWolf = (S) wolves.get(index).copy();
            isBetaWolf = true;
        }
        // Select delta and remove to exclude
        this.selectionOperator.execute(parents);
        parents = this.selectionOperator.getParents();
        iterator = parents.iterator();
        if (iterator.hasNext()) {
            deltaWolf = (S) iterator.next().copy();
            iterator.remove();
        } else {
            int index = -1;
            do {
                index = Tools.getRandomNumberInRange(0, wolves.size()).intValue();
            } while (wolves.get(index).equals(betaWolf) || wolves.get(index).equals(alphaWolf));
            deltaWolf = (S) wolves.get(index).copy();
            isDeltaWolf = true;
        }

        // add back alpha, beta and deta to the archive
        if (!solutions.contains(alphaWolf))
            solutions.add(alphaWolf);
        if (!solutions.contains(betaWolf) && !isBetaWolf)
            solutions.add(betaWolf);
        if (!solutions.contains(deltaWolf) && !isDeltaWolf)
            solutions.add(deltaWolf);

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
    public String toString() {
        return "MOGWO [MAX_ITERATIONS=" + MAX_ITERATIONS + ", nGrid=" + nGrid + ", Problem=" + this.problem.toString()
                + "]";
    }

    @Override
    public ArrayList<S> getSolutions() {
        this.dominanceComparator = new DominanceComparator<>();
        this.dominanceComparator.computeRanking(this.solutions);
        this.solutions = this.dominanceComparator.getSubFront(0);
        return this.solutions;
    }

}
