package com.castellanos94.examples;

import java.util.ArrayList;

import com.castellanos94.algorithms.AbstractAlgorithm;
import com.castellanos94.algorithms.multi.NSGA_II;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.operators.impl.PolynomialMutation;
import com.castellanos94.operators.impl.RepairBoundary;
import com.castellanos94.operators.impl.SBXCrossover;
import com.castellanos94.operators.impl.TournamentSelection;
import com.castellanos94.problems.benchmarks.ZDT1;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.Plotter;
import com.castellanos94.utils.Scatter2D;
import com.castellanos94.utils.Tools;

public class NSGA_IIExample {
    public static void main(String[] args) throws CloneNotSupportedException {
        Tools.setSeed(77003L);

        ZDT1 problem = new ZDT1();
        int maxIteration = 100000, popSize = 100;
        System.out.println(problem);
        SelectionOperator<DoubleSolution> selectionOperator = new TournamentSelection<>(popSize,
                new DominanceComparator<DoubleSolution>());
        // selectionOperator = new RandomSelection(popSize / 2);
        double crossoverProbability = 0.9;
        double crossoverDistributionIndex = 20.0;

        double mutationProbability = 1.0 / problem.getNumberOfDecisionVars();
        double mutationDistributionIndex = 20.0;
        CrossoverOperator<DoubleSolution> crossoverOperator = new SBXCrossover(crossoverDistributionIndex,
                crossoverProbability);
        MutationOperator<DoubleSolution> mutationOperator = new PolynomialMutation(mutationDistributionIndex,
                mutationProbability);
        AbstractAlgorithm<DoubleSolution> algorithm = new NSGA_II<>(problem, maxIteration, popSize, selectionOperator,
                crossoverOperator, mutationOperator, new RepairBoundary());
        System.out.println(algorithm);
        algorithm.execute();
        System.out.println(algorithm.getSolutions().size());
        ArrayList<DoubleSolution> solutions = algorithm.getSolutions();
        System.out.println("Time: " + algorithm.getComputeTime() + " ms.");
        Plotter plotter = new Scatter2D<DoubleSolution>(solutions, "zdt1-nsgaii");
        plotter.plot();

    }
}