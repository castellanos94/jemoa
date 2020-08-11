package com.castellanos94.algorithms.single;

import java.util.ArrayList;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public class GeneticAlgorithm extends AbstractEvolutionaryAlgorithm {
    protected int maxIteration;
    protected int currentIteration;

    public GeneticAlgorithm(Problem problem, int maxIteration, int popSize, SelectionOperator selectionOperator,
            CrossoverOperator crossoverOperator, MutationOperator mutationOperator) {
        super(problem);
        this.maxIteration = maxIteration;
        this.populationSize = popSize;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.currentIteration = 0;
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
            problem.evaluateConstraint(solution);
        }
        return offspring;
    }

    @Override
    protected ArrayList<Solution> replacement(ArrayList<Solution> population, ArrayList<Solution> offspring) {
        // ArrayList<Solution> newPop = new ArrayList<>();
        // newPop.addAll((Collection<? extends Solution>) population.clone());
        for (int i = 0; i < offspring.size(); i++) {
            Solution a = offspring.get(i);
            for (int j = 0; j < population.size(); j++) {
                int value = a.getObjectives().get(0).compareTo(population.get(j).getObjectives().get(0));
                int penal = a.getN_penalties().compareTo(population.get(j).getN_penalties());
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