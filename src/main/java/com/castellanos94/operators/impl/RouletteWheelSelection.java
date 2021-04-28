package com.castellanos94.operators.impl;

import java.util.ArrayList;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.operators.SelectionOperator;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

public class RouletteWheelSelection<S extends Solution<?>> implements SelectionOperator<S> {
    private int n_offsrping;
    private ArrayList<S> parents;

    public RouletteWheelSelection(int n_offsrping) {
        this.n_offsrping = n_offsrping;
    }

    @Override
    public Void execute(ArrayList<S> source) {
        this.parents = new ArrayList<>();
        if (source.isEmpty()) {
            return null;
        }
        Data facum = Data.getZeroByType(source.get(0).getObjective(0));
        ArrayList<Data> probability = new ArrayList<>();
        ArrayList<Data> f_sum = new ArrayList<>();

        for (S individual : source) {
            Data tmp = RealData.ZERO;
            for (Data objective : individual.getObjectives()) {
                tmp = tmp.plus(objective);
            }
            probability.add(tmp.copy());
            f_sum.add(tmp.copy());
            facum = facum.plus(tmp);
        }
        Data sum = RealData.ZERO;
        for (int i = 0; i < source.size(); i++) {
            probability.set(i, probability.get(i).div(facum));
            sum = sum.plus(probability.get(i));
        }
        for (int i = 0; i < Math.min(n_offsrping, source.size()); i++) {
            Data partialSum = RealData.ZERO;
            double roulette = Tools.getRandom().nextDouble();
            for (int j = 0; j < source.size(); j++) {
                partialSum = partialSum.plus(probability.get(j));
                if (partialSum.compareTo(roulette) >= 0) {
                    this.parents.add(source.get(j));
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public ArrayList<S> getParents() {
        return this.parents;
    }

    @Override
    public void setPopulationSize(int size) {
        this.n_offsrping = size;
    }
}
