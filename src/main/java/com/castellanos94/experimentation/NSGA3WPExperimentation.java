package com.castellanos94.experimentation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.castellanos94.algorithms.multi.NSGA_III;
import com.castellanos94.algorithms.multi.NSGA_III_WP;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.operators.impl.PolynomialMutation;
import com.castellanos94.operators.impl.TournamentSelection;
import com.castellanos94.preferences.impl.InterClassnC;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.operators.impl.SBXCrossover;
import com.castellanos94.problems.preferences.dtlz.*;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Plotter;
import com.castellanos94.utils.Scatter3D;
import com.castellanos94.utils.Tools;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 31105.84 ms dtlz2 36910.32 ms dtlz3 48132.62 ms dtlz4 28587.02 ms dtlz5
 * 23557.02 ms dtlz6 27430.4 ms dtlz7 dtlz6 rework
 */
public class NSGA3WPExperimentation {
    private static final Logger logger = LogManager.getLogger(NSGA3WPExperimentation.class);
    private static final int CLASSIFY_EVERY_ITERATION = 0; // Classification F0 each 3%
    private static final int ELEMENTS_TO_REPLACE = 0; // 5 % of population
    static final String DIRECTORY = "experiments" + File.separator + "NSGA3" + File.separator + "C"
            + CLASSIFY_EVERY_ITERATION + "R" + ELEMENTS_TO_REPLACE;
    static final int EXPERIMENT = 50;

    public static void main(String[] args) throws CloneNotSupportedException, IOException {
        new File(DIRECTORY).mkdirs();
        for (int p = 1; p <= 7; p++) {

            Tools.setSeed(1L);
            logger.info("Experimentation: DTLZ with preferences");
            String path = "src/main/resources/DTLZ_INSTANCES/DTLZ" + p + "_Instance.txt";
            DTLZ_Instance instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();
            logger.info(instance);
            DTLZPreferences problem = null;
            switch (p) {
                case 1:
                    problem = new DTLZ1_P(instance);
                    break;
                case 2:
                    problem = new DTLZ2_P(instance);
                    break;
                case 3:
                    problem = new DTLZ3_P(instance);
                    break;
                case 4:
                    problem = new DTLZ4_P(instance);
                    break;
                case 5:
                    problem = new DTLZ5_P(instance);
                    break;
                case 6:
                    problem = new DTLZ6_P(instance);
                    break;
                case 7:
                    problem = new DTLZ7_P(instance);
                    break;
            }
            String subDir = problem.getName().trim();
            if (!new File(DIRECTORY + File.separator + subDir).exists()) {
                new File(DIRECTORY + File.separator + subDir).mkdir();
            }

            int popSize = 92;
            int numberOfDivision = 12;
            SBXCrossover crossover = new SBXCrossover(30, 1.0);
            PolynomialMutation mutation = new PolynomialMutation();
            int maxIterations = 1000;

            SelectionOperator<DoubleSolution> selectionOperator = new TournamentSelection<>(popSize,
                    new DominanceComparator<DoubleSolution>());
            NSGA_III_WP<DoubleSolution> algorithm = new NSGA_III_WP<>(problem, popSize, maxIterations, numberOfDivision,
                    selectionOperator, crossover, mutation);
            algorithm.setClassifyEveryIteration(CLASSIFY_EVERY_ITERATION);
            algorithm.setNElementsToReplace(ELEMENTS_TO_REPLACE);
            logger.info(problem);
            logger.info(algorithm);

            ArrayList<DoubleSolution> bag = new ArrayList<>();
            long averageTime = 0;

            for (int i = 0; i < EXPERIMENT; i++) {
                algorithm = new NSGA_III_WP<>(problem, popSize, maxIterations, numberOfDivision, selectionOperator,
                        crossover, mutation);// algorithm.setReferenceHyperplane(referenceHyperplane);
                // referenceHyperplane.resetCount();
                algorithm.setClassifyEveryIteration(CLASSIFY_EVERY_ITERATION);
                algorithm.setNElementsToReplace(ELEMENTS_TO_REPLACE);
                algorithm.execute();
                averageTime += algorithm.getComputeTime();
                try {
                    Solution.writSolutionsToFile(
                            DIRECTORY + File.separator + subDir + File.separator + "execution_" + i,
                            new ArrayList<>(algorithm.getSolutions()));
                    algorithm.exportReport(
                            DIRECTORY + File.separator + subDir + File.separator + "execution_report_" + i);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                logger.info(i + " time: " + algorithm.getComputeTime() + " ms.");
                // Solution.writSolutionsToFile(directory + File.separator + problem.getName() +
                // "_" + i, algorithm.getSolutions());
                bag.addAll(algorithm.getSolutions());
            }
            reportResume(subDir);

            logger.info("Resume " + problem.getName());
            logger.info("Total time: " + averageTime);
            logger.info("Average time : " + (double) averageTime / EXPERIMENT + " ms.");
            logger.info("Solutions in the bag: " + bag.size());
            Solution.writSolutionsToFile(DIRECTORY + File.separator + subDir + File.separator + "nsga_iii_bag_"
                    + problem.getName() + "_F0_" + problem.getNumberOfObjectives(), new ArrayList<>(bag));
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
            logger.info(String.format("HSat : %3d, Sat : %3d, Dis : %3d, HDis : %3d", hs.size(), s.size(), d.size(),
                    hd.size()));
            logger.info("Front 0: " + front.size());

            ArrayList<String> strings = new ArrayList<>();
            for (DoubleSolution solution : front)
                strings.add(solution.toString());

            Files.write(f.toPath(), strings, Charset.defaultCharset());
            if (problem.getNumberOfObjectives() == 3) {
                Plotter plotter = new Scatter3D<DoubleSolution>(front, DIRECTORY + File.separator + subDir
                        + File.separator + "Class_F0" + problem.getName() + "_nsga3_WP");
                plotter.plot();
                // new Scatter3D(problem.getParetoOptimal3Obj(), directory + File.separator +
                // problem.getName()).plot();
            } else {
                Table table = Table.create(problem.getName() + "_F0_WP_" + problem.getNumberOfObjectives());
                for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                    DoubleColumn column = DoubleColumn.create("objective_" + j);
                    for (int k = 0; k < front.size(); k++) {
                        column.append(front.get(k).getObjective(j).doubleValue());
                    }
                    table.addColumns(column);
                }
                logger.info(table.summary());
            }
            logger.info("End Experimentation.");
        }
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