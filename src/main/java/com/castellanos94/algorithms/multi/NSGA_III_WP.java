package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.preferences.impl.InterClassnC;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.NSGA3Replacement;

/**
 * Explorar entre 5 20 50 aplicar clasificador
 */
public class NSGA_III_WP<S extends Solution<?>> extends NSGA_III<S> {
    private boolean csatPlus;
    private int resetAt;
    private int n = 1;

    public NSGA_III_WP(Problem<S> problem, int populationSize, int maxIterations, int numberOfDivisions,
            SelectionOperator<S> selectionOperator, CrossoverOperator<S> crossoverOperator,
            MutationOperator<S> mutationOperator) {
        super(problem, populationSize, maxIterations, numberOfDivisions, selectionOperator, crossoverOperator,
                mutationOperator);
        this.resetAt = this.maxIterations / 10;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ArrayList<S> replacement(ArrayList<S> population, ArrayList<S> offspring) {
        ArrayList<S> Rt = new ArrayList<>(population);
        Rt.addAll(offspring);
        Rt = new ArrayList<>(Rt.stream().distinct().collect(Collectors.toList()));
        while (Rt.size() < populationSize) {
            S r = problem.randomSolution();
            problem.evaluate(r);
            problem.evaluateConstraint(r);
            Rt.add(r);
        }
        ranking.computeRanking(Rt);
        InterClassnC<S> classifier = new InterClassnC<>(problem);
        ArrayList<ArrayList<S>> _fronts = new ArrayList<>();
        if (ranking.getNumberOfSubFronts() > 0) {
            ArrayList<S> hs = new ArrayList<>();
            ArrayList<S> s = new ArrayList<>();
            ArrayList<S> d = new ArrayList<>();
            ArrayList<S> hd = new ArrayList<>();
            for (S x : ranking.getSubFront(0)) {
                classifier.classify(x);
                int[] iclass = (int[]) x.getAttribute(classifier.getAttributeKey());
                if (iclass[0] > 0) {
                    hs.add(x);
                } else if (iclass[1] > 0) {
                    s.add(x);
                } else if (iclass[2] > 0) {
                    d.add(x);
                } else {
                    hd.add(x);
                }
            }
            if (!hs.isEmpty()) {
                _fronts.add(hs);
            }
            if (!s.isEmpty()) {
                _fronts.add(s);
            }
            if (!d.isEmpty()) {
                _fronts.add(d);
            }
            if (!hd.isEmpty()) {
                _fronts.add(hd);
            }
            for (int i = 1; i < ranking.getNumberOfSubFronts(); i++) {
                _fronts.add(ranking.getSubFront(i));
            }
            csatPlus = hs.size() + s.size() <= this.populationSize / 10;
        }
        ArrayList<S> Pt = new ArrayList<>();
        int indexFront = 0;
        ArrayList<ArrayList<S>> fronts = new ArrayList<>();
        for (; indexFront < _fronts.size(); indexFront++) {
            fronts.add(_fronts.get(indexFront));
            if (Pt.size() + _fronts.get(indexFront).size() <= populationSize) {
                for (S solution : _fronts.get(indexFront)) {
                    Pt.add((S) solution.copy());
                }
            } else {
                break;
            }
        }
        if (Pt.size() == populationSize)
            return Pt;

        NSGA3Replacement<S> selection = new NSGA3Replacement<>(fronts, referenceHyperplane.copy(),
                problem.getNumberOfObjectives(), populationSize);
        selection.execute(Pt);
        ArrayList<S> parents = selection.getParents();
        if (this.currenIteration > 0 && this.currenIteration % resetAt == 0) {
            // if (csatPlus) {
            // System.out.printf("\tCurrent iteration %4d - reset at %3d n %3d \n",
            // this.currenIteration,
            // parents.size() - n * this.populationSize / 10, n * this.populationSize / 10);
            for (int i = parents.size() - n * this.populationSize / 10; i < parents.size(); i++) {
                S randomSolution = this.problem.randomSolution();
                this.problem.evaluate(randomSolution);
                this.problem.evaluateConstraint(randomSolution);
                parents.set(i, randomSolution);
            }
        }
        return parents;

    }

}
