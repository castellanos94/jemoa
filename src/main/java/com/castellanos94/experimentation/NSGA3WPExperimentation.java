package com.castellanos94.experimentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.castellanos94.algorithms.multi.NSGA_III_WP;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.operators.impl.PolynomialMutation;
import com.castellanos94.operators.impl.TournamentSelection;
import com.castellanos94.preferences.impl.InterClassnC;
import com.castellanos94.problems.DTLZP;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.operators.impl.SBXCrossover;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Plotter;
import com.castellanos94.utils.Scatter3D;
import com.castellanos94.utils.Tools;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Table;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Current Algorithm Experimentation used.
 */
public class NSGA3WPExperimentation {
    private static final Logger logger = LogManager.getLogger(NSGA3WPExperimentation.class);
    private static final int CLASSIFY_EVERY_ITERATION = 0; // Classification F0 each
    private static final int ELEMENTS_TO_REPLACE = 0; // 5 % of population
    private static final int numberOfObjectives = 3;
    static final String DIRECTORY = "experiments_test" + File.separator + numberOfObjectives + File.separator + "NSGA3"
            + File.separator + "C" + CLASSIFY_EVERY_ITERATION + "R" + ELEMENTS_TO_REPLACE;
    static final int EXPERIMENT = 10;

    public static void main(String[] args) throws CloneNotSupportedException, IOException {
        new File(DIRECTORY).mkdirs();
        int[] problems = { 1, 2, 3, 4, 5, 6, 7 };
        ArrayList<Integer> problelmArrayList = new ArrayList<>();
        for (int integer : problems) {
            problelmArrayList.add(integer);
        }
        // problelmArrayList.stream().parallel().forEach(p -> {

        for (int p = 1; p <= 7; p++) {

            Tools.setSeed(1L);
            logger.info("Experimentation: DTLZ with preferences");
            String path = "src/main/resources/DTLZ_INSTANCES/" + numberOfObjectives + "/DTLZ" + p + "_Instance.txt";
            DTLZ_Instance instance = null;
            try {
                instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();
            } catch (FileNotFoundException e3) {
                e3.printStackTrace();
            }
            // logger.info(instance);

            NSGA_III_WP<DoubleSolution> algorithm = dtlzTestSuite(p, instance);
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
            for (int i = 0; i < EXPERIMENT; i++) {
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
                    e.printStackTrace();
                }

                logger.info(i + " time: " + algorithm.getComputeTime() + " ms.");
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
                e1.printStackTrace();
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
                e.printStackTrace();
            }
            if (problem.getNumberOfObjectives() == 3) {
              /*  Plotter plotter = new Scatter3D<DoubleSolution>(front, DIRECTORY + File.separator + subDir
                        + File.separator + "Class_F0" + problem.getName() + "_nsga3_WP");
                plotter.plot();
                // new Scatter3D(problem.getParetoOptimal3Obj(), directory + File.separator +
                // problem.getName()).plot();
            } else {
                /*
                 * Table table = Table.create(problem.getName() + "_F0_WP_" +
                 * problem.getNumberOfObjectives()); for (int j = 0; j <
                 * problem.getNumberOfObjectives(); j++) { DoubleColumn column =
                 * DoubleColumn.create("objective_" + j); for (int k = 0; k < front.size(); k++)
                 * { column.append(front.get(k).getObjective(j).doubleValue()); }
                 * table.addColumns(column); } logger.info(table.summary());
                 */
            }
            logger.info("End Experimentation.");
        }
    }

    @SuppressWarnings("unchecked")
    private static NSGA_III_WP<DoubleSolution> dtlzTestSuite(int p, DTLZ_Instance instance) {

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
            } else if (numberOfObjectives == 5 || numberOfObjectives == 9) {
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
            maxIterations = 1000;
            break;
        }

        SelectionOperator<DoubleSolution> selectionOperator = new TournamentSelection<>((int) options.get("pop_size"),
                new DominanceComparator<>());
        return new NSGA_III_WP<>(problem, (int) options.get("pop_size"), maxIterations, (int) options.get("partitions"),
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

    public static void reportResume(String subDir) throws IOException {
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
}