package com.castellanos94.problems.preferences.dtlz;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import com.castellanos94.datatype.RealData;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.preferences.Classifier;
import com.castellanos94.solutions.DoubleSolution;

public class DTLZ4_P extends DTLZPreferences {

    public DTLZ4_P(DTLZ_Instance instance, Classifier<DoubleSolution> classifier) {
        super(instance, classifier);
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

        double g = g2(x);
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
            solution.setObjective(i, new RealData((f[i] < THRESHOLD) ? 0 : f[i]));
        }

    }

    @Override
    public double[][] getParetoOptimal3Obj() throws FileNotFoundException {

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