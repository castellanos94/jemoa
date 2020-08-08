package com.castellanos94.preferences.impl;

import com.castellanos94.components.impl.IntervalDominance;
import com.castellanos94.preferences.Preference;
import com.castellanos94.solutions.Solution;

public class InterClassnC extends Preference<Solution> {
    protected IntervalDominance<Solution> dominance;

    public InterClassnC(IntervalDominance<Solution> dominance) {
        this.dominance = dominance;
    }

    @Override
    public int compare(Solution x, Solution y) {
        // TODO Auto-generated method stub
        return 0;
    }
    private int asc_rule(Solution x){
        ITHDM_Preference pref = new ITHDM_Preference(x, null, dm);
        IntervalValue[] xx = x.getObjectives().toArray(new IntervalValue[x.getNumberOfObjectives()]);
        int clase = -1;
        IntervalValue[][] r2 = instance.getR2()[dm];
        for (int i = 0; i < r2.length && clase == -1; i++) {
            if (pref.outranking(r2[i], xx) == -1)
                clase = i;
        }

        if (clase != -1)
            return clase;
        IntervalValue[][] r1 = instance.getR1()[dm];
        for (int i = 0; i < r1.length && clase == -1; i++) {
            if (pref.outranking(r1[i], xx) == -1)
                clase = i;
        }
        return (clase == -1) ? clase : clase + r2.length;
    }

}