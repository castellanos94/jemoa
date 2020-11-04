package com.castellanos94.utils;

import java.io.File;
import java.util.ArrayList;

import com.castellanos94.solutions.Solution;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.Scatter3DPlot;

public class Scatter3D<S extends Solution<?>> implements Plotter {

    private double[][] matrix;
    private String plotTitle;
    private Table table;

    public Scatter3D(ArrayList<S> front, String title) {
        this.matrix = new double[front.size()][front.get(0).getObjectives().size()];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = front.get(i).getObjectives().get(j).doubleValue();
            }
        }
        this.plotTitle = title;
    }

    public Scatter3D(double[][] matrix, String title) {

        this.matrix = matrix;
        this.plotTitle = title;
    }

    public Scatter3D(double[][] matrix) {
        this(matrix, "Front");
    }

    @Override
    public void plot() {
        int numberOfRows = matrix.length;
        double[] f1 = new double[numberOfRows];
        double[] f2 = new double[numberOfRows];
        double[] f3 = new double[numberOfRows];

        for (int i = 0; i < numberOfRows; i++) {
            f1[i] = matrix[i][0];
            f2[i] = matrix[i][1];
            f3[i] = matrix[i][2];
        }

        table = Table.create("table").addColumns(DoubleColumn.create("f1", f1), DoubleColumn.create("f2", f2),
                DoubleColumn.create("f3", f3));

        System.out.println(table.summary());
        
        Plot.show(Scatter3DPlot.create(plotTitle, table, "f1", "f2", "f3"), new File(plotTitle + ".html"));
    }

    @Override
    public Table getTable() {
        return table;
    }

}