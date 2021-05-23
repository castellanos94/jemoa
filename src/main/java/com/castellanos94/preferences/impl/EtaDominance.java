package com.castellanos94.preferences.impl;

import com.castellanos94.components.impl.IntervalDominance;
import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.Interval;
import com.castellanos94.datatype.RealData;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.BinarySolution;
import com.castellanos94.solutions.Solution;

/**
 * Eta dominance xD(α)y, used at IntervalOutRankingRelations
 * 
 * @see IntervalOutrankingRelations
 */
public class EtaDominance<S extends Solution<?>> extends IntervalDominance<S> {
    protected RealData alpha;

    public EtaDominance(RealData alpha) {
        this.alpha = alpha;
    }

    /**
     * xD(α)y
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
        boolean isMax = false;
        for (int i = 0; i < x.getObjectives().size(); i++) {
            a = x.getObjective(i).toInterval();
            b = y.getObjective(i).toInterval();
            isMax = (x.getProblem() != null) ? x.getProblem().getObjectives_type()[i] == Problem.MAXIMIZATION : false;
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
        boolean s2_dominates_s1 = false, s1_dominates_s2 = false;
        if (value1_strictly_greater && better_a == x.getObjectives().size())
            s1_dominates_s2 = true;
        if (value2_strictly_greater && better_b == x.getObjectives().size())
            s2_dominates_s1 = true;
        if (s2_dominates_s1 && s1_dominates_s2)
            return 0;
        if (s1_dominates_s2)
            return -1;
        if (s2_dominates_s1)
            return 1;
        if (!s2_dominates_s1 && better_a != x.getObjectives().size() && !s1_dominates_s2
                && better_b != x.getObjectives().size()) {
            if (isMax) {
                if (better_a < better_b)
                    return -1;
                if (better_b < better_a)
                    return 1;
            } else {
                if (better_a > better_b)
                    return -1;
                if (better_b > better_a)
                    return 1;
            }
        }
        return 0;
    }

    public RealData getAlpha() {
        return alpha;
    }

    public void setAlpha(RealData alpha) {
        this.alpha = alpha;
    }

    public static void main(String[] args) {
        EtaDominance<BinarySolution> cmp = new EtaDominance<>(new RealData(1.0));
        BinarySolution a = new BinarySolution(3, 0, 0);
        a.setObjective(0, new RealData(0.968));
        a.setObjective(1, new RealData(0.24869));
        a.setObjective(2, new RealData(0));
        a.setNumberOfPenalties(0);
        a.setPenalties(new RealData(0));
        BinarySolution b = new BinarySolution(3, 0, 0);
        b.setObjective(0, new RealData(0.99283));
        b.setObjective(1, new RealData(0.3233));
        b.setObjective(2, new RealData(0));
        b.setNumberOfPenalties(0);
        b.setPenalties(new RealData(0));
        System.out.println(a.getObjectives() + " <> " + b.getObjectives());
        System.out.println(cmp.compare(a, b));
    }
}