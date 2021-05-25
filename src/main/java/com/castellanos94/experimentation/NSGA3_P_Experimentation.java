package com.castellanos94.experimentation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.castellanos94.algorithms.multi.NSGA_III_P;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.mcda.SatClassifier;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.operators.impl.PolynomialMutation;
import com.castellanos94.operators.impl.TournamentSelection;
import com.castellanos94.problems.DTLZP;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.operators.impl.SBXCrossover;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Classifier;
import com.castellanos94.utils.Tools;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

/**
 * Current Algorithm Experimentation used.
 */
public class NSGA3_P_Experimentation {
    private static final Logger logger = LogManager.getLogger(NSGA3_P_Experimentation.class);
    private final int numberOfObjectives;
    private final int CLASSIFY_EVERY_ITERATION; // Classification F0 each
    private final int ELEMENTS_TO_REPLACE; // 5 % of population
    private final String DIRECTORY;
    private final int EXPERIMENT;
    private int initialProblem;
    private int endProblem;

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println(String.format(
                    "The following elements are required:\n\t Experiments \n\t number of objectives \n\t classify every iteration \n\t elements to replace"));
            System.exit(-1);
        }
        System.out.println(Arrays.toString(args));
        NSGA3_P_Experimentation experimentation = new NSGA3_P_Experimentation(Integer.parseInt(args[0]),
                Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        if (args.length == 5) {
            experimentation.setInitialProblem(Integer.parseInt(args[4]));
            experimentation.setEndProblem(Integer.parseInt(args[4]));
        } else if (args.length == 6) {
            experimentation.setInitialProblem(Integer.parseInt(args[4]));
            experimentation.setEndProblem(Integer.parseInt(args[5]));
        }
        logger.info(experimentation);
        try {
            experimentation.execute();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void setInitialProblem(int initialProblem) {
        this.initialProblem = initialProblem;
    }

    public void setEndProblem(int endProblem) {
        this.endProblem = endProblem;
    }

    public NSGA3_P_Experimentation(int numberOfExperiments, int numberOfObjectives, int cLASSIFY_EVERY_ITERATION,
            int eLEMENTS_TO_REPLACE) {
        this.EXPERIMENT = numberOfExperiments;
        this.numberOfObjectives = numberOfObjectives;
        CLASSIFY_EVERY_ITERATION = cLASSIFY_EVERY_ITERATION;
        ELEMENTS_TO_REPLACE = eLEMENTS_TO_REPLACE;
        DIRECTORY = "experiments" + File.separator + numberOfObjectives + File.separator + "NSGA3" + File.separator
                + "C" + CLASSIFY_EVERY_ITERATION + "R" + ELEMENTS_TO_REPLACE + "-sat";
        initialProblem = 1;
        endProblem = 9;
    }

    public void execute() throws IOException {
        new File(DIRECTORY).mkdirs();

        for (int p = initialProblem; p <= endProblem; p++) {

            Tools.setSeed(1L);
            logger.info("Experimentation: DTLZ with preferences");
            String resourseFile = "DTLZ_INSTANCES" + File.separator + numberOfObjectives + File.separator + "DTLZ" + p
                    + "_Instance.txt";
            // resourseFile = "roi_generator" + File.separator + "DTLZ" + p +
            // "_Instance.txt";
            DTLZ_Instance instance = (DTLZ_Instance) new DTLZ_Instance(resourseFile).loadInstance();

            // logger.info(instance);

            NSGA_III_P<DoubleSolution> algorithm = dtlzTestSuite(p, instance);
            algorithm.setClassifyEveryIteration(CLASSIFY_EVERY_ITERATION);
            algorithm.setNumberOfElementToReplace(ELEMENTS_TO_REPLACE);
            DTLZP problem = (DTLZP) algorithm.getProblem();
            String subDir = problem.getName().trim();
            if (!new File(DIRECTORY + File.separator + subDir).exists()) {
                new File(DIRECTORY + File.separator + subDir).mkdir();
            }
            logger.info(problem);
            logger.info(algorithm);

            ArrayList<DoubleSolution> bag = new ArrayList<>();
            LongColumn experimentTimeColumn = LongColumn.create("Experiment Time");
            Table infoTime = Table.create("time");
            List<Integer> collect = IntStream.range(0, EXPERIMENT).boxed().collect(Collectors.toList());
            ProgressBarBuilder progressBarBuilder = new ProgressBarBuilder().setStyle(ProgressBarStyle.ASCII);
            progressBarBuilder.setTaskName("Running experiments");
            // for (int i = 0; i < EXPERIMENT; i++) {
            for (Integer i : ProgressBar.wrap(collect, progressBarBuilder)) {
                algorithm = dtlzTestSuite(p, instance);
                algorithm.setClassifyEveryIteration(CLASSIFY_EVERY_ITERATION);
                algorithm.setNumberOfElementToReplace(ELEMENTS_TO_REPLACE);
                algorithm.execute();
                experimentTimeColumn.append(algorithm.getComputeTime());
                try {
                    Solution.writSolutionsToFile(
                            DIRECTORY + File.separator + subDir + File.separator + "execution_" + i,
                            new ArrayList<>(algorithm.getSolutions()));
                    /*
                     * algorithm.exportReport( DIRECTORY + File.separator + subDir + File.separator
                     * + "execution_report_" + i);
                     */
                } catch (IOException e) {
                    logger.error(e);
                }

                // logger.info(i + " time: " + algorithm.getComputeTime() + " ms.");
                // Solution.writSolutionsToFile(directory + File.separator + problem.getName() +
                // "_" + i, algorithm.getSolutions());
                bag.addAll(algorithm.getSolutions());
            }
            /*
             * try { reportResume(subDir); } catch (IOException e2) { e2.printStackTrace();
             * }
             */
            String str = "Resume " + problem.getName();
            str += "\n" + "Total time: " + experimentTimeColumn.sum();
            str += "\n" + "Average time : " + experimentTimeColumn.mean() + " ms.";
            str += "\n" + "Solutions in the bag: " + bag.size();
            logger.info(str);
            infoTime.addColumns(experimentTimeColumn);
            infoTime.write().csv(DIRECTORY + File.separator + subDir + File.separator + "times.csv");
            try {
                Solution.writSolutionsToFile(DIRECTORY + File.separator + subDir + File.separator + "nsga_iii_bag_"
                        + problem.getName() + "_F0_" + problem.getNumberOfObjectives(), new ArrayList<>(bag));
            } catch (IOException e1) {
                logger.error(e1);
            }
            Ranking<DoubleSolution> compartor = new DominanceComparator<>();
            compartor.computeRanking(bag);

            logger.info("Fronts : " + compartor.getNumberOfSubFronts());
            logger.info("Front 0 - Original: " + compartor.getSubFront(0).size());

            File f = new File(DIRECTORY);
            if (!f.exists())
                f.mkdirs();
            f = new File(DIRECTORY + File.separator + subDir + File.separator + "Class_F0" + problem.getName()
                    + +problem.getNumberOfObjectives() + ".out");
            Classifier<DoubleSolution> classifier = new SatClassifier<>(problem);
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
            /*
             * if (problem.getNumberOfObjectives() == 3) {
             * 
             * Plotter plotter = new Scatter3D<DoubleSolution>(front, DIRECTORY +
             * File.separator + subDir + File.separator + "Class_F0" + problem.getName() +
             * "_nsga3_WP"); plotter.plot(); // new
             * //Scatter3D(problem.getParetoOptimal3Obj(), directory + File.separator +
             * problem.getName()).plot(); } else { Table table =
             * Table.create(problem.getName() + "_F0_WP_" +
             * problem.getNumberOfObjectives()); for (int j = 0; j <
             * problem.getNumberOfObjectives(); j++) { DoubleColumn column =
             * DoubleColumn.create("objective_" + j); for (int k = 0; k < front.size(); k++)
             * { column.append(front.get(k).getObjective(j).doubleValue()); }
             * table.addColumns(column); } logger.info(table.summary());
             * 
             * }
             */
            logger.info("End Experimentation.");
        }
    }

    @SuppressWarnings("unchecked")
    private static NSGA_III_P<DoubleSolution> dtlzTestSuite(int p, DTLZ_Instance instance) {

        HashMap<String, Object> options = setup(instance.getNumObjectives());
        DTLZP problem = new DTLZP(p, instance);
        int maxIterations = 1000;
        int numberOfObjectives = instance.getNumObjectives();
        switch (p) {
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

        SelectionOperator<DoubleSolution> selectionOperator = new TournamentSelection<>((int) options.get("pop_size"),
                new DominanceComparator<>());
        return new NSGA_III_P<>(problem, (int) options.get("pop_size"), maxIterations, (int) options.get("partitions"),
                selectionOperator, (CrossoverOperator<DoubleSolution>) options.get("crossover"),
                (MutationOperator<DoubleSolution>) options.get("mutation"));
    }

    private static void error(String msg) {
        throw new IllegalArgumentException(msg);
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

    public void reportResume(String subDir) throws IOException {
        Table table = null;

        List<String> names = null;
        for (int i = 0; i < EXPERIMENT; i++) {
            String path = DIRECTORY + File.separator + subDir + File.separator + "execution_report_" + i + ".csv";
            if (table == null) {
                table = Table.read().csv(path);
                names = table.columnNames();
            } else {
                Table tmp = Table.read().csv(path);
                for (String name : names) {
                    DoubleColumn add = table.numberColumn(name).add(tmp.numberColumn(name));
                    add.setName(name);
                    table.replaceColumn(name, add);
                }
            }
        }
        for (String string : names) {
            table.replaceColumn(string, table.numberColumn(string).divide(EXPERIMENT));
        }
        table.write().csv(DIRECTORY + File.separator + subDir + File.separator + "report.csv");
    }

    @Override
    public String toString() {
        return "NSGA3WPExperimentation [CLASSIFY_EVERY_ITERATION=" + CLASSIFY_EVERY_ITERATION + ", DIRECTORY="
                + DIRECTORY + ", ELEMENTS_TO_REPLACE=" + ELEMENTS_TO_REPLACE + ", EXPERIMENT=" + EXPERIMENT
                + ", endProblem=" + endProblem + ", initialProblem=" + initialProblem + ", numberOfObjectives="
                + numberOfObjectives + "]";
    }

}