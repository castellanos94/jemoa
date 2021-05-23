package com.castellanos94.examples;

import java.io.FileNotFoundException;
import com.castellanos94.utils.Tools;
import com.castellanos94.datatype.Interval;
import com.castellanos94.instances.PSPI_Instance;
import com.castellanos94.mcda.SatClassifier;
import com.castellanos94.preferences.impl.IntervalOutrankingRelations;
import com.castellanos94.problems.PSPI_GD;
import com.castellanos94.solutions.BinarySolution;
import java.util.Arrays;

public class SatGDExample {
    public static void main(String[] args) throws FileNotFoundException {
        PSPI_Instance ins = (PSPI_Instance) new PSPI_Instance("src/main/resources/instances/gd/GD_ITHDM-UFCA.txt")
                .loadInstance();
        PSPI_GD problem = new PSPI_GD(ins);
        System.out.println(ins);
        System.out.println(problem);
        BinarySolution[] solutions = new BinarySolution[20];
        Tools.setSeed(1L);

        Interval[][] solutionIntervals = (Interval[][]) ins.getParams().get("Solutions");
        solutions = new BinarySolution[solutionIntervals.length + 10];
        for (int i = 0; i < solutions.length; i++) {
            if (i < solutionIntervals.length)
                solutions[i] = problem.createFromString(solutionIntervals[i]);
            else
                solutions[i] = problem.randomSolution();
            problem.evaluate(solutions[i]);
            problem.evaluateConstraint(solutions[i]);
            // System.out.println(solutions[i].getPenalties()+" "+solutions[i]);
        }
        IntervalOutrankingRelations<BinarySolution> preference = new IntervalOutrankingRelations<>(
                problem.getNumberOfObjectives(), problem.getObjectives_type(), problem.getPreferenceModel(1));
        System.out.println("Evaluando el sistema de preferencia del DM 1");
        int[][] matrix = new int[solutions.length][solutions.length];

        for (int i = 0; i < solutions.length; i++) {
            for (int j = 0; j < solutions.length; j++) {
                // System.out.printf("Solucion %d vs %d : %d\n", (i + 1), (j + 1),
                // preference.compare(solutions[i], solutions[j]));
                if (i != j) {
                    matrix[i][j] = preference.compare(solutions[i], solutions[j]);
                }
            }
        }
        System.out.println("Matrix de (S, λ-relation)");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                System.out.printf("%3d, ", matrix[i][j]);
            }
            System.out.println();
        }
        System.out.println("Clasifcando soluciones con InterClass-nC");
        SatClassifier<BinarySolution> classificator = new SatClassifier<>(problem);
        for (int i = 0; i < solutions.length; i++) {
            // System.out.println("Clasificando solucion: " + i);
            classificator.classify(solutions[i]);
        }
        for (BinarySolution s : solutions) {
            System.out.println(s.getPenalties() + ", " + s.getResources() + ", " + s.getResources() + " "
                    + Arrays.toString((int[]) s.getAttribute(classificator.getAttributeKey())));
            // System.out.println(s.getObjectives());
        }

    }
}
