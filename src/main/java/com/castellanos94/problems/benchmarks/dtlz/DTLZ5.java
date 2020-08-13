package com.castellanos94.problems.benchmarks.dtlz;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import com.castellanos94.datatype.RealData;
import com.castellanos94.solutions.DoubleSolution;

/**
 * This problem will test an MOEA’s ability to converge to a cruve and will also
 * allow an easier way to visually demonstrate (just by plotting f_M with any
 * other objective function) the performance of an MOEA. Since there is a
 * natural bias for solutions close to this Pareto-optimal curve, this problem
 * may be easy for an algorithmn to solve. Because of its simplicity its
 * recommended to use a higher number of objectives M∈[5,10]. JMetal-based
 * implementation.
 */
public class DTLZ5 extends DTLZ {
    public DTLZ5() {
        this(3, 12);
    }

    public DTLZ5(int numberOfObjectives, int numberOfVariables) {
        super(numberOfObjectives, numberOfVariables);
    }

    @Override
    public void evaluate(DoubleSolution solution) {
        int numberOfVariables = getNumberOfDecisionVars();
        int numberOfObjectives = getNumberOfObjectives();
        double[] theta = new double[numberOfObjectives - 1];

        double[] f = new double[numberOfObjectives];
        double[] x = new double[numberOfVariables];

        for (int i = 0; i < numberOfVariables; i++) {
            x[i] = solution.getVariable(i);
        }
        double g = g2(x);

        double t = java.lang.Math.PI / (4.0 * (1.0 + g));

        theta[0] = x[0] * java.lang.Math.PI / 2.0;
        for (int i = 1; i < (numberOfObjectives - 1); i++) {
            theta[i] = t * (1.0 + 2.0 * g * x[i]);
        }

        for (int i = 0; i < numberOfObjectives; i++) {
            f[i] = 1.0 + g;
        }

        for (int i = 0; i < numberOfObjectives; i++) {
            for (int j = 0; j < numberOfObjectives - (i + 1); j++) {
                f[i] *= java.lang.Math.cos(theta[j]);
            }
            if (i != 0) {
                int aux = numberOfObjectives - (i + 1);
                f[i] *= java.lang.Math.sin(theta[aux]);
            }
        }
        for (int i = 0; i < numberOfObjectives; i++) {
            solution.setObjective(i, new RealData((f[i] < THRESHOLD)? 0: f[i]));
        }

    }

    @Override
    public double[][] getParetoOptimal3Obj() throws FileNotFoundException {

        Scanner sc = new Scanner(new File("src/main/resources/pointsOfReference/DTLZ/DTLZ.3D/DTLZ5.3D.pf"));
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