package com.castellanos94.components.impl;

import java.util.ArrayList;
import java.util.Comparator;

import com.castellanos94.components.Ranking;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public class DominanceComparator<S extends Solution<?>> implements Comparator<S>, Ranking<S> {

    protected ArrayList<ArrayList<S>> front;

    public DominanceComparator() {
    }

    /**
     * Compare two solution by objective
     * 
     * @param a represent the first solution
     * @param b represent the second solution
     * @return -1 if a dominates b, 0 if a and b are non-dominated or 1 b is
     *         dominated by a.
     */
    @Override
    public int compare(S a, S b) {
        if (a.getObjectives().size() != b.getObjectives().size()) {
            throw new IllegalArgumentException("Solution must be same objective size");
        }
        Problem<?> problem = a.getProblem();

        if (a.getNumberOfPenalties() != b.getNumberOfPenalties() || a.getPenalties().compareTo(0) < 0
                || b.getPenalties().compareTo(0) < 0) {
            if (a.getNumberOfPenalties() < b.getNumberOfPenalties())
                return -1;
            if (b.getNumberOfPenalties() < a.getNumberOfPenalties())
                return 1;
            int value = a.getPenalties().compareTo(b.getPenalties());
            if (value != 0)
                return (-1) * value;
        }

        int a_dom = 0, b_dom = 0;
        for (int i = 0; i < a.getObjectives().size() && (a_dom != 1 || b_dom != 1); i++) {
            int value = a.getObjectives().get(i).compareTo(b.getObjectives().get(i));
            if (problem !=null && problem.getObjectives_type()[i] == Problem.MAXIMIZATION) {
                value *= -1;
            }
            if (value == -1) {
                a_dom = 1;
            } else if (value == 1) {
                b_dom = 1;
            }
        }
        return (a_dom == b_dom) ? 0 : (a_dom == 1) ? -1 : 1;
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
        ArrayList<S> zero = new ArrayList<>();
        ArrayList<S> others = new ArrayList<>();

        for (int i = 0; i < population.size(); i++) {
            population.get(i).setRank(dominate_me.get(i).size());
            if (population.get(i).getRank() == 0)
                zero.add(population.get(i));
            else
                others.add(population.get(i));
        }
        front.add(zero);
        front.add(others);
        // Collections.sort(population);

    }

    @Override
    public ArrayList<S> getSubFront(int index) {
        return this.front.get(index);
    }

    @Override
    public int getNumberOfSubFronts() {
        return 2;
    }
}