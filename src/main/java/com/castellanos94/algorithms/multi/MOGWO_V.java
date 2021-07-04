package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.Iterator;

import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.operators.impl.CrowdingDistanceArchive;
import com.castellanos94.operators.impl.PolynomialMutation;
import com.castellanos94.operators.impl.SBXCrossover;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.Tools;

/**
 * Mirjalili, S., Saremi, S., Mirjalili, S. M., & Coelho, L. dos S. (2016).
 * Multi-objective grey wolf optimizer: A novel algorithm for multi-criterion
 * optimization. Expert Systems with Applications, 47, 106–119.
 * doi:10.1016/j.eswa.2015.10.039
 */
public class MOGWO_V<S extends DoubleSolution> extends MOGWO<S> {

    /**
     * Default ArchiveSelection : CrowdingDistanceArchive
     * 
     * @see com.castellanos94.operators.impl.CrowdingDistanceArchive
     * 
     * @param problem        mop
     * @param populationSize wolf population size
     * @param MAX_ITERATIONS max iteration
     * @param nGrid          external population size
     * @param repairOperator repair operator
     */
    @SuppressWarnings("unchecked")
    public MOGWO_V(Problem<S> problem, int populationSize, int MAX_ITERATIONS, int nGrid,
            RepairOperator<S> repairOperator) {
        super(problem, populationSize, MAX_ITERATIONS, nGrid, repairOperator);
        this.mutationOperator = (MutationOperator<S>) new PolynomialMutation();
        this.crossoverOperator = (CrossoverOperator<S>) new SBXCrossover();
        this.archiveSelection = new CrowdingDistanceArchive<>(nGrid);
    }

    @Override
    public void execute() {
        this.init_time = System.currentTimeMillis();
        wolves = new ArrayList<>();
        this.solutions = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            S wolf = problem.randomSolution();
            // Return back the search agents that go beyond the boundaries of the search
            // space
            repairOperator.execute(wolf);
            // Calculate objective function for each search agent
            problem.evaluate(wolf);
            problem.evaluateConstraint(wolf);
            wolves.add(wolf);
        }

        // Update External Archive
        this.archiveSelection.execute(wolves);

        double a, r1, r2, alpha_ij, beta_ij, delta_ij, x_ij;
        double A[] = new double[3];
        double C[] = new double[3];

        while (!isStoppingCriteriaReached()) {
            // Update alpha, beta and delta
            selectLeader(this.archiveSelection.getParents());
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

            // calculate fitness and update alpha, beta and delta
            for (S wolf : wolves) {
                // Return back the search agents that go beyond the boundaries of the search
                // space
                // mutationOperator.execute(wolf);
                repairOperator.execute(wolf);
                // Calculate objective function for each search agent
                problem.evaluate(wolf);
                problem.evaluateConstraint(wolf);
            }

            // Reproction only EP
            ArrayList<S> leaders = new ArrayList<>();

            leaders.add(alphaWolf);
            leaders.add(betaWolf);
            leaders.add(deltaWolf);
            leaders.add(wolves.get(Tools.getRandom().nextInt(wolves.size())));

            wolves.addAll(reproduction(leaders));
            comparator.computeRanking(wolves);
            ArrayList<S> tmp = new ArrayList<>();
            int indexFront = 0;
            while (tmp.size() < populationSize) {
                for (S wolf : comparator.getSubFront(indexFront)) {
                    if (tmp.size() < populationSize) {
                        tmp.add(wolf);
                    } else {
                        break;
                    }
                }
                indexFront++;
            }
            wolves = tmp;
            // Update External Archive
            this.archiveSelection.execute(wolves);
            // comparator.computeRanking(reproduction(this.archiveSelection.getParents()));

            // this.archiveSelection.execute(comparator.getSubFront(0));

            updateProgress();

        }
        computeTime = System.currentTimeMillis() - init_time;
    }

    /**
     * Select a leader from archive, this remove the element from the list.
     * 
     * @param solutions
     * 
     */
    @SuppressWarnings("unchecked")
    protected void selectLeader(ArrayList<S> solutions) {
        // Select Leader with roulette
        this.selectionOperator.execute(solutions);
        ArrayList<S> parents = this.selectionOperator.getParents();
        Iterator<S> iterator = parents.iterator();

        // Select alfa and remove to exclude
        if (iterator.hasNext()) {
            alphaWolf = (S) iterator.next().copy();
            iterator.remove();
        } else {
            this.selectionOperator.execute(wolves);
            int index = 0;

            alphaWolf = (S) selectionOperator.getParents().get(index++).copy();

        }

        // Select beta and remove to exclude

        this.selectionOperator.execute(parents);
        parents = this.selectionOperator.getParents();
        iterator = parents.iterator();
        boolean isBetaWolf = false, isDeltaWolf = false;
        if (iterator.hasNext()) {
            betaWolf = (S) iterator.next().copy();
            iterator.remove();
        } else {
            S _c = null;
            this.selectionOperator.execute(wolves);
            int index = 0;
            do {
                _c = selectionOperator.getParents().get(index++);
            } while (_c.equals(alphaWolf) && index < selectionOperator.getParents().size());
            betaWolf = (S) _c.copy();
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
            S _c = null;
            this.selectionOperator.execute(wolves);
            int index = 0;
            do {
                _c = selectionOperator.getParents().get(index++);
            } while (_c.equals(betaWolf) || _c.equals(alphaWolf) && index < selectionOperator.getParents().size());
            deltaWolf = (S) wolves.get(index).copy();
            isDeltaWolf = true;
        }

        // add back alpha, beta and deta to the archive

        if (!this.archiveSelection.getParents().contains(alphaWolf))
            this.archiveSelection.getParents().add(alphaWolf);
        if (!this.archiveSelection.getParents().contains(betaWolf) && !isBetaWolf)
            this.archiveSelection.getParents().add(betaWolf);
        if (!this.archiveSelection.getParents().contains(deltaWolf) && !isDeltaWolf)
            this.archiveSelection.getParents().add(deltaWolf);

    }

    @Override
    protected void updateProgress() {
        this.currentIteration += 1;
    }

    @Override
    protected ArrayList<S> reproduction(ArrayList<S> parents) {
        ArrayList<S> offspring = new ArrayList<>();
        for (int i = 0; i < parents.size(); i++) {
            ArrayList<S> p = new ArrayList<>();
            p.add(parents.get(i++));
            p.add(parents.get((i < parents.size()) ? i : 0));
            offspring.addAll(crossoverOperator.execute(p));

        }
        for (S solution : offspring) {
            mutationOperator.execute(solution);
            repairOperator.execute(solution);
            problem.evaluate(solution);
            problem.evaluateConstraint(solution);
        }
        return offspring;

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
        return "MOGWO-V [MAX_ITERATIONS=" + MAX_ITERATIONS + ", nGrid=" + nGrid + ", Problem=" + this.problem.toString()
                + "]";
    }

    @Override
    public ArrayList<S> getSolutions() {
        return this.archiveSelection.getParents();
    }

    @Override
    public MOGWO_V<S> copy() {
        return new MOGWO_V<>(problem, populationSize, MAX_ITERATIONS, nGrid, repairOperator);
    }
}
