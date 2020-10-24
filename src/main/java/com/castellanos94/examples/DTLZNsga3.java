package com.castellanos94.examples;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.castellanos94.algorithms.multi.NSGA_III;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.operators.impl.PolynomialMutation;
import com.castellanos94.operators.impl.SBXCrossover;
import com.castellanos94.operators.impl.TournamentSelection;
import com.castellanos94.problems.benchmarks.dtlz.*;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.Plotter;
import com.castellanos94.utils.Scatter3D;
import com.castellanos94.utils.Tools;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;

public class DTLZNsga3 {
    static final String DIRECTORY = "experiments" + File.separator + "dtlz";
    static final int EXPERIMENT = 50;

    public static void main(String[] args) throws CloneNotSupportedException, IOException {
        Tools.setSeed(1l);

        int numberProblem = 3;
        int numberOfObjectives = 3;

        ArrayList<DoubleSolution> bag = new ArrayList<>();
        long averageTime = 0;
        // 1,3
        NSGA_III<DoubleSolution> algorithm = dtlzTestSuite(numberProblem, numberOfObjectives);

        DTLZ problem = (DTLZ) algorithm.getProblem();
        PrintStream console = System.out;
        PrintStream ps = new PrintStream(
                DIRECTORY + File.separator + "resume_" + problem.getName() + "_" + problem.getNumberOfObjectives());

        System.setOut(console);
        System.out.println(problem);
        System.out.println(algorithm);

        System.setOut(ps);
        System.out.println(problem);
        System.out.println(algorithm);
        ArrayList<Integer> range = new ArrayList<>(IntStream.range(0, EXPERIMENT).boxed().collect(Collectors.toList()));
        ArrayList<Long> time = new ArrayList<>();
        range.stream().parallel().forEach(i -> {
            NSGA_III<DoubleSolution> a = dtlzTestSuite(numberProblem, numberOfObjectives);

            try {
                a.execute();
            } catch (CloneNotSupportedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            time.add(a.getComputeTime());

            System.setOut(console);
            System.out.println(i + " time: " + a.getComputeTime() + " ms.");
            System.setOut(ps);
            System.out.println(i + " time: " + a.getComputeTime() + " ms.");
            bag.addAll(a.getSolutions());
        });
        System.setOut(console);
        averageTime = time.stream().mapToLong(v -> v.longValue()).sum();
        System.out.println("Total time: " + averageTime);
        System.out.println("Average time : " + (double) averageTime / EXPERIMENT + " ms.");
        System.out.println("Solutions in the bag: " + bag.size());

        System.setOut(ps);
        System.out.println("Total time: " + averageTime + " ms.");
        System.out.println("Average time : " + (double) averageTime / EXPERIMENT + " ms.");
        System.out.println("Solutions in the bag: " + bag.size());

        Ranking<DoubleSolution> compartor = new DominanceComparator<>();
        compartor.computeRanking(bag);

        System.setOut(console);
        System.out.println("Fronts : " + compartor.getNumberOfSubFronts());
        System.out.println("Front 0: " + compartor.getSubFront(0).size());

        System.setOut(ps);
        System.out.println("Fronts : " + compartor.getNumberOfSubFronts());
        System.out.println("Front 0: " + compartor.getSubFront(0).size());

        File f = new File(DIRECTORY);
        if (!f.exists())
            f.mkdirs();
        f = new File(DIRECTORY + File.separator + "nsga_iii_bag_" + problem.getName() + "_f0_"
                + problem.getNumberOfObjectives());

        ArrayList<String> strings = new ArrayList<>();
        for (DoubleSolution solution : compartor.getSubFront(0))
            strings.add(solution.toString());

        Files.write(f.toPath(), strings, Charset.defaultCharset());
        if (problem.getNumberOfObjectives() == 3) {
            Plotter plotter = new Scatter3D<>(compartor.getSubFront(0),
                    DIRECTORY + File.separator + problem.getName() + "_nsga3");
            plotter.plot();
            // new Scatter3D(problem.getParetoOptimal3Obj(), directory + File.separator +
            // problem.getName()).plot();
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

    @SuppressWarnings("unchecked")
    private static NSGA_III<DoubleSolution> dtlzTestSuite(int p, int numberOfObjectives) {

        HashMap<String, Object> options = setup(numberOfObjectives);
        DTLZ problem = null;
        int maxIterations = 100;
        switch (p) {
            case 1:
                if (numberOfObjectives == 3) {
                    problem = new DTLZ1();
                    maxIterations = 400;
                } else if (numberOfObjectives == 5) {
                    problem = new DTLZ1(numberOfObjectives, numberOfObjectives + 5).setK(5);
                    maxIterations = 600;
                } else if (numberOfObjectives == 8) {
                    problem = new DTLZ1(numberOfObjectives, numberOfObjectives + 5).setK(5);
                    maxIterations = 750;
                } else if (numberOfObjectives == 10) {
                    problem = new DTLZ1(numberOfObjectives, numberOfObjectives + 5).setK(5);
                    maxIterations = 1000;
                } else if (numberOfObjectives == 15) {
                    problem = new DTLZ1(numberOfObjectives, numberOfObjectives + 5).setK(5);
                    maxIterations = 1500;
                }
                break;
            case 2:
                if (numberOfObjectives == 3) {
                    problem = new DTLZ2();
                    maxIterations = 250;
                } else if (numberOfObjectives == 5) {
                    problem = new DTLZ2(numberOfObjectives, numberOfObjectives + 10).setK(10);
                    maxIterations = 350;
                } else if (numberOfObjectives == 8) {
                    problem = new DTLZ2(numberOfObjectives, numberOfObjectives + 10).setK(10);
                    maxIterations = 500;
                } else if (numberOfObjectives == 10) {
                    problem = new DTLZ2(numberOfObjectives, numberOfObjectives + 10).setK(10);
                    maxIterations = 750;
                } else if (numberOfObjectives == 15) {
                    problem = new DTLZ2(numberOfObjectives, numberOfObjectives + 10).setK(10);
                    maxIterations = 1000;
                }
                break;
            case 3:
                if (numberOfObjectives == 3) {
                    problem = new DTLZ3();
                    maxIterations = 1000;
                } else if (numberOfObjectives == 5 || numberOfObjectives == 9) {
                    problem = new DTLZ3(numberOfObjectives, numberOfObjectives + 10).setK(10);
                    maxIterations = 1000;
                } else if (numberOfObjectives == 10) {
                    problem = new DTLZ3(numberOfObjectives, numberOfObjectives + 10).setK(10);
                    maxIterations = 1500;
                } else if (numberOfObjectives == 15) {
                    problem = new DTLZ3(numberOfObjectives, numberOfObjectives + 10).setK(10);
                    maxIterations = 2000;
                }
                break;
            case 4:
                if (numberOfObjectives == 3) {
                    problem = new DTLZ4();
                    maxIterations = 600;
                } else if (numberOfObjectives == 5) {
                    problem = new DTLZ4(numberOfObjectives, numberOfObjectives + 10).setK(10);
                    maxIterations = 1000;
                } else if (numberOfObjectives == 8) {
                    problem = new DTLZ4(numberOfObjectives, numberOfObjectives + 10).setK(10);
                    maxIterations = 1250;
                } else if (numberOfObjectives == 10) {
                    problem = new DTLZ4(numberOfObjectives, numberOfObjectives + 10).setK(10);
                    maxIterations = 2000;
                } else if (numberOfObjectives == 15) {
                    problem = new DTLZ4(numberOfObjectives, numberOfObjectives + 10).setK(10);
                    maxIterations = 3000;
                }
                break;
            case 5:
                problem = new DTLZ5();
                maxIterations = 500;
                break;
            case 6:
                problem = new DTLZ6();
                maxIterations = 500;
                break;
            case 7:
                problem = new DTLZ7();
                maxIterations = 500;
                break;
            default:
                error("Invalid number problem");
                break;
        }

        SelectionOperator<DoubleSolution> selectionOperator = new TournamentSelection<>((int) options.get("pop_size"),
                new DominanceComparator<>());
        return new NSGA_III<>(problem, (int) options.get("pop_size"), maxIterations, (int) options.get("partitions"),
                selectionOperator, (CrossoverOperator<DoubleSolution>) options.get("crossover"),
                (MutationOperator<DoubleSolution>) options.get("mutation"));
    }

    private static HashMap<String, Object> setup(int numberOfObjectives) {
        HashMap<String, Object> map = new HashMap<>();
        switch (numberOfObjectives) {
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
            default:
                error("Invalid number of objectives");
                break;
        }
        map.put("crossover", new SBXCrossover(30, 1.0));
        map.put("mutation", new PolynomialMutation());
        return map;
    }

    private static void error(String msg) {
        throw new IllegalArgumentException(msg);
    }

}