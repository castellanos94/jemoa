package com.castellanos94.algorithms;

import com.castellanos94.solutions.Solution;

public interface AlgorithmBuilder<A extends AbstractAlgorithm<Solution<?>>> {
    A build();
}