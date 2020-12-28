package com.castellanos94.experimentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.problems.preferences.dtlz.DTLZ1_P;
import com.castellanos94.problems.preferences.dtlz.DTLZPreferences;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;

import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

/**
 * Test global report of Csat front of every execution.
 */
public class Testing {
    private static final String OWNER = "FROM_PROBLEM";

    public static void main(String[] args) throws IOException {
        //generateReportFront();
        String path_roi = "bestCompromise/roi_generator/roi.txt";
        String instance_path = "src/main/resources/DTLZ_INSTANCES/DTLZ1_Instance.txt";
        DTLZ_Instance instance = (DTLZ_Instance) new DTLZ_Instance(instance_path).loadInstance();
        DTLZPreferences problem = new DTLZ1_P(instance);
        ArrayList<DoubleSolution> solutions = loadSolutions(problem,new File(path_roi));
        System.out.println("Load solutions "+solutions.size());
        DominanceComparator<DoubleSolution> dominanceComparator = new DominanceComparator<>();
        dominanceComparator.computeRanking(solutions);
        System.out.println("F0 "+dominanceComparator.getSubFront(0).size());
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

    private static void generateReportFront() throws IOException {
        String algorithmName = "NSGA3";
        // algorithmName = "nsga3-10";
        String DIRECTORY = "experiments" + File.separator + algorithmName + File.separator;
        // DIRECTORY = "experiments" + File.separator + "dtlz_preferences" +
        // File.separator;
        Table table = null;
        StringColumn algorithm = null;
        StringColumn problem = null;
        ColumnType[] columnTypes = { ColumnType.INTEGER, ColumnType.DOUBLE, ColumnType.DOUBLE, ColumnType.DOUBLE };
        for (File f : new File(DIRECTORY).listFiles()) {
            if (f.isDirectory()) {
                System.out.println(f.getName());
                for (File _file : f.listFiles()) {
                    if (_file.isDirectory()) {
                        File report = new File(_file.getAbsolutePath() + File.separator + "report.csv");
                        if (report.exists()) {
                            if (table != null) {
                                Table tmp = Table.read().csv(CsvReadOptions.builder(report).columnTypes(columnTypes));
                                for (int i = 0; i < tmp.rowCount(); i++) {
                                    table.addRow(tmp.row(i));
                                    algorithm.append(algorithmName + "-" + f.getName());
                                    problem.append(_file.getName());
                                }
                            } else {
                                table = Table.read().csv(CsvReadOptions.builder(report).columnTypes(columnTypes));
                                algorithm = StringColumn.create("algorithm");
                                problem = StringColumn.create("problem");
                                for (int i = 0; i < table.rowCount(); i++) {
                                    algorithm.append(algorithmName + "-" + f.getName());
                                    problem.append(_file.getName());
                                }
                            }
                        }
                    }
                }
            }
        }
        if (table != null) {
            table.addColumns(algorithm, problem);
            System.out.println(table.summary());
            table.write().csv(DIRECTORY + "report.csv");
        }
    }
}
