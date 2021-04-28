package com.castellanos94.operators.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.datatype.Data;
import com.castellanos94.operators.ArchiveSelection;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.ExtraInformation;

/**
 * Adaptive Grid <br>
 * Gregorio Toscano-Pulido. «Uso de Auto-Adaptación y Elitismo para Optimización
 * Multiobje-tivo Mediante Cúmulos de Partículas». Tesis doct. Center for
 * Research y Advanced Studies ofthe National Polytechnic Institute, 2005.
 * 
 * @param <S> Solution Domain
 */
public class AdaptiveGrid<S extends Solution<?>> implements ArchiveSelection<S>, ExtraInformation {
    protected ArrayList<S> solutions;
    protected int populationSize;
    protected Comparator<S> comparator;
    protected List<Data> idealPoint;
    protected List<Data> nadirPoint;
    protected List<Data> sizeDiv;
    protected Problem<S> problem;

    public AdaptiveGrid(Problem<S> problem, int populationSize, Comparator<S> comparator) {
        this.solutions = new ArrayList<>(populationSize);
        this.populationSize = populationSize;
        this.comparator = comparator;
        this.problem = problem;
        this.idealPoint = new ArrayList<>(problem.getNumberOfObjectives());
        this.nadirPoint = new ArrayList<>(problem.getNumberOfObjectives());
        this.sizeDiv = new ArrayList<>(problem.getNumberOfObjectives());

        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            this.idealPoint.add(null);
            this.nadirPoint.add(null);
            this.sizeDiv.add(null);
        }
    }

    public AdaptiveGrid(Problem<S> problem, int populationSize) {
        this(problem, populationSize, new DominanceComparator<>());
    }

    /**
     * Add a new solution if it is non-dominated concerning the other solutions.
     * This method also invokes the re-arrangement grid mechanism.
     * 
     * @param solution The solution to join the non-dominant population
     */
    @SuppressWarnings("unchecked")
    public void addSolution(S solution) {
        if (this.solutions.isEmpty()) {
            this.solutions.add((S) solution.copy());
        }
        if (!this.solutions.contains(solution)) {

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
            if (!toRemove.isEmpty()) {
                this.solutions.removeAll(toRemove);
            }
            if (toAdd && this.solutions.size() + 1 <= populationSize) {
                this.solutions.add((S) solution.copy());
            } else if (toAdd) {
                // Grid boundaries
                for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
                    Data min = solutions.get(0).getObjective(i);
                    Data max = min.copy();
                    for (int j = 1; j < this.solutions.size(); j++) {
                        Data current = this.solutions.get(j).getObjective(i);
                        if (min.compareTo(current) > 0) {
                            min = current;
                        }
                        if (max.compareTo(current) < 0) {
                            max = current;
                        }
                    }
                    this.idealPoint.set(i, min);
                    this.nadirPoint.set(i, max);
                    // Hypercube dimensions eq 5.3
                    this.sizeDiv.set(i, max.minus(min).div(populationSize - 1));
                }
                this.solutions.add(solution);

                HashMap<Data, ArrayList<S>> gridMap = new HashMap<>();
                for (S s : solutions) {
                    Data loc = identifyLOC(s);
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
                    Iterator<Data> iterator = gridMap.keySet().iterator();
                    while (iterator.hasNext() && values.size() < populationSize) {
                        Data key = iterator.next();
                        ArrayList<S> tmp = gridMap.get(key);
                        if (!tmp.isEmpty()) {
                            values.add((S) tmp.get(0).copy());
                            tmp.remove(0);
                        }
                        if (tmp.isEmpty()) {
                            iterator.remove();
                        }
                    }
                }
                this.solutions = values;

            }
        }
    }

    /**
     * Region identification: the region to wich the solution 's' belongs
     * 
     * @param solution the 's' solution
     */
    private Data identifyLOC(S solution) {
        Data loc = Data.getZeroByType(idealPoint.get(0));
        for (int objective = 0; objective < problem.getNumberOfObjectives(); objective++) {
            loc = loc.plus(
                    ((solution.getObjective(objective).minus(idealPoint.get(objective))).div(sizeDiv.get(objective))));
        }
        loc = loc.times(populationSize);
        solution.setAttribute(getAttributeKey(), loc);
        return loc;

    }

    @Override
    public Void execute(ArrayList<S> source) {
        for (S s : source) {
            addSolution(s);
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

    @Override
    public String getAttributeKey() {
        return "AdaptativeGrid.LOC";
    }

}
