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

public class DTLZ7 extends Problem {

    public DTLZ7() {
        this(3, 12);
    }

    public DTLZ7(int numberOfObjectives, int numberOfVariables) {
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
    }

    @Override
    public void evaluate(Solution solution) {
        int numberOfVariables = getNumberOfDecisionVars();
        int numberOfObjectives = getNumberOfObjectives();

        double[] f = new double[numberOfObjectives];
        double[] x = new double[numberOfVariables];

        int k = getNumberOfDecisionVars() - getNumberOfObjectives() + 1;

        for (int i = 0; i < numberOfVariables; i++) {
            x[i] = solution.getVariable(i).doubleValue();
        }

        double g = 0.0;
        for (int i = numberOfVariables - k; i < numberOfVariables; i++) {
            g += x[i];
        }

        g = 1 + (9.0 * g) / k;

        System.arraycopy(x, 0, f, 0, numberOfObjectives - 1);

        double h = 0.0;
        for (int i = 0; i < numberOfObjectives - 1; i++) {
            h += (f[i] / (1.0 + g)) * (1 + Math.sin(3.0 * Math.PI * f[i]));
        }

        h = numberOfObjectives - h;

        f[numberOfObjectives - 1] = (1 + g) * h;

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
        return String.format("DTLZ7 [ numberOfObjectives = %d, numberOfVariables = %d]", numberOfObjectives,
                numberOfDecisionVars);
    }

    public static double[][] getParetoOptimal3Obj() throws FileNotFoundException {

        Scanner sc = new Scanner(new File("src/main/resources/pointsOfReference/DTLZ/DTLZ.3D/DTLZ7.3D.pf"));
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