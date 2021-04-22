package com.castellanos94.examples;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;

import com.castellanos94.algorithms.multi.MOGWO_P;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.operators.impl.RepairBoundary;
import com.castellanos94.preferences.impl.InterClassnC;
import com.castellanos94.problems.DTLZP;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;

import com.castellanos94.utils.Tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

public class MOGWOPExample {

    private static final Logger logger = LogManager.getLogger(MOGWOPExample.class);
    static final String DIRECTORY = "experiments" + File.separator + "MOGWOP";
    static final int EXPERIMENT = 31;

    public static void main(String[] args) throws IOException {
        new File(DIRECTORY).mkdirs();
        int initialProblem = 1;
        int endProblem = 9;
        int numberOfObjectives = 3;
        for (int numberOfProblem = initialProblem; numberOfProblem <= endProblem; numberOfProblem++) {

            Tools.setSeed(1L);

            logger.info("Experimentation MOGWO : DTLZ with preferences");
            String resourseFile = "DTLZ_INSTANCES" + File.separator + numberOfObjectives + File.separator + "DTLZ"
                    + numberOfProblem + "_Instance.txt";
            DTLZ_Instance instance = (DTLZ_Instance) new DTLZ_Instance(resourseFile).loadInstance();

            MOGWO_P<DoubleSolution> algorithm = loadConfiguration(numberOfProblem, instance);
            DTLZP problem = (DTLZP) algorithm.getProblem();
            String subDir = problem.getName().trim();

            logger.info(problem);
            logger.info(algorithm);
            ArrayList<DoubleSolution> bag = new ArrayList<>();
            Ranking<DoubleSolution> compartor = new DominanceComparator<>();
            LongColumn experimentTimeColumn = LongColumn.create("Experiment Time");
            Table infoTime = Table.create("time");
            new File(DIRECTORY + File.separator + subDir).mkdirs();
            for (int i = 0; i < EXPERIMENT; i++) {
                algorithm = loadConfiguration(numberOfProblem, instance);
                algorithm.execute();
                try {
                    Solution.writSolutionsToFile(
                            DIRECTORY + File.separator + subDir + File.separator + "execution_" + i,
                            new ArrayList<>(algorithm.getSolutions()));

                } catch (IOException e) {
                    logger.error(e);
                }

                experimentTimeColumn.append(algorithm.getComputeTime());
               // logger.info(i + " time: " + algorithm.getComputeTime() + " ms.");

                bag.addAll(algorithm.getSolutions());
            }
            String str = "Resume " + problem.getName();
            str += "\n" + "Total time: " + experimentTimeColumn.sum();
            str += "\n" + "Average time : " + experimentTimeColumn.mean() + " ms.";
            str += "\n" + "Solutions in the bag: " + bag.size();
            logger.info(str);
            infoTime.addColumns(experimentTimeColumn);
            infoTime.write().csv(DIRECTORY + File.separator + subDir + File.separator + "times.csv");
            try {
                Solution.writSolutionsToFile(DIRECTORY + File.separator + subDir + File.separator + "MOGWOP_iii_bag_"
                        + problem.getName() + "_F0_" + problem.getNumberOfObjectives(), new ArrayList<>(bag));
            } catch (IOException e1) {
                logger.error(e1);
            }
            compartor = new DominanceComparator<>();
            compartor.computeRanking(bag);

            logger.info("Fronts : " + compartor.getNumberOfSubFronts());
            logger.info("Front 0 - Original: " + compartor.getSubFront(0).size());

            File f = new File(DIRECTORY);
            if (!f.exists())
                f.mkdirs();
            f = new File(DIRECTORY + File.separator + subDir + File.separator + "Class_F0" + problem.getName()
                    + +problem.getNumberOfObjectives() + ".out");
            InterClassnC<DoubleSolution> classifier = new InterClassnC<>(problem);
            ArrayList<DoubleSolution> front = new ArrayList<>();
            ArrayList<DoubleSolution> hs = new ArrayList<>();
            ArrayList<DoubleSolution> s = new ArrayList<>();
            ArrayList<DoubleSolution> d = new ArrayList<>();
            ArrayList<DoubleSolution> hd = new ArrayList<>();
            for (DoubleSolution x : compartor.getSubFront(0)) {
                classifier.classify(x);
                int[] iclass = (int[]) x.getAttribute(classifier.getAttributeKey());
                if (iclass[0] > 0) {
                    hs.add(x);
                } else if (iclass[1] > 0) {
                    s.add(x);
                } else if (iclass[2] > 0) {
                    d.add(x);
                } else {
                    hd.add(x);
                }
            }
            if (!hs.isEmpty()) {
                front.addAll(hs);
            }
            if (!s.isEmpty()) {
                front.addAll(s);
            }
            if (front.isEmpty() && !d.isEmpty()) {
                front.addAll(d);
            }
            if (front.isEmpty() && !hd.isEmpty()) {
                front.addAll(hd);
            }
            logger.info(String.format("%s -> HSat : %3d, Sat : %3d, Dis : %3d, HDis : %3d", problem.getName(),
                    hs.size(), s.size(), d.size(), hd.size()));
            logger.info(problem.getName() + " -> Front 0: " + front.size());

            ArrayList<String> strings = new ArrayList<>();
            for (DoubleSolution solution : front)
                strings.add(solution.toString());

            try {
                Files.write(f.toPath(), strings, Charset.defaultCharset());
            } catch (IOException e) {
                logger.error(e);
            }

            Files.write(f.toPath(), strings, Charset.defaultCharset());

            logger.info("End Experimentation.");

        }

    }

    private static MOGWO_P<DoubleSolution> loadConfiguration(int numberOfProblem, DTLZ_Instance instance) {
        DTLZP problem = new DTLZP(numberOfProblem, instance);
        int maxIterations = 1000;
        int numberOfObjectives = instance.getNumObjectives();
        int pop_size;
        switch (numberOfObjectives) {
        case 3:
            pop_size = 92;
            break;
        case 5:
            pop_size = 212;
            break;
        case 8:
            pop_size = 156;
            break;
        case 10:
            pop_size = 271;
            break;
        case 15:
            pop_size = 136;
            break;
        default:
            throw new IllegalArgumentException("Invalid number of objectives");
        }
        switch (numberOfProblem) {
        case 1:
            if (numberOfObjectives == 3) {
                maxIterations = 400;
            } else if (numberOfObjectives == 5) {
                maxIterations = 600;
            } else if (numberOfObjectives == 8) {
                maxIterations = 750;
            } else if (numberOfObjectives == 10) {
                maxIterations = 1000;
            } else if (numberOfObjectives == 15) {
                maxIterations = 1500;
            }
            break;
        case 2:
            if (numberOfObjectives == 3) {
                maxIterations = 250;
            } else if (numberOfObjectives == 5) {
                maxIterations = 350;
            } else if (numberOfObjectives == 8) {
                maxIterations = 500;
            } else if (numberOfObjectives == 10) {
                maxIterations = 750;
            } else if (numberOfObjectives == 15) {
                maxIterations = 1000;
            }
            break;
        case 3:
            if (numberOfObjectives == 3) {
                maxIterations = 1000;
            } else if (numberOfObjectives == 5 || numberOfObjectives == 8) {
                maxIterations = 1000;
            } else if (numberOfObjectives == 10) {
                maxIterations = 1500;
            } else if (numberOfObjectives == 15) {
                maxIterations = 2000;
            }
            break;
        case 4:
            if (numberOfObjectives == 3) {
                maxIterations = 600;
            } else if (numberOfObjectives == 5) {
                maxIterations = 1000;
            } else if (numberOfObjectives == 8) {
                maxIterations = 1250;
            } else if (numberOfObjectives == 10) {
                maxIterations = 2000;
            } else if (numberOfObjectives == 15) {
                maxIterations = 3000;
            }
            break;
        default:
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

        return new MOGWO_P<>(problem, pop_size, maxIterations, pop_size / 2, new RepairBoundary());
    }
}
