package com.castellanos94.solutions;

import java.util.ArrayList;
import java.util.HashMap;

import com.castellanos94.datatype.Data;
import com.castellanos94.problems.Problem;

/*
*@author Castellanos Alvarez, Alejandro
*@since 22/03/202
*/
public class Solution implements Cloneable, Comparable<Solution> {
    protected ArrayList<Data> decision_vars;
    protected ArrayList<Data> objectives;
    protected ArrayList<Data> resources;
    protected Data penalties;

    protected Problem problem;
    protected Integer n_penalties = 0;
    protected HashMap<String, Object> properties;

    protected int rank;

    public Solution(Problem problem) {
        this.problem = problem;
        this.decision_vars = new ArrayList<>(problem.getNumberOfDecisionVars());
        this.objectives = new ArrayList<>(problem.getNumberOfObjectives());
        this.resources = new ArrayList<>(problem.getNumberOfConstrains());
        for (int i = 0; i < problem.getNumberOfDecisionVars(); i++) {
            decision_vars.add(null);
        }
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            objectives.add(null);
        }
        for (int i = 0; i < problem.getNumberOfConstrains(); i++) {
            resources.add(null);
        }
        properties = new HashMap<>();
    }

    public Solution(int n_objectives, int n_decision_vars) {
        this.decision_vars = new ArrayList<>(n_decision_vars);
        this.objectives = new ArrayList<>(n_objectives);
        for (int i = 0; i < n_decision_vars; i++) {
            decision_vars.add(null);
        }
        for (int i = 0; i < n_objectives; i++) {
            objectives.add(null);
        }
        properties = new HashMap<>();

    }

    public Solution() {
        properties = new HashMap<>();
    }

    public void setDecisionVar(int index, Data element) {
        this.decision_vars.set(index, element);
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
    public void setDecision_vars(ArrayList<Data> decision_vars) {
        this.decision_vars = decision_vars;
    }

    /**
     * @return the decision_vars
     */
    public ArrayList<Data> getDecision_vars() {
        return decision_vars;
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
        return rank;
    }

    public ArrayList<Data> getResources() {
        return resources;
    }

    public void setResources(ArrayList<Data> resources) {
        this.resources = resources;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public Integer getN_penalties() {
        return n_penalties;
    }

    public Data getPenalties() {
        return penalties;
    }

    public void setN_penalties(Integer n_penalties) {
        this.n_penalties = n_penalties;
    }

    public void setPenalties(Data penalties) {
        this.penalties = penalties;
    }

    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Solution clone = new Solution(this.getProblem());
        clone.setDecision_vars((ArrayList<Data>) this.getDecision_vars().clone());
        clone.setObjectives((ArrayList<Data>) (this.getObjectives().clone()));
        clone.setResources((ArrayList<Data>) this.getResources().clone());
        clone.setRank(this.getRank());
        if (this.getPenalties() != null) {
            clone.setPenalties((Data) this.getPenalties().clone());
        }
        clone.setN_penalties(this.getN_penalties());
        if (this.properties != null)
            clone.setProperties((HashMap<String, Object>) this.getProperties().clone());
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((decision_vars == null) ? 0 : decision_vars.hashCode());
        result = prime * result + ((n_penalties == null) ? 0 : n_penalties.hashCode());
        result = prime * result + ((objectives == null) ? 0 : objectives.hashCode());
        result = prime * result + ((penalties == null) ? 0 : penalties.hashCode());
        result = prime * result + ((problem == null) ? 0 : problem.hashCode());
        result = prime * result + rank;
        result = prime * result + ((resources == null) ? 0 : resources.hashCode());
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
        if (decision_vars == null) {
            if (other.decision_vars != null)
                return false;
        } else if (!decision_vars.equals(other.decision_vars))
            return false;
        if (n_penalties == null) {
            if (other.n_penalties != null)
                return false;
        } else if (!n_penalties.equals(other.n_penalties))
            return false;
        if (objectives == null) {
            if (other.objectives != null)
                return false;
        } else if (!objectives.equals(other.objectives))
            return false;
        if (penalties == null) {
            if (other.penalties != null)
                return false;
        } else if (!penalties.equals(other.penalties))
            return false;
        if (problem == null) {
            if (other.problem != null)
                return false;
        } else if (!problem.equals(other.problem))
            return false;
        if (rank != other.rank)
            return false;
        if (resources == null) {
            if (other.resources != null)
                return false;
        } else if (!resources.equals(other.resources))
            return false;
        return true;
    }

    @Override
    public String toString() {
        // return String.format("%s * %s * %s * %s * %3d", decision_vars, objectives,
        // resources, penalties, n_penalties);
        return objectives.toString();
    }

    public Data getVariable(int index) {
        return decision_vars.get(index);
    }

    public Data getLowerBound(int i) {
        return problem.getLowerBound()[i];
    }

    public Data getUpperBound(int i) {
        return problem.getUpperBound()[i];
    }

}