package com.castellanos94.cli;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Help.Visibility;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.castellanos94.algorithms.AbstractAlgorithm;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.mcda.SatClassifier;
import com.castellanos94.operators.impl.PolynomialMutation;
import com.castellanos94.operators.impl.SBXCrossover;
import com.castellanos94.problems.DTLZP;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.Tools;

import com.castellanos94.solutions.Solution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

public class CmdLine implements Runnable {
    private static final Logger logger = LogManager.getLogger(CmdLine.class);

    @Option(names = { "-m",
            "--numberOfObjectives" }, required = true, description = "Number of Objectives", showDefaultValue = Visibility.ALWAYS)
    private int numberOfObjectives = 3;
    @Option(names = { "-e",
            "--numberOfExperiments" }, description = "Number of Executions", showDefaultValue = Visibility.ALWAYS)
    private int numberOfExperiments = 31;
    @Option(names = { "--seed" }, description = "Random number generator seed")
    private long seed = -1;
    @Option(names = {
            "--initialProblem" }, description = "Initial problem to solve", showDefaultValue = Visibility.ALWAYS)
    private int initialProblem = 1;
    @Option(names = { "--endProblem" }, description = "Final problem to solve", showDefaultValue = Visibility.ALWAYS)
    private int endProblem = 9;
    @Option(names = { "--problem" }, description = "Problem index to solve", showDefaultValue = Visibility.ALWAYS)
    private int indexProblem = -1;

    @Override
    public void run() {
        logger.info("Subcommand needs: 'moead'");
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new CmdLine()).setCaseInsensitiveEnumValuesAllowed(true).execute(args));
    }

    public void run_experiment(ArrayList<AbstractAlgorithm<DoubleSolution>> algorithms, String algorithmName,
            String subDirectory) {
        final String DIRECTORY = "experiments" + File.separator + numberOfObjectives + File.separator + subDirectory;
        if (algorithms == null) {
            logger.error("Error loading configuration");
            System.exit(-1);
        }
        new File(DIRECTORY).mkdirs();
        if (seed != -1)
            Tools.setSeed(seed);

        logger.info("Experimentation " + algorithmName + " : DTLZ with preferences, seed = " + seed);
        for (AbstractAlgorithm<DoubleSolution> algorithm : algorithms) {
            DTLZP problem = (DTLZP) algorithm.getProblem();
            String subDir = problem.getName().trim();

            logger.info(problem);
            logger.info(algorithm);
            ArrayList<DoubleSolution> bag = new ArrayList<>();
            Ranking<DoubleSolution> compartor = new DominanceComparator<>();
            LongColumn experimentTimeColumn = LongColumn.create("Experiment Time");
            Table infoTime = Table.create("time");
            new File(DIRECTORY + File.separator + subDir).mkdirs();
            List<Integer> collect = IntStream.range(0, numberOfExperiments).boxed().collect(Collectors.toList());
            ProgressBarBuilder progressBarBuilder = new ProgressBarBuilder().setStyle(ProgressBarStyle.ASCII);
            progressBarBuilder.setTaskName("Running experiments");
            // for (int i = 0; i < EXPERIMENT; i++) {
            for (Integer i : ProgressBar.wrap(collect, progressBarBuilder)) {
                algorithm = algorithm.copy();
                algorithm.execute();
                try {
                    Solution.writSolutionsToFile(
                            DIRECTORY + File.separator + subDir + File.separator + "execution_" + i,
                            new ArrayList<>(algorithm.getSolutions()));

                } catch (IOException e) {
                    logger.error(e);
                }

                experimentTimeColumn.append(algorithm.getComputeTime());
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
            f = new File(DIRECTORY + File.separator + subDir + File.separator + algorithmName + "_bag_"
                    + problem.getName() + "_F0_" + problem.getNumberOfObjectives() + ".out");

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
        }
        logger.info("End Experimentation.");

    }

    public ArrayList<DTLZP> loadProblems() {
        if (indexProblem != -1) {
            initialProblem = indexProblem;
            endProblem = indexProblem;
        }
        ArrayList<DTLZP> problems = new ArrayList<>();
        for (int numberOfProblem = initialProblem; numberOfProblem <= endProblem; numberOfProblem++) {
            String resourseFile = "DTLZ_INSTANCES" + File.separator + numberOfObjectives + File.separator + "DTLZ"
                    + numberOfProblem + "_Instance.txt";

            try {
                DTLZ_Instance instance = (DTLZ_Instance) new DTLZ_Instance(resourseFile).loadInstance();
                problems.add(new DTLZP(numberOfProblem, instance));

            } catch (FileNotFoundException e3) {
                logger.error("Error loading dtlz instance for problem" + numberOfProblem + ", path="
                        + new File(resourseFile).getAbsolutePath());
                logger.error(e3);
                System.exit(-1);
            }
        }
        return problems;
    }

    public HashMap<String, Object> setup(int numberOfObjectives) {
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
                throw new IllegalArgumentException("Invalid number of objectives");
        }
        int maxIterations = 1000;
        int numberOfProblem = 1;
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
        map.put("crossover", new SBXCrossover(30, 1.0));
        map.put("mutation", new PolynomialMutation());
        return map;
    }

    public int getNumberOfObjectives() {
        return numberOfObjectives;
    }
}
