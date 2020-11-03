package com.castellanos94.experimentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.preferences.impl.InterClassnC;
import com.castellanos94.problems.Problem;
import com.castellanos94.problems.benchmarks.dtlz.DTLZ1;
import com.castellanos94.problems.preferences.dtlz.DTLZ1_P;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Plotter;
import com.castellanos94.utils.Scatter3D;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;

public class ExperimentationDTLZPreferences {
    static final String DIRECTORY_DTLZ = "experiments" + File.separator + "dtlz";
    static final String DIRECTORY_DTLZ_PREFERENCES = "experiments" + File.separator + "dtlz_preferences";

    public static void main(String[] args) throws FileNotFoundException {
        DTLZ1 dtlz1 = new DTLZ1();
        System.out.println("Reading DTLZ standard");
        ArrayList<ArrayList<DoubleSolution>> result_dtlz1 = new ArrayList<>();
        ArrayList<DoubleSolution> result_dtlz1_front = new ArrayList<>();
        for (String name : new File(DIRECTORY_DTLZ).list()) {
            if (name.contains(".out") && !name.contains("bag")) {
                result_dtlz1.add(readSolution(dtlz1, DIRECTORY_DTLZ + File.separator + name));
            } else if (name.contains("bag")) {
                result_dtlz1_front = readSolution(dtlz1, DIRECTORY_DTLZ + File.separator + name);
            }
        }
        System.out.println(result_dtlz1.size());
        System.out.println(result_dtlz1_front.size());
        System.out.println("Reading DTLZ preferences");
        String path = "src/main/resources/DTLZ_INSTANCES/DTLZ1_Instance.txt";
        // path = "src/main/resources/instances/dtlz/PreferenceDTLZ1_Instance_01.txt";
        DTLZ_Instance instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();

        DTLZ1_P dtlz1_P = new DTLZ1_P(instance);
        ArrayList<ArrayList<DoubleSolution>> result_preferences = new ArrayList<>();
        ArrayList<DoubleSolution> result_preferences_front = new ArrayList<>();
        for (String name : new File(DIRECTORY_DTLZ_PREFERENCES).list()) {
            if (name.contains(".out") && !name.contains("bag") && !name.contains("Class")) {
                result_preferences.add(readSolution(dtlz1_P, DIRECTORY_DTLZ_PREFERENCES + File.separator + name));
            } else if (name.contains("Class") && name.contains(".out")) {
                result_preferences_front = readSolution(dtlz1_P, DIRECTORY_DTLZ_PREFERENCES + File.separator + name);
            }
        }
        System.out.println(result_preferences.size());
        System.out.println(result_preferences_front.size());
        System.out.println("Reading ROI");
        ArrayList<DoubleSolution> roi = readSolution(dtlz1_P,
                "/home/thinkpad/Documents/jemoa/bestCompromise_DTLZ1_P.out");
        System.out.println(roi.size());
        /*
         * if (dtlz1_P.getNumberOfObjectives() == 3) { Plotter plotter = new
         * Scatter3D<DoubleSolution>(roi, DIRECTORY_DTLZ_PREFERENCES + File.separator
         * +"ROI_"+ dtlz1_P.getName()); plotter.plot(); // new
         * Scatter3D(problem.getParetoOptimal3Obj(), directory + File.separator + //
         * problem.getName()).plot(); } else { Table table =
         * Table.create(dtlz1_P.getName() + "ROI_" + dtlz1_P.getNumberOfObjectives());
         * for (int j = 0; j < dtlz1_P.getNumberOfObjectives(); j++) { DoubleColumn
         * column = DoubleColumn.create("objective_" + j); for (int k = 0; k <
         * roi.size(); k++) { column.append(roi.get(k).getObjective(j).doubleValue()); }
         * table.addColumns(column); } }
         */
        classification(dtlz1_P, roi, "ROI_HSAT_SAT_");
        classification(dtlz1_P, result_dtlz1_front, "From_DTLZ1");
        classification(dtlz1_P, result_preferences_front, "From_DTLZ1_Preferences");

    }

    private static void classification(DTLZ1_P problem, ArrayList<DoubleSolution> solutions, String label) {
        InterClassnC<DoubleSolution> classifier = new InterClassnC<>(problem);
        ArrayList<DoubleSolution> front = new ArrayList<>();
        ArrayList<DoubleSolution> hs = new ArrayList<>();
        ArrayList<DoubleSolution> s = new ArrayList<>();
        ArrayList<DoubleSolution> d = new ArrayList<>();
        ArrayList<DoubleSolution> hd = new ArrayList<>();
        for (DoubleSolution x : solutions) {
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
        System.out.println(String.format("HSat : %3d, Sat : %3d, Dis : %3d, HDis : %3d", hs.size(), s.size(), d.size(),
                hd.size()));
        System.out.println("Front Preferences (HSAT + SAT): " + front.size());
        for (DoubleSolution doubleSolution : front) {
            int[] iclass = (int[]) doubleSolution.getAttribute(classifier.getAttributeKey());

            System.out.println(Arrays.toString(iclass) + " " + doubleSolution.getObjectives());
        }
        if (problem.getNumberOfObjectives() == 3) {
            Plotter plotter = new Scatter3D<DoubleSolution>(front,
                    DIRECTORY_DTLZ_PREFERENCES + File.separator + label + problem.getName());
            plotter.plot();
        }

    }

    private static ArrayList<DoubleSolution> readSolution(Problem problem, String path) throws FileNotFoundException {
        ArrayList<DoubleSolution> solutions = new ArrayList<>();
        Scanner sc = new Scanner(new File(path));
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            Solution tmp = problem.generateFromVarString(line.split("\\*")[0].trim());
            solutions.add((DoubleSolution) tmp);
        }
        sc.close();
        return solutions;
    }
}
