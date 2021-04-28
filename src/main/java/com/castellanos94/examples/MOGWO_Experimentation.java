package com.castellanos94.examples;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;

import com.castellanos94.algorithms.multi.MOGWO;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.operators.impl.RepairBoundary;
import com.castellanos94.problems.benchmarks.dtlz.*;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Plotter;
import com.castellanos94.utils.Scatter3D;
import com.castellanos94.utils.Tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

public class MOGWO_Experimentation {

    private static final Logger logger = LogManager.getLogger(MOGWO_Experimentation.class);
    static final int EXPERIMENT = 31;
    static int numberOfObjectives = 3;

    static final String DIRECTORY = "experiments" + File.separator + numberOfObjectives + File.separator + "MOGWO"+File.separator + "MOGWO-V";;

    public static void main(String[] args) throws IOException {
        new File(DIRECTORY).mkdirs();
        int initialProblem = 1;
        int endProblem = 9;
        Tools.setSeed(1L);
        Ranking<DoubleSolution> compartor = new DominanceComparator<>();

        for (int numberOfProblem = initialProblem; numberOfProblem <= endProblem; numberOfProblem++) {
            MOGWO<DoubleSolution> algorithm = loadConfiguration(numberOfProblem, numberOfObjectives);
            DTLZ problem = (DTLZ) algorithm.getProblem();
            logger.info(problem);
            logger.info(algorithm);
            ArrayList<DoubleSolution> bag = new ArrayList<>();
            String subDir = problem.getName().trim();
            LongColumn experimentTimeColumn = LongColumn.create("Experiment Time");
            Table infoTime = Table.create("time");
            for (int i = 0; i < EXPERIMENT; i++) {
                algorithm = loadConfiguration(numberOfProblem, numberOfObjectives);
                algorithm.execute();

                try {
                    Solution.writSolutionsToFile(DIRECTORY + File.separator + "execution_" + i,
                            new ArrayList<>(algorithm.getSolutions()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Solution.writSolutionsToFile(
                            DIRECTORY + File.separator + subDir + File.separator + "execution_" + i,
                            new ArrayList<>(algorithm.getSolutions()));

                } catch (IOException e) {
                    logger.error(e);
                }

                experimentTimeColumn.append(algorithm.getComputeTime());
                logger.info(i + " time: " + algorithm.getComputeTime() + " ms.");

                bag.addAll(algorithm.getSolutions());
            }

            String str = "Resume " + problem.getName();
            str += "\n" + "Total time: " + experimentTimeColumn.sum();
            str += "\n" + "Average time : " + experimentTimeColumn.mean() + " ms.";
            str += "\n" + "Solutions in the bag: " + bag.size();
            logger.info(str);
            infoTime.addColumns(experimentTimeColumn);
            infoTime.write().csv(DIRECTORY + File.separator + subDir + File.separator + "times.csv");

            compartor.computeRanking(bag);

            logger.info("Fronts : " + compartor.getNumberOfSubFronts());
            logger.info("Front 0: " + compartor.getSubFront(0).size());

            File f = new File(DIRECTORY);
            if (!f.exists())
                f.mkdirs();
            f = new File(DIRECTORY + File.separator + subDir + File.separator + "MOGWO_bag_" + problem.getName()
                    + "_F0_" + problem.getNumberOfObjectives());

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

    }

    private static MOGWO<DoubleSolution> loadConfiguration(int numberOfProblem, int numberOfObjectives) {
        int maxIterations = 1000;
        DTLZ problem = null;
        int pop_size;
        if (numberOfObjectives == 3) {
            pop_size = 92;
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
        return new MOGWO<>(problem, pop_size, maxIterations, pop_size / 2, new RepairBoundary());
    }
}
