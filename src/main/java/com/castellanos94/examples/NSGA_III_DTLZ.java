package com.castellanos94.examples;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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
import com.castellanos94.problems.benchmarks.dtlz.*;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Plotter;
import com.castellanos94.utils.Scatter3D;
import com.castellanos94.utils.Tools;

public class NSGA_III_DTLZ {
    static final String directory = "experiments" + File.separator + "dtlz";

    public static void main(String[] args) throws CloneNotSupportedException, IOException {
        Tools.setSeed(141414L);
        DTLZ problem = new DTLZ3();
        PrintStream console = System.out;
        PrintStream ps = new PrintStream(directory + File.separator + "resume_" + problem.getName());

        int EXPERIMENT = 1;
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
        AbstractEvolutionaryAlgorithm algorithm = new NSGA_III(problem, populationSize, maxIterations,
                numberOfDivisions, selection, crossover, mutation);

        System.setOut(console);
        System.out.println(algorithm);
        System.setOut(ps);
        System.out.println(algorithm);

        for (int i = 0; i < EXPERIMENT; i++) {
            algorithm = new NSGA_III(problem, populationSize, maxIterations, numberOfDivisions, selection, crossover,
                    mutation);

            algorithm.execute();
            averageTime += algorithm.getComputeTime();

            System.setOut(console);
            System.out.println(i + " time: " + algorithm.getComputeTime() + " ms.");
            System.setOut(ps);
            System.out.println(i + " time: " + algorithm.getComputeTime() + " ms.");
            bag.addAll(algorithm.getSolutions());
        }
        System.setOut(console);
        System.out.println("Total time: " + averageTime);
        System.out.println("Average time : " + (double) averageTime / EXPERIMENT + " ms.");
        System.out.println("Solutions in the bag: " + bag.size());

        System.setOut(ps);
        System.out.println("Total time: " + averageTime);
        System.out.println("Average time : " + (double) averageTime / EXPERIMENT + " ms.");
        System.out.println("Solutions in the bag: " + bag.size());

        Ranking compartor = new DominanceCompartor();
        compartor.computeRanking(bag);

        System.setOut(console);
        System.out.println("Fronts : " + compartor.getNumberOfSubFronts());
        System.out.println("Front 0: " + compartor.getSubFront(0).size());

        System.setOut(ps);
        System.out.println("Fronts : " + compartor.getNumberOfSubFronts());
        System.out.println("Front 0: " + compartor.getSubFront(0).size());

        File f = new File(directory);
        if (!f.exists())
            f.mkdirs();
        f = new File(directory + File.separator + "nsga_iii_bag_" + problem.getName() + "_f0.txt");

        ArrayList<String> strings = new ArrayList<>();
        for (Solution solution : compartor.getSubFront(0))
            strings.add(solution.toString());

        Files.write(f.toPath(), strings, Charset.defaultCharset());
        /*
         * for (Solution solution : solutions) { System.out.println(solution); }
         */
        Plotter plotter = new Scatter3D(compartor.getSubFront(0),
                directory + File.separator + "nsga3_" + problem.getName());
        plotter.plot();
        new Scatter3D(problem.getParetoOptimal3Obj(), directory + File.separator + problem.getName()).plot();

    }
}