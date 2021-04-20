package com.castellanos94.experimentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.preferences.impl.InterClassnC;
import com.castellanos94.problems.DTLZP;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.BordaRanking;
import com.castellanos94.utils.Distance;
import com.castellanos94.utils.POST_HOC;
import com.castellanos94.utils.StacClient;
import com.castellanos94.utils.Distance.Metric;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.NumericColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

/**
 * Aplica Metricas para los resultados de ejecucion de
 * {@link NSGA3DPExperimentation}, la prueba estadisitca empleada es Friedman
 * Aligned rank. Actual usando ROI Generator and NSGA3WPExperimentation.
 * 
 * Invoca para la generacion de reportes del movimiento de frentes
 * {@link ReportFront}
 */
public class NSGA3WPExperimentationMetrics {
    private final static int numberOfObjectives = 10;
    private static String algorithmName = numberOfObjectives + File.separator + "NSGA3";
    // private static String algorithmName = File.separator + "NSGA3_last";
    private static final String OWNER = "FROM_PROBLEM";
    private static String DIRECTORY = "experiments" + File.separator + algorithmName + File.separator;
    private static Table stats = Table.create("statistic");
    private static StringColumn nameColumn = StringColumn.create("Problem");
    private static StringColumn metricNameColumn = StringColumn.create("Metric Name");
    private static StringColumn resultColumn = StringColumn.create("Friedman Aligned Ranks 0.05");
    private static StringColumn rankingColumn = StringColumn.create("Ranking Finner");
    private static StringColumn techicalColumn = StringColumn.create("Technical");
    private static StringColumn meanColumn = StringColumn.create("Mean");
    private static StringColumn problemColumn = StringColumn.create("Problema");
    private static StringColumn confColumn = StringColumn.create("Configuracion");
    private static StringColumn noDominateColumn = StringColumn.create("NoDominated Size");

    private static StringColumn domColumn = StringColumn.create("Dominancia");
    private static StringColumn chsatColumn = StringColumn.create("CHSat");
    private static StringColumn csatColumn = StringColumn.create("CSat");
    private static StringColumn cdisColumn = StringColumn.create("CDis");
    private static StringColumn dMinColumn = StringColumn.create("Min");
    private static StringColumn dAvgColumn = StringColumn.create("Avg");
    private static StringColumn dMaxColumn = StringColumn.create("Max");
    private static HashMap<String, ArrayList<HashMap<String, Double>>> rankListMetric = new HashMap<>();

    public static void main(String[] args) throws IOException {
        HashMap<String, ArrayList<DoubleSolution>> roi = new HashMap<>();
        HashMap<String, DTLZP> problems = new HashMap<>();
        HashMap<DTLZP, HashMap<String, ArrayList<ArrayList<DoubleSolution>>>> globalSolutionByProblem = new HashMap<>();
        System.out.println("Working directory: " + DIRECTORY);
        for (File f : new File(DIRECTORY).listFiles()) {
            if (f.isDirectory()) {
                HashMap<DTLZP, ArrayList<ArrayList<DoubleSolution>>> algorithmProblems = new HashMap<>();
                for (File _file : f.listFiles()) {
                    if (_file.isDirectory()) {
                        DTLZP currentProblem;
                        // if (_file.getName().equalsIgnoreCase("dtlz4")) {
                        if (problems.containsKey(_file.getName())) {
                            currentProblem = problems.get(_file.getName());
                        } else {
                            currentProblem = loadProblem(_file.getName());
                            problems.put(_file.getName(), currentProblem);
                            System.out.println(_file + " " + loadPathRoi(currentProblem));
                            roi.put(currentProblem.getName(),
                                    loadSolutions(currentProblem, loadPathRoi(currentProblem), true));

                            for (DoubleSolution s : roi.get(currentProblem.getName())) {
                                s.setAttribute(OWNER, "ROI");
                            }
                            globalSolutionByProblem.put(currentProblem, new HashMap<>());
                        }
                        ArrayList<ArrayList<DoubleSolution>> solutionFromProblem = new ArrayList<>();
                        for (File executions : _file.listFiles()) {
                            if (executions.getName().contains("execution") && executions.getName().endsWith(".out")) {
                                solutionFromProblem.add(loadSolutions(currentProblem, executions, false));
                            }
                        }
                        algorithmProblems.put(currentProblem, solutionFromProblem);
                        // }
                    }
                }
                Iterator<DTLZP> _Iterator = algorithmProblems.keySet().iterator();
                while (_Iterator.hasNext()) {
                    DTLZP p = _Iterator.next();
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

        HashMap<DTLZP, ArrayList<ArrayList<DoubleSolution>>> globalSolutionNDByProblem = new HashMap<>();
        HashMap<DTLZP, ArrayList<ArrayList<DoubleSolution>>> globalSolution = new HashMap<>();
        HashMap<DTLZP, ArrayList<ArrayList<DoubleSolution>>> globalCSat = new HashMap<>();

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
                // _csat.add(classifySolutions(_problem, noDomiList.get(i), true, true));
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
        ArrayList<DoubleColumn> disColumns = new ArrayList<>();
        ArrayList<DoubleColumn> zColumns = new ArrayList<>();

        ArrayList<DoubleColumn> euclideanMin = new ArrayList<>();
        ArrayList<DoubleColumn> euclideanAVG = new ArrayList<>();
        ArrayList<DoubleColumn> euclideanMax = new ArrayList<>();

        ArrayList<DoubleColumn> chebyshevMin = new ArrayList<>();
        ArrayList<DoubleColumn> chebyshevAVG = new ArrayList<>();
        ArrayList<DoubleColumn> chebyshevMax = new ArrayList<>();

        for (int i = 0; i < _names_algorithm.length; i++) {
            DoubleColumn _f0 = DoubleColumn.create(_names_algorithm[i] + "-F-0");
            DoubleColumn _hsat = DoubleColumn.create(_names_algorithm[i] + "-HSat");
            DoubleColumn _sat = DoubleColumn.create(_names_algorithm[i] + "-Sat");
            DoubleColumn _dis = DoubleColumn.create(_names_algorithm[i] + "-Dis");

            DoubleColumn _eMin = DoubleColumn.create(_names_algorithm[i] + "-Eulidean-Min");
            DoubleColumn _eAVG = DoubleColumn.create(_names_algorithm[i] + "-Eulidean-AVG");
            DoubleColumn _eMax = DoubleColumn.create(_names_algorithm[i] + "-Eulidean-Max");

            DoubleColumn _cMin = DoubleColumn.create(_names_algorithm[i] + "-Chebyshev-Min");
            DoubleColumn _cAVG = DoubleColumn.create(_names_algorithm[i] + "-Chebyshev-AVG");
            DoubleColumn _cMax = DoubleColumn.create(_names_algorithm[i] + "-Chebyshev-Max");
            euclideanMin.add(_eMin);
            euclideanAVG.add(_eAVG);
            euclideanMax.add(_eMax);
            chebyshevMin.add(_cMin);
            chebyshevAVG.add(_cAVG);
            chebyshevMax.add(_cMax);

            zColumns.add(_f0);
            hsColumns.add(_hsat);
            sColumns.add(_sat);
            disColumns.add(_dis);
        }
        Iterator<String> iterator = problems.keySet().iterator();
        while (iterator.hasNext()) {
            DTLZP dtlz = problems.get(iterator.next());
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
                        if (doubleColumn.name().equals(__name + "-F-0")) {
                            doubleColumn.append(-_solutions.size());
                            break;
                        }
                    }
                });
            }
            for (int index = 0; index < globalCSat.get(dtlz).size(); index++) {
                ArrayList<DoubleSolution> solutions = globalCSat.get(dtlz).get(index);
                HashMap<String, ArrayList<DoubleSolution>> grouped = groupByAlgorithm(solutions, _names_algorithm,
                        false);
                int sizeOfFrontZero = globalSolutionNDByProblem.get(dtlz).get(index).size();
                grouped.forEach((__name, _solutions) -> {
                    double hsat = 0, sat = 0;
                    // CSat Sat
                    for (DoubleSolution s : _solutions) {
                        if (s.getAttribute("class").toString().equalsIgnoreCase("hsat")) {
                            hsat += 1;
                        } else if (s.getAttribute("class").toString().equalsIgnoreCase("sat")) {
                            sat += 1;
                        }
                    }

                    for (DoubleColumn doubleColumn : hsColumns) {
                        if (doubleColumn.name().equals(__name + "-HSat")) {
                            doubleColumn.append(-hsat / sizeOfFrontZero);
                            break;
                        }
                    }
                    for (DoubleColumn doubleColumn : sColumns) {
                        if (doubleColumn.name().equals(__name + "-Sat")) {
                            doubleColumn.append(-sat / sizeOfFrontZero);
                            break;
                        }
                    }
                    for (DoubleColumn doubleColumn : disColumns) {
                        if (doubleColumn.name().equals(__name + "-Dis")) {
                            doubleColumn.append(-(sizeOfFrontZero - (hsat + sat)) / sizeOfFrontZero);
                            break;
                        }
                    }

                });
                if (solutions.isEmpty()) {
                    solutions = globalSolutionNDByProblem.get(dtlz).get(index);
                }
                grouped = groupByAlgorithm(solutions, _names_algorithm, false);
                grouped.forEach((__name, _solutions) -> { // Make Distance
                    double[] euclidean = calculateDistances(_solutions, roi.get(dtlz.getName()),
                            Metric.EUCLIDEAN_DISTANCE);
                    for (DoubleColumn column : euclideanMin) {
                        if (column.name().equals(__name + "-Eulidean-Min")) {
                            column.append(euclidean[0]);
                            break;
                        }
                    }
                    for (DoubleColumn column : euclideanAVG) {
                        if (column.name().equals(__name + "-Eulidean-AVG")) {
                            column.append(euclidean[1]);
                            break;
                        }
                    }
                    for (DoubleColumn column : euclideanMax) {
                        if (column.name().equals(__name + "-Eulidean-Max")) {
                            column.append(euclidean[2]);
                            break;
                        }
                    }
                    // Chebyshev
                    double[] chebyshev = calculateDistances(_solutions, roi.get(dtlz.getName()),
                            Metric.CHEBYSHEV_DISTANCE);

                    for (DoubleColumn column : chebyshevMin) {
                        if (column.name().equals(__name + "-Chebyshev-Min")) {
                            column.append(chebyshev[0]);
                            break;
                        }
                    }
                    for (DoubleColumn column : chebyshevAVG) {
                        if (column.name().equals(__name + "-Chebyshev-AVG")) {
                            column.append(chebyshev[1]);
                            break;
                        }
                    }
                    for (DoubleColumn column : chebyshevMax) {
                        if (column.name().equals(__name + "-Chebyshev-Max")) {
                            column.append(chebyshev[2]);
                            break;
                        }
                    }

                });

            }

            HashMap<String, String> mapTest = doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, zColumns,
                    "Dominance");
            String[] orderNameConfiguration = mapTest.keySet().toArray(new String[mapTest.size()]);
            Arrays.sort(orderNameConfiguration);

            int countNoDominated = 0;
            for (ArrayList<DoubleSolution> solutions : globalSolutionNDByProblem.get(dtlz)) {
                countNoDominated += solutions.size();
            }
            countNoDominated /= globalSolutionNDByProblem.get(dtlz).size();
            for (String key : orderNameConfiguration) {
                problemColumn.append(dtlz.getName());
                confColumn.append(key);
                domColumn.append(mapTest.get(key));
                noDominateColumn.append("" + countNoDominated);
            }
            mapTest = doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, hsColumns, "HSat");
            for (String key : orderNameConfiguration) {
                chsatColumn.append(mapTest.get(key));
            }
            mapTest = doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, sColumns, "Sat");
            for (String key : orderNameConfiguration) {
                csatColumn.append(mapTest.get(key));
            }
            mapTest = doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, disColumns, "Dis");
            for (String key : orderNameConfiguration) {
                cdisColumn.append(mapTest.get(key));
            }
            mapTest = doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, euclideanMin, "Euclidean Min");
            for (String key : orderNameConfiguration) {
                dMinColumn.append(mapTest.get(key));
            }
            mapTest = doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, euclideanAVG, "Euclidean AVG");
            for (String key : orderNameConfiguration) {
                dAvgColumn.append(mapTest.get(key));
            }
            mapTest = doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, euclideanMax, "Euclidean Max");
            for (String key : orderNameConfiguration) {
                dMaxColumn.append(mapTest.get(key));
            }
            mapTest = doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, chebyshevMin, "Chebyshev Min");
            mapTest = doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, chebyshevAVG, "Chebyshev AVG");
            mapTest = doStatisticTest(dtlz.getName(), startProblem, endProblem, _name, chebyshevMax, "Chebyshev Max");
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
        // Dis column
        for (DoubleColumn doubleColumn : disColumns) {
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
        stats.addColumns(nameColumn, metricNameColumn, resultColumn, rankingColumn, meanColumn, techicalColumn);
        stats.write().csv(DIRECTORY + "stac.csv");
        Table reportLatex = Table.create("latex");

        reportLatex.addColumns(problemColumn, confColumn, noDominateColumn, domColumn, chsatColumn, csatColumn,
                cdisColumn, dMinColumn, dAvgColumn, dMaxColumn);
        reportLatex.write().csv(DIRECTORY + "latex_report.csv");

        System.out.println("Generating sum all metrics All...");
        HashMap<String, Double> makeSumRank = BordaRanking.makeSumRank(rankListMetric);
        System.out.println("\tName\tRank");
        makeSumRank.forEach((k, v) -> {
            System.out.println("\t" + k + "\t" + v);
        });
        HashMap<String, ArrayList<HashMap<String, Double>>> euclideanList = new HashMap<>();
        rankListMetric.forEach((k, v) -> {
            if (k.contains("Euclidean")) {
                euclideanList.put(k, v);
            }
        });
        System.out.println("Generating sum all metrics Euclidean...");
        makeSumRank = BordaRanking.makeSumRank(euclideanList);
        System.out.println("\tName\tRank");
        makeSumRank.forEach((k, v) -> {
            System.out.println("\t" + k + "\t" + v);
        });

        HashMap<String, ArrayList<HashMap<String, Double>>> chebyshevList = new HashMap<>();
        rankListMetric.forEach((k, v) -> {
            if (k.contains("Chebyshev")) {
                chebyshevList.put(k, v);
            }
        });
        System.out.println("Generating sum all metrics Chebyshev...");
        makeSumRank = BordaRanking.makeSumRank(chebyshevList);
        System.out.println("\tName\tRank");
        makeSumRank.forEach((k, v) -> {
            System.out.println("\t" + k + "\t" + v);
        });

        HashMap<String, ArrayList<HashMap<String, Double>>> satList = new HashMap<>();
        rankListMetric.forEach((k, v) -> {
            if (k.contains("Sat")) {
                satList.put(k, v);
            }
        });
        System.out.println("Generating sum all metrics SAT...");
        makeSumRank = BordaRanking.makeSumRank(satList);
        System.out.println("\tName\tRank");
        makeSumRank.forEach((k, v) -> {
            System.out.println("\t" + k + "\t" + v);
        });
        System.out.println("Generating Report");
        ReportFront.generateReportFront(algorithmName, DIRECTORY);
    }

    @SuppressWarnings("unchecked")
    private static HashMap<String, String> doStatisticTest(String nameProblem, int startRow, int rowEnd,
            StringColumn problemColumn, ArrayList<DoubleColumn> targetColumn, String metricName) throws IOException {
        Table tmpTable = Table.create("data");
        // tmpTable.addColumns(problemColumn);

        for (DoubleColumn column : targetColumn) {
            tmpTable.addColumns(column);
        }
        String regex = "-F-0|-HSat|-Sat|-Dis|-Eulidean-Min|-Eulidean-AVG|-Eulidean-Max|-Chebyshev-Min|-Chebyshev-AVG|-Chebyshev-Max";

        tmpTable = tmpTable.inRange(startRow, rowEnd).copy();
        for (Column<?> column : tmpTable.columns()) {
            String newName = column.name().toString().replaceAll(regex, "");
            column.setName(newName);
        }
        HashMap<String, Double> summaryMean = new HashMap<>();
        HashMap<String, Double> summarySTD = new HashMap<>();
        double acum = 0, acumSTD = 0;
        double max = Double.MIN_VALUE;
        for (Column<?> c : tmpTable.columns()) {
            NumericColumn column = (NumericColumn) c;
            double _max = column.max();
            if (_max > max) {
                max = _max;
            }
        }
        max *= 10;
        for (Column<?> c : tmpTable.columns()) {
            NumericColumn column = (NumericColumn) c;
            if (column.countMissing() > tmpTable.rowCount() / 3.0)
                column.setMissingTo(max);

            double mean = column.mean();
            acum += mean;
            double std = column.standardDeviation();
            acumSTD += std;
            summaryMean.put(c.name(), mean);
            summarySTD.put(c.name(), std);
        }
        Iterator<String> iterator = summaryMean.keySet().iterator();
        if (metricName.equalsIgnoreCase("Dominance") || metricName.equalsIgnoreCase("hsat")
                || metricName.equalsIgnoreCase("sat") || metricName.equalsIgnoreCase("dis")) {
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (acum != 0 && metricName.equalsIgnoreCase("Dominance"))
                    summaryMean.put(key, Math.abs(summaryMean.get(key) / acum) * 100.0);
                else
                    summaryMean.put(key, Math.abs(summaryMean.get(key)) * 100.0);
                if (acumSTD != 0 && metricName.equalsIgnoreCase("Dominance"))
                    summarySTD.put(key, Math.abs(summarySTD.get(key) / acumSTD));
                else
                    summarySTD.put(key, Math.abs(summarySTD.get(key)));
            }
        }

        File file = File.createTempFile("data", ".csv");
        file.deleteOnExit();
        tmpTable.write().csv(file);
        System.out.println(nameProblem + "/" + metricName + "-> " + file.getAbsolutePath());
        Map<String, Object> friedman = (tmpTable.columnCount() < 5)
                ? StacClient.FRIEDMAN_ALIGNED_RANK(file.getAbsolutePath(), 0.05, POST_HOC.FINNER)
                : StacClient.FRIEDMAN(file.getAbsolutePath(), 0.05, POST_HOC.FINNER);

        Map<String, Object> st = (Map<String, Object>) friedman.get("ranking");
        boolean rs;
        nameColumn.append(nameProblem);
        metricNameColumn.append(metricName);
        if (st != null)
            rs = st.get("result").toString().contains("true");
        else
            rs = false;
        if (st != null)
            resultColumn.append(
                    ((rs) ? "H0 is rejected" : "H0 is accepted") + ", statistic : " + st.get("statistic").toString());
        else
            resultColumn.append("NaN");
        String data = "";
        String ranking_ = "";
        HashMap<String, Double> rankingBorderMap;
        if (nameProblem.toLowerCase().contains("family")) {
            rankingBorderMap = BordaRanking.doGlobalRanking(rankListMetric.get(metricName));
        } else {
            rankingBorderMap = BordaRanking.doRankingBorda(friedman);

            if (!rankListMetric.containsKey(metricName)) {
                rankListMetric.put(metricName, new ArrayList<>());
            }
            rankListMetric.get(metricName).add(rankingBorderMap);
        }

        iterator = summaryMean.keySet().iterator();
        String summary = "";
        HashMap<String, String> sumMap = new HashMap<>();
        if (st != null) {
            ArrayList<Object> names_ = (ArrayList<Object>) st.get("names");
            ArrayList<Object> values_ = (ArrayList<Object>) st.get("rankings");
            ArrayList<Double> valuesD_ = new ArrayList<>();
            for (Object object : values_) {
                valuesD_.add(Double.parseDouble(object.toString()));
            }

            System.out.println(nameProblem + "/" + nameProblem + " > " + names_ + " <-> " + valuesD_);
            for (int i = 0; i < names_.size(); i++) {
                String name__ = names_.get(i).toString().replaceAll(regex, "");
                if (i < names_.size() - 1)
                    ranking_ += String.format("%s, ", name__);
                else
                    ranking_ += String.format("%s", name__);
                data += String.format("%s , %s; ", values_.get(i).toString(), name__);
                double min_rank = valuesD_.stream().min(Double::compare).get();// =
                                                                               // rankingBorderMap.values().stream().min(Double::compare).get();

                summary += String.format(" $%f_{%f}^{%.1f}$ %s,", summaryMean.get(name__), summarySTD.get(name__),
                        rankingBorderMap.get(name__), name__);
                if (metricName.equalsIgnoreCase("Dominance") || metricName.equalsIgnoreCase("hsat")
                        || metricName.equalsIgnoreCase("sat")) {
                    sumMap.put(name__,
                            String.format("%s$%5.3f_{%.3f}^{%.1f}$",
                                    (rs && min_rank == valuesD_.get(i)) ? "\\cellcolor[HTML]{FFFF00}" : "",
                                    summaryMean.get(name__), summarySTD.get(name__), valuesD_.get(i)));
                    // summaryMean.get(name__), summarySTD.get(name__),
                    // rankingBorderMap.get(name__)));
                } else {
                    sumMap.put(name__,
                            String.format("%s$%f_{%.3f}^{%.1f}$",
                                    (rs && min_rank == valuesD_.get(i)) ? "\\cellcolor[HTML]{FFFF00}" : "",
                                    summaryMean.get(name__), summarySTD.get(name__), valuesD_.get(i)));
                    // summaryMean.get(name__), summarySTD.get(name__),
                    // rankingBorderMap.get(name__)));

                }

            }

        }
        meanColumn.append(summary.trim() + " " + acum);

        if (st != null) {
            techicalColumn.append(data.trim());
            rankingColumn.append(ranking_.trim());
        } else {
            techicalColumn.append("Error with data or server error");
            rankingColumn.append("Error with data or server");
        }
        return sumMap;

    }

    private static void globalMetric(HashMap<DTLZP, ArrayList<ArrayList<DoubleSolution>>> globalSolutionNDByProblem,
            HashMap<String, ArrayList<DoubleSolution>> roi, String[] _names_algorithm) throws IOException {
        StringColumn _nameG = StringColumn.create("problem");
        DoubleColumn allG = DoubleColumn.create("solutions");
        DoubleColumn frontZeroG = DoubleColumn.create("F-0");
        ArrayList<DoubleColumn> hsColumnsG = new ArrayList<>();
        ArrayList<DoubleColumn> sColumnsG = new ArrayList<>();
        ArrayList<DoubleColumn> disColumnsG = new ArrayList<>();
        ArrayList<DoubleColumn> zColumnsG = new ArrayList<>();

        ArrayList<DoubleColumn> euclideanMin = new ArrayList<>();
        ArrayList<DoubleColumn> euclideanAVG = new ArrayList<>();
        ArrayList<DoubleColumn> euclideanMax = new ArrayList<>();

        ArrayList<DoubleColumn> chebyshevMin = new ArrayList<>();
        ArrayList<DoubleColumn> chebyshevAVG = new ArrayList<>();
        ArrayList<DoubleColumn> chebyshevMax = new ArrayList<>();
        for (int i = 0; i < _names_algorithm.length; i++) {
            DoubleColumn _f0 = DoubleColumn.create(_names_algorithm[i] + "-F-0");
            DoubleColumn _hsat = DoubleColumn.create(_names_algorithm[i] + "-HSat");
            DoubleColumn _sat = DoubleColumn.create(_names_algorithm[i] + "-Sat");
            DoubleColumn _dis = DoubleColumn.create(_names_algorithm[i] + "-Dis");
            DoubleColumn _eMin = DoubleColumn.create(_names_algorithm[i] + "-Eulidean-Min");
            DoubleColumn _eAVG = DoubleColumn.create(_names_algorithm[i] + "-Eulidean-AVG");
            DoubleColumn _eMax = DoubleColumn.create(_names_algorithm[i] + "-Eulidean-Max");

            DoubleColumn _cMin = DoubleColumn.create(_names_algorithm[i] + "-Chebyshev-Min");
            DoubleColumn _cAVG = DoubleColumn.create(_names_algorithm[i] + "-Chebyshev-AVG");
            DoubleColumn _cMax = DoubleColumn.create(_names_algorithm[i] + "-Chebyshev-Max");
            euclideanMin.add(_eMin);
            euclideanAVG.add(_eAVG);
            euclideanMax.add(_eMax);
            chebyshevMin.add(_cMin);
            chebyshevAVG.add(_cAVG);
            chebyshevMax.add(_cMax);
            zColumnsG.add(_f0);
            hsColumnsG.add(_hsat);
            sColumnsG.add(_sat);
            disColumnsG.add(_dis);
        }
        // Global
        HashMap<String, Double> nodomMap = new HashMap<>();

        globalSolutionNDByProblem.forEach((_p, bags) -> {
            ArrayList<DoubleSolution> bag = new ArrayList<>();
            bags.forEach(b -> bag.addAll(b));
            DominanceComparator<DoubleSolution> comparator = new DominanceComparator<>();
            comparator.computeRanking(bag);
            ArrayList<DoubleSolution> frontZero = comparator.getSubFront(0);
            System.out.println(
                    String.format("Problem : %s, bag : %6d, F0 : %6d", _p.getName(), bags.size(), frontZero.size()));
            nodomMap.put(_p.getName(), (double) frontZero.size());

            _nameG.append(_p.getName());
            allG.append(bag.size());
            frontZeroG.append(frontZero.size());
            HashMap<String, ArrayList<DoubleSolution>> groupByAlgorithm = groupByAlgorithm(frontZero, _names_algorithm,
                    false);
            groupByAlgorithm.forEach((__name, _solutions) -> {
                // Dominance
                for (DoubleColumn doubleColumn : zColumnsG) {
                    if (doubleColumn.name().equals(__name + "-F-0")) {
                        doubleColumn.append(-_solutions.size());
                        break;
                    }
                }

            });

            ArrayList<DoubleSolution> csatSolutions = classifySolutions(_p, frontZero, true, false);
            HashMap<String, ArrayList<DoubleSolution>> grouped = groupByAlgorithm(csatSolutions, _names_algorithm,
                    false);
            grouped.forEach((__name, _solutions) -> {
                double hsat = 0, sat = 0;
                for (DoubleSolution s : _solutions) {
                    if (s.getAttribute("class").toString().equalsIgnoreCase("hsat")) {
                        hsat += 1;
                    } else if (s.getAttribute("class").toString().equalsIgnoreCase("sat")) {
                        sat += 1;
                    }
                }

                for (DoubleColumn doubleColumn : hsColumnsG) {
                    if (doubleColumn.name().equals(__name + "-HSat")) {
                        doubleColumn.append(-hsat / frontZero.size());
                        break;
                    }
                }
                for (DoubleColumn doubleColumn : sColumnsG) {
                    if (doubleColumn.name().equals(__name + "-Sat")) {
                        doubleColumn.append(-sat / frontZero.size());
                        break;
                    }
                }
                for (DoubleColumn doubleColumn : disColumnsG) {
                    if (doubleColumn.name().equals(__name + "-Dis")) {
                        doubleColumn.append(-(frontZero.size() - (hsat + sat)) / frontZero.size());
                        break;
                    }
                }
                // Make Distance
                double[] euclidean = calculateDistances(_solutions, roi.get(_p.getName()), Metric.EUCLIDEAN_DISTANCE);
                for (DoubleColumn column : euclideanMin) {
                    if (column.name().equals(__name + "-Eulidean-Min")) {
                        column.append(euclidean[0]);
                        break;
                    }
                }
                for (DoubleColumn column : euclideanAVG) {
                    if (column.name().equals(__name + "-Eulidean-AVG")) {
                        column.append(euclidean[1]);
                        break;
                    }
                }
                for (DoubleColumn column : euclideanMax) {
                    if (column.name().equals(__name + "-Eulidean-Max")) {
                        column.append(euclidean[2]);
                        break;
                    }
                }
                // Chebyshev
                double[] chebyshev = calculateDistances(_solutions, roi.get(_p.getName()), Metric.CHEBYSHEV_DISTANCE);

                for (DoubleColumn column : chebyshevMin) {
                    if (column.name().equals(__name + "-Chebyshev-Min")) {
                        column.append(chebyshev[0]);
                        break;
                    }
                }
                for (DoubleColumn column : chebyshevAVG) {
                    if (column.name().equals(__name + "-Chebyshev-AVG")) {
                        column.append(chebyshev[1]);
                        break;
                    }
                }
                for (DoubleColumn column : chebyshevMax) {
                    if (column.name().equals(__name + "-Chebyshev-Max")) {
                        column.append(chebyshev[2]);
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
            /*
             * System.out.println("\tCheck domin with roi, F0 " + frontZero.size());
             * frontZero.addAll(roi.get(_p.getName()));
             * System.out.println("\tAfter add roi " + frontZero.size()); comparator = new
             * DominanceComparator<>(); comparator.computeRanking(frontZero);
             * ArrayList<DoubleSolution> fzero = comparator.getSubFront(0);
             * System.out.println("\tF0 : " + fzero.size());
             */
            System.out.println("\tCsat distribution: " + csatSolutions.size());
            HashMap<String, ArrayList<DoubleSolution>> groupByAlgorithm2 = groupByAlgorithm(csatSolutions,
                    _names_algorithm, true);
            groupByAlgorithm2.forEach((k, v) -> {
                ;
                System.out.println("\t\t" + k + " -> "
                        + v.stream().filter(s -> ((String) s.getAttribute("class")).contains("SAT")).count());
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
        for (DoubleColumn doubleColumn : disColumnsG) {
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

        HashMap<String, String> mapTest = doStatisticTest("DTLZ Family", 0, global.rowCount(), _nameG, zColumnsG,
                "Dominance");
        String[] orderNameConfiguration = mapTest.keySet().toArray(new String[mapTest.size()]);
        Arrays.sort(orderNameConfiguration);
        double sum = nodomMap.values().stream().mapToDouble(f->f.doubleValue()).average().getAsDouble();
        for (String key : orderNameConfiguration) {
            problemColumn.append("DTLZ");
            confColumn.append(key);
            noDominateColumn.append(""+sum);

            domColumn.append(mapTest.get(key));
        }
        mapTest = doStatisticTest("DTLZ Family", 0, global.rowCount(), _nameG, hsColumnsG, "HSat");
        for (String key : orderNameConfiguration) {
            chsatColumn.append(mapTest.get(key));
        }
        mapTest = doStatisticTest("DTLZ Family", 0, global.rowCount(), _nameG, sColumnsG, "Sat");
        for (String key : orderNameConfiguration) {
            csatColumn.append(mapTest.get(key));
        }
        mapTest = doStatisticTest("DTLZ Family", 0, global.rowCount(), _nameG, disColumnsG, "Dis");
        for (String key : orderNameConfiguration) {
            cdisColumn.append(mapTest.get(key));
        }
        mapTest = doStatisticTest("DTLZ Family", 0, global.rowCount(), _nameG, euclideanMin, "Euclidean Min");
        for (String key : orderNameConfiguration) {
            dMinColumn.append(mapTest.get(key));
        }
        mapTest = doStatisticTest("DTLZ Family", 0, global.rowCount(), _nameG, euclideanAVG, "Euclidean AVG");
        for (String key : orderNameConfiguration) {
            dAvgColumn.append(mapTest.get(key));
        }
        mapTest = doStatisticTest("DTLZ Family", 0, global.rowCount(), _nameG, euclideanMax, "Euclidean Max");
        for (String key : orderNameConfiguration) {
            dMaxColumn.append(mapTest.get(key));
        }

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
        distances[1] /= (1.0) * distances_.size();

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
                    break;
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

    private static ArrayList<DoubleSolution> classifySolutions(DTLZP dtlzPreferences,
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
            // System.out.print(x+" "+Arrays.toString(iclass)+" >" +x.getAttributes());
            // System.out.println();
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
        if (show) {
            System.out.println(String.format("\tHSat : %3d, Sat : %3d, Dis : %3d, HDis : %3d", hs.size(), s.size(),
                    d.size(), hd.size()));
            System.out.println(String.format("\tFront %5d <> %5d Sum Csat", front.size(), (hs.size() + s.size())));
        }
        return front;
    }

    private static File loadPathRoi(DTLZP problem) {

        String name = String.format("ROI_P_%s_V%d_O%d.txt", problem.getName().trim().replace("_P", ""),
                problem.getNumberOfDecisionVars(), problem.getNumberOfObjectives());

        return new File("roi_generator" + File.separator + problem.getNumberOfObjectives() + File.separator + name);
    }

    private static DTLZP loadProblem(String name) throws FileNotFoundException {
        DTLZ_Instance instance = null;
        String path = null;
        int numberOfProblem = -1;
        System.out.println(name);
        switch (name) {
        case "DTLZ1":
            numberOfProblem = 1;
            break;
        case "DTLZ2":
            numberOfProblem = 2;
            break;
        case "DTLZ3":
            numberOfProblem = 3;
            break;
        case "DTLZ4":
            numberOfProblem = 4;
            break;
        case "DTLZ5":
            numberOfProblem = 5;
            break;
        case "DTLZ6":
            numberOfProblem = 6;
            break;
        case "DTLZ7":
            numberOfProblem = 7;
            break;
        case "DTLZ8":
            numberOfProblem = 8;
            break;
        case "DTLZ9":
            numberOfProblem = 9;
            break;

        }
        path = "DTLZ_INSTANCES" + File.separator + numberOfObjectives + File.separator + "DTLZ" + numberOfProblem
                + "_Instance.txt";
        instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();

        return new DTLZP(numberOfProblem, instance);
    }

    @SuppressWarnings("rawtypes")
    private static ArrayList<DoubleSolution> loadSolutions(DTLZP problem, File file, boolean isROI)
            throws FileNotFoundException {
        ArrayList<DoubleSolution> solutions = new ArrayList<>();
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            Solution tmp;
            // if (isROI && (problem.getDTLZProblem() instanceof DTLZ8 ||
            // problem.getDTLZProblem() instanceof DTLZ9)) {
            if (isROI) {
                tmp = problem.getDTLZProblem().generateFromObjective(line.split("\\*")[1]);
                tmp.setPenalties(RealData.ZERO);
                tmp.setRank(0);
            } else
                tmp = problem.generateFromVarString(line.split("\\*")[0].trim());
            tmp.setAttribute(OWNER, problem.getName());
            solutions.add((DoubleSolution) tmp.copy());
        }
        sc.close();
        return solutions;
    }
}
