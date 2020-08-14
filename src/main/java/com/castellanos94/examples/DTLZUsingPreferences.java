package com.castellanos94.examples;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import com.castellanos94.algorithms.multi.NSGA_III;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.operators.impl.PolynomialMutation;
import com.castellanos94.operators.impl.TournamentSelection;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.components.impl.FastNonDominatedSort;
import com.castellanos94.operators.impl.SBXCrossover;
import com.castellanos94.problems.preferences.dtlz.*;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.Plotter;
import com.castellanos94.utils.ReferenceHyperplane;
import com.castellanos94.utils.Scatter3D;
import com.castellanos94.utils.Tools;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;

public class DTLZUsingPreferences {
    static final String DIRECTORY = "experiments" + File.separator + "dtlz_preferences";
    static final int EXPERIMENT = 50;
    @SuppressWarnings("unchecked")

    public static void main(String[] args) throws CloneNotSupportedException, IOException {
        Tools.setSeed(8435L);
        String path = "src/main/resources/instances/dtlz/DTLZInstance.txt";
        // path = "src/main/resources/instances/dtlz/PreferenceDTLZ1_Instance_01.txt";
        DTLZ_Instance instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();
        System.out.println(instance);

        DTLZ1_P problem = new DTLZ1_P(instance, null);
        ArrayList<DoubleSolution> solutions = new ArrayList<>();
        for (int i = 0; i < instance.getInitialSolutions().length; i++) {
            DoubleSolution s = new DoubleSolution(problem);
            for (int j = 0; j < problem.getNumberOfDecisionVars(); j++) {
                s.setVariable(j, instance.getInitialSolutions()[i][j].doubleValue());
            }
            problem.evaluate(s);
            problem.evaluateConstraint(s);
            solutions.add(s);
        }
        ReferenceHyperplane<DoubleSolution> referencias = new ReferenceHyperplane<>(problem.getNumberOfObjectives(),
                instance.getInitialSolutions().length);
        referencias.transformToReferencePoint(solutions);
        referencias.getReferences().forEach(System.out::println);
        HashMap<String, Object> options = new HashMap<>();
        options.put("pop_size", 92);
        options.put("partitions", 12);
        options.put("crossover", new SBXCrossover(30, 1.0));
        options.put("mutation", new PolynomialMutation());
        int maxIterations = 400;

        SelectionOperator<DoubleSolution> selectionOperator = new TournamentSelection<>((int) options.get("pop_size"),
                new DominanceComparator<DoubleSolution>());
        NSGA_III<DoubleSolution> algorithm = new NSGA_III<>(problem, (int) options.get("pop_size"), maxIterations,
                selectionOperator, (CrossoverOperator<DoubleSolution>) options.get("crossover"),
                (MutationOperator<DoubleSolution>) options.get("mutation"), new FastNonDominatedSort<DoubleSolution>(),
                referencias);
        PrintStream console = System.out;
        PrintStream ps = new PrintStream(
                DIRECTORY + File.separator + "resume_" + problem.getName() + "_" + problem.getNumberOfObjectives());

        System.setOut(console);
        System.out.println(problem);
        System.out.println(algorithm);

        System.setOut(ps);
        System.out.println(problem);
        System.out.println(algorithm);
        ArrayList<DoubleSolution> bag = new ArrayList<>();
        long averageTime = 0;
        for (int i = 0; i < EXPERIMENT; i++) {
            algorithm = new NSGA_III<>(problem, (int) options.get("pop_size"), maxIterations, selectionOperator,
                    (CrossoverOperator<DoubleSolution>) options.get("crossover"),
                    (MutationOperator<DoubleSolution>) options.get("mutation"),
                    new FastNonDominatedSort<DoubleSolution>(), referencias); // algorithm.setReferenceHyperplane(referenceHyperplane);
            // referenceHyperplane.resetCount();

            algorithm.execute();
            averageTime += algorithm.getComputeTime();

            System.setOut(console);
            System.out.println(i + " time: " + algorithm.getComputeTime() + " ms.");
            System.setOut(ps);
            System.out.println(i + " time: " + algorithm.getComputeTime() + " ms.");
            // Solution.writSolutionsToFile(directory + File.separator + problem.getName() +
            // "_" + i, algorithm.getSolutions());
            bag.addAll(algorithm.getSolutions());
        }
        System.setOut(console);
        System.out.println("Total time: " + averageTime);
        System.out.println("Average time : " + (double) averageTime / EXPERIMENT + " ms.");
        System.out.println("Solutions in the bag: " + bag.size());

        System.setOut(ps);
        System.out.println("Total time: " + averageTime + " ms.");
        System.out.println("Average time : " + (double) averageTime / EXPERIMENT + " ms.");
        System.out.println("Solutions in the bag: " + bag.size());

        Ranking<DoubleSolution> compartor = new DominanceComparator<>();
        compartor.computeRanking(bag);

        System.setOut(console);
        System.out.println("Fronts : " + compartor.getNumberOfSubFronts());
        System.out.println("Front 0: " + compartor.getSubFront(0).size());

        System.setOut(ps);
        System.out.println("Fronts : " + compartor.getNumberOfSubFronts());
        System.out.println("Front 0: " + compartor.getSubFront(0).size());

        File f = new File(DIRECTORY);
        if (!f.exists())
            f.mkdirs();
        f = new File(DIRECTORY + File.separator + "nsga_iii_bag_" + problem.getName() + "_f0_"
                + problem.getNumberOfObjectives());

        ArrayList<String> strings = new ArrayList<>();
        for (DoubleSolution solution : compartor.getSubFront(0))
            strings.add(solution.toString());

        Files.write(f.toPath(), strings, Charset.defaultCharset());
        if (problem.getNumberOfObjectives() == 3) {
            Plotter plotter = new Scatter3D<DoubleSolution>(compartor.getSubFront(0),
                    DIRECTORY + File.separator + problem.getName() + "_nsga3");
            plotter.plot();
            // new Scatter3D(problem.getParetoOptimal3Obj(), directory + File.separator +
            // problem.getName()).plot();
        } else {
            Table table = Table.create(problem.getName() + "_f0_" + problem.getNumberOfObjectives());
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                DoubleColumn column = DoubleColumn.create("objective_" + j);
                for (int k = 0; k < compartor.getSubFront(0).size(); k++) {
                    column.append(compartor.getSubFront(0).get(k).getObjective(j).doubleValue());
                }
                table.addColumns(column);
            }
            System.out.println(table.summary());
        }

    }
}