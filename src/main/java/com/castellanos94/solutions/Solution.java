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
public class Solution implements Cloneable, Comparable<Solution> {
    protected ArrayList<Data> variables;
    protected ArrayList<Data> objectives;
    protected ArrayList<Data> resources;
    protected Data penalties;

    protected Problem problem;
    protected Integer n_penalties = 0;
    protected HashMap<String, Object> attributes;
    protected Data[] lowerBound;
    protected Data[] upperBound;

    protected int rank;

    public Solution(Problem problem) {
        this(problem.getNumberOfObjectives(), problem.getNumberOfDecisionVars(), problem.getNumberOfConstrains(),
                problem.getLowerBound(), problem.getUpperBound());
        this.problem = problem;

    }

    public Solution(int numberOfObjectives, int numberOfVariables, int numberOfResources, Data[] lowerBound,
            Data[] upperBound) {
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
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;

    }

    public Solution() {
        this.variables = new ArrayList<>();
        this.objectives = new ArrayList<>();
        this.resources = new ArrayList<>();
        attributes = new HashMap<>();
    }

    public void setVariables(int index, Data element) {
        this.variables.set(index, element);
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
    public void setVariable(ArrayList<Data> decision_vars) {
        this.variables = decision_vars;
    }

    /**
     * @return the decision_vars
     */
    public ArrayList<Data> getVariables() {
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

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() throws CloneNotSupportedException {
        Solution clone = new Solution(this.getProblem());
        clone.setVariable((ArrayList<Data>) this.getVariables().clone());
        clone.setObjectives((ArrayList<Data>) (this.getObjectives().clone()));
        clone.setResources((ArrayList<Data>) this.getResources().clone());
        clone.setRank(this.getRank());
        if (this.getPenalties() != null) {
            clone.setPenalties((Data) this.getPenalties().clone());
        }
        clone.setN_penalties(this.getN_penalties());
        if (this.attributes != null)
            clone.setAttributes((HashMap<String, Object>) this.getAttributes().clone());
        return clone;
    }

    @Override
    public int compareTo(Solution o) {
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

    public Data getVariable(int index) {
        return this.variables.get(index);
    }

    public Data getLowerBound(int i) {
        return this.lowerBound[i];
    }

    public Data getUpperBound(int i) {
        return this.upperBound[i];
    }

    public void setLowerBound(Data[] lowerBound) {
        this.lowerBound = lowerBound;
    }

    public void setUpperBound(Data[] upperBound) {
        this.upperBound = upperBound;
    }

    public void setVariable(int i, Data y) {
        this.variables.set(i, y);
    }

    public Data getObjective(int index) {
        return this.objectives.get(index);
    }

    public Data[] getLowerBound() {
        return this.lowerBound;
    }

    public Data[] getUpperBound() {
        return this.upperBound;
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
        Solution other = (Solution) obj;
        if (variables == null) {
            if (other.variables != null)
                return false;
        } else if (!variables.equals(other.variables))
            return false;
        return true;
    }

    public static void writSolutionsToFile(String string, ArrayList<Solution> solutions) throws IOException {
        ArrayList<String> strings = new ArrayList<>();
        File f = new File(string + ".out");
        for (Solution solution : solutions)
            strings.add(solution.toString());

        Files.write(f.toPath(), strings, Charset.defaultCharset());
    }

}