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
import com.castellanos94.utils.ReferenceHyperplane;
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
    protected ReferenceHyperplane<S> lambdas;
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

    @SuppressWarnings("unchecked")
    public MOGWO_D(Problem<S> problem, int neighborSize, double neighborhoodSelectionProbability, int MAX_ITERATIONS,
            int tSubPackSize, int maximumNumberOfReplacedSolutions, RepairOperator<S> repairOperator) {
        super(problem, neighborSize, MAX_ITERATIONS, tSubPackSize, repairOperator);
        this.distance = new Distance<>(Metric.EUCLIDEAN_DISTANCE);
        this.mutationOperator = (MutationOperator<S>) new PolynomialMutation();
        this.subPackSize = tSubPackSize;
        this.maximumNumberOfReplacedSolutions = maximumNumberOfReplacedSolutions;
        this.lambdas = new ReferenceHyperplane<>(problem.getNumberOfObjectives(), 13);
        this.lambdas.execute();
        this.neighborSize = neighborSize;
        this.neighborhoodSelectionProbability = neighborhoodSelectionProbability;
    }

    @Override
    @SuppressWarnings("unchecked")
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
        ArrayList<ArrayList<Integer>> neightborhood = new ArrayList<>(this.neighborSize);
        // Step 3 - 5
        for (int i = 0; i < this.neighborSize; i++) {
            ArrayList<Integer> bi = new ArrayList<>();
            List<Data> evaluate = distance.evaluateSolutionsToPoint(wolves, lambdas.get(i).getPoint());

            while (bi.size() < this.subPackSize) {
                int indexMin = -1;
                Data min = new RealData(Double.MAX_VALUE);
                for (int j = 0; j < evaluate.size(); j++) {
                    if (!bi.contains(j) && min.compareTo(evaluate.get(j)) > 0) {
                        indexMin = j;
                        min = evaluate.get(j);
                    }
                }
                bi.add(indexMin);
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
                    phi_i = new ArrayList<>();
                    for (Integer index : neightborhood.get(perm[i])) {
                        phi_i.add(wolves.get(index));
                    }
                } else {
                    phi_i = wolves;
                }
                // Step 11 - 12: calculate fitness and update alpha, beta and delta
                ArrayList<S> xnew = computeNewPosition(phi_i);
                for (S s : xnew) {
                    this.mutationOperator.execute(s);
                    problem.evaluate(s);
                    problem.evaluateConstraint(s);
                }
                // Step 13 update idealPoint
                for (S xnew_ : xnew) {
                    for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                        if (idealPoint.getObjective(j).compareTo(xnew_.getObjective(j)) > 0) {
                            idealPoint.setObjective(j, xnew_.getObjective(j).copy());
                        }
                    }
                }
                // Update neightboring subproblems
                int c = 0, intents = 0;
                while (c < maximumNumberOfReplacedSolutions && intents < 2) {
                    for (int j = 0; j < phi_i.size() && c < maximumNumberOfReplacedSolutions; j++) {
                        S xnew_ = xnew.get(c);
                        if (gpbi(xnew_, lambdas.get(i).getPoint(), idealPoint) < gpbi(phi_i.get(j),
                                lambdas.get(i).getPoint(), idealPoint)) {
                            phi_i.set(j, (S) xnew_.copy());
                            c++;
                        }
                    }
                    intents++;
                }

            }
            updateProgress();

        }
        for (int i = 0; i < this.neighborSize; i++) {
            for (Integer index : neightborhood.get(i)) {
                if (!solutions.contains(wolves.get(index)))
                    solutions.add(wolves.get(index));
            }
        }
        DominanceComparator<S> comparator = new DominanceComparator<>();
        comparator.computeRanking(solutions);
        this.solutions = comparator.getSubFront(0);
        computeTime = System.currentTimeMillis() - init_time;

    }

    private double gpbi(S x, List<Data> lambda, S idealPoint2) {
        double d1, d2, nl;
        double theta = 5.0;

        d1 = d2 = nl = 0.0;
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            d1 += x.getObjective(i).minus(idealPoint2.getObjective(i)).times(lambda.get(i)).doubleValue();
            nl += Math.pow(lambda.get(i).doubleValue(), 2.0);
        }
        nl = Math.sqrt(nl);
        d1 = Math.abs(d1) / nl;
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            d2 += Math.pow((x.getObjective(i).minus(idealPoint2.getObjective(i)).doubleValue())
                    - d1 * (lambda.get(i).div(nl).doubleValue()), 2.0);
        }
        d2 = Math.sqrt(d2);
        return (d1 + theta * d2);
    }

    @SuppressWarnings("unchecked")
    private ArrayList<S> computeNewPosition(ArrayList<S> phi_i) {

        // Chpse randomly alpha, beta, delta from phi
        int index;
        ArrayList<Integer> candidates = new ArrayList<>();
        do {
            index = Tools.getRandom().nextInt(phi_i.size());
            if (!candidates.contains(index))
                candidates.add(index);
        } while (candidates.size() < 3 + maximumNumberOfReplacedSolutions);
        alphaWolf = phi_i.get(candidates.get(0));
        betaWolf = phi_i.get(candidates.get(1));
        deltaWolf = phi_i.get(candidates.get(2));
        double a, r1, r2, alpha_ij, beta_ij, delta_ij, x_ij;
        double A[] = new double[3];
        double C[] = new double[3];
        // a decreases linearly fron 2 to 0
        a = 2 - currentIteration * ((2.0) / MAX_ITERATIONS);
        ArrayList<S> news = new ArrayList<>();
        for (int i = 0; i < maximumNumberOfReplacedSolutions; i++) {
            S currentWolf = (S) phi_i.get(candidates.get(3 + i)).copy();

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
            news.add(currentWolf);
        }

        return news;

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

    @Override
    public MOGWO_D<S> copy() {
        return new MOGWO_D<>(problem, neighborSize, neighborhoodSelectionProbability, MAX_ITERATIONS, subPackSize,
                maximumNumberOfReplacedSolutions, repairOperator);
    }

}
