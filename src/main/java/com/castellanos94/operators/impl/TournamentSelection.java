package com.castellanos94.operators.impl;

import java.util.ArrayList;
import java.util.Comparator;

import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

public class TournamentSelection implements SelectionOperator {
    private int n_offsrping;
    private ArrayList<Solution> parents;
    private Comparator<Solution> comparator;

    public TournamentSelection(int n_offsrping, Comparator<Solution> comparator) {
        this.n_offsrping = n_offsrping;
        this.comparator = comparator;
    }

    @Override
    public void execute(final ArrayList<Solution> solutions) {
        this.parents = new ArrayList<>();
        for (int i = 0; i < n_offsrping; i++) {
            int pos_a = Tools.getRandom().nextInt(solutions.size());
            int pos_b;
            do {
                pos_b = Tools.getRandom().nextInt(solutions.size());
            } while (pos_b == pos_a);
            int val = comparator.compare(solutions.get(pos_a), solutions.get(pos_b));
            if (val == 1) {
                parents.add(solutions.get(pos_a));
            } else if (val == -1) {
                parents.add(solutions.get(pos_b));
            } else {
                parents.add(solutions.get(pos_a));
            }

        }

    }

    @Override
    public ArrayList<Solution> getParents() {
        return parents;
    }

}