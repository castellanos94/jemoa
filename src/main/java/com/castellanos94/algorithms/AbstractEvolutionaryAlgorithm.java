package com.castellanos94.algorithms;

import java.util.ArrayList;

import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public abstract class AbstractEvolutionaryAlgorithm extends AbstractAlgorithm {

    public AbstractEvolutionaryAlgorithm(Problem problem) {
        super(problem);
    }

    protected int populationSize;
    protected SelectionOperator selectionOperator;
    protected CrossoverOperator crossoverOperator;
    protected MutationOperator mutationOperator;

    @Override
    public void execute() throws CloneNotSupportedException {
        init_time = System.currentTimeMillis();
        solutions = initPopulation();
        ArrayList<Solution> offspring;
        ArrayList<Solution> parents;
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

    protected ArrayList<Solution> initPopulation() {
        ArrayList<Solution> solutions = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            Solution s = problem.randomSolution();
            problem.evaluate(s);
            problem.evaluateConstraint(s);
            solutions.add(s);
        }
        return solutions;
    }

    protected abstract ArrayList<Solution> reproduction(ArrayList<Solution> parents) throws CloneNotSupportedException;

    protected abstract ArrayList<Solution> replacement(ArrayList<Solution> population, ArrayList<Solution> offspring);

    protected abstract boolean isStoppingCriteriaReached();

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public CrossoverOperator getCrossoverOperator() {
        return crossoverOperator;
    }

    public void setCrossoverOperator(CrossoverOperator crossoverOperator) {
        this.crossoverOperator = crossoverOperator;
    }

    public MutationOperator getMutationOperator() {
        return mutationOperator;
    }

    public void setMutationOperator(MutationOperator mutationOperator) {
        this.mutationOperator = mutationOperator;
    }

    @Override
    public String toString() {
        return "AbstractEvolutionaryAlgorithm [crossoverOperator=" + crossoverOperator + ", mutationOperator="
                + mutationOperator + ", populationSize=" + populationSize + ", selectionOperator=" + selectionOperator
                + "]";
    }

}