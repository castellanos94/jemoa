package com.castellanos94.problems.preferences.dtlz;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import com.castellanos94.datatype.RealData;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.preferences.Classifier;
import com.castellanos94.solutions.DoubleSolution;

public class DTLZ1_P extends DTLZPreferences {

    public DTLZ1_P(DTLZ_Instance instance, Classifier<DoubleSolution> classifier) {
        super(instance, classifier);
    }

    @Override
    public void evaluate(DoubleSolution solution) {
        double[] f = new double[this.numberOfObjectives];
        double[] x = new double[this.numberOfDecisionVars];

        for (int i = 0; i < this.numberOfDecisionVars; i++) {
            x[i] = solution.getVariable(i);
        }

        double g = g(x);
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
            solution.setObjective(i, new RealData((f[i] < THRESHOLD)? 0: f[i]));
         //   solution.setObjective(i, new RealData(f[i]));
        }        

    }

    public double[][] getParetoOptimal3Obj() throws FileNotFoundException {

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
    
}