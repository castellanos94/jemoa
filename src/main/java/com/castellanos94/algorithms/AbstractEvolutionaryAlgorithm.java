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

    protected int maxIteration;
    protected int populationSize;
    protected int currentIteration;
    protected SelectionOperator selectionOperator;
    protected CrossoverOperator crossoverOperator;
    protected MutationOperator mutationOperator;
    

    @Override
    public void execute() {
        init_time = System.currentTimeMillis();
        solutions = initPopulation();
        evaluation(solutions);
        ArrayList<Solution> offspring;
        ArrayList<Solution> parents;
        while (isStoppingCriteriaReached()) {
            selectionOperator.execute(solutions);
            parents = selectionOperator.getParents();
            offspring = reproduction(parents);
            evaluation(offspring);
            solutions = replacement(solutions, offspring);
            updateProgress();
        }
        computeTime = System.currentTimeMillis() - init_time;
    }

    protected ArrayList<Solution> initPopulation() {
        ArrayList<Solution> solutions = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            solutions.add(problem.randomSolution());
        }
        return solutions;
    }

    protected void evaluation(ArrayList<Solution> population) {
        for (Solution solution : population) {
            problem.evaluate(solution);
            problem.evaluateConstraints(solution);
        }
    }

    protected abstract ArrayList<Solution> reproduction(ArrayList<Solution> parents);

    protected abstract ArrayList<Solution> replacement(ArrayList<Solution> population, ArrayList<Solution> offspring);

    protected boolean isStoppingCriteriaReached() {
        return currentIteration < maxIteration;
    }

    protected void updateProgress() {
        currentIteration++;
    }
    
}