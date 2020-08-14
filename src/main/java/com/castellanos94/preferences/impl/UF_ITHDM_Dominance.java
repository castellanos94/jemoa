package com.castellanos94.preferences.impl;

import com.castellanos94.components.impl.IntervalDominance;
import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.Interval;
import com.castellanos94.datatype.RealData;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public class UF_ITHDM_Dominance<S extends Solution<?>> extends IntervalDominance<S> {
    protected RealData alpha;

    public UF_ITHDM_Dominance(RealData alpha) {
        this.alpha = alpha;
    }

    /**
     * ETA-Dominance
     * 
     * @param x object represent first solution
     * @param y object represent second solution
     * @return if x dominate y : -1, if y dominates x: 1 , otherwise both are
     *         non-dominated 0.
     */
    @Override
    public int compare(S x, S y) {
        alpha_ND[0] = -1.0;
        alpha_ND[1] = -1.0;
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

        Interval a, b;
        boolean value1_strictly_greater = false;
        boolean value2_strictly_greater = false;
        int better_a = 0;
        int better_b = 0;
        for (int i = 0; i < x.getObjectives().size(); i++) {
            a = (Interval) x.getObjective(i);
            b = (Interval) y.getObjective(i);
            boolean isMax = x.getProblem().getObjectives_type()[i] == Problem.MAXIMIZATION;
            Data possibility;
            if (isMax) {
                possibility = a.possibility(b);
            } else {
                possibility = b.possibility(a);
            }
            if (possibility.compareTo(alpha) >= 0) {
                if (!value1_strictly_greater && possibility.compareTo(0.5) > 0) {
                    value1_strictly_greater = true;
                }
                better_a += 1;
            }
            if (isMax) {
                possibility = b.possibility(a);
            } else {
                possibility = a.possibility(b);
            }
            if (possibility.compareTo(alpha) >= 0) {
                if (!value2_strictly_greater && possibility.compareTo(0.5) > 0) {
                    value2_strictly_greater = true;
                }
                better_b += 1;
            }
        }
        // x eta-dominates y
        if (value1_strictly_greater && better_a == x.getObjectives().size())
            return -1;
        // y eta-dominates x
        if (value2_strictly_greater && better_b == x.getObjectives().size())
            return -1;
        return 0;

    }

    public RealData getAlpha() {
        return alpha;
    }

    public void setAlpha(RealData alpha) {
        this.alpha = alpha;
    }
}