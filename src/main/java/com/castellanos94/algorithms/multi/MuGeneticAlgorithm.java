package com.castellanos94.algorithms.multi;

import java.util.ArrayList;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.DominanceCompartor;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public class MuGeneticAlgorithm extends AbstractEvolutionaryAlgorithm {
    protected int maxIteration;
    protected int currentIteration;
    protected Ranking ranking;

    public MuGeneticAlgorithm(Problem problem, int maxIteration, int populationSize,
            SelectionOperator selectionOperator, CrossoverOperator crossoverOperator,
            MutationOperator mutationOperator) {
        super(problem);
        this.maxIteration = maxIteration;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.currentIteration = 0;
        this.populationSize = populationSize;
        this.ranking = new DominanceCompartor();
    }

    @Override
    protected void updateProgress() {
        currentIteration++;
    }

    @Override
    protected ArrayList<Solution> reproduction(ArrayList<Solution> parents) throws CloneNotSupportedException {
        ArrayList<Solution> offspring = new ArrayList<>();
        for (int i = 0; i < parents.size(); i++) {
            ArrayList<Solution> p = new ArrayList<>();
            p.add(parents.get(i++));
            p.add(parents.get(i));
            offspring.addAll(crossoverOperator.execute(p));

        }
        for (Solution solution : offspring) {
            mutationOperator.execute(solution);
            problem.evaluate(solution);
            problem.evaluateConstraints(solution);
        }
        return offspring;
    }

    @Override
    protected ArrayList<Solution> replacement(ArrayList<Solution> population, ArrayList<Solution> offspring) {
        population.addAll(offspring);
        ranking.computeRanking(population);
        return new ArrayList<>(ranking.getSubFront(0).subList(0, populationSize));
    }

    @Override
    protected boolean isStoppingCriteriaReached() {
        return currentIteration < maxIteration;
    }

    @Override
    public String toString() {
        return "GeneticAlgorithm [maxIteration=" + maxIteration + "]";
    }

}