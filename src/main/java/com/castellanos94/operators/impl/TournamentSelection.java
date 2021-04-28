package com.castellanos94.operators.impl;

import java.util.ArrayList;
import java.util.Comparator;

import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

public class TournamentSelection<S extends Solution<?>> implements SelectionOperator<S> {
    private int n_offsrping;
    private ArrayList<S> parents;
    private Comparator<S> comparator;

    public TournamentSelection(int n_offsrping, Comparator<S> comparator) {
        this.n_offsrping = n_offsrping;
        this.comparator = comparator;
    }

    @Override
    public Void execute(final ArrayList<S> solutions) {
        this.parents = new ArrayList<>();
        for (int i = 0; i < n_offsrping; i++) {
            int pos_a = Tools.getRandom().nextInt(solutions.size());
            int pos_b;
            do {
                pos_b = Tools.getRandom().nextInt(solutions.size());
            } while (pos_b == pos_a);
            int val = comparator.compare(solutions.get(pos_a), solutions.get(pos_b));
            if (val == -1) {
                parents.add(solutions.get(pos_a));
            } else if (val == 1) {
                parents.add(solutions.get(pos_b));
            } else {
                parents.add(solutions.get(pos_a));
            }

        }
        return null;
    }

    @Override
    public ArrayList<S> getParents() {
        return parents;
    }

    @Override
    public void setPopulationSize(int size) {
        this.n_offsrping = size;
    }

    @Override
    public String toString() {
        return "TournamentSelection [comparator=" + comparator + ", n_offsrping=" + n_offsrping + ", parents=" + parents
                + "]";
    }

}