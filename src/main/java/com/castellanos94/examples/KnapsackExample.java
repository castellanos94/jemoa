package com.castellanos94.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.castellanos94.algorithms.AbstractAlgorithm;
import com.castellanos94.algorithms.single.GeneticAlgorithm;
import com.castellanos94.instances.KnapsackIntance;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.operators.impl.BinaryMutation;
import com.castellanos94.operators.impl.RandomSelection;
import com.castellanos94.operators.impl.HUXCrossover;
import com.castellanos94.problems.KnapsackProblem;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.BinarySolution;

public class KnapsackExample {
    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        Problem<BinarySolution> problem;
        problem = new KnapsackProblem(
                (KnapsackIntance) new KnapsackIntance("src/main/resources/instances/knapsack/Instancia10.txt")
                        .loadInstance());

        int maxIteration = 1000;
        int popSize = 100;
        System.out.println(problem);
        SelectionOperator<BinarySolution> selectionOperator = new RandomSelection<>(popSize / 2);
        CrossoverOperator<BinarySolution> crossoverOperator = new HUXCrossover();
        MutationOperator<BinarySolution> mutationOperator = new BinaryMutation(1.0 / popSize);
        AbstractAlgorithm<BinarySolution> algorithm = new GeneticAlgorithm<>(problem, maxIteration, popSize, selectionOperator,
                crossoverOperator, mutationOperator);
        System.out.println(algorithm);
        algorithm.execute();
        System.out.println("Time: " + algorithm.getComputeTime() + " ms.");
        System.out.println(algorithm.getSolutions().size());
        ArrayList<BinarySolution> solutions = algorithm.getSolutions();
        Collections.sort(solutions, Comparator.comparing(BinarySolution::getObjectives, (a, b) -> {
            return a.get(0).compareTo(b.get(0));
        }).reversed());
        for (int i = 0; i < popSize; i++) {
            System.out.println(i + ") " + algorithm.getSolutions().get(i));
        }
    }
}