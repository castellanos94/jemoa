package com.castellanos94.decision_making;

import com.castellanos94.utils.Tools;

public class DM_Generator {
    protected int number_of_objectives;
    protected int number_of_dms;

    public DM_Generator(int number_of_dms, int number_of_objectives) {
        this.number_of_objectives = number_of_objectives;
        this.number_of_dms = number_of_dms;
    }

    public void execute() {
        for (int i = 0; i < number_of_dms; i++) {
            System.out.println("Generate dm: " + (i + 1));

        }
    }

    public double[] generateVeto(double[] weights) {
        double veto[] = new double[number_of_objectives];

        return veto;

    }

    public double[] generateWeight() {
        double weights[];
        boolean descartar;
        do {
            weights = butler_weights();
            descartar = false;
            double sum = 0.0;
            for (int i = 0; i < weights.length; i++) {
                sum += weights[i];
                if (weights[i] > 0.5 * (1 - weights[i])) {
                    descartar = true;
                }
            }
            if (sum != 1.0) {
                descartar = true;
                System.out.println("Weigths is not 1.0 " + sum);
            }
        } while (descartar);
        return weights;
    }

    private double[] butler_weights() {
        double[] vector = new double[number_of_objectives + 1];
        double[] weights = new double[number_of_objectives];
        boolean iguales = true;

        while (iguales) {
            vector[0] = 0;
            for (int i = 1; i < number_of_objectives; ++i) {
                do {
                    vector[i] = Tools.getRandom().nextInt(1000) / 1000.0;
                } while (vector[i] <= 0 || vector[i] >= 1.0);

            }
            vector[number_of_objectives] = 1;

            iguales = false;
            for (int i = 0; i < number_of_objectives + 1; ++i) {
                for (int j = 0; j < number_of_objectives - i - 1; ++j) {
                    if (vector[j] > vector[j + 1]) {
                        double aux = vector[j];
                        vector[j] = vector[j + 1];
                        vector[j + 1] = aux;
                    }

                    if (vector[j] == vector[j + 1]) {
                        iguales = true;
                    }
                }
            }
        }

        for (int i = 1; i <= number_of_objectives; ++i) {
            weights[i - 1] = vector[i] - vector[i - 1];
        }

        return weights;
    }
}
