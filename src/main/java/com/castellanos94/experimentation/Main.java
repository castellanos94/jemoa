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
import com.castellanos94.algorithms.multi.MOEAD;
import com.castellanos94.algorithms.multi.MOEADO;
import com.castellanos94.algorithms.multi.MOGWO;
import com.castellanos94.algorithms.multi.MOGWO_P;
import com.castellanos94.algorithms.multi.MOGWO_O;
import com.castellanos94.algorithms.multi.MOGWO_V;
import com.castellanos94.algorithms.multi.NSGA_III_P;
import com.castellanos94.algorithms.multi.PI_MOGWO;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.datatype.Data;
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
import com.castellanos94.utils.ReferenceHyperplane;
import com.castellanos94.utils.Tools;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Help.Visibility;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

@Command(name = "Experimentation DTLZ Preferences", mixinStandardHelpOptions = true, version = "Experimentation V1.0", description = "Experimenting the DTLZ Benchmark Suite for Algorithms with Incorporation of Preferences", sortOptions = false)
public class Main implements Runnable {
    private static final Logger logger = LogManager.getLogger(Main.class);

    private enum AlgorithmNames {
        NSGAIII, NSGAIIIP, MOGWO, MOGWOV, PIMOGWO, MOGWOP, MOGWOO, IMOACOR, IMOACORP, MOEAD, MOEADO
    }

    @Option(names = { "-a",
            "--algorithm" }, required = true, description = "Algorithm to run experiment : NSGAIII, NSGAIIIP, MOGWO, MOGWOV, MOGWOP, MOGWOPFN, IMOACOR, IMOACORP, MOEAD, MOEADO")
    private AlgorithmNames algorithmName;
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
    @Option(names = { "-c",
            "--classificationRate" }, description = "Classify every number of iterations [0 - 100] for NSGAIIIP", showDefaultValue = Visibility.ALWAYS)
    private int CLASSIFY_EVERY_ITERATION = 1; // Classification F0 each
    @Option(names = { "-r",
            "--replaceRate" }, description = "Elementos to replace when classification is done [0 - 100] for NSGAIIIP", showDefaultValue = Visibility.ALWAYS)
    private int ELEMENTS_TO_REPLACE = 2; // 5 % of population
    @Option(names = {
            "--mogwo-ep" }, description = "Mogwo external population default  N/2", showDefaultValue = Visibility.ALWAYS)
    private int mogwoExternalPopulation = -1;
    @Option(names = {
            "--mogwo-ep-n" }, description = "Mogwo external population equals to N", showDefaultValue = Visibility.ALWAYS)
    private boolean mogwoExternalPopulationBoolean = false;
    @Option(names = {
            "--sortingKey" }, description = "Classification key sorting at 1(true) or 2 (false)", showDefaultValue = Visibility.ALWAYS)
    private boolean isFirstRank = true;
    @Option(names = {
            "-q" }, description = "diversification process control parameter for IMOACOR", showDefaultValue = Visibility.ALWAYS)
    private double q = 0.1; // 5 % of population
    @Option(names = {
            "-xi" }, description = " convergence rate control parameter for IMOACOR", showDefaultValue = Visibility.ALWAYS)
    private double xi = 0.5;
    @Option(names = {
            "--approach-moead" }, description = "Approach used for scalarization function", showDefaultValue = Visibility.ALWAYS)
    private MOEAD.APPROACH apporachUsed = MOEAD.APPROACH.TCHEBYCHEFF;

    @Option(names = {
            "--neighborhood-size-moead" }, description = "Neighborhood size for moead", showDefaultValue = Visibility.ALWAYS)
    private int neighborhoodSize = 20;
    @Option(names = { "--variant" }, description = "MOEADO variant", showDefaultValue = Visibility.ALWAYS)
    private int variant = 5;
    @Option(names = {
            "-dm" }, description = "DM target, for MODEAO default is the first dm", showDefaultValue = Visibility.ALWAYS)
    private int dm_target = -1;

    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).setCaseInsensitiveEnumValuesAllowed(true).execute(args));
    }

    @Override
    public void run() {
        final String base = "experiments" + File.separator + numberOfObjectives + File.separator;
        final String DIRECTORY;

        if (algorithmName == AlgorithmNames.NSGAIII || algorithmName == AlgorithmNames.NSGAIIIP) {
            if (algorithmName == AlgorithmNames.NSGAIII) {
                CLASSIFY_EVERY_ITERATION = 0;
                ELEMENTS_TO_REPLACE = 0;
            }
            DIRECTORY = base + "NSGAIII" + File.separator + "C" + CLASSIFY_EVERY_ITERATION + "R" + ELEMENTS_TO_REPLACE;
        } else if (algorithmName == AlgorithmNames.MOGWO || algorithmName == AlgorithmNames.MOGWOP
                || algorithmName == AlgorithmNames.MOGWOO || algorithmName == AlgorithmNames.MOGWOV
                || algorithmName == AlgorithmNames.PIMOGWO) {
            String suffix = (mogwoExternalPopulation != -1) ? "-EP" + mogwoExternalPopulation : "";
            if (mogwoExternalPopulationBoolean)
                suffix = "-EPN" + setup(numberOfObjectives).get("pop_size");
            DIRECTORY = base + "MOGWO" + File.separator + enumToString(algorithmName) + suffix;
        } else if (algorithmName == AlgorithmNames.MOEAD || algorithmName == AlgorithmNames.MOEADO) {
            String suffix = "";

            if (algorithmName == AlgorithmNames.MOEADO) {
                suffix = "-" + ((dm_target != -1) ? "DM" + dm_target : "DM1") + "-VAR" + variant;
            }
            if (apporachUsed != MOEAD.APPROACH.TCHEBYCHEFF) {
                suffix += "-" + apporachUsed;
            }
            if (neighborhoodSize != 20) {
                suffix += "-" + neighborhoodSize;
            }
            DIRECTORY = base + "MOEAD" + File.separator + enumToString(algorithmName) + suffix;
        } else {
            String suffix = (this.q != 0.1 || this.xi != 0.5) ? String.format("Q%.3fXI%.2f", this.q, this.xi) : "";
            if (algorithmName == AlgorithmNames.IMOACORP) {
                String algorithmName__ = (isFirstRank) ? enumToString(algorithmName) + "R1" : enumToString(algorithmName) + "R2";
                DIRECTORY = base + "IMOACOR" + File.separator + algorithmName__ + suffix;
            } else {
                DIRECTORY = base + "IMOACOR" + File.separator + algorithmName + suffix;
            }
        }
        new File(DIRECTORY).mkdirs();
        if (indexProblem != -1) {
            initialProblem = indexProblem;
            endProblem = indexProblem;
        }
        for (int numberOfProblem = initialProblem; numberOfProblem <= endProblem; numberOfProblem++) {
            if (seed != -1)
                Tools.setSeed(seed);

            logger.info("Experimentation " + algorithmName + " : DTLZ with preferences, seed = " + seed);
            String resourseFile = "DTLZ_INSTANCES" + File.separator + numberOfObjectives + File.separator + "DTLZ"
                    + numberOfProblem + "_Instance.txt";
            DTLZ_Instance instance = null;
            try {
                instance = (DTLZ_Instance) new DTLZ_Instance(resourseFile).loadInstance();
            } catch (FileNotFoundException e3) {
                logger.error("Error loading dtlz instance for problem" + numberOfProblem + ", path="
                        + new File(resourseFile).getAbsolutePath());
                logger.error(e3);
                System.exit(-1);
            }

            AbstractAlgorithm<DoubleSolution> algorithm = loadConfiguration(numberOfProblem, instance, algorithmName,
                    isFirstRank);
            if (algorithm == null) {
                logger.error("Error loading configuration for " + algorithmName);
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

    private String enumToString(AlgorithmNames algorithm) {
        switch (algorithm) {
            case IMOACORP:
            return "IMOACOR-RP";
            case MOGWOO:
            return "MOGWO-O";
            case MOGWOP: return "MOGWO-P";
            case MOGWOV: return "MOGWO-V";
            case MOEADO: return "MOEAD-O";

            
            default:
                return algorithm.toString();
        }
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
        if (_algorithmName == AlgorithmNames.IMOACOR) {
            if (problem.getNumberOfObjectives() > 5) {
                return new IMOACO_R<>(problem, 1000, (int) options.get("pop_size"), this.q, this.xi,
                        (int) options.get("partitions"));
            }
            return new IMOACO_R<>(problem, maxIterations, this.q, this.xi, (int) options.get("partitions"));
        }
        if (_algorithmName == AlgorithmNames.IMOACORP) {
            if (problem.getNumberOfObjectives() > 5) {
                return new IMOACO_R_P<>(problem, 1000, (int) options.get("pop_size"), this.q, this.xi,
                        (int) options.get("partitions"), isFirstRank);
            }
            return new IMOACO_R_P<>(problem, maxIterations, this.q, this.xi, (int) options.get("partitions"),
                    isFirstRank);
        }
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
        int ep_mogwo = (mogwoExternalPopulation != -1) ? mogwoExternalPopulation : (int) options.get("pop_size") / 2;
        if (mogwoExternalPopulationBoolean) {
            ep_mogwo = (int) setup(numberOfObjectives).get("pop_size");
        }
        if (_algorithmName == AlgorithmNames.MOGWO) {
            return new MOGWO<>(problem, (int) options.get("pop_size"), maxIterations, ep_mogwo, new RepairBoundary());
        }
        if (_algorithmName == AlgorithmNames.MOGWOV) {
            return new MOGWO_V<>(problem, (int) options.get("pop_size"), maxIterations, ep_mogwo, new RepairBoundary());
        }
        if (_algorithmName == AlgorithmNames.MOGWOP) {
            return new MOGWO_P<>(problem, (int) options.get("pop_size"), maxIterations, ep_mogwo, new RepairBoundary());
        }
        if (_algorithmName == AlgorithmNames.MOGWOO) {
            return new MOGWO_O<>(problem, (int) options.get("pop_size"), maxIterations, ep_mogwo, new RepairBoundary());
        }
        if (_algorithmName == AlgorithmNames.PIMOGWO) {
            return new PI_MOGWO<>(problem, (int) options.get("pop_size"), maxIterations, new RepairBoundary());
        }
        if (_algorithmName == AlgorithmNames.MOEAD) {
            ArrayList<ArrayList<Data>> weights = generateWeight(problem, (int) options.get("partitions"));

            return new MOEAD<>(problem, maxIterations, weights.size(), weights, neighborhoodSize,
                    (CrossoverOperator<DoubleSolution>) options.get("crossover"),
                    (MutationOperator<DoubleSolution>) options.get("mutation"), new RepairBoundary(),
                    new DominanceComparator<>(), apporachUsed);
        }

        if (_algorithmName == AlgorithmNames.MOEADO) {
            ArrayList<ArrayList<Data>> weights = generateWeight(problem, (int) options.get("partitions"));

            return new MOEADO<>(problem, maxIterations, weights.size(), weights, neighborhoodSize,
                    problem.getInstance().getPreferenceModel((dm_target == -1) ? 0 : dm_target - 1), variant,
                    (CrossoverOperator<DoubleSolution>) options.get("crossover"),
                    (MutationOperator<DoubleSolution>) options.get("mutation"), new RepairBoundary(),
                    new DominanceComparator<>(), apporachUsed);
        }
        return null;

    }

    private static ArrayList<ArrayList<Data>> generateWeight(DTLZP problem, int h) {
        if (problem.getNumberOfObjectives() <= 5) {
            ReferenceHyperplane<DoubleSolution> referenceHyperplane = new ReferenceHyperplane<>(
                    problem.getNumberOfObjectives(), h);
            referenceHyperplane.execute();
            ArrayList<ArrayList<Data>> data = referenceHyperplane.transformToData();
            return data;
        }

        ReferenceHyperplane<DoubleSolution> referenceHyperplane = new ReferenceHyperplane<>(
                problem.getNumberOfObjectives(), 3);
        referenceHyperplane.execute();
        ArrayList<ArrayList<Data>> data = referenceHyperplane.transformToData();
        ReferenceHyperplane<DoubleSolution> referenceHyperplane2 = new ReferenceHyperplane<>(
                problem.getNumberOfObjectives(), 2);
        referenceHyperplane2.execute();
        data.addAll(referenceHyperplane2.transformToData());
        return data;
    }

    public static HashMap<String, Object> setup(int numberOfObjectives) {
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

    public int getNumberOfObjectives() {
        return numberOfObjectives;
    }
}
