package com.castellanos94.operators;

import com.castellanos94.solutions.Solution;

public interface MutationOperator {
    void execute(Solution solution) throws CloneNotSupportedException;
}