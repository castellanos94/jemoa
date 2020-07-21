package com.castellanos94.examples;

import java.util.ArrayList;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.algorithms.multi.NSGAIII;
import com.castellanos94.components.impl.DominanceCompartor;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.operators.impl.PolyMutation;
import com.castellanos94.operators.impl.SBXCrossover;
import com.castellanos94.operators.impl.TournamentSelection;
import com.castellanos94.problems.Problem;
import com.castellanos94.problems.benchmarks.DTLZ1;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Plotter;
import com.castellanos94.utils.Scatter3D;

public class NSGAIIIEXAMPLE {
    public static void main(String[] args) throws CloneNotSupportedException {
        Problem problem = new DTLZ1();
        int populationSize = 100;
        int maxIterations = 300;
        int numberOfDivisions = 12;
        SelectionOperator selection;
        CrossoverOperator crossover;
        MutationOperator mutation;
        double crossoverProbability = 0.9;
        double crossoverDistributionIndex = 30.0;
        crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

        double mutationProbability = 1.0 / problem.getNumberOfDecisionVars();
        double mutationDistributionIndex = 20.0;
        mutation = new PolyMutation(mutationProbability, mutationDistributionIndex);
        selection = new TournamentSelection(populationSize, new DominanceCompartor());

        AbstractEvolutionaryAlgorithm algorithm = new NSGAIII(problem, populationSize, maxIterations, numberOfDivisions,
                selection, crossover, mutation);
        System.out.println(algorithm);
        algorithm.execute();
        System.out.println(algorithm.getSolutions().size());
        ArrayList<Solution> solutions = algorithm.getSolutions();
        System.out.println("Time: " + algorithm.getComputeTime() + " ms.");
        Plotter plotter = new Scatter3D(solutions, "dtlz1-nsgaiii");
        plotter.plot();

    }
}