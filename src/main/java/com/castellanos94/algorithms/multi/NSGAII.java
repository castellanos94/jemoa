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

public class NSGAII extends AbstractEvolutionaryAlgorithm {
    protected int maxEvaluation;
    protected int currentEvaluation;
    protected DensityEstimator densityEstimator;
    protected FastNonDominatedSort fastNonDominatedSort;
    protected RepairOperator repairOperator;

    public NSGAII(Problem problem, int maxEvaluation, int populationSize, SelectionOperator selectionOperator,
            CrossoverOperator crossoverOperator, MutationOperator mutationOperator) {
        super(problem);
        this.maxEvaluation = maxEvaluation;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.currentEvaluation = 0;
        this.populationSize = populationSize;
        this.densityEstimator = new CrowdingDistance();
        this.fastNonDominatedSort = new FastNonDominatedSort();
        this.repairOperator = new RepairOperator() {

            @Override
            public void repair(Solution solution) {
                // Nothing improvement
            }

        };
    }

    public NSGAII(Problem problem, int maxEvaluation, int populationSize, SelectionOperator selectionOperator,
            CrossoverOperator crossoverOperator, MutationOperator mutationOperator, RepairOperator repairOperator) {
        super(problem);
        this.maxEvaluation = maxEvaluation;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.currentEvaluation = 0;
        this.populationSize = populationSize;
        this.densityEstimator = new CrowdingDistance();
        this.fastNonDominatedSort = new FastNonDominatedSort();
        this.repairOperator = repairOperator;
    }

    public NSGAII(Problem problem, int maxEvaluation, int populationSize, SelectionOperator selectionOperator,
            CrossoverOperator crossoverOperator, MutationOperator mutationOperator, DensityEstimator densityEstimator,
            RepairOperator repairOperator) {
        super(problem);
        this.maxEvaluation = maxEvaluation;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.currentEvaluation = 0;
        this.populationSize = populationSize;
        this.densityEstimator = densityEstimator;
        this.fastNonDominatedSort = new FastNonDominatedSort();
        this.repairOperator = repairOperator;
    }

    @Override
    protected void updateProgress() {
        currentEvaluation += populationSize;
    }

    @Override
    protected ArrayList<Solution> reproduction(ArrayList<Solution> parents) throws CloneNotSupportedException {
        ArrayList<Solution> offspring = new ArrayList<>();
        for (int i = 0; i < parents.size(); i++) {
            ArrayList<Solution> p = new ArrayList<>();
            p.add(parents.get(i++));
            p.add(parents.get((i < parents.size()) ? i : 0));
            offspring.addAll(crossoverOperator.execute(p));

        }
        for (Solution solution : offspring) {
            mutationOperator.execute(solution);
            repairOperator.repair(solution);
            problem.evaluate(solution);
            problem.evaluateConstraints(solution);
        }
        return offspring;
    }

    @Override
    protected ArrayList<Solution> replacement(ArrayList<Solution> population, ArrayList<Solution> offspring) {
        ArrayList<Solution> Pt = new ArrayList<>();
        population.addAll(offspring);
        fastNonDominatedSort.computeRanking(population);
        int index = 0, frontIndex = 0;
        while (index < populationSize && frontIndex < fastNonDominatedSort.getNumberOfSubFronts()) {
            ArrayList<Solution> front = fastNonDominatedSort.getSubFront(frontIndex++);
            if (front.size() + index < populationSize) {
                for (int i = 0; i < front.size(); i++) {
                    try {
                        Pt.add((Solution) front.get(i).clone());
                        index++;
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                densityEstimator.compute(front);
                front = densityEstimator.sort(front);
                for (int i = 0; i < front.size() && index < populationSize; i++) {
                    try {
                        Pt.add((Solution) front.get(i).clone());
                        index++;
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return Pt;
    }

    @Override
    protected boolean isStoppingCriteriaReached() {
        return currentEvaluation < maxEvaluation;
    }

}