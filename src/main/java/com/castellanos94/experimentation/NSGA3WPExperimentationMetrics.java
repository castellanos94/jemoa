package com.castellanos94.experimentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.datatype.Data;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.preferences.impl.InterClassnC;
import com.castellanos94.problems.preferences.dtlz.*;
import com.castellanos94.problems.preferences.dtlz.DTLZPreferences;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Distance;
import com.castellanos94.utils.POST_HOC;
import com.castellanos94.utils.StacClient;
import com.castellanos94.utils.Distance.Metric;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

/**
 * Actual usando ROI Generator and NSGA3WPExperimentation.
 */
public class NSGA3WPExperimentationMetrics {
    private static String algorithmName = "NSGA3_old";
    private static final String OWNER = "FROM_PROBLEM";
    private static String DIRECTORY = "experiments" + File.separator + algorithmName + File.separator;
    private static Table stats = Table.create("statistic");
    private static StringColumn nameColumn = StringColumn.create("Problem");
    private static StringColumn metricNameColumn = StringColumn.create("Metric Name");
    private static StringColumn resultColumn = StringColumn.create("Friedman Aligned Ranks 0.05");
    private static StringColumn techicalColumn = StringColumn.create("Technical");

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
                                    loadSolutions(currentProblem, loadPathRoi(currentProblem)));

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
            roi.put(key, classifySolutions(problems.get(key), roi.get(key), true, true));
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
                _csat.add(classifySolutions(_problem, noDomiList.get(i), false, true));
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

        ArrayList<DoubleColumn> euclideanMin = new ArrayList<>();
        ArrayList<DoubleColumn> euclideanAVG = new ArrayList<>();
        ArrayList<DoubleColumn> euclideanMax = new ArrayList<>();

        ArrayList<DoubleColumn> chebyshevMin = new ArrayList<>();
        ArrayList<DoubleColumn> chebyshevAVG = new ArrayList<>();
        ArrayList<DoubleColumn> chebyshevMax = new ArrayList<>();

        for (int i = 0; i < _names_algorithm.length; i++) {
            DoubleColumn _f0 = DoubleColumn.create("A-" + _names_algorithm[i] + "-F-0");
            DoubleColumn _hsat = DoubleColumn.create("A-" + _names_algorithm[i] + "-HSat");
            DoubleColumn _sat = DoubleColumn.create("A-" + _names_algorithm[i] + "-Sat");

            DoubleColumn _eMin = DoubleColumn.create("A-" + _names_algorithm[i] + "-Eulidean-Min");
            DoubleColumn _eAVG = DoubleColumn.create("A-" + _names_algorithm[i] + "-Eulidean-AVG");
            DoubleColumn _eMax = DoubleColumn.create("A-" + _names_algorithm[i] + "-Eulidean-Max");

            DoubleColumn _cMin = DoubleColumn.create("A-" + _names_algorithm[i] + "-Chebyshev-Min");
            DoubleColumn _cAVG = DoubleColumn.create("A-" + _names_algorithm[i] + "-Chebyshev-AVG");
            DoubleColumn _cMax = DoubleColumn.create("A-" + _names_algorithm[i] + "-Chebyshev-Max");
            euclideanMin.add(_eMin);
            euclideanAVG.add(_eAVG);
            euclideanMax.add(_eMax);
            chebyshevMin.add(_cMin);
            chebyshevAVG.add(_cAVG);
            chebyshevMax.add(_cMax);

            zColumns.add(_f0);
            hsColumns.add(_hsat);
            sColumns.add(_sat);
        }
        Iterator<String> iterator = problems.keySet().iterator();
        while (iterator.hasNext()) {
            DTLZPreferences dtlz = problems.get(iterator.next());
            int startProblem = (_name.size() - 1 > 0) ? _name.size() - 1 : 0;
            int endProblem = startProblem;
            for (int j = 0; j < globalSolution.get(dtlz).size(); j++) {
                ArrayList<DoubleSolution> b = globalSolution.get(dtlz).get(j);
                all.append(b.size());
                _name.append(dtlz.getName() + "-" + j);
                endProblem++;
            }
            for (ArrayList<DoubleSolution> solutions : globalSolutionNDByProblem.get(dtlz)) {
                frontZero.append(solutions.size());
                HashMap<String, ArrayList<DoubleSolution>> grouped = groupByAlgorithm(solutions, _names_algorithm,
                        false);
                grouped.forEach((__name, _solutions) -> {
                    // Dominance
                    for (DoubleColumn doubleColumn : zColumns) {
                        if (doubleColumn.name().equals("A-" + __name + "-F-0")) {
                            doubleColumn.append(-_solutions.size());
                            break;
                        }
                    }
                    // Make Distance
                    double[] euclidean = calculateDistances(_solutions, roi.get(dtlz.getName()),
                            Metric.EUCLIDEAN_DISTANCE);
                    for (DoubleColumn column : euclideanMin) {
                        if (column.name().equals("A-" + __name + "-Eulidean-Min")) {
                            column.append(euclidean[0]);
                            break;
                        }
                    }
                    for (DoubleColumn column : euclideanAVG) {
                        if (column.name().equals("A-" + __name + "-Eulidean-AVG")) {
                            column.append(euclidean[1]);
                            break;
                        }
                    }
                    for (DoubleColumn column : euclideanMax) {
                        if (column.name().equals("A-" + __name + "-Eulidean-Max")) {
                            column.append(euclidean[2]);
                            break;
                        }
                    }
                    // Chebyshev
                    double[] chebyshev = calculateDistances(_solutions, roi.get(dtlz.getName()),
                            Metric.CHEBYSHEV_DISTANCE);

                    for (DoubleColumn column : chebyshevMin) {
                        if (column.name().equals("A-" + __name + "-Chebyshev-Min")) {
                            column.append(chebyshev[0]);
                            break;
                        }
                    }
                    for (DoubleColumn column : chebyshevAVG) {
                        if (column.name().equals("A-" + __name + "-Chebyshev-AVG")) {
                            column.append(chebyshev[1]);
                            break;
                        }
                    }
                    for (DoubleColumn column : chebyshevMax) {
                        if (column.name().equals("A-" + __name + "-Chebyshev-Max")) {
                            column.append(chebyshev[2]);
                            break;
                        }
                    }
                });
            }
            for (ArrayList<DoubleSolution> solutions : globalCSat.get(dtlz)) {
                HashMap<String, ArrayList<DoubleSolution>> grouped = groupByAlgorithm(solutions, _names_algorithm,
                        false);
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

            doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, zColumns, "Dominance");
            doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, hsColumns, "HSat");
            doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, sColumns, "Sat");
            doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, euclideanMin, "Euclidean Min");
            doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, euclideanAVG, "Euclidean AVG");
            doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, euclideanMax, "Euclidean Max");

            doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, chebyshevMin, "Chebyshev Min");
            doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, chebyshevAVG, "Chebyshev AVG");
            doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, chebyshevMax, "Chebyshev Max");
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

        // Distances
        for (DoubleColumn doubleColumn : euclideanMin) {
            table.addColumns(doubleColumn);
        }
        for (DoubleColumn doubleColumn : euclideanAVG) {
            table.addColumns(doubleColumn);
        }
        for (DoubleColumn doubleColumn : euclideanMax) {
            table.addColumns(doubleColumn);
        }
        for (DoubleColumn doubleColumn : chebyshevMin) {
            table.addColumns(doubleColumn);
        }
        for (DoubleColumn doubleColumn : chebyshevAVG) {
            table.addColumns(doubleColumn);
        }
        for (DoubleColumn doubleColumn : chebyshevMax) {
            table.addColumns(doubleColumn);
        }
        // System.out.println(table.summary());
        table.write().csv(DIRECTORY + "metrics.csv");
        // Reset
        globalMetric(globalSolutionNDByProblem, roi, _names_algorithm);
        stats.addColumns(nameColumn, metricNameColumn, resultColumn, techicalColumn);
        stats.write().csv(DIRECTORY + "stac.csv");

    }

    private static void doStatisticTest(String nameProblem, int startRow, int rowEnd, StringColumn problemColumn,
            ArrayList<DoubleColumn> targetColumn, String metricName) throws IOException {
        Table tmpTable = Table.create("data");
        tmpTable.addColumns(problemColumn);

        for (DoubleColumn column : targetColumn) {
            tmpTable.addColumns(column);
        }
        tmpTable = tmpTable.inRange(startRow, rowEnd);
        File file = File.createTempFile("data", ".csv");
        file.deleteOnExit();
        tmpTable.write().csv(file);
        System.out.println(file.getAbsolutePath());
        Map<String, Object> friedman = StacClient.FRIEDMAN_ALIGNED_RANK(file.getAbsolutePath(), 0.05, POST_HOC.FINNER);
        Map<String, Object> st = (Map<String, Object>) friedman.get("ranking");
        boolean rs;
        nameColumn.append(nameProblem);
        metricNameColumn.append(metricName);
        if (st != null)
            rs = st.get("result").toString().contains("true");
        else
            rs = false;
        if (st != null)
            resultColumn.append((rs) ? "H0 is rejected" : "H0 is accepted");
        else
            resultColumn.append("NaN");

        if (st != null)
            techicalColumn.append(friedman.toString());
        else
            techicalColumn.append("Error with data or server error");

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

        ArrayList<DoubleColumn> euclideanMin = new ArrayList<>();
        ArrayList<DoubleColumn> euclideanAVG = new ArrayList<>();
        ArrayList<DoubleColumn> euclideanMax = new ArrayList<>();

        ArrayList<DoubleColumn> chebyshevMin = new ArrayList<>();
        ArrayList<DoubleColumn> chebyshevAVG = new ArrayList<>();
        ArrayList<DoubleColumn> chebyshevMax = new ArrayList<>();
        for (int i = 0; i < _names_algorithm.length; i++) {
            DoubleColumn _f0 = DoubleColumn.create("A-" + _names_algorithm[i] + "-F-0");
            DoubleColumn _hsat = DoubleColumn.create("A-" + _names_algorithm[i] + "-HSat");
            DoubleColumn _sat = DoubleColumn.create("A-" + _names_algorithm[i] + "-Sat");
            DoubleColumn _eMin = DoubleColumn.create("A-" + _names_algorithm[i] + "-Eulidean-Min");
            DoubleColumn _eAVG = DoubleColumn.create("A-" + _names_algorithm[i] + "-Eulidean-AVG");
            DoubleColumn _eMax = DoubleColumn.create("A-" + _names_algorithm[i] + "-Eulidean-Max");

            DoubleColumn _cMin = DoubleColumn.create("A-" + _names_algorithm[i] + "-Chebyshev-Min");
            DoubleColumn _cAVG = DoubleColumn.create("A-" + _names_algorithm[i] + "-Chebyshev-AVG");
            DoubleColumn _cMax = DoubleColumn.create("A-" + _names_algorithm[i] + "-Chebyshev-Max");
            euclideanMin.add(_eMin);
            euclideanAVG.add(_eAVG);
            euclideanMax.add(_eMax);
            chebyshevMin.add(_cMin);
            chebyshevAVG.add(_cAVG);
            chebyshevMax.add(_cMax);
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
            System.out.println(
                    String.format("Problem : %s, bag : %6d, F0 : %6d", _p.getName(), bags.size(), front.size()));

            _nameG.append(_p.getName());
            allG.append(bag.size());
            frontZeroG.append(front.size());
            HashMap<String, ArrayList<DoubleSolution>> groupByAlgorithm = groupByAlgorithm(front, _names_algorithm,
                    false);
            groupByAlgorithm.forEach((__name, _solutions) -> {
                // Dominance
                for (DoubleColumn doubleColumn : zColumnsG) {
                    if (doubleColumn.name().equals("A-" + __name + "-F-0")) {
                        doubleColumn.append(-_solutions.size());
                        break;
                    }
                }
                // Make Distance
                double[] euclidean = calculateDistances(_solutions, roi.get(_p.getName()), Metric.EUCLIDEAN_DISTANCE);
                for (DoubleColumn column : euclideanMin) {
                    if (column.name().equals("A-" + __name + "-Eulidean-Min")) {
                        column.append(euclidean[0]);
                        break;
                    }
                }
                for (DoubleColumn column : euclideanAVG) {
                    if (column.name().equals("A-" + __name + "-Eulidean-AVG")) {
                        column.append(euclidean[1]);
                        break;
                    }
                }
                for (DoubleColumn column : euclideanMax) {
                    if (column.name().equals("A-" + __name + "-Eulidean-Max")) {
                        column.append(euclidean[2]);
                        break;
                    }
                }
                // Chebyshev
                double[] chebyshev = calculateDistances(_solutions, roi.get(_p.getName()), Metric.CHEBYSHEV_DISTANCE);

                for (DoubleColumn column : chebyshevMin) {
                    if (column.name().equals("A-" + __name + "-Chebyshev-Min")) {
                        column.append(chebyshev[0]);
                        break;
                    }
                }
                for (DoubleColumn column : chebyshevAVG) {
                    if (column.name().equals("A-" + __name + "-Chebyshev-AVG")) {
                        column.append(chebyshev[1]);
                        break;
                    }
                }
                for (DoubleColumn column : chebyshevMax) {
                    if (column.name().equals("A-" + __name + "-Chebyshev-Max")) {
                        column.append(chebyshev[2]);
                        break;
                    }
                }
            });

            ArrayList<DoubleSolution> csatSolutions = classifySolutions(_p, front, true, false);
            HashMap<String, ArrayList<DoubleSolution>> grouped = groupByAlgorithm(csatSolutions, _names_algorithm,
                    false);
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
                System.out.println("\tExport all solutions with class");
                EXPORT_OBJECTIVES_TO_CSV(csatSolutions, _p.getName() + "_ALL");
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("\tCheck domin with roi, F0 " + front.size());
            front.addAll(roi.get(_p.getName()));
            System.out.println("\tAfter add roi " + front.size());
            comparator = new DominanceComparator<>();
            comparator.computeRanking(front);
            ArrayList<DoubleSolution> fzero = comparator.getSubFront(0);
            System.out.println("\tF0 : " + fzero.size());

            HashMap<String, ArrayList<DoubleSolution>> groupByAlgorithm2 = groupByAlgorithm(fzero, _names_algorithm,
                    true);
            groupByAlgorithm2.forEach((k, v) -> {
                System.out.println("\t" + k + " -> " + v.size());
            });
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
        // Distances
        for (DoubleColumn doubleColumn : euclideanMin) {
            global.addColumns(doubleColumn);
        }
        for (DoubleColumn doubleColumn : euclideanAVG) {
            global.addColumns(doubleColumn);
        }
        for (DoubleColumn doubleColumn : euclideanMax) {
            global.addColumns(doubleColumn);
        }
        for (DoubleColumn doubleColumn : chebyshevMin) {
            global.addColumns(doubleColumn);
        }
        for (DoubleColumn doubleColumn : chebyshevAVG) {
            global.addColumns(doubleColumn);
        }
        for (DoubleColumn doubleColumn : chebyshevMax) {
            global.addColumns(doubleColumn);
        }
        global.write().csv(DIRECTORY + "global-metrics.csv");
        // Performance statistic Tests

        doStatisticTest("DTLZ Family", 0, global.rowCount(), _nameG, zColumnsG, "Dominance");
        doStatisticTest("DTLZ Family", 0, global.rowCount(), _nameG, hsColumnsG, "HSat");
        doStatisticTest("DTLZ Family", 0, global.rowCount(), _nameG, sColumnsG, "Sat");
        doStatisticTest("DTLZ Family", 0, global.rowCount(), _nameG, euclideanMin, "Euclidean Min");
        doStatisticTest("DTLZ Family", 0, global.rowCount(), _nameG, euclideanAVG, "Euclidean AVG");
        doStatisticTest("DTLZ Family", 0, global.rowCount(), _nameG, euclideanMax, "Euclidean Max");

        doStatisticTest("DTLZ Family", 0, global.rowCount(), _nameG, chebyshevMin, "Chebyshev Min");
        doStatisticTest("DTLZ Family", 0, global.rowCount(), _nameG, chebyshevAVG, "Chebyshev AVG");
        doStatisticTest("DTLZ Family", 0, global.rowCount(), _nameG, chebyshevMax, "Chebyshev Max");

    }

    private static double[] calculateDistances(ArrayList<DoubleSolution> solutions, ArrayList<DoubleSolution> roi_sat,
            Metric metric) {
        double[] distances = new double[3];
        for (int i = 0; i < distances.length; i++) {
            distances[i] = Double.NaN;
        }
        if (solutions.isEmpty()) {
            for (int i = 0; i < distances.length; i++) {
                distances[i] = Double.NaN;
            }
            return distances;
        }
        Distance<DoubleSolution> distance = new Distance<>(metric);
        List<Data> distances_ = distance.evaluate(solutions, roi_sat);
        distances[0] = Double.MAX_VALUE;
        distances[1] = 0;
        distances[2] = Double.MIN_VALUE;
        for (int j = 0; j < distances_.size(); j++) {
            double tmp = distances_.get(j).doubleValue();
            if (tmp < distances[0]) {
                distances[0] = tmp;
            }
            distances[1] += tmp;
            if (tmp > distances[2]) {
                distances[2] = tmp;
            }
        }

        return distances;
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
                owner = owner.split(":")[0];
            }
            column.append(owner);
            category.append((String) solution_.getAttribute("class"));
        }

        table.addColumns(column, category);
        if (!new File(DIRECTORY + File.separator + "FRONT_PREFERENCES").exists()) {
            new File(DIRECTORY + File.separator + "FRONT_PREFERENCES").mkdirs();
        }
        table.write().csv(DIRECTORY + File.separator + "FRONT_PREFERENCES" + File.separator + label + ".csv");
    }

    private static HashMap<String, ArrayList<DoubleSolution>> groupByAlgorithm(ArrayList<DoubleSolution> front,
            String[] algorithmName, boolean groupAll) {
        HashMap<String, ArrayList<DoubleSolution>> map = new HashMap<>();

        for (int i = 0; i < algorithmName.length; i++) {
            map.put(algorithmName[i], new ArrayList<>());
        }

        for (DoubleSolution s : front) {
            boolean wasAdded = false;
            String owner = s.getAttribute(OWNER).toString().split(":")[0];
            for (int j = 0; j < algorithmName.length; j++) {
                if (owner.equals(algorithmName[j])) {
                    map.get(algorithmName[j]).add(s);
                    wasAdded = true;
                }
            }
            if (!wasAdded && groupAll) {
                if (map.containsKey(owner)) {
                    map.get(owner).add(s);
                } else {
                    ArrayList<DoubleSolution> tmp = new ArrayList<>();
                    tmp.add(s);
                    map.put(owner, tmp);
                }
            }
        }
        return map;
    }

    private static ArrayList<DoubleSolution> classifySolutions(DTLZPreferences dtlzPreferences,
            ArrayList<DoubleSolution> solutions, boolean show, boolean isOnlyCSat) {
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

        if (!d.isEmpty()) {
            if (!isOnlyCSat || front.isEmpty())
                front.addAll(d);
        }
        if (!hd.isEmpty()) {
            if (!isOnlyCSat || front.isEmpty())
                front.addAll(hd);
        }
        if (show)
            System.out.println(String.format("\tHSat : %3d, Sat : %3d, Dis : %3d, HDis : %3d", hs.size(), s.size(),
                    d.size(), hd.size()));
        return front;
    }

    private static File loadPathRoi(DTLZPreferences problem) {

        String name = String.format("ROI_P_%s_V%d_O%d.txt", problem.getName().trim().replace("_P", ""),
                problem.getNumberOfDecisionVars(), problem.getNumberOfObjectives());
        /*
         * switch (name) { case "DTLZ1_P": //path =
         * "/home/thinkpad/Documents/jemoa/bestCompromise/dtlzV3/3/bestCompromise_DTLZ1_P.out";
         * path = "/home/thinkpad/Documents/jemoa/roi_generator/ROI_P_DTLZ1_V7_O3.txt";
         * break; case "DTLZ2_P": path =
         * "/home/thinkpad/Documents/jemoa/bestCompromise/dtlzV3/3/bestCompromise_DTLZ2_P.out";
         * break; case "DTLZ3_P": path =
         * "/home/thinkpad/Documents/jemoa/bestCompromise/dtlzV3/3/bestCompromise_DTLZ3_P.out";
         * break; case "DTLZ4_P": path =
         * "/home/thinkpad/Documents/jemoa/bestCompromise/dtlzV3/3/bestCompromise_DTLZ4_P.out";
         * break; case "DTLZ5_P": path =
         * "/home/thinkpad/Documents/jemoa/bestCompromise/dtlzV3/3/bestCompromise_DTLZ5_P.out";
         * break; case "DTLZ6_P": path =
         * "/home/thinkpad/Documents/jemoa/bestCompromise/dtlzV3/3/bestCompromise_DTLZ6_P.out";
         * 
         * break; case "DTLZ7_P": path =
         * "/home/thinkpad/Documents/jemoa/bestCompromise/dtlzV3/3/bestCompromise_DTLZ7_P.out";
         * break;
         * 
         * }
         */
        return new File("/home/thinkpad/Documents/jemoa/roi_generator/" + name);
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
