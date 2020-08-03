package com.castellanos94.components.impl;

import java.util.Comparator;

import com.castellanos94.datatype.Interval;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public class IntervalDominance<S extends Solution> implements Comparator<Solution> {
    protected Double alpha_ND[]; // Interval non-dominance support of solution s1 and solution s2

    public IntervalDominance() {
        this.alpha_ND = new Double[2];

    }
    /**
     * @param x object represent first solution
     * @param y object represent second solution
     * @return if x dominate y : -1, if y dominates x:  1 , otherwise both are non-dominated 0.
     */
    @Override
    public int compare(Solution x, Solution y) {
        this.alpha_ND[0] = -1.0;
        this.alpha_ND[1] = -1.0;

        if (x.getN_penalties() != y.getN_penalties() || x.getPenalties().compareTo(0) < 0
                || y.getPenalties().compareTo(0) < 0) {
            if (x.getN_penalties() < y.getN_penalties())
                return -1;
            if (x.getN_penalties() < y.getN_penalties())
                return 1;
            int value = x.getPenalties().compareTo(y.getPenalties());
            if (value != 0)
                return (-1) * value;
        }
        int dominateX = 0, dominateY = 0;

        Interval a, b;
        int flag = 0;
        for (int i = 0; i < x.getObjectives().size(); i++) {
            a = (Interval) x.getObjective(i);
            b = (Interval) y.getObjective(i);
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
    

}