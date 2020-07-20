package com.castellanos94.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.castellanos94.algorithms.AbstractAlgorithm;
import com.castellanos94.algorithms.single.GeneticAlgorithm;
import com.castellanos94.instances.KnapsackIntance;
import com.castellanos94.operators.BinaryMutation;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.RandomSelection;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.operators.UniformCrossover;
import com.castellanos94.problems.KnapsackProblem;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public class KnapsackExample {
    public static void main(String[] args) throws IOException, CloneNotSupportedException {
        Problem problem;// new KnapsackProblem( (KnapsackIntance)
                        // Tools.getInstanceFromResource(ProblemType.Knapsack, "Instancia10.txt"));
        problem = new KnapsackProblem((KnapsackIntance) new KnapsackIntance()
                .loadInstance("src/main/resources/instances/knapsack/Instancia10.txt"));
        int maxIteration = 1000, popSize = 100;
        System.out.println(problem);
        SelectionOperator selectionOperator = new RandomSelection(popSize / 2);
        CrossoverOperator crossoverOperator = new UniformCrossover();
        MutationOperator mutationOperator = new BinaryMutation(1.0 / popSize);
        AbstractAlgorithm algorithm = new GeneticAlgorithm(problem, maxIteration, popSize, selectionOperator,
                crossoverOperator, mutationOperator);
        System.out.println(algorithm);
        algorithm.execute();
        System.out.println("Time: " + algorithm.getComputeTime() + " ms.");
        System.out.println(algorithm.getSolutions().size());
        ArrayList<Solution> solutions = algorithm.getSolutions();
        Collections.sort(solutions, Comparator.comparing(Solution::getObjectives,(a,b)->{
            return a.get(0).compareTo(b.get(0));
        }).reversed());
        for (int i = 0; i < popSize; i++) {
            System.out.println(i + ") " + algorithm.getSolutions().get(i));
        }
    }
}