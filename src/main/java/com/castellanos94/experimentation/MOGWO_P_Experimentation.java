package com.castellanos94.experimentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.castellanos94.algorithms.AbstractAlgorithm;
import com.castellanos94.algorithms.multi.MOGWO;
import com.castellanos94.algorithms.multi.MOGWO_P;
import com.castellanos94.algorithms.multi.MOGWO_O;
import com.castellanos94.algorithms.multi.MOGWO_V;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.mcda.SatClassifier;
import com.castellanos94.operators.impl.RepairBoundary;
import com.castellanos94.problems.DTLZP;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;

import com.castellanos94.utils.Tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

/**
 * Experimentacion de todas las variantes [MOGWO, MOGWO-V, MOGWO-P, MOGWO-PFN]
 */
public class MOGWO_P_Experimentation {

    private static final Logger logger = LogManager.getLogger(MOGWO_Experimentation.class);

    public static void main(String[] args) throws IOException {
        if (args.length <= 2) {
            System.out.println(String.format(
                    "The following elements are required:\n\t Number of Experiments \n\t number of objectives \n\t algorithm [MOGWO, MOGWO-V, MOGWO-P, MOGWO-PFN] "));
            System.exit(-1);
        }
        System.out.println(Arrays.toString(args));

        final int EXPERIMENT = Integer.parseInt(args[0]);
        final int numberOfObjectives = Integer.parseInt(args[1]);
        final String algorithmName = args[2];
        if (!"MOGWO|MOGWO-V|MOGWO-P|MOGWO-PFN".contains(algorithmName)) {
            System.err.println("Nombre invalido [MOGWO, MOGWO-V, MOGWO-P, MOGWO-PFN]");
            System.exit(-1);
        }
        final String DIRECTORY = "experiments" + File.separator + numberOfObjectives + File.separator + "MOGWO"
                + File.separator + algorithmName;

        new File(DIRECTORY).mkdirs();
        int tmpInit = 1, tmpEdn = 9;
        if (args.length == 4) {
            tmpInit = Integer.parseInt(args[3]);
            tmpEdn = Integer.parseInt(args[3]);
        }
        if (args.length == 5) {
            tmpInit = Integer.parseInt(args[3]);
            tmpEdn = Integer.parseInt(args[4]);
        }

        final int initialProblem = tmpInit;
        final int endProblem = tmpEdn;

        for (int numberOfProblem = initialProblem; numberOfProblem <= endProblem; numberOfProblem++) {
            // indexProblem.stream().parallel().forEach( numberOfProblem -> {

            Tools.setSeed(1L);

            logger.info("Experimentation MOGWO : DTLZ with preferences");
            String resourseFile = "DTLZ_INSTANCES" + File.separator + numberOfObjectives + File.separator + "DTLZ"
                    + numberOfProblem + "_Instance.txt";
            DTLZ_Instance instance = null;
            try {
                instance = (DTLZ_Instance) new DTLZ_Instance(resourseFile).loadInstance();
            } catch (FileNotFoundException e3) {
                logger.error(e3);
            }

            AbstractAlgorithm<DoubleSolution> algorithm = loadConfiguration(numberOfProblem, instance, algorithmName);
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
                algorithm = loadConfiguration(numberOfProblem, instance, algorithmName);
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
            try {
                infoTime.write().csv(DIRECTORY + File.separator + subDir + File.separator + "times.csv");
            } catch (IOException e2) {
                logger.error(e2);
            }
            compartor.computeRanking(bag);

            logger.info("Fronts : " + compartor.getNumberOfSubFronts());
            logger.info("Front 0: " + compartor.getSubFront(0).size());

            File f = new File(DIRECTORY);
            if (!f.exists())
                f.mkdirs();
            f = new File(DIRECTORY + File.separator + subDir + File.separator + "MOGWOP_iii_bag_" + problem.getName()
                    + "_F0_" + problem.getNumberOfObjectives());

            ArrayList<String> strings = new ArrayList<>();
            for (DoubleSolution solution : compartor.getSubFront(0))
                strings.add(solution.toString());

            try {
                Files.write(f.toPath(), strings, Charset.defaultCharset());
            } catch (IOException e1) {
                logger.error(e1);
            }

            f = new File(DIRECTORY + File.separator + subDir + File.separator + "Class_F0" + problem.getName()
                    + +problem.getNumberOfObjectives() + ".out");
            SatClassifier<DoubleSolution> classifier = new SatClassifier<>(problem);
            HashMap<String, ArrayList<DoubleSolution>> map = classifier.classify(compartor.getSubFront(0));

            logger.info(String.format("%s -> HSat : %3d, Sat : %3d, Dis : %3d, HDis : %3d", problem.getName(),
                    map.get(SatClassifier.HSAT_CLASS_TAG).size(), map.get(SatClassifier.SAT_CLASS_TAG).size(),
                    map.get(SatClassifier.DIS_CLASS_TAG).size(), map.get(SatClassifier.HDIS_CLASS_TAG).size()));
            ArrayList<DoubleSolution> front = new ArrayList<>();
            if (!map.get(SatClassifier.HSAT_CLASS_TAG).isEmpty()) {
                front.addAll(map.get(SatClassifier.HSAT_CLASS_TAG));
            }
            if (!map.get(SatClassifier.SAT_CLASS_TAG).isEmpty()) {
                front.addAll(map.get(SatClassifier.SAT_CLASS_TAG));
            }
            if (front.isEmpty()) {
                front.addAll(map.get(SatClassifier.DIS_CLASS_TAG));
                front.addAll(map.get(SatClassifier.HDIS_CLASS_TAG));
            }
            logger.info(problem.getName() + " -> Front 0: " + front.size());

            strings = new ArrayList<>();
            for (DoubleSolution solution : front)
                strings.add(solution.toString());

            try {
                Files.write(f.toPath(), strings, Charset.defaultCharset());
            } catch (IOException e) {
                logger.error(e);
            }

            try {
                Files.write(f.toPath(), strings, Charset.defaultCharset());
            } catch (IOException e) {
                logger.error(e);
            }

            logger.info("End Experimentation.");

        }

    }

    private static AbstractAlgorithm<DoubleSolution> loadConfiguration(int numberOfProblem, DTLZ_Instance instance,
            String algorithm) {
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
        if (algorithm.equalsIgnoreCase("mogwo-p")) {
            return new MOGWO_P<>(problem, pop_size, maxIterations, pop_size / 2, new RepairBoundary());
        }
        if (algorithm.equalsIgnoreCase("mogwo-pfn")) {
            return new MOGWO_O<>(problem, pop_size, maxIterations, pop_size / 2, new RepairBoundary());
        }

        if (algorithm.equalsIgnoreCase("mogwo-v"))
            return new MOGWO_V<>(problem, pop_size, maxIterations, pop_size / 2, new RepairBoundary());

        return new MOGWO<>(problem, pop_size, maxIterations, pop_size / 2, new RepairBoundary());
    }
}
