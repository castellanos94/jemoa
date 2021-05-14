package com.castellanos94.instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.IntegerData;
import com.castellanos94.datatype.Trapezoidal;
import com.castellanos94.problems.Problem;

/**
 * Instance for the PSP with Trapezoidal data and GD.
 * 
 * @see com.castellanos94.datatype.Trapezoidal
 */
public class TRI_PSP_Instance extends Instance {

    public TRI_PSP_Instance(String path) {
        super(path);
    }

    /**
     * Expected in this order: budget (Trapezoidal), number of objectves (int),
     * weights (Trapezoidal), veto (Trapezoidal), indifference (Trapezoidal), number
     * of areas (int), areas (Trapezoidal), number of regions (Trapezoidal), number
     * of projects (int), projects (Trapezoidal) [cost, area, region, obj_1, ...,
     * obj_n]
     */
    @Override
    public Instance loadInstance() throws FileNotFoundException {
        Scanner in = new Scanner(new File(path));
        String[] data = this.readNextDataLine(in);
        // Read budget
        Trapezoidal budget = new Trapezoidal(Double.parseDouble(data[0]), Double.parseDouble(data[1]),
                Double.parseDouble(data[2]), Double.parseDouble(data[3]));
        // Read number of objectives
        Integer numberOfObjectives = Integer.parseInt(this.readNextDataLine(in)[0]);
       /* // Read weights
        Trapezoidal[] weights = new Trapezoidal[numberOfObjectives];
        data = this.readNextDataLine(in);
        int index = 0;
        for (int i = 0; i < weights.length; i++) {
            weights[i] = new Trapezoidal(Double.parseDouble(data[index++]), Double.parseDouble(data[index++]),
                    Double.parseDouble(data[index++]), Double.parseDouble(data[index++]));
        }
        // Read veto threshold
        Trapezoidal[] vetoThreshold = new Trapezoidal[numberOfObjectives];
        data = this.readNextDataLine(in);
        index = 0;
        for (int i = 0; i < numberOfObjectives; i++) {
            vetoThreshold[i] = new Trapezoidal(Double.parseDouble(data[index++]), Double.parseDouble(data[index++]),
                    Double.parseDouble(data[index++]), Double.parseDouble(data[index++]));
        }
        // Read indifference threshold
        Trapezoidal[] indifferenceThreshold = new Trapezoidal[numberOfObjectives];
        data = this.readNextDataLine(in);
        
        index = 0;
        for (int i = 0; i < numberOfObjectives; i++) {
            indifferenceThreshold[i] = new Trapezoidal(Double.parseDouble(data[index++]),
                    Double.parseDouble(data[index++]), Double.parseDouble(data[index++]),
                    Double.parseDouble(data[index++]));
        }*/
        int index = 0;
        // Read areas
        Integer numberOfAreas = Integer.parseInt(this.readNextDataLine(in)[0]);
        Trapezoidal[][] areas = new Trapezoidal[numberOfAreas][2];
        for (int i = 0; i < numberOfAreas; i++) {
            data = this.readNextDataLine(in);
            index = 0;
            areas[i][0] = new Trapezoidal(Double.parseDouble(data[index++]), Double.parseDouble(data[index++]),
                    Double.parseDouble(data[index++]), Double.parseDouble(data[index++]));
            areas[i][1] = new Trapezoidal(Double.parseDouble(data[index++]), Double.parseDouble(data[index++]),
                    Double.parseDouble(data[index++]), Double.parseDouble(data[index++]));
        }
        // Read regions
        Integer numberOfRegions = Integer.parseInt(this.readNextDataLine(in)[0]);
        Trapezoidal[][] regions = new Trapezoidal[numberOfRegions][2];

        for (int i = 0; i < numberOfRegions; i++) {
            data = this.readNextDataLine(in);
            index = 0;
            regions[i][0] = new Trapezoidal(Double.parseDouble(data[index++]), Double.parseDouble(data[index++]),
                    Double.parseDouble(data[index++]), Double.parseDouble(data[index++]));
            regions[i][1] = new Trapezoidal(Double.parseDouble(data[index++]), Double.parseDouble(data[index++]),
                    Double.parseDouble(data[index++]), Double.parseDouble(data[index++]));
        }
        // Read projects [cost, area, region, obj_1, ..., obj_n]
        Integer numberOfProjects = Integer.parseInt(this.readNextDataLine(in)[0]);
        Data[][] projects = new Data[numberOfProjects][1 + 1 + 1 + numberOfObjectives];

        for (int i = 0; i < numberOfProjects; i++) {
            data = this.readNextDataLine(in);
            index = 0;
            // Cost
            projects[i][0] = new Trapezoidal(Double.parseDouble(data[index++]), Double.parseDouble(data[index++]),
                    Double.parseDouble(data[index++]), Double.parseDouble(data[index++]));
            // area
            projects[i][1] = new IntegerData(Double.parseDouble(data[index++]));
            // region
            projects[i][2] = new IntegerData(Double.parseDouble(data[index++]));

            for (int j = 3; j < (3 + numberOfObjectives); j++) {
                projects[i][j] = new Trapezoidal(Double.parseDouble(data[index++]), Double.parseDouble(data[index++]),
                        Double.parseDouble(data[index++]), Double.parseDouble(data[index++]));
            }
        }
        this.getParams().put("budget", budget);
        this.getParams().put("numberOfObjectives", numberOfObjectives);
        
        this.getParams().put("numberOfAreas", numberOfAreas);
        this.getParams().put("areas", areas);
        this.getParams().put("numberOfRegions", numberOfRegions);
        this.getParams().put("regions", regions);
        this.getParams().put("numberOfProjects", numberOfProjects);
        this.getParams().put("projects", projects);
        this.getParams().put("problem_type", new IntegerData(Problem.MAXIMIZATION));

        in.close();
        return this;
    }

    public Trapezoidal getBudget() {
        return (Trapezoidal) this.getParams().get("budget");
    }

    public Integer getNumberOfObjectives() {
        return (Integer) this.params.get("numberOfObjectives");
    }


    public Integer getNumberOfAreas() {
        return (Integer) this.params.get("numberOfAreas");
    }

    public Trapezoidal[][] getAreas() {
        return (Trapezoidal[][]) this.getParams().get("areas");
    }

    public Integer getNumberOfRegions() {
        return (Integer) this.params.get("numberOfRegions");
    }

    public Trapezoidal[][] getRegions() {
        return (Trapezoidal[][]) this.getParams().get("regions");
    }

    public Integer getNumberOfProjects() {
        return (Integer) this.params.get("numberOfProjects");
    }

    public Data[][] getProjects() {
        return (Data[][]) this.getParams().get("projects");
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

    @Override
    public String toString() {
        return "PSP with Trapezoidal data";
    }
}