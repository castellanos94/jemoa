package com.castellanos94.examples;

import java.util.ArrayList;

import com.castellanos94.algorithms.AbstractAlgorithm;
import com.castellanos94.algorithms.multi.MuGeneticAlgorithm;
import com.castellanos94.components.impl.DominanceCompartor;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.operators.impl.SBXCrossover;
import com.castellanos94.operators.impl.SimpleDecimalMutation;
import com.castellanos94.operators.impl.TournamentSelection;
import com.castellanos94.problems.Problem;
import com.castellanos94.problems.benchmarks.ZDT1;
import com.castellanos94.solutions.Solution;

public class GAMultiExample {
    public static void main(String[] args) throws CloneNotSupportedException {
        Problem problem = new ZDT1();
        int maxIteration = 50000, popSize = 100;
        System.out.println(problem);
        SelectionOperator selectionOperator = new TournamentSelection(popSize, new DominanceCompartor());
        double crossoverProbability = 0.9;
        double crossoverDistributionIndex = 20.0;

        CrossoverOperator crossoverOperator = new SBXCrossover(crossoverDistributionIndex, crossoverProbability);
        MutationOperator mutationOperator = new SimpleDecimalMutation(1.0 / problem.getNumberOfDecisionVars());
        AbstractAlgorithm algorithm = new MuGeneticAlgorithm(problem, maxIteration, popSize, selectionOperator,
                crossoverOperator, mutationOperator);
        System.out.println(algorithm);
        algorithm.execute();
        System.out.println("Time: " + algorithm.getComputeTime() + " ms.");
        System.out.println(algorithm.getSolutions().size());
        ArrayList<Solution> solutions = algorithm.getSolutions();
        for (int i = 0; i < popSize; i++) {
            System.out.println(i + ") " + solutions.get(i));
        }
        System.out.println("Time: " + algorithm.getComputeTime() + " ms.");

    }
}