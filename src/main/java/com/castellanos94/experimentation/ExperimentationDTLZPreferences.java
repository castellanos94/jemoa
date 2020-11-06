package com.castellanos94.experimentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.preferences.impl.InterClassnC;
import com.castellanos94.problems.Problem;
import com.castellanos94.problems.benchmarks.dtlz.DTLZ;
import com.castellanos94.problems.benchmarks.dtlz.DTLZ1;
import com.castellanos94.problems.benchmarks.dtlz.DTLZ2;
import com.castellanos94.problems.benchmarks.dtlz.DTLZ3;
import com.castellanos94.problems.benchmarks.dtlz.DTLZ4;
import com.castellanos94.problems.preferences.dtlz.DTLZ1_P;
import com.castellanos94.problems.preferences.dtlz.DTLZ2_P;
import com.castellanos94.problems.preferences.dtlz.DTLZ3_P;
import com.castellanos94.problems.preferences.dtlz.DTLZ4_P;
import com.castellanos94.problems.preferences.dtlz.DTLZPreferences;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Distance;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class ExperimentationDTLZPreferences {
    static String name = "dtlz1";
    static final String DIRECTORY_EXPERIMENTS = "experiments";
    static final String DIRECTORY_DTLZ = "experiments" + File.separator + "dtlz" + File.separator + name;
    static final String DIRECTORY_DTLZ_PREFERENCES = "experiments" + File.separator + "dtlz_preferences"
            + File.separator + name;
    static final String OWNER = "FROM_PROBLEM";

    public static void main(String[] args) throws FileNotFoundException {
        DTLZ1 dtlz1 = new DTLZ1();
        System.out.println(dtlz1);
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
        String path_roi = "/home/thinkpad/Documents/jemoa/bestCompromise_DTLZ1_P.out";
        DTLZ_Instance instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();

        DTLZ1_P dtlz1_P = new DTLZ1_P(instance);

        System.out.println(dtlz1_P);
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
        ArrayList<DoubleSolution> roi = readSolution(dtlz1_P, path_roi);
        System.out.println(roi.size());
        System.out.println("Roi HSat Sat");
        ArrayList<DoubleSolution> roi_sat = makeFrontHSatSat(dtlz1_P, roi);

        System.out.println("Uniting fronts, seeking a global 0 front");
        ArrayList<DoubleSolution> bag = new ArrayList<>();
        bag.addAll(result_dtlz1_front);
        bag.addAll(result_preferences_front);
        System.out.println("All non-dominated solutions: " + bag.size());
        Ranking<DoubleSolution> compartor = new DominanceComparator<>();
        compartor.computeRanking(bag);
        System.out.println("Front 0 from bag : " + compartor.getSubFront(0).size() + " "
                + ((double) compartor.getSubFront(0).size() / bag.size()));

        int c_solutions_standard = 0, c_solutions_preferences = 0;
        for (DoubleSolution doubleSolution : compartor.getSubFront(0)) {
            if (doubleSolution.getAttribute(OWNER).equals(dtlz1.getName())) {
                c_solutions_standard++;
            } else {
                c_solutions_preferences++;
            }
        }
        System.out.printf("Solutions %3d (%5.3f) form %s (%5.3f)\n", c_solutions_standard,
                (double) c_solutions_standard / compartor.getSubFront(0).size(), dtlz1.getName(),
                (double) c_solutions_standard / result_dtlz1_front.size());
        System.out.printf("Solutions %3d (%5.3f) form %s (%5.3f)\n", c_solutions_preferences,
                (double) c_solutions_preferences / compartor.getSubFront(0).size(), dtlz1_P.getName(),
                (double) c_solutions_preferences / result_preferences_front.size());
        ArrayList<DoubleSolution> front_preferences = makeFrontHSatSat(dtlz1_P, compartor.getSubFront(0));
        c_solutions_standard = 0;
        c_solutions_preferences = 0;
        for (DoubleSolution doubleSolution : front_preferences) {
            if (doubleSolution.getAttribute(OWNER).equals(dtlz1.getName())) {
                c_solutions_standard++;
            } else {
                c_solutions_preferences++;
            }
        }
        System.out.println("After classify the solutions on the preference front");
        System.out.printf("Solutions HSat or Sat : %3d (%5.3f) form %s (%5.3f)\n", c_solutions_standard,
                (double) c_solutions_standard / front_preferences.size(), dtlz1.getName(),
                (double) c_solutions_standard / result_dtlz1_front.size());
        System.out.printf("Solutions HSat or Sat : %3d (%5.3f) form %s (%5.3f)\n", c_solutions_preferences,
                (double) c_solutions_preferences / front_preferences.size(), dtlz1_P.getName(),
                (double) c_solutions_preferences / result_preferences_front.size());
        try {
            for (DoubleSolution doubleSolution : roi_sat) {
                doubleSolution.setAttribute(OWNER, "ROI_PREFERENCES");
            }
            front_preferences.addAll(roi_sat);
            EXPORT_OBJECTIVES_TO_CSV(front_preferences, dtlz1_P.getName().trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Calculate metrics distance " + name);
        calculate_metrics(result_dtlz1, roi_sat, dtlz1_P, name);

        System.out.println("Calculate metrics distance " + name + "_p");
        calculate_metrics(result_preferences, roi_sat, dtlz1_P, name + "_p");

        System.out.println("Calculate metrics dom " + name + "_p");
        calculate_metrics_dom(result_dtlz1, result_preferences, roi_sat, dtlz1_P, dtlz1, name + "_p");

    }

    private static void calculate_metrics_dom(ArrayList<ArrayList<DoubleSolution>> result_dtlz,
            ArrayList<ArrayList<DoubleSolution>> result_preferences, ArrayList<DoubleSolution> roi_sat,
            DTLZPreferences problem_preferences, DTLZ problem_original, String name) {
        Table table = Table.create(problem_original.getName() + " to ROI Preferences.");
        DoubleColumn corrida = DoubleColumn.create("corrida");
        DoubleColumn frente_zero = DoubleColumn.create("Frente Cero");
        DoubleColumn dom = DoubleColumn.create("Dominancia " + problem_original.getName());
        DoubleColumn dom_p = DoubleColumn.create("Dominancia " + problem_preferences.getName());
        DoubleColumn hsat = DoubleColumn.create("HSAT " + problem_original.getName());
        DoubleColumn sat = DoubleColumn.create("SAT " + problem_original.getName());
        DoubleColumn hsat_p = DoubleColumn.create("HSAT " + problem_preferences.getName());
        DoubleColumn sat_p = DoubleColumn.create("SAT " + problem_preferences.getName());
        for (int i = 0; i < result_dtlz.size(); i++) {
            ArrayList<DoubleSolution> bag = new ArrayList<>();
            bag.addAll(result_dtlz.get(i));
            bag.addAll(result_preferences.get(i));
            Ranking<DoubleSolution> compartor = new DominanceComparator<>();
            compartor.computeRanking(bag);

            int c_solutions_standard = 0, c_solutions_preferences = 0;
            for (DoubleSolution doubleSolution : compartor.getSubFront(0)) {
                if (doubleSolution.getAttribute(OWNER).equals(problem_original.getName())) {
                    c_solutions_standard++;
                } else {
                    c_solutions_preferences++;
                }
            }
            // Classification
            InterClassnC<DoubleSolution> classifier = new InterClassnC<>(problem_preferences);
            ArrayList<DoubleSolution> hs = new ArrayList<>();
            ArrayList<DoubleSolution> s = new ArrayList<>();
            ArrayList<DoubleSolution> d = new ArrayList<>();
            ArrayList<DoubleSolution> hd = new ArrayList<>();
            for (DoubleSolution x : compartor.getSubFront(0)) {
                classifier.classify(x);
                int[] iclass = (int[]) x.getAttribute(classifier.getAttributeKey());
                if (iclass[0] > 0) {
                    hs.add(x);
                    x.setAttribute("class", "HSAT");
                } else if (iclass[1] > 0) {
                    s.add(x);
                    x.setAttribute("class", "SAT");
                } else if (iclass[2] > 0) {
                    d.add(x);
                    x.setAttribute("class", "DIS");
                } else {
                    hd.add(x);
                    x.setAttribute("class", "HDIS");
                }
            }

            corrida.append(i);
            frente_zero.append(compartor.getSubFront(0).size());
            dom.append(c_solutions_standard);
            dom_p.append(c_solutions_preferences);
            // Verificar hsat en original y preferences
            c_solutions_standard = 0;
            c_solutions_preferences = 0;
            for (DoubleSolution doubleSolution : hs) {
                if (doubleSolution.getAttribute(OWNER).equals(problem_original.getName())) {
                    c_solutions_standard++;
                } else {
                    c_solutions_preferences++;
                }
            }
            hsat.append(c_solutions_standard);
            hsat_p.append(c_solutions_preferences);
            // Verificar sat en original y preferences
            c_solutions_standard = 0;
            c_solutions_preferences = 0;
            for (DoubleSolution doubleSolution : s) {
                if (doubleSolution.getAttribute(OWNER).equals(problem_original.getName())) {
                    c_solutions_standard++;
                } else {
                    c_solutions_preferences++;
                }
            }

            sat.append(c_solutions_standard);
            sat_p.append(c_solutions_preferences);
        }
        table.addColumns(corrida, frente_zero, dom, dom_p, hsat, sat, hsat_p, sat_p);
        try {
            System.out.println(table.summary());
            table.write().csv(DIRECTORY_EXPERIMENTS + File.separator + "resume_dominancia_" + name + ".csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void calculate_metrics(ArrayList<ArrayList<DoubleSolution>> results,
            ArrayList<DoubleSolution> roi_sat, DTLZPreferences problem, String label) {
        // ArrayList<ArrayList<DoubleSolution>> result_sat = new ArrayList<>();
        /*
         * for (int i = 0; i < results.size(); i++) {
         * result_sat.add(makeFrontHSatSat(problem, results.get(i))); }
         */
        Table table = Table.create(label + " to ROI Preferences.");
        DoubleColumn corrida = DoubleColumn.create("corrida");
        DoubleColumn euclidean = DoubleColumn.create("euclidean");
        DoubleColumn chebyshev = DoubleColumn.create("chebyshev");

        Distance<DoubleSolution> distance = new Distance<>(Distance.Metric.EUCLIDEAN_DISTANCE);
        for (int i = 0; i < results.size(); i++) {
            distance.setMetric(Distance.Metric.EUCLIDEAN_DISTANCE);
            List<Data> distances_ = distance.evaluate(results.get(i), roi_sat);
            RealData min = new RealData(Double.MAX_VALUE);
            for (int j = 0; j < distances_.size(); j++) {
                if (min.compareTo(distances_.get(j)) > 0) {
                    min = (RealData) distances_.get(j).copy();
                }
            }
            corrida.append(i);
            euclidean.append(min.doubleValue());

            distance.setMetric(Distance.Metric.CHEBYSHEV_DISTANCE);
            distances_ = distance.evaluate(results.get(i), roi_sat);
            min = new RealData(Double.MAX_VALUE);
            for (int j = 0; j < distances_.size(); j++) {
                if (min.compareTo(distances_.get(j)) > 0) {
                    min = (RealData) distances_.get(j).copy();
                }
            }
            chebyshev.append(min.doubleValue());
        }
        table.addColumns(corrida, euclidean, chebyshev);
        try {
            System.out.println(table.summary());
            table.write().csv(DIRECTORY_EXPERIMENTS + File.separator + "resume_distances_" + label + ".csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    private static void EXPORT_OBJECTIVES_TO_CSV(ArrayList<DoubleSolution> front_preferences, String label)
            throws IOException {
        Table table = Table.create("FRONT_PREFERENCES");
        for (int i = 0; i < front_preferences.get(0).getProblem().getNumberOfObjectives(); i++) {
            StringColumn column = StringColumn.create("F-" + (i + 1));
            for (Solution solution_ : front_preferences)
                column.append(solution_.getObjective(i).toString());
            table.addColumns(column);
        }
        StringColumn column = StringColumn.create("Problem");
        StringColumn category = StringColumn.create("Class");
        for (Solution solution_ : front_preferences) {
            column.append((String) solution_.getAttribute(OWNER));
            category.append((String) solution_.getAttribute("class"));
        }

        table.addColumns(column, category);

        table.write().csv(DIRECTORY_EXPERIMENTS + File.separator + "FRONT_PREFERENCES_" + label + ".csv");
    }

    private static ArrayList<DoubleSolution> makeFrontHSatSat(DTLZPreferences problem,
            ArrayList<DoubleSolution> solutions) {
        System.out.println("******* Prefrences classification *******");
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
                x.setAttribute("class", "HSAT");
            } else if (iclass[1] > 0) {
                s.add(x);
                x.setAttribute("class", "SAT");
            } else if (iclass[2] > 0) {
                d.add(x);
                x.setAttribute("class", "DIS");
            } else {
                hd.add(x);
                x.setAttribute("class", "HDIS");
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
        System.out.println("******* END *******");
        return front;

    }

    @SuppressWarnings("rawtypes")
    private static ArrayList<DoubleSolution> readSolution(Problem problem, String path) throws FileNotFoundException {
        ArrayList<DoubleSolution> solutions = new ArrayList<>();
        Scanner sc = new Scanner(new File(path));
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            Solution tmp = problem.generateFromVarString(line.split("\\*")[0].trim());
            tmp.setAttribute(OWNER, problem.getName());
            solutions.add((DoubleSolution) tmp.copy());
        }
        sc.close();
        return solutions;
    }
}
