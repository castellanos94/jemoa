package com.castellanos94.experimentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.preferences.impl.InterClassnC;
import com.castellanos94.problems.preferences.dtlz.*;
import com.castellanos94.problems.preferences.dtlz.DTLZPreferences;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.StacClient;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class NSGA3DPMetrics {
    private static String algorithmName = "nsga3dp";
    private static final String OWNER = "FROM_PROBLEM";
    private static String DIRECTORY = "experiments" + File.separator + algorithmName + File.separator;

    public static void main(String[] args) throws IOException {
        HashMap<String, ArrayList<DoubleSolution>> roi = new HashMap<>();
        HashMap<String, DTLZPreferences> problems = new HashMap<>();
        HashMap<DTLZPreferences, HashMap<String, ArrayList<ArrayList<DoubleSolution>>>> globalSolutionByProblem = new HashMap<>();
        for (File f : new File(DIRECTORY).listFiles()) {
            if (f.isDirectory()) {
                HashMap<DTLZPreferences, ArrayList<ArrayList<DoubleSolution>>> algorithmProblems = new HashMap<>();
                for (File _file : f.listFiles()) {
                    if (_file.isDirectory()) {
                        DTLZPreferences currentProblem;
                        if (problems.containsKey(_file.getName())) {
                            currentProblem = problems.get(_file.getName());
                        } else {
                            currentProblem = loadProblem(_file.getName());
                            problems.put(_file.getName(), currentProblem);
                            roi.put(currentProblem.getName(),
                                    loadSolutions(currentProblem, loadPathRoi(_file.getName())));

                            for (DoubleSolution s : roi.get(currentProblem.getName())) {
                                s.setAttribute(OWNER, "ROI");
                            }
                            globalSolutionByProblem.put(currentProblem, new HashMap<>());
                        }
                        ArrayList<ArrayList<DoubleSolution>> solutionFromProblem = new ArrayList<>();
                        for (File executions : _file.listFiles()) {
                            if (executions.getName().contains("execution") && executions.getName().endsWith(".out")) {
                                solutionFromProblem.add(loadSolutions(currentProblem, executions));
                            }
                        }
                        algorithmProblems.put(currentProblem, solutionFromProblem);
                    }
                }
                Iterator<DTLZPreferences> _Iterator = algorithmProblems.keySet().iterator();
                while (_Iterator.hasNext()) {
                    DTLZPreferences p = _Iterator.next();
                    HashMap<String, ArrayList<ArrayList<DoubleSolution>>> map = globalSolutionByProblem.get(p);
                    map.put(f.getName(), algorithmProblems.get(p));
                }

            }
        }
        // make hsat roi for problem
        System.out.println("Make roi preferences");
        Iterator<String> problem_Iterator = problems.keySet().iterator();
        String[] algorithmStrings = null;

        while (problem_Iterator.hasNext()) {
            String key = problem_Iterator.next();
            System.out.print(key + " ");
            roi.put(key, makeCSat(problems.get(key), roi.get(key), true));
            if (algorithmStrings == null) {
                algorithmStrings = globalSolutionByProblem.get(problems.get(key)).keySet()
                        .toArray(new String[globalSolutionByProblem.get(problems.get(key)).keySet().size()]);
            }

        }
        final String[] _names_algorithm = algorithmStrings;
        // Check
        globalSolutionByProblem.forEach((_problem, map) -> {
            System.out.println("Problem : " + _problem.getName() + " -> " + map.keySet());
        });

        HashMap<DTLZPreferences, ArrayList<ArrayList<DoubleSolution>>> globalSolutionNDByProblem = new HashMap<>();
        HashMap<DTLZPreferences, ArrayList<ArrayList<DoubleSolution>>> globalSolution = new HashMap<>();
        HashMap<DTLZPreferences, ArrayList<ArrayList<DoubleSolution>>> globalCSat = new HashMap<>();

        globalSolutionByProblem.forEach((_problem, map) -> {
            ArrayList<ArrayList<DoubleSolution>> currentBag = new ArrayList<>();
            ArrayList<ArrayList<DoubleSolution>> noDomiList = new ArrayList<>();
            ArrayList<ArrayList<DoubleSolution>> _csat = new ArrayList<>();

            int size = map.get(map.keySet().iterator().next()).size();
            for (int i = 0; i < size; i++) {
                currentBag.add(new ArrayList<>());
            }
            map.forEach((_algorithm_name, _solutions) -> {
                int _index = 0;
                for (ArrayList<DoubleSolution> _execution : _solutions) {
                    for (DoubleSolution _Solution : _execution) {
                        _Solution.setAttribute(OWNER, _algorithm_name + ":" + _problem.getName());
                    }
                    currentBag.get(_index++).addAll(_execution);
                }
            });
            for (int i = 0; i < currentBag.size(); i++) {
                // Dominance
                DominanceComparator<DoubleSolution> comparator = new DominanceComparator<>();
                comparator.computeRanking(currentBag.get(i));
                noDomiList.add(comparator.getSubFront(0));
                // CSat Only F0
                _csat.add(makeCSat(_problem, noDomiList.get(i), false));
            }

            globalSolutionNDByProblem.put(_problem, noDomiList);
            globalSolution.put(_problem, currentBag);
            globalCSat.put(_problem, _csat);
        });
        Table table = Table.create("Metrics DTLZ");
        StringColumn _name = StringColumn.create("problem");
        DoubleColumn all = DoubleColumn.create("solutions");
        DoubleColumn frontZero = DoubleColumn.create("F-0");
        ArrayList<DoubleColumn> hsColumns = new ArrayList<>();
        ArrayList<DoubleColumn> sColumns = new ArrayList<>();
        ArrayList<DoubleColumn> zColumns = new ArrayList<>();
        for (int i = 0; i < _names_algorithm.length; i++) {
            DoubleColumn _f0 = DoubleColumn.create("A-" + _names_algorithm[i] + "-F-0");
            DoubleColumn _hsat = DoubleColumn.create("A-" + _names_algorithm[i] + "-HSat");
            DoubleColumn _sat = DoubleColumn.create("A-" + _names_algorithm[i] + "-Sat");
            zColumns.add(_f0);
            hsColumns.add(_hsat);
            sColumns.add(_sat);
        }
        Iterator<String> iterator = problems.keySet().iterator();
        while (iterator.hasNext()) {
            DTLZPreferences dtlz = problems.get(iterator.next());
            for (int j = 0; j < globalSolution.get(dtlz).size(); j++) {
                ArrayList<DoubleSolution> b = globalSolution.get(dtlz).get(j);
                all.append(b.size());
                _name.append(dtlz.getName() + "-" + j);
            }
            for (ArrayList<DoubleSolution> solutions : globalSolutionNDByProblem.get(dtlz)) {
                frontZero.append(solutions.size());
                HashMap<String, ArrayList<DoubleSolution>> grouped = groupByAlgorithm(solutions, _names_algorithm);
                grouped.forEach((__name, _solutions) -> {
                    for (DoubleColumn doubleColumn : zColumns) {
                        if (doubleColumn.name().equals("A-" + __name + "-F-0")) {
                            doubleColumn.append(-_solutions.size());
                            break;
                        }
                    }
                });

            }
            for (ArrayList<DoubleSolution> solutions : globalCSat.get(dtlz)) {
                HashMap<String, ArrayList<DoubleSolution>> grouped = groupByAlgorithm(solutions, _names_algorithm);
                grouped.forEach((__name, _solutions) -> {
                    int hsat = 0, sat = 0;
                    for (DoubleSolution s : _solutions) {
                        if (s.getAttribute("class").toString().equalsIgnoreCase("hsat")) {
                            hsat++;
                        } else if (s.getAttribute("class").toString().equalsIgnoreCase("sat")) {
                            sat++;
                        }
                    }

                    for (DoubleColumn doubleColumn : hsColumns) {
                        if (doubleColumn.name().equals("A-" + __name + "-HSat")) {
                            doubleColumn.append(-hsat);
                            break;
                        }
                    }
                    for (DoubleColumn doubleColumn : sColumns) {
                        if (doubleColumn.name().equals("A-" + __name + "-Sat")) {
                            doubleColumn.append(-sat);
                            break;
                        }
                    }

                });

            }
        }

        table.addColumns(_name, all, frontZero);

        for (DoubleColumn doubleColumn : zColumns) {
            table.addColumns(doubleColumn);
        }

        for (DoubleColumn doubleColumn : hsColumns) {
            table.addColumns(doubleColumn);
        }

        for (DoubleColumn doubleColumn : sColumns) {
            table.addColumns(doubleColumn);
        }
        // System.out.println(table.summary());
        table.write().csv(DIRECTORY + "metrics.csv");
        // Reset
        globalMetric(globalSolutionNDByProblem, roi, _names_algorithm);

    }

    private static void globalMetric(
            HashMap<DTLZPreferences, ArrayList<ArrayList<DoubleSolution>>> globalSolutionNDByProblem,
            HashMap<String, ArrayList<DoubleSolution>> roi, String[] _names_algorithm) throws IOException {
        StringColumn _nameG = StringColumn.create("problem");
        DoubleColumn allG = DoubleColumn.create("solutions");
        DoubleColumn frontZeroG = DoubleColumn.create("F-0");
        ArrayList<DoubleColumn> hsColumnsG = new ArrayList<>();
        ArrayList<DoubleColumn> sColumnsG = new ArrayList<>();
        ArrayList<DoubleColumn> zColumnsG = new ArrayList<>();
        for (int i = 0; i < _names_algorithm.length; i++) {
            DoubleColumn _f0 = DoubleColumn.create("A-" + _names_algorithm[i] + "-F-0");
            DoubleColumn _hsat = DoubleColumn.create("A-" + _names_algorithm[i] + "-HSat");
            DoubleColumn _sat = DoubleColumn.create("A-" + _names_algorithm[i] + "-Sat");
            zColumnsG.add(_f0);
            hsColumnsG.add(_hsat);
            sColumnsG.add(_sat);
        }
        // Global
        globalSolutionNDByProblem.forEach((_p, bags) -> {
            ArrayList<DoubleSolution> bag = new ArrayList<>();
            bags.forEach(b -> bag.addAll(b));
            DominanceComparator<DoubleSolution> comparator = new DominanceComparator<>();
            comparator.computeRanking(bag);
            ArrayList<DoubleSolution> front = comparator.getSubFront(0);
            _nameG.append(_p.getName());
            allG.append(bag.size());
            frontZeroG.append(front.size());
            HashMap<String, ArrayList<DoubleSolution>> groupByAlgorithm = groupByAlgorithm(front, _names_algorithm);
            groupByAlgorithm.forEach((__name, _solutions) -> {
                for (DoubleColumn doubleColumn : zColumnsG) {
                    if (doubleColumn.name().equals("A-" + __name + "-F-0")) {
                        doubleColumn.append(-_solutions.size());
                        break;
                    }
                }
            });
            ArrayList<DoubleSolution> csatSolutions = makeCSat(_p, front, true);
            HashMap<String, ArrayList<DoubleSolution>> grouped = groupByAlgorithm(csatSolutions, _names_algorithm);
            grouped.forEach((__name, _solutions) -> {
                int hsat = 0, sat = 0;
                for (DoubleSolution s : _solutions) {
                    if (s.getAttribute("class").toString().equalsIgnoreCase("hsat")) {
                        hsat++;
                    } else if (s.getAttribute("class").toString().equalsIgnoreCase("sat")) {
                        sat++;
                    }
                }

                for (DoubleColumn doubleColumn : hsColumnsG) {
                    if (doubleColumn.name().equals("A-" + __name + "-HSat")) {
                        doubleColumn.append(-hsat);
                        break;
                    }
                }
                for (DoubleColumn doubleColumn : sColumnsG) {
                    if (doubleColumn.name().equals("A-" + __name + "-Sat")) {
                        doubleColumn.append(-sat);
                        break;
                    }
                }

            });

            csatSolutions.addAll(roi.get(_p.getName()));

            try {
                EXPORT_OBJECTIVES_TO_CSV(csatSolutions, _p.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        Table global = Table.create("global");
        global.addColumns(_nameG, allG, frontZeroG);

        for (DoubleColumn doubleColumn : zColumnsG) {
            global.addColumns(doubleColumn);
        }

        for (DoubleColumn doubleColumn : hsColumnsG) {
            global.addColumns(doubleColumn);
        }

        for (DoubleColumn doubleColumn : sColumnsG) {
            global.addColumns(doubleColumn);
        }
        global.write().csv(DIRECTORY + "metrics-global.csv");
    }

    private static void EXPORT_OBJECTIVES_TO_CSV(ArrayList<DoubleSolution> front_preferences, String label)
            throws IOException {
        Table table = Table.create("FRONT_PREFERENCES");
        for (int i = 0; i < front_preferences.get(0).getProblem().getNumberOfObjectives(); i++) {
            StringColumn column = StringColumn.create("F-" + (i + 1));
            for (DoubleSolution solution_ : front_preferences)
                column.append(solution_.getObjective(i).toString());
            table.addColumns(column);
        }
        StringColumn column = StringColumn.create("Algorithm");
        StringColumn category = StringColumn.create("Class");
        for (DoubleSolution solution_ : front_preferences) {
            String owner = ((String) solution_.getAttribute(OWNER));
            if (owner.contains(":")) {
                owner = "NSGA3-DP" + owner.split(":")[0];
            }
            column.append(owner);
            category.append((String) solution_.getAttribute("class"));
        }

        table.addColumns(column, category);
        if(!new File(DIRECTORY + File.separator+ "FRONT_PREFERENCES").exists()){
            new File(DIRECTORY + File.separator+ "FRONT_PREFERENCES").mkdirs();
        }
        table.write().csv(DIRECTORY + File.separator+ "FRONT_PREFERENCES"+File.separator  + label + ".csv");
    }

    private static HashMap<String, ArrayList<DoubleSolution>> groupByAlgorithm(ArrayList<DoubleSolution> front,
            String[] algorithmName) {
        HashMap<String, ArrayList<DoubleSolution>> map = new HashMap<>();

        for (int i = 0; i < algorithmName.length; i++) {
            map.put(algorithmName[i], new ArrayList<>());
        }
        for (DoubleSolution s : front) {
            for (int j = 0; j < algorithmName.length; j++) {
                String owner = s.getAttribute(OWNER).toString().split(":")[0];
                if (owner.equals(algorithmName[j])) {
                    map.get(algorithmName[j]).add(s);
                }
            }
        }
        return map;
    }

    private static ArrayList<DoubleSolution> makeCSat(DTLZPreferences dtlzPreferences,
            ArrayList<DoubleSolution> solutions, boolean show) {
        InterClassnC<DoubleSolution> classifier = new InterClassnC<>(dtlzPreferences);
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
        if (show)
            System.out.println(String.format("HSat : %3d, Sat : %3d, Dis : %3d, HDis : %3d", hs.size(), s.size(),
                    d.size(), hd.size()));
        return front;
    }

    private static File loadPathRoi(String name) {
        String path = null;
        switch (name) {
            case "DTLZ1_P":
                path = "/home/thinkpad/Documents/jemoa/bestCompromise/dtlz/3/bestCompromise_DTLZ1_P.out";
                break;
            case "DTLZ2_P":
                path = "/home/thinkpad/Documents/jemoa/bestCompromise/dtlz/3/bestCompromise_DTLZ2_P.out";
                break;
            case "DTLZ3_P":
                path = "/home/thinkpad/Documents/jemoa/bestCompromise/dtlz/3/bestCompromise_DTLZ3_P.out";
                break;
            case "DTLZ4_P":
                path = "/home/thinkpad/Documents/jemoa/bestCompromise/dtlz/3/bestCompromise_DTLZ4_P.out";
                break;
            case "DTLZ5_P":
                path = "/home/thinkpad/Documents/jemoa/bestCompromise/dtlz/3/bestCompromise_DTLZ5_P.out";
                break;
            case "DTLZ6_P":
                path = "/home/thinkpad/Documents/jemoa/bestCompromise/dtlz/3/bestCompromise_DTLZ6_P.out";

                break;
            case "DTLZ7_P":
                path = "/home/thinkpad/Documents/jemoa/bestCompromise/dtlz/3/bestCompromise_DTLZ7_P.out";
                break;

        }
        return new File(path);
    }

    private static DTLZPreferences loadProblem(String name) throws FileNotFoundException {
        DTLZPreferences dtlzPreferences = null;
        DTLZ_Instance instance = null;
        String path = null;
        switch (name) {
            case "DTLZ1_P":
                path = "src/main/resources/DTLZ_INSTANCES/DTLZ1_Instance.txt";
                instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();
                dtlzPreferences = new DTLZ1_P(instance);
                break;
            case "DTLZ2_P":
                path = "src/main/resources/DTLZ_INSTANCES/DTLZ2_Instance.txt";
                instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();
                dtlzPreferences = new DTLZ2_P(instance);
                break;
            case "DTLZ3_P":
                path = "src/main/resources/DTLZ_INSTANCES/DTLZ3_Instance.txt";
                instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();
                dtlzPreferences = new DTLZ3_P(instance);
                break;
            case "DTLZ4_P":
                path = "src/main/resources/DTLZ_INSTANCES/DTLZ4_Instance.txt";
                instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();
                dtlzPreferences = new DTLZ4_P(instance);
                break;
            case "DTLZ5_P":
                path = "src/main/resources/DTLZ_INSTANCES/DTLZ5_Instance.txt";
                instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();
                dtlzPreferences = new DTLZ5_P(instance);
                break;
            case "DTLZ6_P":
                path = "src/main/resources/DTLZ_INSTANCES/DTLZ6_Instance.txt";
                instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();
                dtlzPreferences = new DTLZ6_P(instance);
                break;
            case "DTLZ7_P":
                path = "src/main/resources/DTLZ_INSTANCES/DTLZ7_Instance.txt";
                instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();
                dtlzPreferences = new DTLZ7_P(instance);
                break;

        }
        return dtlzPreferences;
    }

    @SuppressWarnings("rawtypes")
    private static ArrayList<DoubleSolution> loadSolutions(DTLZPreferences problem, File file)
            throws FileNotFoundException {
        ArrayList<DoubleSolution> solutions = new ArrayList<>();
        Scanner sc = new Scanner(file);
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
