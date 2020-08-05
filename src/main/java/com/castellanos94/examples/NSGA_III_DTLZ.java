package com.castellanos94.examples;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

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

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;

public class NSGA_III_DTLZ {
    static final String directory = "experiments" + File.separator + "dtlz";

    public static void main(String[] args) throws CloneNotSupportedException, IOException {
        // Tools.setSeed(141414L);
        Tools.setSeed(8435L);

        int EXPERIMENT = 1;
        int n_problem = 1;
        int number_of_objectives = 3;

        ArrayList<Solution> bag = new ArrayList<>();
        long averageTime = 0;
        AbstractEvolutionaryAlgorithm algorithm = DTLZ_TestSuite(n_problem, number_of_objectives);
        DTLZ problem = (DTLZ) algorithm.getProblem();
        PrintStream console = System.out;
        PrintStream ps = new PrintStream(
                directory + File.separator + "resume_" + problem.getName() + "_" + problem.getNumberOfObjectives());

        System.setOut(console);
        System.out.println(problem);
        System.out.println(algorithm);

        System.setOut(ps);
        System.out.println(problem);
        System.out.println(algorithm);

        for (int i = 0; i < EXPERIMENT; i++) {
            algorithm = DTLZ_TestSuite(n_problem, number_of_objectives);

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
        System.out.println("Total time: " + averageTime + " ms.");
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
        f = new File(directory + File.separator + "nsga_iii_bag_" + problem.getName() + "_f0_" + problem.getNumberOfObjectives());

        ArrayList<String> strings = new ArrayList<>();
        for (Solution solution : compartor.getSubFront(0))
            strings.add(solution.toString());

        Files.write(f.toPath(), strings, Charset.defaultCharset());
        if (problem.getNumberOfObjectives() == 3) {
            Plotter plotter = new Scatter3D(compartor.getSubFront(0),
                    directory + File.separator + problem.getName() + "_nsga3");
            plotter.plot();
            new Scatter3D(problem.getParetoOptimal3Obj(), directory + File.separator + problem.getName()).plot();
        } else {
            Table table = Table.create(problem.getName() + "_f0_" + problem.getNumberOfObjectives());
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                DoubleColumn column = DoubleColumn.create("objective_" + j);
                for (int k = 0; k < compartor.getSubFront(0).size(); k++) {
                    column.append(compartor.getSubFront(0).get(k).getObjective(j).doubleValue());
                }
                table.addColumns(column);
            }
            System.out.println(table.summary());
        }
    }

    private static AbstractEvolutionaryAlgorithm DTLZ_TestSuite(int p, int number_of_objectives) {

        HashMap<String, Object> options = setup(number_of_objectives);
        DTLZ problem = null;
        int maxIterations = 100;
        switch (p) {
            case 1:
                if (number_of_objectives == 3) {
                    problem = new DTLZ1();
                    maxIterations = 400;
                } else if (number_of_objectives == 5) {
                    problem = new DTLZ1(number_of_objectives, number_of_objectives + 5).setK(5);
                    maxIterations = 600;
                } else if (number_of_objectives == 8) {
                    problem = new DTLZ1(number_of_objectives, number_of_objectives + 5).setK(5);
                    maxIterations = 750;
                } else if (number_of_objectives == 10) {
                    problem = new DTLZ1(number_of_objectives, number_of_objectives + 5).setK(5);
                    maxIterations = 1000;
                } else if (number_of_objectives == 15) {
                    problem = new DTLZ1(number_of_objectives, number_of_objectives + 5).setK(5);
                    maxIterations = 1500;
                }
                break;
            case 2:
                if (number_of_objectives == 3) {
                    problem = new DTLZ2();
                    maxIterations = 250;
                } else if (number_of_objectives == 5) {
                    problem = new DTLZ2(number_of_objectives, number_of_objectives + 10).setK(10);
                    maxIterations = 350;
                } else if (number_of_objectives == 8) {
                    problem = new DTLZ2(number_of_objectives, number_of_objectives + 10).setK(10);
                    maxIterations = 500;
                } else if (number_of_objectives == 10) {
                    problem = new DTLZ2(number_of_objectives, number_of_objectives + 10).setK(10);
                    maxIterations = 750;
                } else if (number_of_objectives == 15) {
                    problem = new DTLZ2(number_of_objectives, number_of_objectives + 10).setK(10);
                    maxIterations = 1000;
                }
                break;
            case 3:
                if (number_of_objectives == 3) {
                    problem = new DTLZ3();
                    maxIterations = 1000;
                } else if (number_of_objectives == 5) {
                    problem = new DTLZ3(number_of_objectives, number_of_objectives + 10).setK(10);
                    maxIterations = 1000;
                } else if (number_of_objectives == 8) {
                    problem = new DTLZ3(number_of_objectives, number_of_objectives + 10).setK(10);
                    maxIterations = 1000;
                } else if (number_of_objectives == 10) {
                    problem = new DTLZ3(number_of_objectives, number_of_objectives + 10).setK(10);
                    maxIterations = 1500;
                } else if (number_of_objectives == 15) {
                    problem = new DTLZ3(number_of_objectives, number_of_objectives + 10).setK(10);
                    maxIterations = 2000;
                }
                break;
            case 4:
                if (number_of_objectives == 3) {
                    problem = new DTLZ4();
                    maxIterations = 600;
                } else if (number_of_objectives == 5) {
                    problem = new DTLZ4(number_of_objectives, number_of_objectives + 10).setK(10);
                    maxIterations = 1000;
                } else if (number_of_objectives == 8) {
                    problem = new DTLZ4(number_of_objectives, number_of_objectives + 10).setK(10);
                    maxIterations = 1250;
                } else if (number_of_objectives == 10) {
                    problem = new DTLZ4(number_of_objectives, number_of_objectives + 10).setK(10);
                    maxIterations = 2000;
                } else if (number_of_objectives == 15) {
                    problem = new DTLZ4(number_of_objectives, number_of_objectives + 10).setK(10);
                    maxIterations = 3000;
                }
                break;
            case 7:
                problem =new DTLZ7();
                maxIterations = 500;
        }

        SelectionOperator selectionOperator = new TournamentSelection((int) options.get("pop_size"),
                new DominanceCompartor());
        return new NSGA_III(problem, (int) options.get("pop_size"), maxIterations, (int) options.get("partitions"),
                selectionOperator, (CrossoverOperator) options.get("crossover"),
                (MutationOperator) options.get("mutation"));
    }

    private static HashMap<String, Object> setup(int number_of_objectives) {
        HashMap<String, Object> map = new HashMap<>();
        switch (number_of_objectives) {
            case 3:
                map.put("pop_size", 92);
                map.put("partitions", 12);
                break;
            case 5:
                map.put("pop_size", 212);
                map.put("partitions", 6);
                break;
            case 8:
                map.put("pop_size", 156);
                map.put("partitions", 5);
                break;
            case 10:
                map.put("pop_size", 271);
                map.put("partitions", 5);
                break;
            case 15:
                map.put("pop_size", 136);
                map.put("partitions", 3);
                break;
        }
        map.put("crossover", new SBXCrossover(30, 1.0));
        map.put("mutation", new PolynomialMutation());
        return map;
    }

}