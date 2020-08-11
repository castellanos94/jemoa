package com.castellanos94.instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.castellanos94.datatype.Interval;
import com.castellanos94.preferences.impl.OutrankingModel;

public class DTLZ_Instance extends Instance {
    protected OutrankingModel preferenceModel[];
    protected int problem_number;

    public DTLZ_Instance(String path) {
        super(path);
    }

    @Override
    public Instance loadInstance() throws FileNotFoundException {
        File f = new File(path);
        Scanner in = new Scanner(f);
        String[] data = this.readNextDataLine(in);
        problem_number = Integer.parseInt(data[0]);
        data = this.readNextDataLine(in);
        int n = Integer.parseInt(data[0]);
        this.params.put("NumObjectives", Integer.parseInt(data[0]));
        data = this.readNextDataLine(in);
        int dv = Integer.parseInt(data[0]);
        this.params.put("NumDecisionVariables", Integer.parseInt(data[0]));
        data = this.readNextDataLine(in);
        int m = Integer.parseInt(data[0]);
        this.params.put("NumDMs", m);
        Interval[][] weights_DMs = new Interval[m][n];

        int k;
        for (k = 0; k < m; ++k) {
            data = this.readNextDataLine(in);

            for (k = 0; k < n; ++k) {
                weights_DMs[k][k] = new Interval(Double.parseDouble(data[k * 2]), Double.parseDouble(data[k * 2 + 1]));
            }
        }

        // this.params.put("Weights_DMs", weights_DMs);
        Interval[][] vetos_DMs = new Interval[m][n];

        for (k = 0; k < m; ++k) {
            data = this.readNextDataLine(in);

            for (k = 0; k < n; ++k) {
                vetos_DMs[k][k] = new Interval(Double.parseDouble(data[k * 2]), Double.parseDouble(data[k * 2 + 1]));
            }
        }

        // this.params.put("Vetos_DMs", vetos_DMs);
        Interval[] lambdas_DMs = new Interval[m];

        for (k = 0; k < m; ++k) {
            data = this.readNextDataLine(in);
            lambdas_DMs[k] = new Interval(Double.parseDouble(data[0]), Double.parseDouble(data[1]));
        }

        // this.params.put("Lambdas_DMs", lambdas_DMs);
        preferenceModel = new OutrankingModel[this.getNumDMs()];
        for (int i = 0; i < this.getNumDMs(); i++) {
            preferenceModel[i] = new OutrankingModel();
            preferenceModel[i].setLambda(lambdas_DMs[i]);
            preferenceModel[i].setVetos(vetos_DMs[i]);
            preferenceModel[i].setWeights(weights_DMs[i]);
        }

        data = this.readNextDataLine(in);
        if (!data[0].equals("TRUE")) {
            this.params.put("BestCompromises", (Object) null);
        } else {
            Interval[][] solutions_DMs = new Interval[m][dv];
            k = 0;

            while (true) {
                if (k >= m) {
                    this.params.put("BestCompromises", solutions_DMs);
                    break;
                }

                data = this.readNextDataLine(in);

                for (k = 0; k < dv; ++k) {
                    solutions_DMs[k][k] = new Interval(Double.parseDouble(data[k]));
                }

                ++k;
            }
        }

        data = this.readNextDataLine(in);
        if (!data[0].equals("TRUE")) {
            this.params.put("InitialSolutions", (Object) null);
        } else {
            data = this.readNextDataLine(in);
            k = Integer.parseInt(data[0]);
            Interval[][] initial_solutions = new Interval[k][dv];
            k = 0;

            while (true) {
                if (k >= k) {
                    this.params.put("InitialSolutions", initial_solutions);
                    break;
                }

                data = this.readNextDataLine(in);

                for (int j = 0; j < dv; ++j) {
                    initial_solutions[k][j] = new Interval(Double.parseDouble(data[j]));
                }

                ++k;
            }
        }

        in.close();
        return this;
    }

    private String[] readNextDataLine(Scanner in) {
        String[] final_data = null;

        do {
            String line = in.nextLine();
            String[] data = line.split("//");
            data = data[0].split("[\\s\\t]");
            int j = 0;

            int i;
            for (i = 0; i < data.length; ++i) {
                if (data[i].length() > 0) {
                    ++j;
                }
            }

            if (j > 0) {
                final_data = new String[j];
                j = 0;

                for (i = 0; i < data.length; ++i) {
                    if (data[i].length() > 0) {
                        final_data[j++] = data[i];
                    }
                }
            }
        } while (in.hasNextLine() && final_data == null);

        return final_data;
    }

    public Integer getNumObjectives() {
        return (Integer) this.params.get("NumObjectives");
    }

    public Integer getNumDecisionVariables() {
        return (Integer) this.params.get("NumDecisionVariables");
    }

    public Integer getNumDMs() {
        return (Integer) this.params.get("NumDMs");
    }

    public OutrankingModel[] getPreferenceModels() {
        return preferenceModel;
    }

    public OutrankingModel getPreferenceModel(int dm) {
        return this.preferenceModel[dm];
    }

    public int getProblem_number() {
        return problem_number;
    }

}