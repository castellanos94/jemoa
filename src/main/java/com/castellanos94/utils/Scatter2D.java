package com.castellanos94.utils;

import java.io.File;
import java.util.ArrayList;

import com.castellanos94.solutions.Solution;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.ScatterPlot;

public class Scatter2D<S extends Solution<?>> implements Plotter {

    private double[][] matrix;
    private String plotTitle;
    protected Table table;

    public Scatter2D(ArrayList<S> front, String title) {
        this.matrix = new double[front.size()][front.get(0).getObjectives().size()];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = front.get(i).getObjectives().get(j).doubleValue();
            }
        }
        this.plotTitle = title;
    }

    public Scatter2D(double[][] matrix, String title) {
        this.matrix = matrix;
        this.plotTitle = title;
    }

    public Scatter2D(double[][] matrix) {
        this(matrix, "Front");
    }

    @Override
    public void plot() {
        int numberOfRows = matrix.length;
        double[] f1 = new double[numberOfRows];
        double[] f2 = new double[numberOfRows];

        for (int i = 0; i < numberOfRows; i++) {
            f1[i] = matrix[i][0];
            f2[i] = matrix[i][1];
        }

        table = Table.create("table").addColumns(DoubleColumn.create("f1", f1), DoubleColumn.create("f2", f2));
        System.out.println(table.summary());

        Plot.show(ScatterPlot.create(plotTitle, table, "f1", "f2"), new File(plotTitle + ".html"));
    }

    @Override
    public Table getTable() {
        return table;
    }

}