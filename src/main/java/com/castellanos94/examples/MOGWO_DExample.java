package com.castellanos94.examples;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;

import com.castellanos94.algorithms.multi.MOGWO_D;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.operators.impl.RepairBoundary;
import com.castellanos94.problems.benchmarks.dtlz.*;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.Plotter;
import com.castellanos94.utils.Scatter3D;
import com.castellanos94.utils.Tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MOGWO_DExample {

    private static final Logger logger = LogManager.getLogger(MOGWO_DExample.class);
    static final String DIRECTORY = "experiments" + File.separator + "MOGWO-D";
    static final int EXPERIMENT = 31;

    public static void main(String[] args) throws IOException {
        new File(DIRECTORY).mkdirs();
        Tools.setSeed(1L);
        int numberOfProblem = 1;
        int numberOfObjectives = 3;
        MOGWO_D<DoubleSolution> algorithm = loadConfiguration(numberOfProblem, numberOfObjectives);
        DTLZ problem = (DTLZ) algorithm.getProblem();
        logger.info(problem);
        logger.info(algorithm);
        ArrayList<Long> time = new ArrayList<>();
        ArrayList<DoubleSolution> bag = new ArrayList<>();
        Ranking<DoubleSolution> compartor = new DominanceComparator<>();

        for (int i = 0; i < EXPERIMENT; i++) {
            algorithm = loadConfiguration(numberOfProblem, numberOfObjectives);
            algorithm.execute();
            /*
             * try { Solution.writSolutionsToFile(DIRECTORY + File.separator + "execution_"
             * + i, new ArrayList<>(algorithm.getSolutions())); } catch (IOException e) {
             * e.printStackTrace(); }
             */
            time.add(algorithm.getComputeTime());
            compartor.computeRanking(algorithm.getSolutions());
            logger.info(i + " time: " + algorithm.getComputeTime() + " ms.");

            bag.addAll(compartor.getSubFront(0));
        }
        long averageTime = time.stream().mapToLong(v -> v.longValue()).sum();
        logger.info("Resume " + problem.getName());
        logger.info("Total time: " + averageTime);
        logger.info("Average time : " + (double) averageTime / EXPERIMENT + " ms.");
        logger.info("Solutions in the bag: " + bag.size());

        compartor.computeRanking(bag);

        logger.info("Fronts : " + compartor.getNumberOfSubFronts());
        logger.info("Front 0: " + compartor.getSubFront(0).size());

        File f = new File(DIRECTORY);
        if (!f.exists())
            f.mkdirs();
        f = new File(DIRECTORY + File.separator + "MOGWO_bag_" + problem.getName() + "_F0_"
                + problem.getNumberOfObjectives());

        ArrayList<String> strings = new ArrayList<>();
        for (DoubleSolution solution : compartor.getSubFront(0))
            strings.add(solution.toString());

        Files.write(f.toPath(), strings, Charset.defaultCharset());
        if (problem.getNumberOfObjectives() == 3) {
            Plotter plotter = new Scatter3D<>(compartor.getSubFront(0),
                    DIRECTORY + File.separator + problem.getName() + "_MOGWO");
            plotter.plot();
        }

    }

    private static MOGWO_D<DoubleSolution> loadConfiguration(int numberOfProblem, int numberOfObjectives) {
        int maxIterations = 1000;
        DTLZ problem = null;
        int pop_size;
        int T = 20;
        int nr = 2;
        if (numberOfObjectives == 3) {
            pop_size = 210;
        } else if (numberOfObjectives == 5) {
            pop_size = 212;
        } else {
            pop_size = 271;
        }
        switch (numberOfProblem) {
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
            } else if (numberOfObjectives == 5 || numberOfObjectives == 8) {
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
        default:
            if (numberOfProblem == 5) {
                problem = new DTLZ5();
            } else if (numberOfProblem == 6) {
                problem = new DTLZ6();
            } else if (numberOfProblem == 7) {
                problem = new DTLZ7();
            } else if (numberOfProblem == 8) {
                problem = new DTLZ8();
            } else {
                problem = new DTLZ9();
            }
            if (numberOfObjectives == 3) {
                maxIterations = 750;
            } else if (numberOfObjectives == 5)
                maxIterations = 1000;
            else if (numberOfObjectives == 8)
                maxIterations = 1250;
            else {
                maxIterations = 1500;
            }
            break;
        }
        return new MOGWO_D<>(problem, pop_size, 0.9, maxIterations, T, nr, new RepairBoundary());
    }
}
