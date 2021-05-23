package com.castellanos94.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.mcda.SatClassifier;
import com.castellanos94.problems.DTLZP;
import com.castellanos94.solutions.DoubleSolution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Java Bag 4591, F0 - Original : 266. HSat : 154, Sat : 0, Dis : 0, HDis : 112
 * Python Bag 4600, F0 - Original : 1613. 556 0 0 1057.
 */
public class ReadSolution {
    static final String DIRECTORY = "experiments" + File.separator + "dtlz_preferences";
    private final static String FROM_ALGORITHM = "from-algorhtm";
    private static final Logger logger = LogManager.getLogger(ReadSolution.class);

    public static void main(String[] args) throws IOException {
        String solutionPath = "/home/thinkpad/Documents/jemoa/experiments/dtlz_preferences/Class_F0DTLZ1_P3.out";

        String path = "src/main/resources/DTLZ_INSTANCES/DTLZ1_Instance.txt";
        // path = "src/main/resources/instances/dtlz/PreferenceDTLZ1_Instance_01.txt";
        DTLZ_Instance instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();
        logger.info(instance);
        int numberOfProblem = 1;
        DTLZP problem = new DTLZP(numberOfProblem, instance);
        DoubleSolution _best = problem.generate();
        for (int i = 0; i < _best.getNumberOfVariables(); i++) {
            _best.setVariable(i, instance.getBestCompromises()[0][i].doubleValue());
        }
        problem.evaluate(_best);
        problem.evaluateConstraint(_best);
        System.out.println(_best);
        SatClassifier<DoubleSolution> classifier = new SatClassifier<>(problem);

        classifier.classify(_best);
        int[] _iclass = (int[]) _best.getAttribute(classifier.getAttributeKey());

        System.out.println(Arrays.toString(_iclass) + " " + _best.getObjectives());
        ArrayList<DoubleSolution> bag_java = loadFromFile(problem, solutionPath);
        for (DoubleSolution doubleSolution : bag_java) {
            doubleSolution.setAttribute(FROM_ALGORITHM, "java");
        }
        ArrayList<DoubleSolution> bag_python = loadFromFile(problem,
                "/home/thinkpad/PycharmProjects/jMetalPy/results/Class_F0enfoque_frontsNSGAIII_custom.DTLZ1P_3.csv");
        for (DoubleSolution doubleSolution : bag_python) {
            doubleSolution.setAttribute(FROM_ALGORITHM, "python");
        }
        logger.info("Bag java : " + bag_java.size());
        logger.info("Bag python : " + bag_python.size());
        ArrayList<DoubleSolution> solutions = new ArrayList<>();
        // Join bags
        solutions.addAll(bag_java);
        solutions.addAll(bag_python);
        logger.info("Bag : " + solutions.size());

        // Ranking
        Ranking<DoubleSolution> compartor = new DominanceComparator<>();
        compartor.computeRanking(solutions);
        double size = compartor.getSubFront(0).size();
        logger.info(size);
        // Counting solutions of x algorithm
        int from_java = 0, from_python = 0;
        for (DoubleSolution doubleSolution : compartor.getSubFront(0)) {
            if (((String) doubleSolution.getAttribute(FROM_ALGORITHM)).contains("java")) {
                from_java += 1;
            } else {
                from_python += 1;
            }
        }
        System.out.printf("Java Solutions %5.3f, Python Solutions %5.3f\n", from_java / size, from_python / size);
        // Classifcation
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
        } else if (!s.isEmpty()) {
            front.addAll(s);
        } else if (!d.isEmpty()) {
            front.addAll(d);
        } else if (!hd.isEmpty()) {
            front.addAll(hd);
        }
        from_java = 0;
        from_python = 0;
        for (DoubleSolution doubleSolution : hs) {
            if (((String) doubleSolution.getAttribute(FROM_ALGORITHM)).contains("java")) {
                from_java += 1;
            } else {
                from_python += 1;
            }
        }
        logger.info(String.format("HSat : %3d, Sat : %3d, Dis : %3d, HDis : %3d", hs.size(), s.size(), d.size(),
                hd.size()));
        logger.info("Front 0: " + front.size());

        System.out.printf("HSAT -> Java Solutions %5.3f, Python Solutions %5.3f\n", from_java / size,
                from_python / size);

        from_java = 0;
        from_python = 0;
        for (DoubleSolution doubleSolution : s) {
            if (((String) doubleSolution.getAttribute(FROM_ALGORITHM)).contains("java")) {
                from_java += 1;
            } else {
                from_python += 1;
            }
        }
        System.out.printf("SAT -> Java Solutions %5.3f, Python Solutions %5.3f\n", (double) from_java / s.size(),
                (double) from_python / s.size());

        /*
         * for (DoubleSolution doubleSolution : front) {
         * System.out.println(Arrays.toString(((int[])
         * doubleSolution.getAttribute(classifier.getAttributeKey()))) + " - " +
         * doubleSolution.getObjectives()); }
         */
        /*
         * File f = new File(DIRECTORY + File.separator + "nsga3" + problem.getName() +
         * "_F0_WP" + problem.getNumberOfObjectives());
         * 
         * ArrayList<String> strings = new ArrayList<>(); for (DoubleSolution solution :
         * front) strings.add(solution.toString());
         * 
         * Files.write(f.toPath(), strings, Charset.defaultCharset()); if
         * (problem.getNumberOfObjectives() == 3) { Plotter plotter = new
         * Scatter3D<DoubleSolution>(front, DIRECTORY + File.separator +
         * problem.getName() + "F0_WP_nsga3"); plotter.plot(); // new
         * Scatter3D(problem.getParetoOptimal3Obj(), directory + File.separator + //
         * problem.getName()).plot(); } else { Table table =
         * Table.create(problem.getName() + "_F0_WP_" +
         * problem.getNumberOfObjectives()); for (int j = 0; j <
         * problem.getNumberOfObjectives(); j++) { DoubleColumn column =
         * DoubleColumn.create("objective_" + j); for (int k = 0; k < front.size(); k++)
         * { column.append(front.get(k).getObjective(j).doubleValue()); }
         * table.addColumns(column); } logger.info(table.summary()); }
         */

    }

    private static ArrayList<DoubleSolution> loadFromFile(DTLZP problem, String path) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(path));
        ArrayList<DoubleSolution> solutions = new ArrayList<>();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (!line.contains("variables"))
                solutions.add(problem.generateFromVarString(line.split("\\*")[0]));
        }
        sc.close();
        return solutions;
    }
}
