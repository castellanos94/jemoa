package com.castellanos94.experimentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Scanner;

import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.preferences.impl.InterClassnC;
import com.castellanos94.problems.Problem;
import com.castellanos94.problems.benchmarks.dtlz.DTLZ1;
import com.castellanos94.problems.preferences.dtlz.DTLZ1_P;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;

import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class ExperimentationDTLZPreferences {
    static final String DIRECTORY_DTLZ = "experiments" + File.separator + "dtlz";
    static final String DIRECTORY_DTLZ_PREFERENCES = "experiments" + File.separator + "dtlz_preferences";
    static final String OWNER = "FROM_PROBLEM";

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
            EXPORT_OBJECTIVES_TO_CSV(front_preferences);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    private static void EXPORT_OBJECTIVES_TO_CSV(ArrayList<DoubleSolution> front_preferences) throws IOException {
        Table table = Table.create("FRONT_PREFERENCES");
        for (int i = 0; i < front_preferences.get(0).getProblem().getNumberOfObjectives(); i++) {
            StringColumn column = StringColumn.create("F-" + (i + 1));
            for (Solution solution_ : front_preferences)
                column.append(solution_.getObjective(i).toString());
            table.addColumns(column);
        }
        StringColumn column = StringColumn.create("Problem");
        for (Solution solution_ : front_preferences)
            column.append((String) solution_.getAttribute(OWNER));

        table.addColumns(column);

        table.write().csv(DIRECTORY_DTLZ_PREFERENCES + File.separator + "FRONT_PREFERENCES.csv");
    }

    private static ArrayList<DoubleSolution> makeFrontHSatSat(DTLZ1_P problem, ArrayList<DoubleSolution> solutions) {
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
            solutions.add((DoubleSolution) tmp);
        }
        sc.close();
        return solutions;
    }
}
