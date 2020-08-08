package com.castellanos94.instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.castellanos94.datatype.IntegerData;
import com.castellanos94.datatype.Interval;
import com.castellanos94.datatype.RealData;

public class PspIntervalInstance_GD extends Instance {

    public PspIntervalInstance_GD(String path) {
        super(path);
    }

    @Override
    public Instance loadInstance() throws FileNotFoundException {
        Scanner in = new Scanner(new File(path));
        String[] data;

        int n, m, r, p;

        Interval[][] weights_DMs; // pesos de los atributos
        Interval[][] vetos_DMs; // umbrales de veto
        Interval[] betas_DMs; // umbral de credibilidad del outranking
        RealData[][] chis_DMs; // umbral para restricciones
        RealData[] alphas_DMs; // umbral de dominancia
        Interval[] lambdas_DMs; // umbral para la mayoria

        Interval[][] projects;

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
        weights_DMs = new Interval[m][n];
        for (int j = 0; j < m; ++j) {
            data = this.readNextDataLine(in); // read objectives of DM j

            for (int i = 0; i < n; ++i) {
                weights_DMs[j][i] = new Interval(Double.parseDouble(data[i * 2]), Double.parseDouble(data[i * 2 + 1]));
            }
        }

        this.addParam("Weights_DMs", weights_DMs);

        // VETO, interval veto per DM j and objective i
        vetos_DMs = new Interval[m][n];
        for (int j = 0; j < m; ++j) {
            data = this.readNextDataLine(in); // read objectives of DM j

            for (int i = 0; i < n; ++i) {
                vetos_DMs[j][i] = new Interval(Double.parseDouble(data[i * 2]), Double.parseDouble(data[i * 2 + 1]));
            }
        }

        this.addParam("Vetos_DMs", vetos_DMs);

        // BETA, interval BETA per DM j and objective i
        betas_DMs = new Interval[m];
        for (int j = 0; j < m; ++j) {
            data = this.readNextDataLine(in); // read objectives of DM j

            betas_DMs[j] = new Interval(Double.parseDouble(data[0]), Double.parseDouble(data[1]));
        }

        this.addParam("Betas_DMs", betas_DMs);

        // ==

        // CHIs, interval CHI per DM j and constraint/resource i
        chis_DMs = new RealData[m][r];
        for (int j = 0; j < m; ++j) {
            data = this.readNextDataLine(in); // read constraint/resource of DM j (it is a single value not an interval)

            for (int i = 0; i < r; ++i) {
                chis_DMs[j][i] = new RealData(Double.parseDouble(data[i * 2]));
            }
        }

        this.addParam("Chis_DMs", chis_DMs);

        // ==ALPHA
        alphas_DMs = new RealData[m];

        for (int i = 0; i < m; ++i) {
            data = this.readNextDataLine(in); // read alpha of DM i (it is a single value, no tan interval)
            alphas_DMs[i] = new RealData(Double.parseDouble(data[0]));
        }

        this.addParam("Alphas_DMs", alphas_DMs);

        // ==

        lambdas_DMs = new Interval[m];

        for (int i = 0; i < m; ++i) {
            data = this.readNextDataLine(in); // read delta of DM i
            lambdas_DMs[i] = new Interval(Double.parseDouble(data[0]), Double.parseDouble(data[1]));
        }

        this.addParam("Lambdas_DMs", lambdas_DMs);

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

    public Interval[][] getWeights_DMs() {
        return (Interval[][]) this.getDataMatrix("Weights_DMs");
    }

    public Interval[][] getVetos_DMs() {
        return (Interval[][]) this.getDataMatrix("Vetos_DMs");
    }

    public Interval[] getBetas_DMs() {
        return (Interval[]) this.getDataVector("Betas_DMs");
    }

    public RealData[][] getChis_DMs() {
        return (RealData[][]) this.getDataMatrix("Chis_DMs");
    }

    public RealData[] getAlphas_DMs() {
        return (RealData[]) this.getDataVector("Alphas_DMs");

    }

    public Interval[] getLambdas_DMs() {
        return (Interval[]) this.getDataVector("Lambdas_DMs");

    }

    public Integer getNumProjects() {
        return (Integer) this.params.get("NumProjects");
    }

    public Interval[][] getProjects() {
        return (Interval[][]) this.getDataMatrix("Projects");
    }


    @Override
    public String toString() {
        return "PspIntervalInstance_GD []";
    }
}