package com.castellanos94.experimentation;

import java.io.File;
import java.io.IOException;

import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
/**
 * Test global report of Csat front of every execution. 
 */
public class Testing {
    public static void main(String[] args) throws IOException {
        String algorithmName = "NSGA3";
        //algorithmName = "nsga3-10";
        String DIRECTORY = "experiments" + File.separator + algorithmName + File.separator;
        //DIRECTORY = "experiments" + File.separator + "dtlz_preferences" + File.separator;
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
