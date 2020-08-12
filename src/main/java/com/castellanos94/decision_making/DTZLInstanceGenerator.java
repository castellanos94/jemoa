package com.castellanos94.decision_making;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.castellanos94.datatype.Interval;
import com.castellanos94.datatype.RealData;
import com.castellanos94.utils.Tools;
import com.google.common.io.Files;

public class DTZLInstanceGenerator {

    protected int number_of_objectives;
    protected int number_of_dms;
    protected Interval[] max_objectives;
    protected int numberOfVars;
    protected Interval lambdaInterval;

    public DTZLInstanceGenerator(int number_of_dms, int numberOfVars, int number_of_objectives,
            Interval[] max_objectives) {
        this.number_of_objectives = number_of_objectives;
        this.number_of_dms = number_of_dms;
        this.numberOfVars = numberOfVars;
        this.max_objectives = max_objectives;
        this.lambdaInterval = new Interval(0.51, 0.67);
    }

    public void execute(String path) throws IOException {
        StringBuilder content = new StringBuilder();
        content.append(number_of_objectives + " //objectives\n");
        content.append(numberOfVars + " //vars\n");
        content.append(number_of_dms + " //dms\n");
        Interval[][] weights = new Interval[number_of_dms][number_of_objectives];
        Interval[][] vetos = new Interval[number_of_dms][number_of_objectives];

        for (int i = 0; i < number_of_dms; i++) {

            System.out.println("Generate dm: " + (i + 1));
            if (number_of_objectives > 3)
                weights[i] = generateWeight();
            else {
                for (int j = 0; j < number_of_objectives; j++) {
                    weights[i][j] = new Interval(Tools.round(1.0 / 3, 3));
                }
            }
            content.append(String.format("%s // weight dm %d\n",
                    Arrays.toString(weights[i]).replace("[", "").replace("]", ""), (i + 1)));
            vetos[i] = generateVeto(weights[i]);
        }
        System.out.println("Agregando vetos");
        for (int i = 0; i < number_of_dms; i++) {
            content.append(String.format("%s // veto dm %d\n",
                    Arrays.toString(vetos[i]).replace("[", "").replace("]", ""), (i + 1)));
        }
        System.out.println("Agregando lambdas");
        for (int i = 0; i < number_of_dms; i++) {
            content.append(String.format("%s // lambda %d\n", lambdaInterval, (i + 1)));
        }
        content.append("FALSE\n");
        content.append("FALSE\n");
        if (path == null || path.isEmpty())
            System.out.println(content);
        else {
            File file = new File(path);
            Files.write(content.toString().getBytes(), file);
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

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
       // Tools.setSeed(8435L);
        int dms = 1;
        int number_of_objectives = 3;
        int numberOfVars = 12;
        Interval max_objectives[] = new Interval[number_of_objectives];
        for (int i = 0; i < number_of_objectives; i++) {
            max_objectives[i] = new Interval(0, 1);
        }
        DTZLInstanceGenerator dm_Generator = new DTZLInstanceGenerator(dms, numberOfVars, number_of_objectives,
                max_objectives);
        String path = "src/main/resources/instances/dtlz/DTLZ7Instance.txt";
        dm_Generator.execute(path);
        long end = System.currentTimeMillis() - start;
        System.out.println("Time :" + end + " ms.");
    }
}
