package com.castellanos94.operators;

import java.util.ArrayList;
import java.util.Iterator;

import com.castellanos94.solutions.Solution;

public abstract class SelectionOperator {
    protected ArrayList<Solution> parents;
    protected Iterator<Solution> iterator;

    public abstract void execute(ArrayList<Solution> solutions);

    public Solution getNextSolution() {
        if (iterator == null && parents == null)
            return null;
        else if (parents != null && iterator == null)
            iterator = parents.iterator();
        if (iterator.hasNext())
            return iterator.next();
        iterator = parents.iterator();
        return iterator.next();
    }
    public ArrayList<Solution> getParents() {
        return parents;
    }
}