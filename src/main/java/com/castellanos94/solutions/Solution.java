package com.castellanos94.solutions;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.castellanos94.datatype.Data;
import com.castellanos94.problems.Problem;

/*
*@author Castellanos Alvarez, Alejandro
*@since 22/03/202
*/
public abstract class Solution<T> implements Comparable<Solution<?>> {
    protected List<T> variables;
    protected List<Data> objectives;
    protected List<Data> resources;
    protected Data penalties;

    protected Problem<?> problem;
    protected Integer numberOfPenalties = 0;
    protected HashMap<String, Object> attributes;

    protected int rank;
    protected int numberOfObjectives;
    protected int numberOfResources;
    protected int numberOfVariables;

    public Solution(Problem<?> problem) {
        this(problem.getNumberOfObjectives(), problem.getNumberOfDecisionVars(), problem.getNumberOfConstrains());
        this.problem = problem;
        this.rank = 0;

    }

    public Solution(int numberOfObjectives, int numberOfVariables, int numberOfResources) {
        this.variables = new ArrayList<>(numberOfVariables);
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
        this.rank = 0;

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
    public List<Data> getObjectives() {
        return objectives;
    }

    /**
     * @param objectives the objectives to set
     */
    public void setObjectives(List<Data> objectives) {
        this.objectives = objectives;
    }

    /**
     * @param decision_vars the decision_vars to set
     */
    public void setVariables(List<T> decision_vars) {
        this.variables = decision_vars;
    }

    /**
     * @return the decision_vars
     */
    public List<T> getVariables() {
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

    public List<Data> getResources() {
        return this.resources;
    }

    public void setResources(List<Data> resources) {
        this.resources = resources;
    }

    public Problem<?> getProblem() {
        return this.problem;
    }

    public void setProblem(Problem<?> problem) {
        this.problem = problem;
    }

    public Integer getNumberOfPenalties() {
        return this.numberOfPenalties;
    }

    public Data getPenalties() {
        return this.penalties;
    }

    public void setNumberOfPenalties(Integer numberOfPenalties) {
        this.numberOfPenalties = numberOfPenalties;
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

    public abstract Object copy();

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
                resources.toString().replace("[", "").replace("]", ""), penalties, numberOfPenalties);
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

    public Data getResource(int index) {
        return this.resources.get(index);
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
    public static void writSolutionsToFile(String string, List<Solution> solutions) throws IOException {
        List<String> strings = new ArrayList<>();
        File f = new File(string + ".out");

        for (Solution solution : solutions)
            strings.add(solution.toString());

        Files.write(f.toPath(), strings, Charset.defaultCharset());
    }

    public static boolean compareByObjective(DoubleSolution a, DoubleSolution b) {
        if (a.getNumberOfObjectives() != b.getNumberOfObjectives()) {
            throw new IllegalArgumentException("Solutions must be equal number of objectives.");
        }
        int count = 0;
        for (Data data : b.getObjectives()) {
            boolean flag = false;
            for (Data c : a.getObjectives()) {
                if (c.compareTo(data) == 0) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                count++;
            }            
        }
        
        return count == 0;
    }

}