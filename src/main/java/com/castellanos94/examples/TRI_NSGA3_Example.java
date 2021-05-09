package com.castellanos94.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.castellanos94.algorithms.multi.NSGA_III;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.instances.TRI_PSP_Instance;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.operators.impl.BinaryMutation;
import com.castellanos94.operators.impl.HUXCrossover;
import com.castellanos94.operators.impl.TournamentSelection;
import com.castellanos94.problems.PSP_TRI;
import com.castellanos94.solutions.BinarySolution;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 36126.16 dtlz2 34126.96 ms dtlz3 30877.86 ms dtlz4 34637.68 ms dtlz5 25228.0
 * ms dtlz6 33271.8 ms dtlz7
 */
public class TRI_NSGA3_Example {
    private static final Logger logger = LogManager.getLogger(TRI_NSGA3_Example.class);
    static final String DIRECTORY = "experiments" + File.separator + "TRI_NSGA3_Example";
    static final int EXPERIMENT = 1;

    public static void main(String[] args) throws CloneNotSupportedException, IOException {
        new File(DIRECTORY).mkdirs();
        Tools.setSeed(1l);

        ArrayList<BinarySolution> bag = new ArrayList<>();
        long averageTime = 0;
        // 1,3
        NSGA_III<BinarySolution> algorithm = loadProblem();
        logger.info("Experimentation: TRI_NSGA3_Example");
        PSP_TRI problem = (PSP_TRI) algorithm.getProblem();
        logger.info(problem);
        logger.info(algorithm);

        ArrayList<Integer> range = new ArrayList<>(IntStream.range(0, EXPERIMENT).boxed().collect(Collectors.toList()));
        ArrayList<Long> time = new ArrayList<>();
        range.stream().sequential().forEach(i -> {
            NSGA_III<BinarySolution> a = null;
            try {
                a = loadProblem();
            } catch (FileNotFoundException e1) {
                logger.error(e1);
            }

            a.execute();
            time.add(a.getComputeTime());
            try {
                Solution.writSolutionsToFile(DIRECTORY + File.separator + "execution_" + i,
                        new ArrayList<>(a.getSolutions()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            logger.info(i + " time: " + a.getComputeTime() + " ms.");
            bag.addAll(a.getSolutions());
        });
        averageTime = time.stream().mapToLong(v -> v.longValue()).sum();
        logger.info("Resume " + problem.getName());
        logger.info("Total time: " + averageTime);
        logger.info("Average time : " + (double) averageTime / EXPERIMENT + " ms.");
        logger.info("Solutions in the bag: " + bag.size());

        Ranking<BinarySolution> compartor = new DominanceComparator<>();
        compartor.computeRanking(bag);

        logger.info("Fronts : " + compartor.getNumberOfSubFronts());
        logger.info("Front 0: " + compartor.getSubFront(0).size());

        File f = new File(DIRECTORY);
        if (!f.exists())
            f.mkdirs();
        f = new File(DIRECTORY + File.separator + "nsga_iii_bag_" + problem.getName() + "_F0_"
                + problem.getNumberOfObjectives());

        ArrayList<String> strings = new ArrayList<>();
        for (BinarySolution solution : compartor.getSubFront(0))
            strings.add(solution.toString());

        Files.write(f.toPath(), strings, Charset.defaultCharset());
        logger.info("End Experimentation.");
    }

    private static NSGA_III<BinarySolution> loadProblem() throws FileNotFoundException {

        TRI_PSP_Instance instance = new TRI_PSP_Instance("src/main/resources/instances/psp/triO4P25.txt");
        PSP_TRI problem = new PSP_TRI((TRI_PSP_Instance) instance.loadInstance());
        SelectionOperator<BinarySolution> selectionOperator = new TournamentSelection<>(100,
                new DominanceComparator<>());
        NSGA_III<BinarySolution> nsga3 = new NSGA_III<>(problem, 100, 500, 12, selectionOperator,
                (CrossoverOperator<BinarySolution>) new HUXCrossover(),
                (MutationOperator<BinarySolution>) new BinaryMutation(0.1));
        return nsga3;
    }

}