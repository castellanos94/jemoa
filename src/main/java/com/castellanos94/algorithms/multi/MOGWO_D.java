package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.List;

import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.operators.impl.PolynomialMutation;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.Distance;
import com.castellanos94.utils.Tools;
import com.castellanos94.utils.Distance.Metric;

/**
 * Zapotecas-Martínez, S., García-Nájera, A., & López-Jaimes, A. (2019).
 * Multi-objective grey wolf optimizer based on decomposition. Expert Systems
 * with Applications, 120, 357–371. doi:10.1016/j.eswa.2018.12.003
 */
public class MOGWO_D<S extends DoubleSolution> extends MOGWO<S> {
    /**
     * N: the number of subproblems to be decomposed
     */
    protected int neighborSize;
    /**
     * Lambda: a well-distributed set of weight vectors
     */
    protected ArrayList<S> weightList;
    /**
     * p: a neighborhood selection probability
     */
    protected double neighborhoodSelectionProbability;
    /**
     * T: the sub-pack size
     */
    protected int subPackSize;
    /**
     * n_r: a number maximum of replacements into the sub-pack
     */
    protected int maximumNumberOfReplacedSolutions;
    /**
     * ideal point
     */
    protected S idealPoint;
    protected Distance<S> distance;

    public MOGWO_D(Problem<S> problem, int neighborSize, int MAX_ITERATIONS, int tSubPackSize,
            int maximumNumberOfReplacedSolutions, RepairOperator<S> repairOperator) {
        super(problem, neighborSize, MAX_ITERATIONS, tSubPackSize, repairOperator);
        this.distance = new Distance<>(Metric.EUCLIDEAN_DISTANCE);
        this.mutationOperator = (MutationOperator<S>) new PolynomialMutation();
        this.subPackSize = tSubPackSize;
        this.maximumNumberOfReplacedSolutions = maximumNumberOfReplacedSolutions;
    }

    @Override
    public void execute() {
        this.init_time = System.currentTimeMillis();
        wolves = new ArrayList<>();
        this.solutions = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            S wolf = problem.randomSolution();
            problem.evaluate(wolf);
            problem.evaluateConstraint(wolf);
            wolves.add(wolf);
        }

        this.idealPoint = createIdealPoint();
        // b_i
        ArrayList<ArrayList<S>> neightborhood = new ArrayList<>();
        // Step 3 - 5
        for (int i = 0; i < this.neighborSize; i++) {
            ArrayList<S> bi = new ArrayList<>();
            bi.add(weightList.get(i));
            List<Data> evaluate = distance.evaluate(bi, wolves);
            bi.clear();

            int indexMin = -1;
            Data min = new RealData(Double.MAX_VALUE);
            ArrayList<Integer> ignore = new ArrayList<>();
            while (bi.size() < this.subPackSize) {
                for (int j = 0; j < evaluate.size(); j++) {
                    if (min.compareTo(evaluate.get(j)) > 0 && !ignore.contains(j)) {
                        indexMin = j;
                        min = evaluate.get(j);
                    }
                }
                bi.add(wolves.get(indexMin));
                ignore.add(indexMin);
            }
            neightborhood.add(bi);
            // update 5
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                Data zj = wolves.get(0).getObjective(j);
                for (int k = 1; k < wolves.size(); k++) {
                    if (zj.compareTo(wolves.get(k).getObjective(j)) > 0) {
                        zj = wolves.get(k).getObjective(j);
                    }
                }
                idealPoint.setObjective(j, zj.copy());
            }

        }

        while (!isStoppingCriteriaReached()) {
            int perm[] = new int[this.neighborSize];
            Tools.randomPermutation(perm, this.neighborSize);
            ArrayList<S> phi_i;
            for (int i = 0; i < perm.length; i++) {
                if (Tools.getRandom().nextDouble() < this.neighborhoodSelectionProbability) {
                    phi_i = neightborhood.get(perm[i]);
                } else {
                    phi_i = wolves;
                }
                // Step 11 - 12: calculate fitness and update alpha, beta and delta
                S xnew = this.mutationOperator.execute(computeNewPosition(phi_i));
                problem.evaluate(xnew);
                problem.evaluateConstraint(xnew);
                // Step 13 update idealPoint
                for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                    if (idealPoint.getObjective(j).compareTo(xnew.getObjective(j)) > 0) {
                        idealPoint.setObjective(j, xnew.getObjective(j).copy());
                    }
                }
                // Update neightboring subproblems
                int c = 0;
                for (int j = 0; j < phi_i.size() && c < this.maximumNumberOfReplacedSolutions; j++) {
                    if (gpbi(xnew, weightList.get(i), idealPoint) < gpbi(phi_i.get(j), weightList.get(i), idealPoint)) {
                        phi_i.set(j, (S) xnew.copy());
                        c++;
                    }
                }
            }
            updateProgress();

        }
        for (int i = 0; i < this.neighborSize; i++) {
            solutions.addAll(neightborhood.get(i));
        }
        DominanceComparator<S> comparator = new DominanceComparator<>();
        comparator.computeRanking(solutions);
        this.solutions = comparator.getSubFront(0);
        computeTime = System.currentTimeMillis() - init_time;

    }

    private double gpbi(S x, S lambda, S idealPoint2) {
        double d1, d2, nl;
        double theta = 5.0;

        d1 = d2 = nl = 0.0;
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            d1 += x.getObjective(i).minus(idealPoint2.getObjective(i)).times(lambda.getObjective(i)).doubleValue();
            nl += Math.pow(lambda.getObjective(i).doubleValue(), 2.0);
        }
        nl = Math.sqrt(nl);
        d1 = Math.abs(d1) / nl;
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            d2 += Math.pow((x.getObjective(i).minus(idealPoint2.getObjective(i)).doubleValue())
                    - d1 * (lambda.getObjective(i).div(nl).doubleValue()), 2.0);
        }
        d2 = Math.sqrt(d2);
        return (d1 + theta * d2);
    }

    private S computeNewPosition(ArrayList<S> phi_i) {

        // Chpse randomly alpha, beta, delta from phi
        int index;
        ArrayList<Integer> candidates = new ArrayList<>();
        do {
            index = Tools.getRandom().nextInt(phi_i.size());
        } while (!candidates.contains(index) && candidates.size() < 3);
        alphaWolf = phi_i.get(candidates.get(0));
        betaWolf = phi_i.get(candidates.get(1));
        deltaWolf = phi_i.get(candidates.get(2));
        double a, r1, r2, alpha_ij, beta_ij, delta_ij, x_ij;
        double A[] = new double[3];
        double C[] = new double[3];
        // a decreases linearly fron 2 to 0
        a = 2 - currentIteration * ((2.0) / MAX_ITERATIONS);
        S currentWolf = (S) alphaWolf.copy();

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

        return currentWolf;

    }

    protected S createIdealPoint() {
        S tmp = problem.randomSolution();
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            tmp.setObjective(i, new RealData(Double.MAX_VALUE));
        }
        tmp.setPenalties(RealData.ZERO);
        tmp.setNumberOfPenalties(0);
        return tmp;
    }

    @Override
    public ArrayList<S> getSolutions() {
        return this.solutions;
    }

    @Override
    public String toString() {
        return "MOGWO_D [maximumNumberOfReplacedSolutions=" + maximumNumberOfReplacedSolutions + ", neighborSize="
                + neighborSize + ", neighborhoodSelectionProbability=" + neighborhoodSelectionProbability
                + ", subPackSize=" + subPackSize + "]";
    }

}
