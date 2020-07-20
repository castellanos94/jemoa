package com.castellanos94.algorithms;

public interface AlgorithmBuilder<A extends AbstractAlgorithm> {
    A build();
}