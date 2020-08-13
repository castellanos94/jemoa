package com.castellanos94.problems.benchmarks.dtlz;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import com.castellanos94.datatype.RealData;
import com.castellanos94.solutions.DoubleSolution;

/**
 * In order to investigate an MOEA’s ability to converge to the global
 * Pareto-optimal front, we suggest using the above problem with the g function
 * equal to Rastrigin. The g function introduces (3k−1) local Pareto-optimal
 * fronts and one global Pareto-optimal front. All local Pareto-optimal fronts
 * are parallel to the global Pareto-optimal front and an MOEA can get stuck at
 * any of these local Pareto-optimal fronts, before converging to the global
 * Pareto-optimal front (at g∗=0). JMetal-based implementation.
 */
public class DTLZ3 extends DTLZ {

    public DTLZ3() {
        this(3, 12);
    }

    public DTLZ3(int numberOfObjectives, int numberOfVariables) {
        super(numberOfObjectives, numberOfVariables);
    }

    @Override
    public void evaluate(DoubleSolution solution) {
        int numberOfVariables = getNumberOfDecisionVars();
        int numberOfObjectives = getNumberOfObjectives();
        double[] f = new double[numberOfObjectives];
        double[] x = new double[numberOfVariables];

        for (int i = 0; i < numberOfVariables; i++) {
            x[i] = solution.getVariable(i);
        }

        double g = g(x);
        for (int i = 0; i < numberOfObjectives; i++) {
            f[i] = 1.0 + g;
        }

        for (int i = 0; i < numberOfObjectives; i++) {
            for (int j = 0; j < numberOfObjectives - (i + 1); j++) {
                f[i] *= java.lang.Math.cos(x[j] * 0.5 * java.lang.Math.PI);
            }
            if (i != 0) {
                int aux = numberOfObjectives - (i + 1);
                f[i] *= java.lang.Math.sin(x[aux] * 0.5 * java.lang.Math.PI);
            }
        }

        for (int i = 0; i < numberOfObjectives; i++) {
            solution.setObjective(i, new RealData((f[i] < THRESHOLD)? 0: f[i]));
        }

    }

    public double[][] getParetoOptimal3Obj() throws FileNotFoundException {

        Scanner sc = new Scanner(new File("src/main/resources/pointsOfReference/DTLZ/DTLZ.3D/DTLZ3.3D.pf"));
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