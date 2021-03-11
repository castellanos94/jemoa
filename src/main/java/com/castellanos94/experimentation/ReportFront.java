package com.castellanos94.experimentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.castellanos94.problems.DTLZP;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;

import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

/**
 * Test global report of Csat front of every execution.
 */
public class ReportFront {
    private static final String OWNER = "FROM_PROBLEM";

    @SuppressWarnings("rawtypes")
    public static ArrayList<DoubleSolution> loadSolutions(DTLZP problem, File file)
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

    public static void generateReportFront(String algorithmName, String DIRECTORY) throws IOException {
        //String algorithmName = "NSGA3";
        // algorithmName = "nsga3-10";
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
