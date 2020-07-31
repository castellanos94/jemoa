package com.castellanos94.examples;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.algorithms.multi.NSGA_III;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceCompartor;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.operators.impl.PolynomialMutation;
import com.castellanos94.operators.impl.SBXCrossover;
import com.castellanos94.operators.impl.TournamentSelection;
import com.castellanos94.problems.Problem;
import com.castellanos94.problems.benchmarks.dtlz.DTLZ1;
import com.castellanos94.problems.benchmarks.dtlz.DTLZ2;
import com.castellanos94.problems.benchmarks.dtlz.DTLZ3;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Plotter;
import com.castellanos94.utils.Scatter3D;
import com.castellanos94.utils.Tools;

public class NSGA_IIIEXAMPLE {
    static final String directory = "experiments";

    public static void main(String[] args) throws CloneNotSupportedException, IOException {
        Tools.setSeed(141414L);
        Problem problem = new DTLZ3();
        int EXPERIMENT = 30;
        int populationSize = 100;
        int maxIterations = 300;
        int numberOfDivisions = 12;
        SelectionOperator selection;
        CrossoverOperator crossover;
        MutationOperator mutation;
        double crossoverProbability = 0.9;
        double crossoverDistributionIndex = 30.0;
        crossover = new SBXCrossover(crossoverDistributionIndex, crossoverProbability);

        double mutationProbability = 1.0 / problem.getNumberOfDecisionVars();
        double mutationDistributionIndex = 20.0;
        mutation = new PolynomialMutation(mutationDistributionIndex, mutationProbability);
        selection = new TournamentSelection(populationSize, new DominanceCompartor());

        ArrayList<Solution> bag = new ArrayList<>();
        long averageTime = 0;
        for (int i = 0; i < EXPERIMENT; i++) {
            AbstractEvolutionaryAlgorithm algorithm = new NSGA_III(problem, populationSize, maxIterations,
                    numberOfDivisions, selection, crossover, mutation);
            algorithm.execute();
            averageTime += algorithm.getComputeTime();
            System.out.println(i+" time: " + algorithm.getComputeTime() + " ms.");
            bag.addAll(algorithm.getSolutions());
        }
        System.out.println("Total time: "+averageTime);
        System.out.println("Average time : " + (double) averageTime / EXPERIMENT+" ms.");
        System.out.println("Solutions in the bag: " + bag.size());
        Ranking compartor = new DominanceCompartor();
        compartor.computeRanking(bag);
        System.out.println("Fronts : " + compartor.getNumberOfSubFronts());
        System.out.println("Front 0: " + compartor.getSubFront(0).size());
        File f = new File(directory);
        if(!f.exists())
            f.mkdirs();
        f = new File(directory +File.separator+"nsga_iii_bag_f0.txt");

        ArrayList<String> strings = new  ArrayList<>();
        for(Solution solution: compartor.getSubFront(0))
            strings.add(solution.toString());

        Files.write(f.toPath(),strings, Charset.defaultCharset());
        /*
         * for (Solution solution : solutions) { System.out.println(solution); }
         */
        Plotter plotter = new Scatter3D(compartor.getSubFront(0), "dtlz3-nsgaiii");
        plotter.plot();
       // new Scatter3D(DTLZ3.getParetoOptimal3Obj(),"dtlz3").plot();
        

    }
}