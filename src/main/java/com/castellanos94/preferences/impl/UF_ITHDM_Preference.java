package com.castellanos94.preferences.impl;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.Interval;
import com.castellanos94.datatype.RealData;
import com.castellanos94.preferences.Preference;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

/**
 * Pendiente de revisar, implementacion base rara.
 */
public class UF_ITHDM_Preference extends Preference {
    protected UF_ITHDM_Dominance<Solution> dominance;
    protected PreferenceModel model;
    protected Problem p;

    public UF_ITHDM_Preference(Problem p, PreferenceModel model) {
        this.model = model;
        this.p = p;
        this.dominance = new UF_ITHDM_Dominance<>((RealData) model.getAlpha());
    }

    /**
     * @return -1 if xPy, 0 x~y
     */
    @Override
    public int compare(Solution x, Solution y) {
        // TODO: Pendiende revisar esto
        if (dominance.compare(x, y) == -1)
            return -1;
        Data alpha = model.getAlpha();
        Interval ux = new Interval(0), uy = new Interval(0);
        Data weights[] = model.getWeights();
        for (int i = 0; i < p.getNumberOfObjectives(); i++) {
            ux = (Interval) ux.plus(weights[i].times(x.getObjective(i)));
            uy = (Interval) uy.plus(weights[i].times(y.getObjective(i)));
        }
        // Se asume de maximizacion;
        Interval poss = new Interval(ux.possGreaterThanOrEq(uy));
        if (poss.compareTo(alpha) >= 0)
            return -1;
        return 0;
    }
}