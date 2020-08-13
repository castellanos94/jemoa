package com.castellanos94.problems.benchmarks.dtlz;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import com.castellanos94.datatype.RealData;
import com.castellanos94.solutions.DoubleSolution;

/**
 * The search space contains a dense area of solutions next to the fM/f1 plane.
 * In order to investigate an MOEA’s ability to maintain a good distribution of
 * solutions, we modify problem DTLZ2 with a different parametric variable
 * mapping JMetal-based implementation.
 */
public class DTLZ4 extends DTLZ {
    public DTLZ4() {
        this(3, 12);
    }

    public DTLZ4(int numberOfObjectives, int numberOfVariables) {
        super(numberOfObjectives, numberOfVariables);
    }

    @Override
    public void evaluate(DoubleSolution solution) {
        int numberOfVariables = getNumberOfDecisionVars();
        int numberOfObjectives = getNumberOfObjectives();
        double alpha = 100.0;

        double[] f = new double[numberOfObjectives];
        double[] x = new double[numberOfVariables];


        for (int i = 0; i < numberOfVariables; i++) {
            x[i] = solution.getVariable(i);
        }

        double g =g2(x);
        for (int i = 0; i < numberOfObjectives; i++) {
            f[i] = 1.0 + g;
        }

        for (int i = 0; i < numberOfObjectives; i++) {
            for (int j = 0; j < numberOfObjectives - (i + 1); j++) {
                f[i] *= java.lang.Math.cos(java.lang.Math.pow(x[j], alpha) * (java.lang.Math.PI / 2.0));
            }
            if (i != 0) {
                int aux = numberOfObjectives - (i + 1);
                f[i] *= java.lang.Math.sin(java.lang.Math.pow(x[aux], alpha) * (java.lang.Math.PI / 2.0));
            }
        }
        for (int i = 0; i < numberOfObjectives; i++) {
            solution.setObjective(i, new RealData((f[i] < THRESHOLD)? 0: f[i]));
        }

    }

    @Override
    public  double[][] getParetoOptimal3Obj() throws FileNotFoundException {

        Scanner sc = new Scanner(new File("src/main/resources/pointsOfReference/DTLZ/DTLZ.3D/DTLZ4.3D.pf"));
        ArrayList<Double[]> list = new ArrayList<>();
        while (sc.hasNext()) {
            Double row[] = new Double[3];
            for (int i = 0; i < row.length; i++) {
                row[i] = sc.nextDouble();
            }
            list.add(row);
        }
        int max = list.size();
        double matrix[][] = new double[max][list.get(0).length];
        for (int i = 0; i < max; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = list.get(i)[j];
            }
        }
        sc.close();
        return matrix;
    }

    
}