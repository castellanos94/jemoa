package com.castellanos94.problems.benchmarks.dtlz;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import com.castellanos94.datatype.RealData;
import com.castellanos94.solutions.DoubleSolution;

/**
 * This function can also be used to investigate an MOEA’s ability to scale up
 * its performance in large number of objectives. Like in DTLZ1, for M>3, the
 * Pareto-optimal solutions must lie inside the first octant of the unit sphere
 * in a three-objective plot with fM as one of the axes. Since all
 * Pareto-optimal solutions require to satisfy ∑Mm=1f2m=1, the difference
 * between the left term with the obtained solutions and one can be used as a
 * metric for convergence as well. Besides the suggestions given in DTLZ1, the
 * problem can be made more difficult by replacing each variable xi (for i=1 to
 * (M−1)) with the mean value of p variables: xi=1p∑ipk=(i−1)p+1xk. *
 * JMetal-based implementation.
 */
public class DTLZ2 extends DTLZ {

    public DTLZ2() {
        this(3, 12);
    }

    public DTLZ2(int numberOfObjectives, int numberOfVariables) {
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

        double g = g2(x);

        for (int i = 0; i < numberOfObjectives; i++) {
            f[i] = 1.0 + g;
        }

        for (int i = 0; i < numberOfObjectives; i++) {
            for (int j = 0; j < numberOfObjectives - (i + 1); j++) {
                f[i] *= Math.cos(x[j] * 0.5 * Math.PI);
            }
            if (i != 0) {
                int aux = numberOfObjectives - (i + 1);
                f[i] *= Math.sin(x[aux] * 0.5 * Math.PI);
            }
        }

        for (int i = 0; i < numberOfObjectives; i++) {
           // solution.setObjective(i, new RealData((f[i] < THRESHOLD)? 0: f[i]));
            solution.setObjective(i, new RealData(f[i]));

        }

    }

    public double[][] getParetoOptimal3Obj() throws FileNotFoundException {

        Scanner sc = new Scanner(new File("src/main/resources/pointsOfReference/DTLZ/DTLZ.3D/DTLZ2.3D.pf"));
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