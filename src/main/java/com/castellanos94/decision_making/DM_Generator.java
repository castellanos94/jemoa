package com.castellanos94.decision_making;

import java.util.Arrays;

import com.castellanos94.datatype.Interval;
import com.castellanos94.datatype.RealData;
import com.castellanos94.utils.Tools;

public class DM_Generator {

    protected int number_of_objectives;
    protected int number_of_dms;
    protected Interval[] max_objectives;

    public DM_Generator(int number_of_dms, int number_of_objectives, Interval[] max_objectives) {
        this.number_of_objectives = number_of_objectives;
        this.number_of_dms = number_of_dms;
        this.max_objectives = max_objectives;
    }

    public void execute() {
        for (int i = 0; i < number_of_dms; i++) {
            System.out.println("Generate dm: " + (i + 1));
            Interval weights[] = generateWeight();
            System.out.println(Arrays.toString(weights));
            // generar vetos, requiero los pesos previos generados y los objetivos

            Interval vetos[] = generateVeto(weights);
            System.out.println(Arrays.toString(vetos));

        }
    }

    public Interval[] generateVeto(Interval[] weights) {
        Interval veto[] = new Interval[number_of_objectives];
        for (int i = 0; i < number_of_objectives; i++) {
            double mindpoit = max_objectives[i].mindPoint().doubleValue();
            double widht = max_objectives[i].width().doubleValue();
            double r1 = Tools.getRandom().nextDouble();
            double vl = mindpoit - r1 * (widht / 10.0);
            vl = Tools.round(vl, Tools.PLACES);
            double r2 = Tools.getRandom().nextDouble();
            double vu = mindpoit + r2 * (widht / 10.0);
            vu = Tools.round(vu, Tools.PLACES);
            veto[i] = new Interval(vl, vu);
            boolean descartar = false;
            for (int j = 0; j < i; j++) {
                RealData poss_w = weights[j].possGreaterThanOrEq(weights[i]);
                RealData poss_v = veto[j].possGreaterThanOrEq(veto[i]);
                if (poss_w.compareTo(0.5) > 0 && poss_v.compareTo(0.5) > 0) {
                    descartar = true;
                    break;
                }
                poss_w = weights[i].possGreaterThanOrEq(weights[j]);
                poss_v = veto[i].possGreaterThanOrEq(veto[j]);
                if (poss_w.compareTo(0.5) > 0 && poss_v.compareTo(0.5) > 0) {
                    descartar = true;
                    break;
                }
            }
            if (descartar)
                --i;
        }
        return veto;

    }

    public Interval[] generateWeight() {
        double weights[];
        boolean descartar;
        do {
            weights = butler_weights();
            // Arrays.sort(weights);
            descartar = false;
            double sum = 0.0;
            for (int i = 0; i < weights.length; i++) {
                sum += weights[i];
                double v = 0.5 * (1 - weights[i]);
                if (weights[i] > v) {
                    descartar = true;
                }
            }

            descartar = (descartar) ? descartar : sum != 1;

        } while (descartar);
        Interval iw[] = new Interval[weights.length];
        for (int i = 0; i < iw.length; i++) {
            iw[i] = new Interval(weights[i]);
        }
        return iw;
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
                    // vector[i] = Tools.round(vector[i], 5);
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
            weights[i - 1] = Tools.round(weights[i - 1], Tools.PLACES);
        }

        return weights;
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Tools.setSeed(8435L);
        int dms = 2;
        int number_of_objectives = 10;
        Interval max_objectives[] = new Interval[number_of_objectives];
        for (int i = 0; i < number_of_objectives; i++) {
            max_objectives[i] = new Interval(0, 0.5);
        }
        DM_Generator dm_Generator = new DM_Generator(dms, number_of_objectives, max_objectives);
        dm_Generator.execute();
        long end = System.currentTimeMillis() - start;
        System.out.println("Time :" + end + " ms.");
    }
}
