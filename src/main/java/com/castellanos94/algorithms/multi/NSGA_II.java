package com.castellanos94.algorithms.multi;

import java.util.ArrayList;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.components.DensityEstimator;
import com.castellanos94.components.impl.CrowdingDistance;
import com.castellanos94.components.impl.FastNonDominatedSort;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public class NSGA_II<S extends Solution<?>> extends AbstractEvolutionaryAlgorithm<S> {
    protected int maxEvaluation;
    protected int currentEvaluation;
    protected DensityEstimator<S> densityEstimator;
    protected FastNonDominatedSort<S> fastNonDominatedSort;
    protected RepairOperator<S> repairOperator;

    public NSGA_II(Problem<S> problem, int maxEvaluation, int populationSize, SelectionOperator<S> selectionOperator,
            CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator) {
        super(problem);
        this.maxEvaluation = maxEvaluation;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.currentEvaluation = 0;
        this.populationSize = populationSize;
        this.densityEstimator = new CrowdingDistance<>();
        this.fastNonDominatedSort = new FastNonDominatedSort<>();
        this.repairOperator = new RepairOperator<>() {

            @Override
            public Void execute(S source) {
                return null;
            }

        };
    }

    public NSGA_II(Problem<S> problem, int maxEvaluation, int populationSize, SelectionOperator<S> selectionOperator,
            CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
            RepairOperator<S> repairOperator) {
        super(problem);
        this.maxEvaluation = maxEvaluation;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.currentEvaluation = 0;
        this.populationSize = populationSize;
        this.densityEstimator = new CrowdingDistance<S>();
        this.fastNonDominatedSort = new FastNonDominatedSort<S>();
        this.repairOperator = repairOperator;
    }

    public NSGA_II(Problem<S> problem, int maxEvaluation, int populationSize, SelectionOperator<S> selectionOperator,
            CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
            DensityEstimator<S> densityEstimator, RepairOperator<S> repairOperator) {
        super(problem);
        this.maxEvaluation = maxEvaluation;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.currentEvaluation = 0;
        this.populationSize = populationSize;
        this.densityEstimator = densityEstimator;
        this.fastNonDominatedSort = new FastNonDominatedSort<S>();
        this.repairOperator = repairOperator;
    }

    @Override
    protected void updateProgress() {
        currentEvaluation += populationSize;
    }

    @Override
    protected ArrayList<S> reproduction(ArrayList<S> parents) {
        ArrayList<S> offspring = new ArrayList<>();
        for (int i = 0; i < parents.size(); i++) {
            ArrayList<S> p = new ArrayList<>();
            p.add(parents.get(i++));
            p.add(parents.get((i < parents.size()) ? i : 0));
            offspring.addAll(crossoverOperator.execute(p));

        }
        for (S solution : offspring) {
            mutationOperator.execute(solution);
            repairOperator.execute(solution);
            problem.evaluate(solution);
            problem.evaluateConstraint(solution);
        }
        return offspring;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ArrayList<S> replacement(ArrayList<S> population, ArrayList<S> offspring) {
        ArrayList<S> Pt = new ArrayList<>();
        population.addAll(offspring);
        fastNonDominatedSort.computeRanking(population);
        int index = 0, frontIndex = 0;
        while (index < populationSize && frontIndex < fastNonDominatedSort.getNumberOfSubFronts()) {
            ArrayList<S> front = fastNonDominatedSort.getSubFront(frontIndex++);
            if (front.size() + index < populationSize) {
                for (int i = 0; i < front.size(); i++, index++) {
                    Pt.add((S) front.get(i).copy());
                }

            } else {
                densityEstimator.compute(front);
                front = densityEstimator.sort(front);
                for (int i = 0; i < front.size() && index < populationSize; i++, index++) {
                    Pt.add((S) front.get(i).copy());
                }
            }
        }
        return Pt;
    }

    @Override
    protected boolean isStoppingCriteriaReached() {
        return currentEvaluation < maxEvaluation;
    }

    @Override
    public NSGA_II<S> copy() {
        return new NSGA_II<>(problem, maxEvaluation, populationSize, selectionOperator, crossoverOperator,
                mutationOperator, densityEstimator, repairOperator);
    }

}