package com.castellanos94.instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.castellanos94.datatype.IntegerData;
import com.castellanos94.datatype.Interval;
import com.castellanos94.datatype.RealData;
import com.castellanos94.preferences.impl.OutrankingModel;

/**
 * Instance for the PSP with interval data and GD.
 * 
 * @see com.castellanos94.problems.PSPI_GD
 */
public class PSPI_Instance extends Instance {

    public PSPI_Instance(String path) {
        super(path);
    }

    @Override
    public Instance loadInstance() throws FileNotFoundException {
        Scanner in = new Scanner(new File(path));
        String[] data;

        int n, m, r, p;

        /*
         * Interval[][] weights_DMs; // pesos de los atributos Interval[][] vetos_DMs;
         * // umbrales de veto Interval[] betas_DMs; // umbral de credibilidad del
         * outranking RealData[][] chis_DMs; // umbral para restricciones RealData[]
         * alphas_DMs; // umbral de dominancia Interval[] lambdas_DMs; // umbral para la
         * mayoria
         */ 

        Interval[][] projects;

        Interval[][] solutions_DMs; // the best compromises
        Interval[][][] frontier_DMs;
        Interval[][] initial_solutions;
        // Read Interval Budget
        data = this.readNextDataLine(in);
        this.addParam("Budget", new Interval(Double.parseDouble(data[0]), Double.parseDouble(data[1])));

        data = this.readNextDataLine(in);
        n = Integer.parseInt(data[0]);
        this.addParam("NumObjectives", n); // number of objectives

        data = this.readNextDataLine(in);
        m = Integer.parseInt(data[0]);
        this.addParam("NumDMs", m); // number of DMs
        IntegerData dm_type[] = new IntegerData[m];
        for (int i = 0; i < dm_type.length; i++) {
            data = this.readNextDataLine(in);
            dm_type[i] = new IntegerData(Integer.parseInt(data[0]));
        }
        this.addParam("TypeDMs", dm_type);
        data = this.readNextDataLine(in);
        r = Integer.parseInt(data[0]);
        this.addParam("NumResources", Integer.parseInt(data[0])); // number of resources or constraints

        // WEIGHTS, interval weights per DM j and objective i
        // Load Preference model for dm
        OutrankingModel[] models = new OutrankingModel[m];
        for (int i = 0; i < models.length; i++) {
            models[i] = new OutrankingModel();
            models[i].setSupportsUtilityFunction(dm_type[i].compareTo(1) == 0);
        }
        // weights_DMs = new Interval[m][n];
        for (int j = 0; j < m; ++j) {
            data = this.readNextDataLine(in); // read objectives of DM j
            Interval weights_dm[] = new Interval[n];
            for (int i = 0; i < n; ++i) {// obj
                weights_dm[i] = new Interval(Double.parseDouble(data[i * 2]), Double.parseDouble(data[i * 2 + 1]));
            }
            models[j].setWeights(weights_dm.clone());
        }

        // this.addParam("Weights_DMs", weights_DMs);

        // VETO, interval veto per DM j and objective i
        // vetos_DMs = new Interval[m][n];
        for (int j = 0; j < m; ++j) {
            data = this.readNextDataLine(in); // read objectives of DM j
            Interval vetos[] = new Interval[n];
            for (int i = 0; i < n; ++i) {// obj
                vetos[i] = new Interval(Double.parseDouble(data[i * 2]), Double.parseDouble(data[i * 2 + 1]));
            }
            models[j].setVetos(vetos.clone());
        }

        // this.addParam("Vetos_DMs", vetos_DMs);

        // BETA, interval BETA per DM j and objective i
        // betas_DMs = new Interval[m];
        for (int j = 0; j < m; ++j) {
            data = this.readNextDataLine(in); // read objectives of DM j

            models[j].setBeta(new Interval(Double.parseDouble(data[0]), Double.parseDouble(data[1])));
        }

        // this.addParam("Betas_DMs", betas_DMs);

        // ==

        // CHIs, interval CHI per DM j and constraint/resource i
        // chis_DMs = new RealData[m][r];
        for (int j = 0; j < m; ++j) {
            data = this.readNextDataLine(in); // read constraint/resource of DM j (it is a single value not an interval)

            for (int i = 0; i < r; ++i) {
                models[i].setChi(new RealData(Double.parseDouble(data[i * 2])));
            }
        }

        // this.addParam("Chis_DMs", chis_DMs);

        // ==ALPHA
        // alphas_DMs = new RealData[m];

        for (int i = 0; i < m; ++i) {
            data = this.readNextDataLine(in); // read alpha of DM i (it is a single value, no tan interval)
            models[i].setAlpha(new RealData(Double.parseDouble(data[0])));
        }

        // this.addParam("Alphas_DMs", alphas_DMs);

        // ==

        // lambdas_DMs = new Interval[m];

        for (int i = 0; i < m; ++i) {
            data = this.readNextDataLine(in); // read delta of DM i
            models[i].setLambda(new Interval(Double.parseDouble(data[0]), Double.parseDouble(data[1])));
        }

        // this.addParam("Lambdas_DMs", lambdas_DMs);
        this.addParam("outranking_models", models);
        // Read number of proyects
        data = this.readNextDataLine(in);
        p = Integer.parseInt(data[0]);
        this.addParam("NumProjects", Integer.parseInt(data[0])); // number of proyects

        // read sdm projects, siempre estará despues de la info de los DMs
        projects = new Interval[p][n + 1]; // espacio para almacenar informacion de proeyctos [COSTO] [Obj_1] ...
                                           // [Obj_n]

        for (int j = 0; j < p; ++j) {
            data = this.readNextDataLine(in); // read project j info [COST] [Obj 1] ... [Obj n]
            for (int i = 0; i < n + 1; ++i) {
                projects[j][i] = new Interval(Double.parseDouble(data[2 * i]), Double.parseDouble(data[2 * i + 1]));
            }
        }

        this.addParam("Projects", projects);

        data = this.readNextDataLine(in);
        if (data[0].equals("TRUE")) {
            // Read DMs Best Compromises (Obtained possibly using I-NOSGA)

            solutions_DMs = new Interval[m][p];
            for (int k = 0; k < m; ++k) {
                data = this.readNextDataLine(in); // read soluiton DM[k]
                for (int j = 0; j < p; ++j) {
                    solutions_DMs[k][j] = new Interval(Double.parseDouble(data[j]));
                }
            }

            this.addParam("Solutions", solutions_DMs);
        } else {
            this.addParam("Solutions", null);
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

            frontier_DMs = new Interval[m][][];
            Interval[][][] r2 = new Interval[m][][];
            Interval[][][] r1 = new Interval[m][][];

            for (int k = 0; k < m; ++k) {
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

        if (data[0].equals("TRUE")) {
            // Read DMs Best Compromises (Obtained possibly using I-NOSGA)

            data = this.readNextDataLine(in);

            int num = Integer.parseInt(data[0]);

            initial_solutions = new Interval[num][p];
            for (int k = 0; k < num; ++k) {
                data = this.readNextDataLine(in); // read soluiton DM[k]
                for (int j = 0; j < p; ++j) {
                    initial_solutions[k][j] = new Interval(Double.parseDouble(data[j]));
                }
            }

            this.addParam("InitialSolutions", initial_solutions);
        } else {
            this.addParam("InitialSolutions", null);
        }
        in.close();
        return this;
    }

    private String[] readNextDataLine(Scanner in) {
        String[] final_data = null;
        String line;

        do {
            line = in.nextLine();

            // First, Eliminate comments (they are expected to the right of the any //
            String[] data = line.replaceAll("\"", "").split("//");

            // After that, separate tokens divided by blanks or tabs
            data = data[0].split("[\\s\\t]");

            int j = 0;

            // Depure the result, just deliber those strings with length > 0
            for (int i = 0; i < data.length; ++i) {
                if (data[i].length() > 0) {
                    ++j;
                }
            }

            if (j > 0) {
                final_data = new String[j];
                j = 0;

                for (int i = 0; i < data.length; ++i) {
                    if (data[i].length() > 0) {
                        final_data[j++] = data[i];
                    }
                }
            }
        } while (in.hasNextLine() && final_data == null);

        return final_data;
    }

    public Interval getBudget() {
        return (Interval) this.getData("Budget");
    }

    public Integer getNumObjectives() {
        return (Integer) this.params.get("NumObjectives");
    }

    public Integer getNumDMs() {
        return (Integer) this.params.get("NumDMs");
    }

    public Integer getNumResources() {
        return (Integer) this.params.get("NumResources");
    }

    /*
     * public Interval[][] getWeights_DMs() { return (Interval[][])
     * this.getDataMatrix("Weights_DMs"); }
     * 
     * public Interval[][] getVetos_DMs() { return (Interval[][])
     * this.getDataMatrix("Vetos_DMs"); }
     * 
     * public Interval[] getBetas_DMs() { return (Interval[])
     * this.getDataVector("Betas_DMs"); }
     * 
     * public RealData[][] getChis_DMs() { return (RealData[][])
     * this.getDataMatrix("Chis_DMs"); }
     * 
     * public RealData[] getAlphas_DMs() { return (RealData[])
     * this.getDataVector("Alphas_DMs");
     * 
     * }
     * 
     * public Interval[] getLambdas_DMs() { return (Interval[])
     * this.getDataVector("Lambdas_DMs");
     * 
     * }
     */
    public OutrankingModel[] getOutrankingModels() {
        return (OutrankingModel[]) this.params.get("outranking_models");
    }

    public Integer getNumProjects() {
        return (Integer) this.params.get("NumProjects");
    }

    public Interval[][] getProjects() {
        return (Interval[][]) this.getDataMatrix("Projects");
    }

    public Interval[][] getSolutions() {
        return (Interval[][]) this.params.get("Solutions");
    }

    public Interval[][][] getFrontiers() {
        return (Interval[][][]) this.params.get("Frontiers");
    }

    public Interval[][] getInitialSolutions() {
        return (Interval[][]) this.params.get("InitialSolutions");
    }

    public Interval[][][] getR2() {
        return (Interval[][][]) this.getParams().get("R2");
    }

    public Interval[][][] getR1() {
        return (Interval[][][]) this.params.get("R1");
    }

    public IntegerData[] getTypesOfDMs() {
        return (IntegerData[]) this.getDataVector("TypeDMs");
    }

    @Override
    public String toString() {
        return "PspIntervalInstance_GD []";
    }
}