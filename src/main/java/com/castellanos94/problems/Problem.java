package com.castellanos94.problems;

import java.util.Arrays;

import com.castellanos94.datatype.Data;
import com.castellanos94.instances.Instance;
import com.castellanos94.solutions.Solution;


/*
*@author Castellanos Alvarez, Alejandro
*@since 22/03/202
*/
public abstract class Problem {
    public static int MAXIMIZATION = 1;
    public static int MINIMIZATION = -1;
    protected int numberOfObjectives;
    protected int numberOfDecisionVars;
    protected int numberOfConstrains;
    protected int[] objectives_type;
    protected int problem_type;
    protected Instance instance;
    protected Data[] lowerBound;
    protected Data[] upperBound;

    public abstract void evaluate(Solution object);

    public abstract int evaluateConstraints(Solution object);

    public abstract Solution randomSolution();

    /**
     * @return the instance
     */
    public Instance getInstance() {
        return instance;
    }

    public int getNumberOfConstrains() {
        return numberOfConstrains;
    }
    public int getNumberOfDecisionVars() {
        return numberOfDecisionVars;
    }
    public int getNumberOfObjectives() {
        return numberOfObjectives;
    }
    /**
     * @param instance the instance to set
     */
    public void setInstance(Instance instance) {
        this.instance = instance;
    }
    public void setNumberOfConstrains(int numberOfConstrains) {
        this.numberOfConstrains = numberOfConstrains;
    }
    public void setNumberOfDecisionVars(int numberOfDecisionVars) {
        this.numberOfDecisionVars = numberOfDecisionVars;
    }
    public void setNumberOfObjectives(int numberOfObjectives) {
        this.numberOfObjectives = numberOfObjectives;
    }

    /**
     * @return the objectives_type
     */
    public int[] getObjectives_type() {
        return objectives_type;
    }
    /**
     * @param objectives_type the objectives_type to set
     */
    public void setObjectives_type(int[] objectives_type) {
        this.objectives_type = objectives_type;
    }

    /**
     * @return the problem_type
     */
    public int getProblem_type() {
        return problem_type;
    }

    /**
     * @param problem_type the problem_type to set
     */
    public void setProblem_type(int problem_type) {
        this.problem_type = problem_type;
    }
    public Data[] getLowerBound() {
        return lowerBound;
    }
    public Data[] getUpperBound() {
        return upperBound;
    }
    public void setUpperBound(Data[] upperBound) {
        this.upperBound = upperBound;
    }
    public void setLowerBound(Data[] lowerBound) {
        this.lowerBound = lowerBound;
    }
    @Override
    public String toString() {
        return "Problem [\ninstance=" + instance + ",\nnConstraints=" + numberOfConstrains + ",\nnDecisionVars=" + numberOfDecisionVars
                + ",\nnObjectives=" + numberOfObjectives + ",\nobjectives_type=" + Arrays.toString(objectives_type)
                + ",\nproblem_type=" + problem_type + "\n]";
    }

    
}