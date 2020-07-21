package com.castellanos94.algorithms.multi;

import java.util.ArrayList;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.datatype.Data;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.ReferencePoint;

public class NSGAIII extends AbstractEvolutionaryAlgorithm {
    protected int maxEvalution;
    protected int currentEvaluation;
    protected int numberOfDivisions;
    protected ArrayList<ReferencePoint> referencePoints = new ArrayList<>();

    @Override
    protected void updateProgress() {
        currentEvaluation += populationSize;
    }

    @Override
    protected ArrayList<Solution> reproduction(ArrayList<Solution> parents) throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ArrayList<Solution> replacement(ArrayList<Solution> population, ArrayList<Solution> offspring) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean isStoppingCriteriaReached() {
        return currentEvaluation < maxEvalution;
    }

    public NSGAIII(Problem problem, int populationSize, int maxEvalution, int numberOfDivisions,
            CrossoverOperator crossoverOperator, MutationOperator mutationOperator) {
        super(problem);
        this.maxEvalution = maxEvalution;
        this.numberOfDivisions = numberOfDivisions;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.populationSize = populationSize;

        (new ReferencePoint()).generateReferencePoints(referencePoints, getProblem().getNumberOfObjectives(),
                numberOfDivisions);
    }

}