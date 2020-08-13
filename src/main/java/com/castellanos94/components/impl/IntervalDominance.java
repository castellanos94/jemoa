package com.castellanos94.components.impl;

import java.util.ArrayList;

import com.castellanos94.datatype.Interval;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public class IntervalDominance<S extends Solution<?>> extends DominanceComparator<S> {
    protected Double alpha_ND[]; // Interval non-dominance support of solution s1 and solution s2
    protected ArrayList<S> front;

    public IntervalDominance() {
        this.alpha_ND = new Double[2];

    }

    /**
     * @param x object represent first solution
     * @param y object represent second solution
     * @return if x dominate y : -1, if y dominates x: 1 , otherwise both are
     *         non-dominated 0.
     */
    @Override
    public int compare(S x, S y) {
        this.alpha_ND[0] = -1.0;
        this.alpha_ND[1] = -1.0;

        if (x.getNumberOfPenalties() != y.getNumberOfPenalties() || x.getPenalties().compareTo(0) < 0
                || y.getPenalties().compareTo(0) < 0) {
            if (x.getNumberOfPenalties() < y.getNumberOfPenalties())
                return -1;
            if (x.getNumberOfPenalties() < y.getNumberOfPenalties())
                return 1;
            int value = x.getPenalties().compareTo(y.getPenalties());
            if (value != 0)
                return (-1) * value;
        }
        int dominateX = 0, dominateY = 0;

        Interval a, b;
        int flag = 0;
        for (int i = 0; i < x.getObjectives().size(); i++) {
            a = x.getObjective(i).toInterval();
            b = y.getObjective(i).toInterval();
            int aVSb = a.compareTo(b);
            double possibility;
            boolean isMax = x.getProblem().getObjectives_type()[i] == Problem.MAXIMIZATION;
            if (isMax) {
                possibility = a.possibility(b).doubleValue();
            } else {
                possibility = b.possibility(a).doubleValue();
            }
            if (possibility > alpha_ND[0])
                alpha_ND[0] = possibility;
            if (isMax) {
                possibility = b.possibility(a).doubleValue();
            } else {
                possibility = a.possibility(b).doubleValue();
            }
            if (possibility > alpha_ND[1])
                alpha_ND[1] = possibility;
            if (!isMax && aVSb < 0 || isMax && aVSb > 0) {
                flag = -1;
            } else if (!isMax && aVSb > 0 || isMax && aVSb < 0) {
                flag = 1;
            } else {
                flag = 0;
            }
            if (flag == -1)
                dominateX = 1;
            if (flag == 1)
                dominateY = 1;
        }
        if (dominateX == dominateY)
            return 0;
        if (dominateX == 1)
            return -1;
        return 1;
    }

    public Double[] getAlpha_ND() {
        return alpha_ND;
    }

    @Override
    public void computeRanking(ArrayList<S> population) {
        ArrayList<ArrayList<Integer>> dominate_me = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            dominate_me.add(new ArrayList<>());
        }
        for (int i = 0; i < population.size() - 1; i++) {
            for (int j = 1; j < population.size(); j++) {
                // if (i != j && !population.get(i).equals(population.get(j))) {
                int value = compare(population.get(i), population.get(j));
                if (value == -1 && !dominate_me.get(j).contains(i)) {
                    dominate_me.get(j).add(i);
                } else if (value == 1 && !dominate_me.get(i).contains(j)) {
                    dominate_me.get(i).add(j);
                }
                // }
            }
        }
        this.front = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            population.get(i).setRank(dominate_me.get(i).size());
            if (population.get(i).getRank() == 0)
                front.add(population.get(i));
        }
        // Collections.sort(population);

    }

    @Override
    public ArrayList<S> getSubFront(int index) {
        return this.front;
    }

    @Override
    public int getNumberOfSubFronts() {
        return 1;
    }
}