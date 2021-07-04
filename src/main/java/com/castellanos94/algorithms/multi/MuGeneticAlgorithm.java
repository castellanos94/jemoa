package com.castellanos94.algorithms.multi;

import java.util.ArrayList;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public class MuGeneticAlgorithm<S extends Solution<?>> extends AbstractEvolutionaryAlgorithm<S> {
    protected int maxIteration;
    protected int currentIteration;
    protected Ranking<S> ranking;

    public MuGeneticAlgorithm(Problem<S> problem, int maxIteration, int populationSize,
            SelectionOperator<S> selectionOperator, CrossoverOperator<S> crossoverOperator,
            MutationOperator<S> mutationOperator) {
        super(problem);
        this.maxIteration = maxIteration;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.currentIteration = 0;
        this.populationSize = populationSize;
        this.ranking = new DominanceComparator<>();
    }

    @Override
    protected void updateProgress() {
        currentIteration++;
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
        for (S s : offspring) {
            mutationOperator.execute(s);
            problem.evaluate(s);
            problem.evaluateConstraint(s);
        }
        return offspring;
    }

    @Override
    protected ArrayList<S> replacement(ArrayList<S> population, ArrayList<S> offspring) {
        population.addAll(offspring);
        ranking.computeRanking(population);

        if (ranking.getSubFront(0).size() >= populationSize)
            return new ArrayList<>(ranking.getSubFront(0).subList(0, populationSize));
        ArrayList<S> r = new ArrayList<>();
        r.addAll(ranking.getSubFront(0));
        for (int i = 0; i < ranking.getSubFront(1).size() && r.size() < populationSize; i++) {
            r.add(ranking.getSubFront(1).get(i));
        }
        return r;
    }

    @Override
    protected boolean isStoppingCriteriaReached() {
        return currentIteration < maxIteration;
    }

    @Override
    public String toString() {
        return "GeneticAlgorithm [maxIteration=" + maxIteration + "]";
    }

    @Override
    public MuGeneticAlgorithm<S> copy() {
        return new MuGeneticAlgorithm<>(problem, maxIteration, populationSize, selectionOperator, crossoverOperator,
                mutationOperator);
    }

}