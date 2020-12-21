package com.castellanos94.indicators.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.castellanos94.datatype.Data;
import com.castellanos94.indicators.Indicator;
import com.castellanos94.solutions.Solution;

public class PMetric<S extends Solution<?>> implements Indicator<S> {
    protected Data[][] referencePoint;
    protected List<List<Data>> mappedDirection;

    public PMetric(Data[][] referencePoint) {
        this.referencePoint = referencePoint;
        this.mappedDirection = new ArrayList<>();
        translate();
    }

    private void translate() {
    }

    @Override
    public Data evaluate(List<S> solution) {
        return null;
    }

    public static void main(String[] args) {
        double[][] _r = { { 0, 0, 1 }, { 0, 0.25, 0.25 }, { 0, 0.5, 0.5 }, { 0, 0.75, 0.25 }, { 0, 0, 1 } };
        double[] ideal = { 0, 0, 0 };
        for (double[] ds : _r) {
            
            // min = min*(180/Math.PI);

            System.out.print(Arrays.toString(ds) + " -> (");
            for (int i = 0; i < ds.length; i++) {
                double tmp = 1 - Math.cos(Math.toRadians(ds[i]+ideal[i]));
                
                System.out.print(" " +tmp);
            }
            System.out.println();
        }
    }
}
