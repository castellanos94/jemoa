package com.castellanos94.operators.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Collectors;

import com.castellanos94.components.impl.CrowdingDistance;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.datatype.Data;
import com.castellanos94.operators.ArchiveSelection;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.HeapSort;

public class CrowdingDistanceArchive<S extends Solution<?>> implements ArchiveSelection<S> {
    protected ArrayList<S> solutions;
    protected int populationSize;
    protected CrowdingDistance<S> crowdingDistance;
    protected DominanceComparator<S> comparator;
    protected HeapSort<S> heapSortSolutions;

    /**
     * 
     * @param populationSize of the archive
     * @param comparator     the Dominance comparator to uses
     * @see com.castellanos94.components.impl.DominanceComparator
     * 
     */
    public CrowdingDistanceArchive(int populationSize, DominanceComparator<S> comparator) {
        this.populationSize = populationSize;
        this.solutions = new ArrayList<>();
        this.comparator = comparator;
        this.crowdingDistance = new CrowdingDistance<>();
        this.heapSortSolutions = new HeapSort<>(crowdingDistance.getComparator().reversed());
    }

    /**
     * This use Classic Dominance
     * 
     * @see com.castellanos94.components.impl.DominanceComparator
     * @param populationSize of the archive
     */
    public CrowdingDistanceArchive(int populationSize) {
        this(populationSize, new DominanceComparator<>());
    }

    /**
     * Add a new solution if it is non-dominated concerning the other solutions.
     * This method also invokes the re-arrangement with crowding distance mechanism.
     * 
     * @param solution The solution to join the non-dominant population
     */
    @SuppressWarnings("unchecked")
    public void addSolution(S solution) {
        if (this.solutions.isEmpty()) {
            this.solutions.add((S) solution.copy());
        } else if (!this.solutions.contains(solution)) {
            ArrayList<S> toRemove = new ArrayList<>();
            boolean toAdd = true;
            for (int index = 0; index < this.solutions.size(); index++) {
                S next = this.solutions.get(index);
                int value = comparator.compare(solution, next);
                if (value == 1) {
                    toAdd = false;
                    break;
                } else if (value == -1) {
                    toRemove.add(next);
                }
            }
            if (toAdd && !toRemove.isEmpty()) {
                this.solutions.removeAll(toRemove);
            }
            if (toAdd && this.solutions.size() + 1 <= populationSize) {
                this.solutions.add((S) solution.copy());
            } else if (toAdd) {
                this.solutions = new ArrayList<>(this.solutions.stream().distinct().collect(Collectors.toList()));
                ArrayList<S> tmpList = new ArrayList<>(solutions);
                tmpList.add(solution);
                if (tmpList.size() > this.populationSize) {
                    // Compute crowding distance
                    crowdingDistance.compute(tmpList);
                    // ArrayList<S> sorted = crowdingDistance.sort(tmpList);
                    HashMap<Data, ArrayList<S>> gridMap = new HashMap<>();
                    for (S s : solutions) {
                        Data loc = (Data) s.getAttribute(crowdingDistance.getAttributeKey());
                        if (gridMap.containsKey(loc)) {
                            gridMap.get(loc).add(s);
                        } else {
                            ArrayList<S> tmp = new ArrayList<>();
                            tmp.add(s);
                            gridMap.put(loc, tmp);
                        }
                    }
                    ArrayList<S> values = new ArrayList<>();
                    while (values.size() < populationSize) {
                        Iterator<Data> iterator2 = gridMap.keySet().iterator();
                        while (iterator2.hasNext() && values.size() < populationSize) {
                            Data key = iterator2.next();
                            ArrayList<S> tmp = gridMap.get(key);
                            if (!tmp.isEmpty()) {
                                values.add((S) tmp.get(0).copy());
                                tmp.remove(0);
                            }
                            if (tmp.isEmpty()) {
                                iterator2.remove();
                            }
                        }
                    }
                    this.solutions = values;
                } else {
                    this.solutions = tmpList;
                }

            }
        }
    }

    @Override
    public Void execute(ArrayList<S> source) {
        if (solutions.isEmpty()) {
            comparator.computeRanking(source);
            for (S s : comparator.getSubFront(0)) {
                if (!solutions.contains(s) && solutions.size() < this.populationSize)
                    solutions.add(s);
            }
        } else {
            for (int i = 0; i < source.size(); i++) {
                addSolution(source.get(i));
            }
        }
        return null;
    }

    @Override
    public ArrayList<S> getParents() {
        return this.solutions;
    }

    @Override
    public void setPopulationSize(int size) {
        this.populationSize = size;
    }

}
