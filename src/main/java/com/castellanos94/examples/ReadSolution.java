package com.castellanos94.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;

import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.preferences.impl.InterClassnC;
import com.castellanos94.problems.Problem;
import com.castellanos94.problems.preferences.dtlz.DTLZ1_P;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.Plotter;
import com.castellanos94.utils.Scatter3D;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;

public class ReadSolution {
    static final String DIRECTORY = "experiments" + File.separator + "dtlz_preferences";

    private static final Logger logger = LogManager.getLogger(ReadSolution.class);

    public static void main(String[] args) throws IOException {
        String solutionPath = "experiments/dtlz_preferences/nsga_iii_wp_bag_DTLZ1_P_f0_3";
        String path = "src/main/resources/DTLZ_INSTANCES/DTLZ1_Instance.txt";
        // path = "src/main/resources/instances/dtlz/PreferenceDTLZ1_Instance_01.txt";
        DTLZ_Instance instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();
        logger.info(instance);

        DTLZ1_P problem = new DTLZ1_P(instance);
        Scanner sc = new Scanner(new File(solutionPath));
        ArrayList<DoubleSolution> solutions = new ArrayList<>();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            solutions.add(problem.generateFromVarString(line.split("\\*")[0]));
        }
        logger.info(solutions.size());
        InterClassnC<DoubleSolution> classifier = new InterClassnC<>(problem);
        ArrayList<DoubleSolution> front = new ArrayList<>();
        ArrayList<DoubleSolution> hs = new ArrayList<>();
        ArrayList<DoubleSolution> s = new ArrayList<>();
        ArrayList<DoubleSolution> d = new ArrayList<>();
        ArrayList<DoubleSolution> hd = new ArrayList<>();
        for (DoubleSolution x : solutions) {
            classifier.classify(x);
            int[] iclass = (int[]) x.getAttribute(classifier.getAttributeKey());
            if (iclass[0] > 0) {
                hs.add(x);
            } else if (iclass[1] > 0) {
                s.add(x);
            } else if (iclass[2] > 0) {
                d.add(x);
            } else {
                hd.add(x);
            }
        }
        if (!hs.isEmpty()) {
            front.addAll(hs);
        } else if (!s.isEmpty()) {
            front.addAll(s);
        } else if (!d.isEmpty()) {
            front.addAll(d);
        } else if (!hd.isEmpty()) {
            front.addAll(hd);
        }
        logger.info(String.format("HSat : %3d, Sat : %3d, Dis : %3d, HDis : %3d", hs.size(), s.size(), d.size(),
                hd.size()));
        logger.info("Front 0: " + front.size());
        File f = new File(DIRECTORY + File.separator + "nsga3" + problem.getName() + "_F0_WP"
                + problem.getNumberOfObjectives());

        ArrayList<String> strings = new ArrayList<>();
        for (DoubleSolution solution : front)
            strings.add(solution.toString());

        Files.write(f.toPath(), strings, Charset.defaultCharset());
        if (problem.getNumberOfObjectives() == 3) {
            Plotter plotter = new Scatter3D<DoubleSolution>(front,
                    DIRECTORY + File.separator + problem.getName() + "F0_WP_nsga3");
            plotter.plot();
            // new Scatter3D(problem.getParetoOptimal3Obj(), directory + File.separator +
            // problem.getName()).plot();
        } else {
            Table table = Table.create(problem.getName() + "_F0_WP_" + problem.getNumberOfObjectives());
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                DoubleColumn column = DoubleColumn.create("objective_" + j);
                for (int k = 0; k < front.size(); k++) {
                    column.append(front.get(k).getObjective(j).doubleValue());
                }
                table.addColumns(column);
            }
            logger.info(table.summary());
        }

    }

}
