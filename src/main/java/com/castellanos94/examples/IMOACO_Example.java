package com.castellanos94.examples;

import com.castellanos94.algorithms.multi.IMOACO_R;

import com.castellanos94.problems.benchmarks.dtlz.DTLZ1;
import com.castellanos94.solutions.DoubleSolution;

public class IMOACO_Example {
    public static void main(String[] args) {
        DTLZ1 problem = new DTLZ1();
        IMOACO_R<DoubleSolution> algorithm = new IMOACO_R<>(problem, 416, 0.1, 0.5, 14);
        algorithm.execute();
        System.out.println("Time " + algorithm.getComputeTime());
    }

}
