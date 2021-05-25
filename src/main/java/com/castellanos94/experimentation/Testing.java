package com.castellanos94.experimentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.castellanos94.algorithms.AbstractAlgorithm;
import com.castellanos94.algorithms.multi.IMOACO_R;
import com.castellanos94.algorithms.multi.IMOACO_R_P;
import com.castellanos94.algorithms.multi.MOGWO;
import com.castellanos94.algorithms.multi.MOGWO_P;
import com.castellanos94.algorithms.multi.MOGWO_PFN;
import com.castellanos94.algorithms.multi.MOGWO_V;
import com.castellanos94.algorithms.multi.NSGA_III_P;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.mcda.SatClassifier;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.operators.impl.PolynomialMutation;
import com.castellanos94.operators.impl.RepairBoundary;
import com.castellanos94.operators.impl.SBXCrossover;
import com.castellanos94.operators.impl.TournamentSelection;
import com.castellanos94.problems.DTLZP;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

@Command(name = "Experimentation DTLZ Preferences", mixinStandardHelpOptions = true, version = "Experimentation V1.0", description = "Experimenting the DTLZ Benchmark Suite for Algorithms with Incorporation of Preferences")
public class Testing implements Runnable {
    private static final Logger logger = LogManager.getLogger(Testing.class);

    private enum AlgorithmNames {
        NSGAIII, NSGAIIIP, MOGWO, MOGWOV, MOGWOP, MOGWOPFN, IMOACOR, IMOACORP
    }

    @Option(names = { "-a", "--algorithm" }, required = true, description = "Algorithm to used")
    private AlgorithmNames algorithmName;
    @Option(names = { "-e", "--numberOfExperiments" }, required = true, description = "Number of Executions")
    private int numberOfExperiments = 31;
    @Option(names = { "-m", "--numberOfObjectives" }, required = true, description = "Number of Objectives")
    private int numberOfObjectives = 3;
    @Option(names = { "-c",
            "--classifyEveryIteration" }, description = "Classify every number of iterations [0 - 100] for NSGAIIIP")
    private int CLASSIFY_EVERY_ITERATION = 1; // Classification F0 each
    @Option(names = { "-c",
            "--classifyEveryIteration" }, description = "Elementos to replace when classification is done [0 - 100] for NSGAIIIP")
    private int ELEMENTS_TO_REPLACE = 2; // 5 % of population
    @Option(names = { "-ip", "--initialProblem" }, description = "Initial problem to solve")
    private int initialProblem = 1;
    @Option(names = { "-ep", "--endProblem" }, description = "Final problem to solve")
    private int endProblem = 9;
    @Option(names = { "-r", "--ranking" }, description = "Classification key sorting at 1(true) or 2 (false)")
    private boolean isFirstRank = false;

    public static void main(String[] args) {
        System.out.println(new CommandLine(new Testing()).execute(args));
    }

    @Override
    public void run() {
        final String DIRECTORY;
        if (algorithmName == AlgorithmNames.NSGAIII || algorithmName == AlgorithmNames.NSGAIIIP) {
            if (algorithmName == AlgorithmNames.NSGAIII) {
                CLASSIFY_EVERY_ITERATION = 0;
                ELEMENTS_TO_REPLACE = 0;
            }
            DIRECTORY = "experiments" + File.separator + numberOfObjectives + File.separator + "NSGAIII"
                    + File.separator + "C" + CLASSIFY_EVERY_ITERATION + "R" + ELEMENTS_TO_REPLACE;
        } else if (algorithmName == AlgorithmNames.MOGWO || algorithmName == AlgorithmNames.MOGWOP
                || algorithmName == AlgorithmNames.MOGWOPFN || algorithmName == AlgorithmNames.MOGWOV) {
            DIRECTORY = "experiments" + File.separator + numberOfObjectives + File.separator + "MOGWO" + File.separator
                    + algorithmName;
        } else {
            DIRECTORY = "experiments" + File.separator + numberOfObjectives + File.separator + "IMOACOR"
                    + File.separator + algorithmName;
        }
        new File(DIRECTORY).mkdirs();
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
                System.err.println("Error loading dtlz instance for problem" + numberOfProblem + ", path="
                        + new File(resourseFile).getAbsolutePath());
                logger.error(e3);
                System.exit(-1);
            }

            AbstractAlgorithm<DoubleSolution> algorithm = loadConfiguration(numberOfProblem, instance, algorithmName,
                    isFirstRank);
            if (algorithm == null) {
                System.err.println("Error loading configuration for " + algorithmName);
                System.exit(-1);
            }
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
                algorithm = loadConfiguration(numberOfProblem, instance, algorithmName, isFirstRank);
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

    @SuppressWarnings("unchecked")
    private AbstractAlgorithm<DoubleSolution> loadConfiguration(int numberOfProblem, DTLZ_Instance instance,
            AlgorithmNames _algorithmName, boolean isFirstRank) {
        DTLZP problem = new DTLZP(numberOfProblem, instance);
        int maxIterations = 1000;
        int numberOfObjectives = instance.getNumObjectives();
        HashMap<String, Object> options = setup(instance.getNumObjectives());

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
        if (_algorithmName == AlgorithmNames.IMOACOR)
            return new IMOACO_R<>(problem, maxIterations, 0.1, 0.5, (int) options.get("partitions"));
        if (_algorithmName == AlgorithmNames.IMOACORP)
            return new IMOACO_R_P<>(problem, maxIterations, 0.1, 0.5, (int) options.get("partitions"), isFirstRank);
        if (_algorithmName == AlgorithmNames.NSGAIIIP || _algorithmName == AlgorithmNames.NSGAIII) {

            SelectionOperator<DoubleSolution> selectionOperator = new TournamentSelection<>(
                    (int) options.get("pop_size"), new DominanceComparator<>());
            NSGA_III_P<DoubleSolution> algorithm = new NSGA_III_P<>(problem, (int) options.get("pop_size"),
                    maxIterations, (int) options.get("partitions"), selectionOperator,
                    (CrossoverOperator<DoubleSolution>) options.get("crossover"),
                    (MutationOperator<DoubleSolution>) options.get("mutation"));
            algorithm.setClassifyEveryIteration(CLASSIFY_EVERY_ITERATION);
            algorithm.setNumberOfElementToReplace(ELEMENTS_TO_REPLACE);
            return algorithm;
        }
        if (_algorithmName == AlgorithmNames.MOGWO) {
            return new MOGWO<>(problem, (int) options.get("pop_size"), maxIterations, (int) options.get("pop_size") / 2,
                    new RepairBoundary());
        }
        if (_algorithmName == AlgorithmNames.MOGWOV) {
            return new MOGWO_V<>(problem, (int) options.get("pop_size"), maxIterations,
                    (int) options.get("pop_size") / 2, new RepairBoundary());
        }
        if (_algorithmName == AlgorithmNames.MOGWOP) {
            return new MOGWO_P<>(problem, (int) options.get("pop_size"), maxIterations,
                    (int) options.get("pop_size") / 2, new RepairBoundary());
        }
        if (_algorithmName == AlgorithmNames.MOGWOPFN) {
            return new MOGWO_PFN<>(problem, (int) options.get("pop_size"), maxIterations,
                    (int) options.get("pop_size") / 2, new RepairBoundary());
        }
        return null;

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
                throw new IllegalArgumentException("Invalid number of objectives");
        }
        map.put("crossover", new SBXCrossover(30, 1.0));
        map.put("mutation", new PolynomialMutation());
        return map;
    }

}
