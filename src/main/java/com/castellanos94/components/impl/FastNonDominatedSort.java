package com.castellanos94.components.impl;

import java.util.ArrayList;
import java.util.Iterator;

import com.castellanos94.components.Ranking;
import com.castellanos94.solutions.Solution;

public class FastNonDominatedSort implements Ranking {
    protected ArrayList<ArrayList<Solution>> fronts;
    protected DominanceCompartor paretoDominance;

    public FastNonDominatedSort() {
        this.paretoDominance = new DominanceCompartor();
    }

    @Override
    public void computeRanking(ArrayList<Solution> population) {
        if (fronts != null) {
            fronts = new ArrayList<>();
        }

        ArrayList<ArrayList<Integer>> dominate_me = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            dominate_me.add(new ArrayList<>());
            fronts.add(new ArrayList<>());
        }
        for (int i = 0; i < population.size(); i++) {
            for (int j = 0; j < population.size(); j++) {
                if (i != j && !population.get(i).equals(population.get(j))) {
                    int value = paretoDominance.compare(population.get(i), population.get(j));
                    if (value == 1 && !dominate_me.get(j).contains(i)) {
                        dominate_me.get(j).add(i);
                    } else if (value == -1 && !dominate_me.get(i).contains(j)) {
                        dominate_me.get(i).add(j);
                    }
                }
            }
        }
        // find front 0
        int i = 0;
        Iterator<ArrayList<Integer>> iter = dominate_me.iterator();

        ArrayList<Integer> toRemove = new ArrayList<>();
        while (iter.hasNext()) {
            ArrayList<Integer> dom = iter.next();
            if (dom.size() == 0) {
                fronts.get(0).add(population.get(i));
                toRemove.add(i);
            }
            i++;
        }
        for (Integer integer : toRemove) {
            solutionRemove(integer, dominate_me);
        }

        for (int j = 1; j < fronts.size(); j++) {
            ArrayList<Solution> currentFront = fronts.get(j);
            i = 0;
            for (ArrayList<Integer> dom_me : dominate_me) {
                if (!toRemove.contains(i) && dom_me.size() == 0) {
                    currentFront.add(population.get(i));
                    toRemove.add(i);
                }
                i++;
            }
            for (Integer integer : toRemove) {
                solutionRemove(integer, dominate_me);
            }

        }
        fronts.removeIf(front -> front.size() == 0);

        for (int j = 0; j < fronts.size(); j++) {
            ArrayList<Solution> front = fronts.get(j);
            for (Solution solution : front) {
                solution.setRank(j);
            }
        }

    }

    private void solutionRemove(Integer i, ArrayList<ArrayList<Integer>> dominate_me) {
        for (ArrayList<Integer> arrayList : dominate_me) {
            if (arrayList.contains(i)) {
                arrayList.remove(i);
            }
        }
    }

    @Override
    public ArrayList<Solution> getSubFront(int index) {
        return fronts.get(index);
    }

    @Override
    public int getNumberOfSubFronts() {
        return fronts.size();
    }

}