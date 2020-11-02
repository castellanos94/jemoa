package com.castellanos94.algorithms;

import java.util.ArrayList;

import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public abstract class AbstractEvolutionaryAlgorithm<S extends Solution<?>> extends AbstractAlgorithm<S> {

    public AbstractEvolutionaryAlgorithm(Problem<S> problem) {
        super(problem);
    }

    protected int populationSize;
    protected SelectionOperator<S> selectionOperator;
    protected CrossoverOperator<S> crossoverOperator;
    protected MutationOperator<S> mutationOperator;

    @Override
    public void execute() {
        init_time = System.currentTimeMillis();
        solutions = initPopulation();
        ArrayList<S> offspring;
        ArrayList<S> parents;
        updateProgress();
        while (isStoppingCriteriaReached()) {
            selectionOperator.execute(solutions);
            parents = selectionOperator.getParents();
            offspring = reproduction(parents);
            solutions = replacement(solutions, offspring);
            updateProgress();
        }
        computeTime = System.currentTimeMillis() - init_time;
    }

    protected abstract void updateProgress();

    protected ArrayList<S> initPopulation() {
        ArrayList<S> solutions = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            S s = problem.randomSolution();
            problem.evaluate(s);
            problem.evaluateConstraint(s);
            solutions.add(s);
        }
        return solutions;
    }

    protected abstract ArrayList<S> reproduction(ArrayList<S> parents);

    protected abstract ArrayList<S> replacement(ArrayList<S> population, ArrayList<S> offspring);

    protected abstract boolean isStoppingCriteriaReached();

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public CrossoverOperator<S> getCrossoverOperator() {
        return crossoverOperator;
    }

    public void setCrossoverOperator(CrossoverOperator<S> crossoverOperator) {
        this.crossoverOperator = crossoverOperator;
    }

    public MutationOperator<S> getMutationOperator() {
        return mutationOperator;
    }

    public void setMutationOperator(MutationOperator<S> mutationOperator) {
        this.mutationOperator = mutationOperator;
    }

    @Override
    public String toString() {
        return "AbstractEvolutionaryAlgorithm [crossoverOperator=" + crossoverOperator + ", mutationOperator="
                + mutationOperator + ", populationSize=" + populationSize + ", selectionOperator=" + selectionOperator
                + "]";
    }

}