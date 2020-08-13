package com.castellanos94.solutions;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import com.castellanos94.datatype.Data;
import com.castellanos94.problems.Problem;

/*
*@author Castellanos Alvarez, Alejandro
*@since 22/03/202
*/
public abstract class Solution<T> implements Cloneable, Comparable<Solution<?>> {
    protected ArrayList<T> variables;
    protected ArrayList<Data> objectives;
    protected ArrayList<Data> resources;
    protected Data penalties;

    protected Problem problem;
    protected Integer n_penalties = 0;
    protected HashMap<String, Object> attributes;

    protected int rank;
    protected int numberOfObjectives;
    protected int numberOfResources;
    protected int numberOfVariables;

    public Solution(Problem problem) {
        this(problem.getNumberOfObjectives(), problem.getNumberOfDecisionVars(), problem.getNumberOfConstrains());
        this.problem = problem;

    }

    public Solution(int numberOfObjectives, int numberOfVariables, int numberOfResources) {
        this.variables = new ArrayList<>(numberOfObjectives);
        this.objectives = new ArrayList<>(numberOfObjectives);
        this.resources = new ArrayList<>(numberOfResources);

        for (int i = 0; i < numberOfVariables; i++) {
            variables.add(null);
        }
        for (int i = 0; i < numberOfObjectives; i++) {
            objectives.add(null);
        }
        for (int i = 0; i < numberOfResources; i++) {
            resources.add(null);
        }
        attributes = new HashMap<>();
        this.numberOfObjectives = numberOfObjectives;
        this.numberOfVariables = numberOfVariables;
        this.numberOfResources = numberOfResources;

    }

    public int getNumberOfObjectives() {
        return numberOfObjectives;
    }

    public int getNumberOfResources() {
        return numberOfResources;
    }

    public int getNumberOfVariables() {
        return numberOfVariables;
    }

    public void setNumberOfObjectives(int numberOfObjectives) {
        this.numberOfObjectives = numberOfObjectives;
    }

    public void setNumberOfResources(int numberOfResources) {
        this.numberOfResources = numberOfResources;
    }

    public void setNumberOfVariables(int numberOfVariables) {
        this.numberOfVariables = numberOfVariables;
    }

    public Solution() {
        this.variables = new ArrayList<>();
        this.objectives = new ArrayList<>();
        this.resources = new ArrayList<>();
        attributes = new HashMap<>();
    }

    public void setObjective(int index, Data element) {
        this.objectives.set(index, element);
    }

    public void setResource(int index, Data element) {
        this.resources.set(index, element);
    }

    /**
     * @return the objectives
     */
    public ArrayList<Data> getObjectives() {
        return objectives;
    }

    /**
     * @param objectives the objectives to set
     */
    public void setObjectives(ArrayList<Data> objectives) {
        this.objectives = objectives;
    }

    /**
     * @param decision_vars the decision_vars to set
     */
    public void setVariables(ArrayList<T> decision_vars) {
        this.variables = decision_vars;
    }

    /**
     * @return the decision_vars
     */
    public ArrayList<T> getVariables() {
        return this.variables;
    }

    /**
     * @param rank the rank to set
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * @return the rank
     */
    public Integer getRank() {
        return this.rank;
    }

    public ArrayList<Data> getResources() {
        return this.resources;
    }

    public void setResources(ArrayList<Data> resources) {
        this.resources = resources;
    }

    public Problem getProblem() {
        return this.problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public Integer getN_penalties() {
        return this.n_penalties;
    }

    public Data getPenalties() {
        return this.penalties;
    }

    public void setN_penalties(Integer n_penalties) {
        this.n_penalties = n_penalties;
    }

    public void setPenalties(Data penalties) {
        this.penalties = penalties;
    }

    public HashMap<String, Object> getAttributes() {
        return this.attributes;
    }

    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    public void setAttributes(HashMap<String, Object> properties) {
        this.attributes = properties;
    }

    @Override
    public abstract Object clone() throws CloneNotSupportedException;

    @Override
    public int compareTo(Solution<?> o) {
        if (rank > o.getRank())
            return 1;
        if (rank == o.getRank())
            return 0;
        return -1;
    }

    @Override
    public String toString() {
        return String.format("%s * %s * %s * %s * %3d", variables.toString().replace("[", "").replace("]", ""),
                objectives.toString().replace("[", "").replace("]", ""),
                resources.toString().replace("[", "").replace("]", ""), penalties, n_penalties);
        // return objectives.toString();
    }

    public T getVariable(int index) {
        return this.variables.get(index);
    }

    public void setVariable(int i, T value) {
        this.variables.set(i, value);
    }

    public Data getObjective(int index) {
        return this.objectives.get(index);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((variables == null) ? 0 : variables.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Solution<?> other = (Solution<?>) obj;
        if (variables == null) {
            if (other.variables != null)
                return false;
        } else if (!variables.equals(other.variables))
            return false;
        return true;
    }

    @SuppressWarnings("rawtypes")
    public static void writSolutionsToFile(String string, ArrayList<Solution> solutions) throws IOException {
        ArrayList<String> strings = new ArrayList<>();
        File f = new File(string + ".out");
        for (Solution solution : solutions)
            strings.add(solution.toString());

        Files.write(f.toPath(), strings, Charset.defaultCharset());
    }

}