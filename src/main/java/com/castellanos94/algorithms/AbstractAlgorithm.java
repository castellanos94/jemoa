package com.castellanos94.algorithms;

import java.util.ArrayList;

import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public abstract class AbstractAlgorithm {
    protected Problem problem;
    protected ArrayList<Solution> solutions;
    protected long init_time;
    protected long computeTime;

    public AbstractAlgorithm(Problem problem) {
        this.problem = problem;
    }

    public abstract void execute();

    public ArrayList<Solution> getSolutions() {
        return solutions;
    }

    public long getComputeTime() {
        return computeTime;
    }

    public long getInit_time() {
        return init_time;
    }
    public Problem getProblem() {
        return problem;
    }
}