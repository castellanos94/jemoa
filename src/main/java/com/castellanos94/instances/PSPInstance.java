package com.castellanos94.instances;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.Scanner;

import com.castellanos94.datatype.IntegerData;
import com.castellanos94.problems.Problem;

/*
*@author Castellanos Alvarez, Alejandro
*@since 22/03/202
*/
public class PSPInstance extends Instance {

    public PSPInstance(String path) {
        super(path);
    }

    public Instance loadInstance() throws FileNotFoundException {
        File f = new File(path);
        Scanner sc = new Scanner(f);
        read(sc);
        this.setId(f.getName());
        if (sc != null)
            sc.close();
        return this;
    }

    private void read(Scanner sc) {
        IntegerData budget = new IntegerData(Integer.parseInt(sc.nextLine().trim()));
        IntegerData nObj = new IntegerData(Integer.parseInt(sc.nextLine().trim()));
        IntegerData objectives[] = new IntegerData[nObj.intValue()];
        String line[] = sc.nextLine().trim().split(" ");
        for (int i = 0; i < objectives.length; i++) {
            objectives[i] = new IntegerData(Integer.parseInt(line[i]));
        }
        IntegerData umbral_veto[] = new IntegerData[nObj.intValue()];
        IntegerData umbral_indiferencia[] = new IntegerData[nObj.intValue()];
        line = sc.nextLine().trim().split(" ");
        for (int i = 0; i < umbral_indiferencia.length; i++) {
            umbral_indiferencia[i] = new IntegerData(Integer.parseInt(line[i]));
        }
        line = sc.nextLine().trim().split(" ");
        for (int j = 0; j < umbral_veto.length; j++) {
            umbral_veto[j] = new IntegerData(Integer.parseInt(line[j]));
        }
        IntegerData[][] areas = new IntegerData[Integer.parseInt(sc.nextLine().trim())][2];

        for (int i = 0; i < areas.length; i++) {
            line = sc.nextLine().trim().split(" ");
            for (int j = 0; j < line.length; j++) {
                areas[i][j] = new IntegerData(Integer.parseInt(line[j]));
            }
        }
        IntegerData[][] regions = new IntegerData[Integer.parseInt(sc.nextLine().trim())][2];

        for (int i = 0; i < regions.length; i++) {
            line = sc.nextLine().trim().split(" ");
            for (int j = 0; j < line.length; j++) {
                regions[i][j] = new IntegerData(Integer.parseInt(line[j]));
            }
        }
        IntegerData nProjects = new IntegerData(Integer.parseInt(sc.nextLine().trim()));
        IntegerData projects[][] = new IntegerData[nProjects.intValue()][1 + 1 + 1 + nObj.intValue()];
        for (int i = 0; i < nProjects.intValue(); i++) {
            line = sc.nextLine().trim().split(" ");
            for (int j = 0; j < projects[0].length; j++) {
                projects[i][j] = new IntegerData(Double.parseDouble(line[j]));
            }
        }
        sc.close();
        this.getParams().put("budget", budget);
        this.getParams().put("nObj", nObj);
        this.getParams().put("objectives", objectives);
        this.getParams().put("umbral_indiferencia", umbral_indiferencia);
        this.getParams().put("umbral_veto", umbral_veto);
        this.getParams().put("areas", areas);
        this.getParams().put("regions", regions);
        this.getParams().put("nProjects", nProjects);
        this.getParams().put("projects", projects);
        this.getParams().put("problem_type", new IntegerData(Problem.MAXIMIZATION));
        // return instance;
    }

}