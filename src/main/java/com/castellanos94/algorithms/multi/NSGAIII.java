package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.FastNonDominatedSort;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.operators.impl.EnvironmentalSelection;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.ReferencePoint;

import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer.PopulationSize;

public class NSGAIII extends AbstractEvolutionaryAlgorithm {
    protected int maxIterations;
    protected int currenIteration;
    protected int numberOfDivisions;
    protected ArrayList<ReferencePoint> referencePoints = new ArrayList<>();
    protected Ranking ranking;

    @Override
    protected void updateProgress() {
        currenIteration++;
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
            problem.evaluate(solution);
            problem.evaluateConstraints(solution);
        }
        return offspring;
    }

    @Override
    protected ArrayList<Solution> replacement(ArrayList<Solution> population, ArrayList<Solution> offspring) {
        ArrayList<Solution> Rt = new ArrayList<>(population);
        Rt.addAll(offspring);
        ranking.computeRanking(Rt);
        ArrayList<Solution> Pt = new ArrayList<>();
        int indexFront = 0;
        for (; indexFront < ranking.getNumberOfSubFronts(); indexFront++) {
            if (Pt.size() + ranking.getSubFront(indexFront).size() <= populationSize) {
                for (Solution solution : ranking.getSubFront(indexFront)) {
                    try {
                        Pt.add((Solution) solution.clone());
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                break;
            }
        }
        if (Pt.size() == populationSize)
            return Pt;
        List<List<Solution>> fronts = new ArrayList<>();
        for (int i = 0; i < indexFront; i++) {
            fronts.addAll((Collection<? extends ArrayList<Solution>>) ranking.getSubFront(i).clone());
        }
        EnvironmentalSelection selection = new EnvironmentalSelection(fronts, getPopulationSize(),
                getReferencePointsCopy(), getProblem().getNumberOfObjectives());
        return selection.getParents();
    }

    @Override
    protected boolean isStoppingCriteriaReached() {
        return currenIteration < maxIterations;
    }

    public NSGAIII(Problem problem, int populationSize, int maxIterations, int numberOfDivisions,
            SelectionOperator selectionOperator, CrossoverOperator crossoverOperator,
            MutationOperator mutationOperator) {
        super(problem);
        this.maxIterations = maxIterations;
        this.numberOfDivisions = numberOfDivisions;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.populationSize = populationSize;
        this.ranking = new FastNonDominatedSort();

        (new ReferencePoint()).generateReferencePoints(referencePoints, getProblem().getNumberOfObjectives(),
                numberOfDivisions);

        int populationRSize = referencePoints.size();
        while (populationRSize % 4 > 0) {
            populationRSize++;
        }

        setPopulationSize(populationRSize);
    }

    public NSGAIII(Problem problem, int populationSize, int maxIterations, int numberOfDivisions,
            SelectionOperator selectionOperator, CrossoverOperator crossoverOperator, MutationOperator mutationOperator,
            Ranking ranking) {
        super(problem);
        this.maxIterations = maxIterations;
        this.numberOfDivisions = numberOfDivisions;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.populationSize = populationSize;
        this.ranking = ranking;

        (new ReferencePoint()).generateReferencePoints(referencePoints, getProblem().getNumberOfObjectives(),
                numberOfDivisions);

        int populationRSize = referencePoints.size();
        while (populationRSize % 4 > 0) {
            populationRSize++;
        }

        setPopulationSize(populationRSize);
    }

    private List<ReferencePoint> getReferencePointsCopy() {
        List<ReferencePoint> copy = new ArrayList<>();
        for (ReferencePoint r : this.referencePoints) {
            copy.add(new ReferencePoint(r));
        }
        return copy;
    }

    @Override
    public String toString() {
        return "NSGAIII [currenIteration=" + currenIteration + ", maxIterations=" + maxIterations
                + ", numberOfDivisions=" + numberOfDivisions + ", ranking=" + ranking + ", referencePoints="
                + referencePoints.size() + "]";
    }

}