package com.castellanos94.components.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.castellanos94.components.Ranking;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

public class DominanceCompartor implements Comparator<Solution>, Ranking {

    protected ArrayList<Solution> front;

    public DominanceCompartor() {
    }

    /**
     * Compare two solution by objective
     * 
     * @param a represent the first solution
     * @param b represent the second solution
     * @return 1, 0 or -1 if a dominates b, if a and b are non-dominated or a is
     *         dominated by b, respectively.
     */
    @Override
    public int compare(Solution a, Solution b) {
        if (a.getObjectives().size() != b.getObjectives().size()) {
            throw new IllegalArgumentException("Solution must be same objective size");
        }
        int a_dom = 0, b_dom = 0;
        int flag = 0;
        Problem problem = a.getProblem();

        if (a.getN_penalties() != b.getN_penalties() || a.getPenalties().compareTo(0) < 0
                || b.getPenalties().compareTo(0) < 0) {

            if (a.getN_penalties() < b.getN_penalties())
                return 1;
            if (b.getN_penalties() < a.getN_penalties())
                return -1;
        }

        for (int i = 0; i < a.getObjectives().size() && (a_dom != 1 || b_dom != 1); i++) {
            int value = a.getObjectives().get(i).compareTo(b.getObjectives().get(i));
            int objective_type = problem.getObjectives_type()[i];
            if (objective_type == Problem.MAXIMIZATION) {
                if (value > 0) {
                    flag = 1;
                } else if (value == 0) {
                    flag = 0;
                } else {
                    flag = -1;
                }
            } else {
                if (value < 0) {
                    flag = 1;
                } else if (value == 0) {
                    flag = 0;
                } else {
                    flag = -1;
                }
            }
            if (flag == 1) {
                a_dom = 1;
            } else if (flag == -1) {
                b_dom = 1;
            }
        }
        if (a_dom == b_dom)
            return 0;
        if (a_dom == 1)
            return 1;
        return -1;
    }

    @Override
    public void computeRanking(ArrayList<Solution> population) {
        ArrayList<ArrayList<Integer>> dominate_me = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            dominate_me.add(new ArrayList<>());
        }
        for (int i = 0; i < population.size(); i++) {
            for (int j = 0; j < population.size(); j++) {
                if (i != j && !population.get(i).equals(population.get(j))) {
                    int value = compare(population.get(i), population.get(j));
                    if (value == 1 && !dominate_me.get(j).contains(i)) {
                        dominate_me.get(j).add(i);
                    } else if (value == -1 && !dominate_me.get(i).contains(j)) {
                        dominate_me.get(i).add(j);
                    }
                }
            }
        }
        this.front = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            population.get(i).setRank(dominate_me.get(i).size());
            if(population.get(i).getRank()==0)
            front.add(population.get(i));
        }
        Collections.sort(population);        
        
        
    }

    @Override
    public ArrayList<Solution> getSubFront(int index) {
        return this.front;
    }

    @Override
    public int getNumberOfSubFronts() {
        return 1;
    }

}