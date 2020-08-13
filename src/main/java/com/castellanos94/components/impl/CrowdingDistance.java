package com.castellanos94.components.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.castellanos94.components.DensityEstimator;
import com.castellanos94.datatype.Data;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.ObjectiveComparator;

public class CrowdingDistance<S extends Solution<?>> implements DensityEstimator<S> {

    @Override
    public void compute(ArrayList<S> solutions) {
        if (solutions.size() == 0) {
            return;
        } else if (solutions.size() == 1) {
            solutions.get(0).getAttributes().put(getAttributeKey(), maxValue(solutions.get(0)));
            return;
        } else if (solutions.size() == 2) {
            solutions.get(0).getAttributes().put(getAttributeKey(), maxValue(solutions.get(0)));
            solutions.get(1).getAttributes().put(getAttributeKey(), maxValue(solutions.get(0)));
            return;
        }
        for (S solution : solutions) {
            solution.getAttributes().put(getAttributeKey(), Data.getZeroByType(solution.getObjectives().get(0)));
        }
        int numOfObj = solutions.get(0).getObjectives().size();
        Data MaxValue = maxValue(solutions.get(0));
        for (int i = 0; i < numOfObj; i++) {
            Collections.sort(solutions, new ObjectiveComparator(i));
            try {
                solutions.get(0).getAttributes().put(getAttributeKey(), (Data) MaxValue.clone());
                solutions.get(solutions.size() - 1).getAttributes().put(getAttributeKey(), (Data) MaxValue.clone());
                Data min = solutions.get(0).getObjectives().get(i);
                Data max = solutions.get(solutions.size() - 1).getObjectives().get(i);
                Data distance = Data.getZeroByType(min);
                for (int j = 1; j < solutions.size() - 1; j++) {
                    distance = solutions.get(j + 1).getObjectives().get(i)
                            .minus(solutions.get(j - 1).getObjectives().get(i));
                    distance = distance.div(max.minus(min));
                    distance = distance.plus(
                            (Data) solutions.get(j).getAttributes().getOrDefault(getAttributeKey(), Data.getZeroByType(min)));
                    solutions.get(j).getAttributes().put(getAttributeKey(), distance);
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    private Data maxValue(S solution) {
        return Data.initByRefType(solution.getObjectives().get(0), Double.POSITIVE_INFINITY);
    }

    @Override
    public String getAttributeKey() {
        return getClass().getName();
    }

    @Override
    public ArrayList<S> sort(ArrayList<S> solutions) {
        solutions.sort(new CrowdingDistanceComparator<>().reversed());
        return solutions;
    }

    public class CrowdingDistanceComparator<S extends Solution<?>> implements Comparator<S> {

        @Override
        public int compare(S a, S b) {
            return ((Data) a.getAttributes().get(getAttributeKey()))
                    .compareTo(((Data) b.getAttributes().get(getAttributeKey())));
        }

    }

}