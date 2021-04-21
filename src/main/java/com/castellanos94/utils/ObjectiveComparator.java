package com.castellanos94.utils;

import java.util.Comparator;

import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public class ObjectiveComparator implements Comparator<Solution<?>> {
    protected int index;
    protected ORDER type;

    public enum ORDER {
        ASC, DESC
    };

    public ObjectiveComparator(int index) {
        this.index = index;
    }

    public ObjectiveComparator(int index, ORDER type) {
        this.index = index;
        this.type = type;
    }

    @Override
    public int compare(Solution<?> a, Solution<?> b) {
        if (type == null) {
            if (a.getProblem().getObjectives_type()[index] == Problem.MINIMIZATION) {
                type = ORDER.ASC;
            } else {
                type = ORDER.DESC;
            }
        }

        return (type == ORDER.ASC) ? a.getObjective(index).minus(b.getObjective(index)).intValue()
                : b.getObjective(index).minus(a.getObjective(index)).intValue();
    }

    @Override
    public String toString() {
        return "ObjectiveComparator [index=" + index + ", type=" + type + "]";
    }

    public int getIndex() {
        return index;
    }

    public ORDER getType() {
        return type;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setType(ORDER type) {
        this.type = type;
    }
}