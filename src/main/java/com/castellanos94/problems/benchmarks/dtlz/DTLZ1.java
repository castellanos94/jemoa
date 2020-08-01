package com.castellanos94.problems.benchmarks.dtlz;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.IntegerData;
import com.castellanos94.datatype.RealData;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The optimal pareto front lies on a linear hyperplane 0.5.
 * JMetal-based implementation.
 */
public class DTLZ1 extends Problem {
    public DTLZ1() {
        this(3, 7);
    }

    public DTLZ1(int numberOfObjectives, int numberOfVariables) {
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
        setName("DTLZ1");
    }

    @Override
    public void evaluate(Solution solution) {
        double[] f = new double[this.numberOfObjectives];
        double[] x = new double[this.numberOfDecisionVars];

        int k = getNumberOfDecisionVars() - getNumberOfObjectives() + 1;

        for (int i = 0; i < this.numberOfDecisionVars; i++) {
            x[i] = solution.getVariable(i).doubleValue();
        }

        double g = 0.0;
        for (int i = this.numberOfDecisionVars - k; i < this.numberOfDecisionVars; i++) {
            g += (x[i] - 0.5) * (x[i] - 0.5) - Math.cos(20.0 * Math.PI * (x[i] - 0.5));
        }

        g = 100 * (k + g);
        for (int i = 0; i < this.numberOfObjectives; i++) {
            f[i] = (1.0 + g) * 0.5;
        }

        for (int i = 0; i < this.numberOfObjectives; i++) {
            for (int j = 0; j < this.numberOfObjectives - (i + 1); j++) {
                f[i] *= x[j];
            }
            if (i != 0) {
                int aux = this.numberOfObjectives - (i + 1);
                f[i] *= 1 - x[aux];
            }
        }

        for (int i = 0; i < this.numberOfObjectives; i++) {
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
        return String.format("DTLZ1 [ numberOfObjectives = %d, numberOfVariables = %d]", numberOfObjectives,
                numberOfDecisionVars);
    }

    public static double[][] getParetoOptimal3Obj() throws FileNotFoundException {

        Scanner sc = new Scanner(new File("src/main/resources/pointsOfReference/DTLZ/DTLZ.3D/DTLZ1.3D.pf"));

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