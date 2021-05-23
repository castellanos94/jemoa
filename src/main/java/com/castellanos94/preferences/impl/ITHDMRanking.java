package com.castellanos94.preferences.impl;

import java.util.ArrayList;
import java.util.Comparator;

import com.castellanos94.components.Ranking;
import com.castellanos94.datatype.Data;
import com.castellanos94.solutions.Solution;
/**
 * No funciona
 * 
 */
public class ITHDMRanking<S extends Solution<?>> implements Ranking<S> {
    protected IntervalOutrankingRelations<S> preference;
    protected int numberOfComparisons;
    protected ArrayList<S> front;
    public ITHDMRanking(IntervalOutrankingRelations<S> preference) {
        this.preference = preference;
    }

    /**
     * Compute ranking of solutions based on preference system.
     *
     * @param population solution list
     */
    @Override
    public void computeRanking(ArrayList<S> population) {
        int[] dominating_ith = new int[population.size()];
        ArrayList<ArrayList<Integer>> ith_dominated = new ArrayList<>();
        for (int i = 0; i < dominating_ith.length; i++) {
            ith_dominated.add(new ArrayList<>());
        }
        for (int i = 0; i < population.size() - 1; i++) {
            for (int j = 1; j < population.size(); j++) {
                int dominance_test_result = this.preference.compare(population.get(i), population.get(j));
                if (dominance_test_result == -1 || dominance_test_result == -2) {
                    population.get(i).setAttribute("net_score",
                            this.preference.getSigmaXY().minus(this.preference.getSigmaYX()));
                    ith_dominated.get(i).add(j);
                    dominating_ith[i] += 1;
                }
            }
        }
        this.front = new ArrayList<>();
        for (int i = 0; i < dominating_ith.length; i++) {
            if (dominating_ith[i] == 0){
                if(population.get(i).getAttribute("net_score")!=null){
                    population.get(i).setAttribute("dominance_ranking", 0);
                    front.add(population.get(i));
                }
            }
        }
        this.front.sort(new Comparator<S>(){

            @Override
            public int compare(S o1, S o2) {
                Data a = (Data) o1.getAttribute("net_score");
                Data b = (Data) o2.getAttribute("net_score");

                return a.compareTo(b);
            }
            
        }.reversed());
    }

    @Override
    public ArrayList<S> getSubFront(int index) {
        return this.front;
    }

    @Override
    public int getNumberOfSubFronts() {
        return 1;
    }

}
