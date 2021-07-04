package com.castellanos94.algorithms.single;

import java.util.ArrayList;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public class GeneticAlgorithm<S extends Solution<?>> extends AbstractEvolutionaryAlgorithm<S> {
    protected int maxIteration;
    protected int currentIteration;

    public GeneticAlgorithm(Problem<S> problem, int maxIteration, int popSize, SelectionOperator<S> selectionOperator,
            CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator) {
        super(problem);
        this.maxIteration = maxIteration;
        this.populationSize = popSize;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.currentIteration = 0;
    }

    @Override
    public GeneticAlgorithm<S> copy() {
        return new GeneticAlgorithm<>(problem, maxIteration, populationSize, selectionOperator, crossoverOperator,
                mutationOperator);
    }

    @Override
    protected ArrayList<S> reproduction(ArrayList<S> parents) {
        ArrayList<S> offspring = new ArrayList<>();
        for (int i = 0; i < parents.size(); i++) {
            ArrayList<S> p = new ArrayList<>();
            p.add(parents.get(i++));
            p.add(parents.get(i));
            offspring.addAll(crossoverOperator.execute(p));

        }
        for (S solution : offspring) {
            mutationOperator.execute(solution);
            problem.evaluate(solution);
            problem.evaluateConstraint(solution);
        }
        return offspring;
    }

    @Override
    protected ArrayList<S> replacement(ArrayList<S> population, ArrayList<S> offspring) {
        // ArrayList<Solution> newPop = new ArrayList<>();
        // newPop.addAll((Collection<? extends Solution>) population.clone());
        for (int i = 0; i < offspring.size(); i++) {
            S a = offspring.get(i);
            for (int j = 0; j < population.size(); j++) {
                int value = a.getObjectives().get(0).compareTo(population.get(j).getObjectives().get(0));
                int penal = a.getNumberOfPenalties().compareTo(population.get(j).getNumberOfPenalties());
                if (Problem.MAXIMIZATION == problem.getObjectives_type()[0]) {
                    if (value == 1 && penal < 0) {
                        population.set(j, a);
                    }
                } else {
                    if (value == -1 && penal < 0) {
                        population.set(j, a);
                    }
                }
            }
        }
        return population;
    }

    @Override
    public String toString() {
        return "GeneticAlgorithm [" + super.toString() + "]";
    }

    @Override
    protected void updateProgress() {
        currentIteration++;
    }

    @Override
    protected boolean isStoppingCriteriaReached() {
        return currentIteration < maxIteration;
    }

    public int getMaxIteration() {
        return maxIteration;
    }

    public void setMaxIteration(int maxIteration) {
        this.maxIteration = maxIteration;
    }

}