package com.castellanos94.problems.benchmarks.dtlz;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.IntegerData;
import com.castellanos94.datatype.RealData;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

public class DTLZ5 extends Problem {
    public DTLZ5() {
        this(3, 12);
    }

    public DTLZ5(int numberOfObjectives, int numberOfVariables) {
        this.numberOfObjectives = numberOfObjectives;
        this.numberOfDecisionVars = numberOfVariables;
        numberOfConstrains = 0;
        lowerBound = new Data[numberOfDecisionVars];
        upperBound = new Data[numberOfDecisionVars];
        objectives_type = new int[numberOfObjectives];
        for (int i = 0; i < numberOfObjectives; i++) {
            objectives_type[i] = Problem.MINIMIZATION;
        }
        for (int i = 0; i < lowerBound.length; i++) {
            lowerBound[i] = new RealData(0);
            upperBound[i] = new RealData(1);
        }
        setName("DTLZ5");
    }

    @Override
    public void evaluate(Solution solution) {
        int numberOfVariables = getNumberOfDecisionVars();
        int numberOfObjectives = getNumberOfObjectives();
        double[] theta = new double[numberOfObjectives - 1];
        double g = 0.0;

        double[] f = new double[numberOfObjectives];
        double[] x = new double[numberOfVariables];

        int k = getNumberOfDecisionVars() - getNumberOfObjectives() + 1;

        for (int i = 0; i < numberOfVariables; i++) {
            x[i] = solution.getVariable(i).doubleValue();
        }

        for (int i = numberOfVariables - k; i < numberOfVariables; i++) {
            g += (x[i] - 0.5) * (x[i] - 0.5);
        }

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
            solution.setObjective(i, new RealData(f[i]));
        }

    }

    @Override
    public Solution randomSolution() {
        Solution solution = new Solution(this);
        for (int i = 0; i < this.numberOfDecisionVars; i++) {
            solution.setVariable(i, new RealData(Tools.getRandom().nextDouble()));
        }
        return solution;
    }

    @Override
    public String toString() {
        return String.format("DTLZ5 [ numberOfObjectives = %d, numberOfVariables = %d]", numberOfObjectives,
                numberOfDecisionVars);
    }

    public static double[][] getParetoOptimal3Obj() throws FileNotFoundException {

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

    @Override
    public int evaluateConstraints(Solution solution) {
        /*
         * int n = 0; for (Data data : solution.getObjectives()){
         * if(data.compareTo(0)<0){ n++; } } solution.setPenalties(new IntegerData(n));
         */
        solution.setPenalties(new IntegerData(0));
        return 0;
    }
}