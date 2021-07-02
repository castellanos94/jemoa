package com.castellanos94.algorithms.multi;

import java.util.ArrayList;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.datatype.Data;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

/**
 * Qingfu Zhang, & Hui Li. (2007). MOEA/D: A Multiobjective Evolutionary
 * Algorithm Based on Decomposition. IEEE Transactions on Evolutionary
 * Computation, 11(6), 712–731. doi:10.1109/tevc.2007.892759
 */
public class MOEAD<S extends Solution<?>> extends AbstractEvolutionaryAlgorithm<S> {
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
    protected int currentIteration;

    /**
     * 
     * @param problem        MOP
     * @param MAX_ITERATIONS a stopping criterion
     * @param N              the numbre of the subproblems considered in MOEA/D
     * @param weightVectors  a uniform spread of N weight vectors
     * @param T              the number of the weight vectors in the neighborhood of
     *                       each weight vector
     */
    public MOEAD(Problem<S> problem, int MAX_ITERATIONS, int N, ArrayList<ArrayList<Data>> weightVectors, int T) {
        super(problem);
        this.N = N;
        this.MAX_ITERATIONS = MAX_ITERATIONS;
        this.lambda = weightVectors;
        this.T = T;
    }

    @Override
    protected void updateProgress() {
        this.currentIteration += 1;
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
        return currentIteration < MAX_ITERATIONS;
    }

}
