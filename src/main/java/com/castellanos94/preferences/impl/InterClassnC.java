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

}