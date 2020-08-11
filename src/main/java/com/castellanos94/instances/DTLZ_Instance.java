package com.castellanos94.instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

import com.castellanos94.datatype.Interval;
import com.castellanos94.datatype.RealData;
import com.castellanos94.preferences.impl.OutrankingModel;

public class DTLZ_Instance extends Instance {
    protected OutrankingModel preferenceModel[];

    public DTLZ_Instance(String path) {
        super(path);
    }

    @Override
    public Instance loadInstance() throws FileNotFoundException {
        File f = new File(path);
        Scanner in = new Scanner(f);
        String[] data = this.readNextDataLine(in);

        int n = Integer.parseInt(data[0]);
        this.params.put("NumObjectives", Integer.parseInt(data[0]));
        data = this.readNextDataLine(in);
        int dv = Integer.parseInt(data[0]);
        this.params.put("NumDecisionVariables", Integer.parseInt(data[0]));
        data = this.readNextDataLine(in);
        int dm = Integer.parseInt(data[0]);
        this.params.put("NumDMs", dm);
        Interval[][] weights_DMs = new Interval[dm][n];

        int dms;
        int k;
        for (dms = 0; dms < dm; ++dms) {
            data = this.readNextDataLine(in);

            for (k = 0; k < n; ++k) {
                weights_DMs[dms][k] = new Interval(Double.parseDouble(data[k * 2]),
                        Double.parseDouble(data[k * 2 + 1]));
            }
        }

        // this.params.put("Weights_DMs", weights_DMs);
        Interval[][] vetos_DMs = new Interval[dm][n];

        for (dms = 0; dms < dm; ++dms) {
            data = this.readNextDataLine(in);

            for (k = 0; k < n; ++k) {
                vetos_DMs[dms][k] = new Interval(Double.parseDouble(data[k * 2]), Double.parseDouble(data[k * 2 + 1]));
            }
        }

        // this.params.put("Vetos_DMs", vetos_DMs);
        Interval[] lambdas_DMs = new Interval[dm];

        for (dms = 0; dms < dm; ++dms) {
            data = this.readNextDataLine(in);
            lambdas_DMs[dms] = new Interval(Double.parseDouble(data[0]), Double.parseDouble(data[1]));
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
            data = this.readNextDataLine(in);
            int n_solutions = Integer.parseInt(data[0]);
            RealData[][] solutions_DMs = new RealData[n_solutions][dv];
            System.out.println(n_solutions);
            for (int i = 0; i < n_solutions; i++) {
                data = this.readNextDataLine(in);

                for (int j = 0; j < dv; j++) {
                    solutions_DMs[i][j] = new RealData(Double.parseDouble(data[j]));
                }
            }
            this.params.put("BestCompromises", solutions_DMs);

        }

        data = this.readNextDataLine(in);
        if (!data[0].equals("TRUE")) {
            this.params.put("InitialSolutions", (Object) null);
        } else {
            data = this.readNextDataLine(in);
            int n_solutions = Integer.parseInt(data[0]);
            RealData[][] initial_solutions = new RealData[n_solutions][dv];
            System.out.println(n_solutions);
            for (int i = 0; i < n_solutions; i++) {
                data = this.readNextDataLine(in);

                for (int j = 0; j < dv; j++) {
                    initial_solutions[i][j] = new RealData(Double.parseDouble(data[j]));
                }
            }
            this.params.put("InitialSolutions", initial_solutions);

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

    public RealData[][] getBestCompromises() {
        return (RealData[][]) ((RealData[][]) this.getDataMatrix("BestCompromises"));
    }

    public RealData[][] getInitialSolutions() {
        return (RealData[][]) ((RealData[][]) this.getDataMatrix("InitialSolutions"));
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("\n");
        for (int i = 0; i < preferenceModel.length; i++) {
            str.append(String.format("Dm %3d: %s\n", (i + 1), preferenceModel[i]));
        }
        StringBuilder other = new StringBuilder();
        if (getBestCompromises() != null) {
            other.append("Best compromises:\n");
            for (RealData[] sInterval : getBestCompromises()) {
                other.append(Arrays.toString(sInterval) + "\n");
            }
        }
        if (getInitialSolutions() != null) {
            other.append("Initial solutions:\n");
            for (RealData[] sInterval : getInitialSolutions()) {
                other.append(Arrays.toString(sInterval) + "\n");
            }
        }
        return String.format("Objetives : %3d, Vars : %3d, DMs : %3d\nPreference Model: %s\n%s", getNumObjectives(),
                getNumDecisionVariables(), getNumDMs(), str.toString(), other.toString());

    }

    public static void main(String[] args) throws FileNotFoundException {
        DTLZ_Instance instance = (DTLZ_Instance) new DTLZ_Instance(
                "src/main/resources/instances/dtlz/PreferenceDTLZ1_Instance_01.txt").loadInstance();
        System.out.println(instance);
        System.out.println();
    }
}