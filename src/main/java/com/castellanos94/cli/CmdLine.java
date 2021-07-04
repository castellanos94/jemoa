package com.castellanos94.cli;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Help.Visibility;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.castellanos94.algorithms.AbstractAlgorithm;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.problems.DTLZP;
import com.castellanos94.problems.benchmarks.dtlz.DTLZ;
import com.castellanos94.solutions.DoubleSolution;
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

    public void run_experiment(AbstractAlgorithm<DoubleSolution> algorithm, String subDirectory) {
        final String DIRECTORY = "experiments" + File.separator + numberOfObjectives + File.separator + subDirectory;
        if (algorithm == null) {
            logger.error("Error loading configuration");
            System.exit(-1);
        }
        new File(DIRECTORY).mkdirs();
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
}
