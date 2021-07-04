package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.Comparator;

import com.castellanos94.operators.RepairOperator;
import com.castellanos94.preferences.impl.IntervalOutrankingRelations;
import com.castellanos94.problems.GDProblem;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.ExtraInformation;
import com.castellanos94.utils.HeapSort;

public class MOGWO_O<S extends DoubleSolution> extends MOGWO<S> implements ExtraInformation {
    protected IntervalOutrankingRelations<S> preferences;
    protected HeapSort<S> heapSortSolutions;

    /**
     * Default ArchiveSelection : AdaptiveGrid
     * 
     * @see com.castellanos94.operators.impl.AdaptiveGrid
     * 
     * @param problem        mop
     * @param populationSize wolf population size
     * @param MAX_ITERATIONS max iteration
     * @param nGrid          external population size
     * @param repairOperator repair operator
     * @see MOGWO
     */
    public MOGWO_O(Problem<S> problem, int populationSize, int MAX_ITERATIONS, int nGrid,
            RepairOperator<S> repairOperator) {
        super(problem, populationSize, MAX_ITERATIONS, nGrid, repairOperator);
        this.preferences = new IntervalOutrankingRelations<>(problem.getNumberOfObjectives(),
                problem.getObjectives_type(), ((GDProblem<S>) problem).getPreferenceModel(0));
        Comparator<S> cmp = (a, b) -> {
            double netscore_a = (double) a.getAttribute(getAttributeKey());
            double netscore_b = (double) b.getAttribute(getAttributeKey());
            return Double.compare(netscore_a, netscore_b);
        };
        this.heapSortSolutions = new HeapSort<>(cmp.reversed());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void selectLeader(ArrayList<S> solutions) {
        // Filter

        calculateNetScore(solutions);
        int index = 0;
        alphaWolf = (S) solutions.get(index++).copy();
        boolean isBetaWolf = false, isDeltaWolf = false, wasCalculteNetScoreForWolves = false;
        int wolfIndex = 0;
        if (index < solutions.size()) {
            betaWolf = (S) solutions.get(index++).copy();
        } else {
            calculateNetScore(this.wolves);
            wasCalculteNetScoreForWolves = true;
            betaWolf = (S) this.wolves.get(wolfIndex++).copy();
            isBetaWolf = true;
        }
        // Select delta and remove to exclude

        if (index < solutions.size()) {
            deltaWolf = (S) solutions.get(index++).copy();
        } else {
            if (!wasCalculteNetScoreForWolves)
                calculateNetScore(this.wolves);
            deltaWolf = (S) this.wolves.get(wolfIndex++).copy();
            isDeltaWolf = true;
        }

        // add back alpha, beta and deta to the archive

        if (!this.archiveSelection.getParents().contains(alphaWolf))
            this.archiveSelection.getParents().add(alphaWolf);
        if (!this.archiveSelection.getParents().contains(betaWolf) && !isBetaWolf)
            this.archiveSelection.getParents().add(betaWolf);
        if (!this.archiveSelection.getParents().contains(deltaWolf) && !isDeltaWolf)
            this.archiveSelection.getParents().add(deltaWolf);

    }

    protected void calculateNetScore(ArrayList<S> solutions) {
        for (int i = 0; i < solutions.size(); i++) {
            double sigma_out = 0, sigma_in = 0;
            S x = solutions.get(i);
            for (int j = 0; j < solutions.size(); j++) {
                if (i != j) {
                    S y = solutions.get(j);
                    this.preferences.compare(x, y);
                    sigma_out += this.preferences.getSigmaXY().doubleValue();
                    sigma_in += this.preferences.getSigmaYX().doubleValue();
                }
            }
            x.setAttribute(getAttributeKey(), sigma_out - sigma_in);
        }
        heapSortSolutions.sort(solutions);
    }

    @Override
    public String toString() {
        return "MOGWO-O [MAX_ITERATIONS=" + MAX_ITERATIONS + ", nGrid=" + nGrid + ", Problem=" + this.problem.toString()
                + "]";
    }

    @Override
    public String getAttributeKey() {
        return "NET_SCORE_MOGWO_PFN";
    }
    @Override
    public MOGWO_O<S> copy() {
        return new MOGWO_O<>(problem, populationSize, MAX_ITERATIONS, nGrid, repairOperator);
    }
}
