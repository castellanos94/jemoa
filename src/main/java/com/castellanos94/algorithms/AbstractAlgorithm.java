package com.castellanos94.algorithms;

import java.util.ArrayList;

import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public abstract class AbstractAlgorithm<S extends Solution<?>> {
    protected Problem<S> problem;
    protected ArrayList<S> solutions;
    protected long init_time;
    protected long computeTime;

    public AbstractAlgorithm(Problem<S> problem) {
        this.problem = problem;
    }

    public abstract void execute();

    public ArrayList<S> getSolutions() {
        return solutions;
    }

    /**
     * Execution time
     * 
     * @return miliseconds
     */
    public long getComputeTime() {
        return computeTime;
    }

    public long getInit_time() {
        return init_time;
    }

    public Problem<S> getProblem() {
        return problem;
    }

    public abstract AbstractAlgorithm<S> copy();
}