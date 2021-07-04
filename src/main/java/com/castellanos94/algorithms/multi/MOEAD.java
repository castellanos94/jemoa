package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.List;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.datatype.Data;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.operators.impl.AdaptiveGrid;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Distance;
import com.castellanos94.utils.Tools;

import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Qingfu Zhang, & Hui Li. (2007). MOEA/D: A Multiobjective Evolutionary
 * Algorithm Based on Decomposition. IEEE Transactions on Evolutionary
 * Computation, 11(6), 712–731. doi:10.1109/tevc.2007.892759
 */
public class MOEAD<S extends Solution<?>> extends AbstractEvolutionaryAlgorithm<S> {
    public static enum APPROACH {
        TCHEBYCHEFF, WEIGHT_SUM, BOUNDARY_INTERSECTION
    };

    protected final APPROACH apporachUsed;
    /**
     * N: the numbre of the subproblems considered in MOEA/D
     */
    protected final int N;
    /**
     * Weight vectors
     */
    protected final ArrayList<ArrayList<Data>> lambda;
    /**
     * T: the number of the weight vectors in the neighborhood of each weight vector
     */
    protected final int T;
    /**
     * Max iterations stopping criterion
     */
    protected final int MAX_ITERATIONS;
    /**
     * B, only index save
     */
    protected int b[][];
    /**
     * Z
     */
    protected ArrayList<Data> idealPoint;
    protected int currentIteration;
    protected RepairOperator<S> repairOperator;
    protected DominanceComparator<S> dominanceComparator;
    protected AdaptiveGrid<S> adaptiveGrid;

    /**
     * 
     * @param problem             MOP
     * @param MAX_ITERATIONS      a stopping criterion
     * @param N                   the numbre of the subproblems considered in MOEA/D
     * @param weightVectors       a uniform spread of N weight vectors
     * @param T                   the number of the weight vectors in the
     *                            neighborhood of each weight vector
     * @param crossoverOperator   genetic operator
     * @param mutationOperator    genetic operator
     * @param repairOperator      repair or improvement operator
     * @param dominanceComparator @see {@link DominanceComparator}
     * @param apporachUsed        Approach to evaluete: @see {@link APPROACH}
     * @see AdaptiveGrid
     */
    public MOEAD(Problem<S> problem, int MAX_ITERATIONS, int N, ArrayList<ArrayList<Data>> weightVectors, int T,
            CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
            RepairOperator<S> repairOperator, DominanceComparator<S> dominanceComparator, APPROACH apporachUsed) {
        super(problem);
        this.N = N;
        this.populationSize = N;
        this.MAX_ITERATIONS = MAX_ITERATIONS;
        this.lambda = weightVectors;
        this.T = T;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.repairOperator = repairOperator;
        this.apporachUsed = apporachUsed;
        this.dominanceComparator = dominanceComparator;
        this.currentIteration = 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute() {
        this.init_time = System.currentTimeMillis();
        // Step 1: initialization
        this.adaptiveGrid = new AdaptiveGrid<>(problem, populationSize, this.dominanceComparator);

        b = new int[N][T];
        // Step 1.2: compute eculidean distance between any two weight vectors and then
        // work out the T closest weight vectors to each wegith vector
        for (int i = 0; i < b.length; i++) {
            List<Data> x = lambda.get(i);
            ArrayList<ImmutablePair<Data, Integer>> distance_ = new ArrayList<>();
            for (int j = 0; j < b.length; j++) {
                List<Data> y = lambda.get(j);
                if (i != j) {
                    distance_.add(new ImmutablePair<Data, Integer>(Distance.euclideanDistance(x, y), j));
                } else {
                    distance_.add(new ImmutablePair<Data, Integer>(Data.getZeroByType(x.get(0)), i));
                }
            }
            distance_.sort((a, b) -> a.left.compareTo(b.left));
            for (int j = 0; j < T; j++) {
                b[i][j] = distance_.get(j).getRight();
            }
        }
        // Step 1.3: generate an initial population x_1 to x_N
        ArrayList<S> FV = initPopulation();
        for (int i = 0; i < FV.size(); i++) {
            this.adaptiveGrid.addSolution((S) FV.get(i).copy());
        }
        // Step 1.4: initialize z
        this.idealPoint = new ArrayList<>(problem.getNumberOfObjectives());
        Data ref = FV.get(0).getObjective(0);
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            idealPoint.add(Data.initByRefType(ref,
                    (problem.getObjectives_type()[i] == Problem.MAXIMIZATION) ? -Double.MAX_VALUE : Double.MAX_VALUE));
        }
        updateUtopianPoint(FV);
        // Stopping criteria
        while (isStoppingCriteriaReached()) {
            for (int i = 0; i < N; i++) {
                // Step 2.1 & 2.2: reproduction & impovement
                ArrayList<S> parents = new ArrayList<>(2);
                int k = Tools.getRandom().nextInt(T), l = Tools.getRandom().nextInt(T);
                parents.add(FV.get(b[i][k]));
                parents.add(FV.get(b[i][l]));
                ArrayList<S> child = reproduction(parents);
                // Step 2.3: update of z
                updateUtopianPoint(child);
                // Step 2.4: update of neighboring solutions
                updateNeighboring(i, FV, child);
                // Step 2.5: update of EP
                replacement(null, child);
            }
            updateProgress();
        }
        this.solutions = this.adaptiveGrid.getParents();
        computeTime = System.currentTimeMillis() - init_time;
    }

    @SuppressWarnings("unchecked")
    protected void updateNeighboring(int i, ArrayList<S> FV, ArrayList<S> child) {
        for (int j = 0; j < T; j++) {
            for (int index = 0; index < child.size(); index++) {
                S y = child.get(index);
                S x = FV.get(b[i][j]);
                if (g(y, lambda.get(b[i][j])).compareTo(g(x, lambda.get(b[i][j]))) <= 0) {
                    FV.set(b[i][j], (S) y.copy());
                }
            }
        }
    }

    protected Data g(S x, ArrayList<Data> lambda_j) {
        if (apporachUsed == APPROACH.TCHEBYCHEFF) {
            return g_te(x, lambda_j);
        } else if (apporachUsed == APPROACH.WEIGHT_SUM) {
            return g_ws(x, lambda_j);
        } else {
            return g_bi(x, lambda_j);
        }
    }

    /**
     * Boundary Intersection approach
     * 
     * @param x
     * @param lambda_j
     * @return
     */
    private Data g_bi(S x, ArrayList<Data> lambda_j) {
        double theta = 0.5;
        Data d1 = (x.getObjective(0).minus(idealPoint.get(0))).times(lambda_j.get(0));
        Data nl = lambda_j.get(0).times(lambda_j.get(0));
        for (int i = 1; i < problem.getNumberOfObjectives(); i++) {
            d1 = d1.plus(x.getObjective(i).minus(idealPoint.get(i))).times(lambda_j.get(i));
            nl = nl.plus(lambda_j.get(i).times(lambda_j.get(i)));
        }
        nl = nl.sqrt();
        d1 = d1.abs().div(nl);
        Data d2 = (idealPoint.get(0).minus(idealPoint.get(0)).minus(d1.times(lambda_j.get(0).div(nl)))).pow(2);
        for (int i = 1; i < problem.getNumberOfObjectives(); i++) {
            d2 = d2.plus((idealPoint.get(0).minus(idealPoint.get(0)).minus(d1.times(lambda_j.get(0).div(nl)))).pow(2));
        }
        d2 = d2.sqrt();

        return d1.plus(theta).plus(d2);
    }

    /**
     * Weighted Sum Approach
     * 
     * @param x
     * @param lambda_j
     * @return
     */
    private Data g_ws(S x, ArrayList<Data> lambda_j) {
        Data rs = x.getObjective(0).times(lambda_j.get(0));
        for (int i = 1; i < problem.getNumberOfObjectives(); i++) {
            rs = rs.plus(x.getObjective(i).times(lambda_j.get(i)));
        }
        return rs;
    }

    /**
     * Tchebycheff approach
     * 
     * @param x
     * @param lambda_j
     * @return
     */
    private Data g_te(S x, ArrayList<Data> lambda_j) {
        Data rs = lambda_j.get(0).times(x.getObjective(0).minus(idealPoint.get(0).abs()));
        for (int i = 1; i < problem.getNumberOfObjectives(); i++) {
            rs = Data.getMax(rs, lambda_j.get(i).times((x.getObjective(i).minus(idealPoint.get(i))).abs()));
        }
        return rs;
    }

    protected void updateUtopianPoint(ArrayList<S> solutions) {
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            if (problem.getObjectives_type()[i] == Problem.MINIMIZATION) {
                for (S s : solutions) {
                    if (s.getObjective(i).compareTo(idealPoint.get(i)) < 0) {
                        idealPoint.set(i, s.getObjective(i));
                    }
                }
            } else {
                for (S s : solutions) {
                    if (s.getObjective(i).compareTo(idealPoint.get(i)) > 0) {
                        idealPoint.set(i, s.getObjective(i));
                    }
                }
            }
        }
    }

    @Override
    protected void updateProgress() {
        this.currentIteration += 1;
    }

    @Override
    protected ArrayList<S> reproduction(ArrayList<S> parents) {
        ArrayList<S> child = this.crossoverOperator.execute(parents);
        for (S s : child) {
            this.mutationOperator.execute(s);
            this.repairOperator.execute(s);
            this.problem.evaluate(s);
            this.problem.evaluateConstraint(s);
        }
        return child;
    }

    @Override
    protected ArrayList<S> replacement(ArrayList<S> population, ArrayList<S> offspring) {
        offspring.forEach(s -> adaptiveGrid.addSolution(s));
        /*
         * if (rs.size() > N) { ArrayList<ImmutablePair<Data, S>> data = new
         * ArrayList<>(); for (S tmpS : rs) { data.add(new ImmutablePair<Data,
         * S>(Distance.chebyshevDistance(tmpS.getObjectives(), idealPoint), tmpS)); }
         * data.sort((a, b) -> a.getLeft().compareTo(b.getLeft()));
         * 
         * tmp = new ArrayList<>(); for (ImmutablePair<Data, S> immutablePair :
         * data.subList(0, N)) { tmp.add(immutablePair.getRight()); } return tmp; }
         */
        return adaptiveGrid.getParents();
    }

    @Override
    protected boolean isStoppingCriteriaReached() {
        return currentIteration < MAX_ITERATIONS;
    }

    @Override
    public String toString() {
        return "MOEAD [MAX_ITERATIONS=" + MAX_ITERATIONS + ", N=" + N + ", T=" + T + ", apporachUsed=" + apporachUsed
                + "]";
    }

    @Override
    public MOEAD<S> copy() {
        return new MOEAD<>(problem, MAX_ITERATIONS, N, lambda, T, crossoverOperator, mutationOperator, repairOperator,
                dominanceComparator, apporachUsed);
    }

}
