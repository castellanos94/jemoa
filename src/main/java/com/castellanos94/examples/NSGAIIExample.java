package com.castellanos94.examples;

import java.util.ArrayList;

import com.castellanos94.algorithms.AbstractAlgorithm;
import com.castellanos94.algorithms.multi.NSGAII;
import com.castellanos94.components.impl.DominanceCompartor;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.operators.impl.PolyMutation;
import com.castellanos94.operators.impl.SBXCrossover;
import com.castellanos94.operators.impl.TournamentSelection;
import com.castellanos94.problems.Problem;
import com.castellanos94.problems.benchmarks.DTLZ1;
import com.castellanos94.problems.benchmarks.ZDT1;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Plotter;
import com.castellanos94.utils.Scatter2D;

public class NSGAIIExample {
    public static void main(String[] args) throws CloneNotSupportedException {
        Problem problem = new DTLZ1();
        int maxIteration = 50000, popSize = 100;
        System.out.println(problem);
        SelectionOperator selectionOperator = new TournamentSelection(popSize, new DominanceCompartor());
        // selectionOperator = new RandomSelection(popSize / 2);
        double crossoverProbability = 0.9;
        double crossoverDistributionIndex = 20.0;

        double mutationProbability = 1.0 / problem.getNumberOfDecisionVars();
        double mutationDistributionIndex = 20.0;
        CrossoverOperator crossoverOperator = new SBXCrossover(crossoverDistributionIndex, crossoverProbability);
        MutationOperator mutationOperator = new PolyMutation(mutationDistributionIndex, mutationProbability);
        AbstractAlgorithm algorithm = new NSGAII(problem, maxIteration, popSize, selectionOperator, crossoverOperator,
                mutationOperator);
        System.out.println(algorithm);
        algorithm.execute();
        System.out.println(algorithm.getSolutions().size());
        ArrayList<Solution> solutions = algorithm.getSolutions();
        System.out.println("Time: " + algorithm.getComputeTime() + " ms.");
        Plotter plotter = new Scatter2D(solutions, "dtlz1-nsgaii");
        plotter.plot();

    }
}