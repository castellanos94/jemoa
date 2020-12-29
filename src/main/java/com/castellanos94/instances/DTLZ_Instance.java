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
        Interval[] betas = new Interval[dm];
        for (dms = 0; dms < dm; ++dms) {
            data = this.readNextDataLine(in);
            betas[dms] = new Interval(Double.parseDouble(data[0]), Double.parseDouble(data[1]));
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
            preferenceModel[i].setAlpha(RealData.ONE);
            preferenceModel[i].setBeta(betas[i]);
        }

        data = this.readNextDataLine(in);
        if (!data[0].equalsIgnoreCase("TRUE")) {
            this.params.put("BestCompromises", (Object) null);
        } else {
            boolean isObjective = false;
            if(data.length > 1 && data[1].equalsIgnoreCase("true")){
                isObjective = true;
            }
            data = this.readNextDataLine(in);
            int n_solutions = Integer.parseInt(data[0]);
            RealData[][] solutions_DMs = new RealData[n_solutions][(isObjective)? n:dv];
            for (int i = 0; i < n_solutions; i++) {
                data = this.readNextDataLine(in);

                for (int j = 0; j < ((isObjective)? n:dv); j++) {
                    solutions_DMs[i][j] = new RealData(Double.parseDouble(data[j]));
                }
            }
            this.params.put("BestCompromises", solutions_DMs);
        }

        data = this.readNextDataLine(in);
        if (data[0].equals("TRUE")) {
            // Read DMs Frontier for Csat Cdis, there are m Sets of Lines
            // each set begins with a line with: a single value ( or interval value) fi
            // denoting the number of solutions in the frontier of DM i
            // after that there will be fi lines each holding the realizations of each
            // objective of a solution，it is assumed that the solutions are feasible
            // if data[1] exists and is true it means that the fi values are interval
            // values, otherwise, they will be single values
            boolean as_interval = false;

            if (data.length > 1 && data[1].equals("TRUE"))
                as_interval = true;
            int m = getNumDMs();

            Interval[][][] frontier_DMs = new Interval[m][][];
            Interval[][][] r2 = new Interval[m][][];
            Interval[][][] r1 = new Interval[m][][];

            for (k = 0; k < m; ++k) {
                data = this.readNextDataLine(in); // read number f_i of solutions in frontier
                int n_fi = Integer.parseInt(data[0]);

                frontier_DMs[k] = new Interval[n_fi][n];

                for (int j = 0; j < n_fi; ++j) {
                    data = this.readNextDataLine(in);
                    for (int l = 0; l < n; ++l) {
                        if (as_interval)
                            frontier_DMs[k][j][l] = new Interval(Double.parseDouble(data[2 * l]),
                                    Double.parseDouble(data[2 * l + 1]));
                        if (!as_interval)
                            frontier_DMs[k][j][l] = new Interval(Double.parseDouble(data[l]));
                    }
                }
                r2[k] = new Interval[n_fi / 2][n];
                r1[k] = new Interval[n_fi / 2][n];
                for (int i = 0, j = 0; i < n_fi; i++) {
                    if (i < n_fi / 2) {
                        System.arraycopy(frontier_DMs[k][i], 0, r2[k][i], 0, n);
                    } else {
                        System.arraycopy(frontier_DMs[k][i], 0, r1[k][j++], 0, n);
                    }
                }
                this.addParam("R2", r2);
                this.addParam("R1", r1);
            }

            this.addParam("Frontiers", frontier_DMs);

        } else {
            this.addParam("Frontiers", null);
        }

        data = this.readNextDataLine(in);
        if (!data[0].equals("TRUE")) {
            this.params.put("InitialSolutions", (Object) null);
        } else {
            data = this.readNextDataLine(in);
            int n_solutions = Integer.parseInt(data[0]);
            RealData[][] initial_solutions = new RealData[n_solutions][dv];
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
                        final_data[j++] = data[i].replaceAll(",", "");
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

    public Interval[][][] getFrontiers() {
        return (Interval[][][]) this.params.get("Frontiers");
    }

    public RealData[][] getInitialSolutions() {
        return (RealData[][]) this.params.get("InitialSolutions");
    }

    public Interval[][][] getR2() {
        return (Interval[][][]) this.getParams().get("R2");
    }

    public Interval[][][] getR1() {
        return (Interval[][][]) this.params.get("R1");
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
        if (getR2() != null) {
            other.append("R2:\n");
            for (int i = 0; i < preferenceModel.length; i++) {
                for (Interval[] sInterval : getR2()[i]) {
                    other.append(Arrays.toString(sInterval) + "\n");
                }
            }
        }

        if (getR2() != null) {
            other.append("R1:\n");
            for (int i = 0; i < preferenceModel.length; i++) {
                for (Interval[] sInterval : getR1()[i]) {
                    other.append(Arrays.toString(sInterval) + "\n");
                }
            }
        }

        return String.format("Objetives : %3d, Vars : %3d, DMs : %3d\nPreference Model: %s\n%s", getNumObjectives(),
                getNumDecisionVariables(), getNumDMs(), str.toString(), other.toString());

    }

    public static void main(String[] args) throws FileNotFoundException {
        DTLZ_Instance instance = (DTLZ_Instance) new DTLZ_Instance(
                "src/main/resources/DTLZ_INSTANCES/DTLZ1_Instance.txt").loadInstance();
        System.out.println(instance);
        System.out.println();
    }
}