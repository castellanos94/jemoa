package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.FastNonDominatedSort;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.NSGA3Replacement;
import com.castellanos94.utils.ReferenceHyperplane;

public class NSGA_III<S extends Solution<?>> extends AbstractEvolutionaryAlgorithm<S> {
    protected int maxIterations;
    protected int currenIteration;
    protected int numberOfDivisions;
    // protected ArrayList<ReferencePoint> referencePoints = new ArrayList<>();
    protected ReferenceHyperplane<S> referenceHyperplane;
    protected Ranking<S> ranking;

    @Override
    protected void updateProgress() {
        currenIteration++;
    }

    @Override
    protected ArrayList<S> reproduction(ArrayList<S> parents) {
        ArrayList<S> offspring = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            ArrayList<S> p = new ArrayList<>();
            p.add(parents.get(i++));
            p.add(parents.get((i < parents.size()) ? i : 0));
            offspring.addAll(crossoverOperator.execute(p));

        }
        for (int i = 0; i < offspring.size(); i++) {
            offspring.set(i, mutationOperator.execute(offspring.get(i)));
            problem.evaluate(offspring.get(i));
            problem.evaluateConstraint(offspring.get(i));
        }
        return offspring;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ArrayList<S> replacement(ArrayList<S> population, ArrayList<S> offspring) {
        ArrayList<S> Rt = new ArrayList<>(population);
        Rt.addAll(offspring);
        Rt = new ArrayList<>(Rt.stream().distinct().collect(Collectors.toList()));
        while (Rt.size() < populationSize) {
            S r = problem.randomSolution();
            problem.evaluate(r);
            problem.evaluateConstraint(r);
            Rt.add(r);
        }
        ranking.computeRanking(Rt);
        ArrayList<S> Pt = new ArrayList<>();
        int indexFront = 0;
        ArrayList<ArrayList<S>> fronts = new ArrayList<>();
        for (; indexFront < ranking.getNumberOfSubFronts(); indexFront++) {
            fronts.add(ranking.getSubFront(indexFront));
            if (Pt.size() + ranking.getSubFront(indexFront).size() <= populationSize) {
                for (S solution : ranking.getSubFront(indexFront)) {
                    Pt.add((S) solution.copy());
                }
            } else {
                break;
            }
        }
        if (Pt.size() == populationSize)
            return Pt;

        NSGA3Replacement<S> selection = new NSGA3Replacement<>(fronts, referenceHyperplane.copy(),
                problem.getNumberOfObjectives(), populationSize);
        selection.execute(Pt);
        return selection.getParents();
    }

    @Override
    protected boolean isStoppingCriteriaReached() {
        return currenIteration < maxIterations;
    }

    public NSGA_III(Problem<S> problem, int populationSize, int maxIterations, int numberOfDivisions,
            SelectionOperator<S> selectionOperator, CrossoverOperator<S> crossoverOperator,
            MutationOperator<S> mutationOperator) {
        super(problem);
        this.maxIterations = maxIterations;
        this.numberOfDivisions = numberOfDivisions;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.populationSize = populationSize;
        this.ranking = new FastNonDominatedSort<S>();

        /*
         * (new ReferencePoint()).generateReferencePoints(referencePoints,
         * getProblem().getNumberOfObjectives(), numberOfDivisions);
         */
        referenceHyperplane = new ReferenceHyperplane<S>(problem.getNumberOfObjectives(), numberOfDivisions);
        referenceHyperplane.execute();
        // System.out.println(referenceHyperplane.getNumberOfPoints());
        // int populationRSize = referenceHyperplane.getNumberOfPoints();

        // setPopulationSize(populationRSize);
        // selectionOperator.setPopulaitonSize(populationRSize);

    }

    public NSGA_III(Problem<S> problem, int populationSize, int maxIterations, SelectionOperator<S> selectionOperator,
            CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator, Ranking<S> ranking,
            ReferenceHyperplane<S> referenceHyperplane) {
        super(problem);
        this.maxIterations = maxIterations;
        this.numberOfDivisions = referenceHyperplane.getNumberOfPoints();
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.populationSize = populationSize;
        this.ranking = ranking;
        this.referenceHyperplane = referenceHyperplane;

    }

    public void setReferenceHyperplane(ReferenceHyperplane<S> referenceHyperplane) {
        this.referenceHyperplane = referenceHyperplane;
    }

    public int getNumberOfDivisions() {
        return numberOfDivisions;
    }

    @Override
    public String toString() {
        return "NSGAIII [pop_size=" + populationSize + ", maxIterations=" + maxIterations + ", numberOfDivisions="
                + numberOfDivisions + ", ranking=" + ranking + ", referencePoints="
                + referenceHyperplane.getNumberOfPoints() + "]";
    }

    @Override
    public NSGA_III<S> copy() {
        return new NSGA_III<>(problem, populationSize, maxIterations, selectionOperator, crossoverOperator,
                mutationOperator, ranking, referenceHyperplane);
    }
}