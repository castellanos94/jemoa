package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.castellanos94.algorithms.AbstractEvolutionaryAlgorithm;
import com.castellanos94.components.Ranking;
import com.castellanos94.components.impl.FastNonDominatedSort;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.operators.impl.EnvironmentalSelection;
import com.castellanos94.operators.impl.NSGA3Selection;
import com.castellanos94.operators.impl.RepairRandomBoundary;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.ReferenceHyperplane;
import com.castellanos94.utils.ReferencePoint;

public class NSGA_III extends AbstractEvolutionaryAlgorithm {
    protected int maxIterations;
    protected int currenIteration;
    protected int numberOfDivisions;
    // protected ArrayList<ReferencePoint> referencePoints = new ArrayList<>();
    protected ReferenceHyperplane referenceHyperplane;
    protected Ranking ranking;
    protected RepairOperator repair;

    @Override
    protected void updateProgress() {
        currenIteration++;
    }

    @Override
    protected ArrayList<Solution> reproduction(ArrayList<Solution> parents) throws CloneNotSupportedException {
        ArrayList<Solution> offspring = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            ArrayList<Solution> p = new ArrayList<>();
            p.add(parents.get(i++));
            p.add(parents.get((i < parents.size()) ? i : 0));
            offspring.addAll(crossoverOperator.execute(p));

        }
        for (Solution solution : offspring) {
            mutationOperator.execute(solution);
            repair.repair(solution);
            problem.evaluate(solution);
            problem.evaluateConstraints(solution);
        }
        return offspring;
    }

    @Override
    protected ArrayList<Solution> replacement(ArrayList<Solution> population, ArrayList<Solution> offspring) {
        ArrayList<Solution> Rt = new ArrayList<>(population);
        Rt.addAll(offspring);
        Rt = new ArrayList<>(Rt.stream().distinct().collect(Collectors.toList()));
        while (Rt.size() < populationSize) {
            Solution r = problem.randomSolution();
            problem.evaluate(r);
            problem.evaluateConstraints(r);
            Rt.add(r);
        }
        ranking.computeRanking(Rt);
        ArrayList<Solution> Pt = new ArrayList<>();
        int indexFront = 0;
        List<List<Solution>> fronts = new ArrayList<>();
        for (; indexFront < ranking.getNumberOfSubFronts(); indexFront++) {
            fronts.add(ranking.getSubFront(indexFront));
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
        try {
            NSGA3Selection nsga3Selection = new NSGA3Selection(Rt, populationSize - Pt.size(),referenceHyperplane, populationSize, indexFront);
            nsga3Selection.execute(Pt);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
       /* EnvironmentalSelection selection = new EnvironmentalSelection(fronts, getPopulationSize(),
                getReferencePointsCopy(), getProblem().getNumberOfObjectives());
        selection.execute(Pt);*/
        //return selection.getParents();
        return Pt;
    }

    @Override
    protected boolean isStoppingCriteriaReached() {
        return currenIteration < maxIterations;
    }

    public NSGA_III(Problem problem, int populationSize, int maxIterations, int numberOfDivisions,
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
        this.repair = new RepairRandomBoundary();

        /*
         * (new ReferencePoint()).generateReferencePoints(referencePoints,
         * getProblem().getNumberOfObjectives(), numberOfDivisions);
         */
        referenceHyperplane = new ReferenceHyperplane(problem.getNumberOfObjectives(), numberOfDivisions);
        referenceHyperplane.execute();
       // int populationRSize = referenceHyperplane.getNumberOfReferencePoints();

       // setPopulationSize(populationRSize);
       // selectionOperator.setPopulaitonSize(populationRSize);

    }

    public NSGA_III(Problem problem, int populationSize, int maxIterations, int numberOfDivisions,
            SelectionOperator selectionOperator, CrossoverOperator crossoverOperator, MutationOperator mutationOperator,
            Ranking ranking, RepairOperator repairOperator, ReferenceHyperplane referenceHyperplane) {
        super(problem);
        this.maxIterations = maxIterations;
        this.numberOfDivisions = numberOfDivisions;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.populationSize = populationSize;
        this.ranking = ranking;
        this.repair = repairOperator;

        /*
         * (new ReferencePoint()).generateReferencePoints(referencePoints,
         * getProblem().getNumberOfObjectives(), numberOfDivisions);
         */
        this.referenceHyperplane = referenceHyperplane;
    

       // setPopulationSize(populationRSize);
       // selectionOperator.setPopulaitonSize(populationRSize);

    }

    @Deprecated
    private List<ReferencePoint> getReferencePointsCopy() {
        List<ReferencePoint> copy = new ArrayList<>();
        /*
         * for (ReferencePoint r : this.referencePoints) { copy.add(new
         * ReferencePoint(r)); }
         */
        return copy;
    }
    public void setReferenceHyperplane(ReferenceHyperplane referenceHyperplane) {
        this.referenceHyperplane = referenceHyperplane;
    }
    public int getNumberOfDivisions() {
        return numberOfDivisions;
    }
    @Override
    public String toString() {
        return "NSGAIII [pop_size=" + populationSize + ", maxIterations=" + maxIterations + ", numberOfDivisions="
                + numberOfDivisions + ", ranking=" + ranking + ", referencePoints="
                + referenceHyperplane.getNumberOfReferencePoints() + "]";
    }

}