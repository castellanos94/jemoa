package com.castellanos94.examples;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;

import com.castellanos94.algorithms.multi.IMOACO_R;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.problems.benchmarks.dtlz.*;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Plotter;
import com.castellanos94.utils.Scatter3D;
import com.castellanos94.utils.Tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IMOACO_Example {
    private static final Logger logger = LogManager.getLogger(IMOACO_Example.class);

    static final String DIRECTORY = "experiments" + File.separator + "IMOACOR";
    static final int EXPERIMENT = 31;

    public static void main(String[] args) throws IOException {
        File f = new File(DIRECTORY);
        if (!f.exists())
            f.mkdirs();
        DTLZ problem = new DTLZ7();
        logger.info(problem);
        ArrayList<Long> time = new ArrayList<>();
        ArrayList<DoubleSolution> bag = new ArrayList<>();
        Ranking<DoubleSolution> compartor = new DominanceComparator<>();
        Tools.setSeed(1L);
        for (int i = 0; i < EXPERIMENT; i++) {

            IMOACO_R<DoubleSolution> algorithm = new IMOACO_R<>(problem, 465, 0.1, 0.5, 14);
            if (i == 0) {
                logger.info(algorithm.toString());
            }
            algorithm.execute();
            logger.info(i + " time: " + algorithm.getComputeTime() + " ms.");
            time.add(algorithm.getComputeTime());

            try {
                Solution.writSolutionsToFile(DIRECTORY + File.separator + "execution_" + i,
                        new ArrayList<>(algorithm.getSolutions()));
            } catch (IOException e) {
                logger.error(e);
            }
            bag.addAll(algorithm.getSolutions());
        }

        long averageTime = time.stream().mapToLong(v -> v.longValue()).sum();
        logger.info("Resume " + problem.getName());
        logger.info("Total time: " + averageTime);
        logger.info("Average time : " + (double) averageTime / EXPERIMENT + " ms.");
        logger.info("Solutions in the bag: " + bag.size());

        compartor.computeRanking(bag);

        logger.info("Fronts : " + compartor.getNumberOfSubFronts());
        logger.info("Front 0: " + compartor.getSubFront(0).size());

        f = new File(DIRECTORY + File.separator + "_IMOACOR_" + problem.getName() + "_F0_"
                + problem.getNumberOfObjectives());

        ArrayList<String> strings = new ArrayList<>();
        for (DoubleSolution solution : compartor.getSubFront(0))
            strings.add(solution.toString());

        Files.write(f.toPath(), strings, Charset.defaultCharset());
        if (problem.getNumberOfObjectives() == 3) {
            Plotter plotter = new Scatter3D<>(compartor.getSubFront(0),
                    DIRECTORY + File.separator + problem.getName() + "_IMOACOR");
            plotter.plot();
        }
    }

}
