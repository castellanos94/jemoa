package com.castellanos94.operators;

import com.castellanos94.solutions.Solution;

public interface ArchiveSelection<S extends Solution<?>> extends SelectionOperator<S> {
    public void addSolution(S solution);
}
