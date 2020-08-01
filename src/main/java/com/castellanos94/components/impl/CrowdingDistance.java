package com.castellanos94.components.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.castellanos94.components.DensityEstimator;
import com.castellanos94.datatype.Data;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.ObjectiveComparator;

public class CrowdingDistance implements DensityEstimator {

    @Override
    public void compute(ArrayList<Solution> solutions) {
        if (solutions.size() == 0) {
            return;
        } else if (solutions.size() == 1) {
            solutions.get(0).getProperties().put(getKey(), maxValue(solutions.get(0)));
            return;
        } else if (solutions.size() == 2) {
            solutions.get(0).getProperties().put(getKey(), maxValue(solutions.get(0)));
            solutions.get(1).getProperties().put(getKey(), maxValue(solutions.get(0)));
            return;
        }
        for (Solution solution : solutions) {
            solution.getProperties().put(getKey(), Data.getZeroByType(solution.getObjectives().get(0)));
        }
        int numOfObj = solutions.get(0).getObjectives().size();
        Data MaxValue = maxValue(solutions.get(0));
        for (int i = 0; i < numOfObj; i++) {
            Collections.sort(solutions, new ObjectiveComparator(i));
            try {
                solutions.get(0).getProperties().put(getKey(), (Data) MaxValue.clone());
                solutions.get(solutions.size() - 1).getProperties().put(getKey(), (Data) MaxValue.clone());
                Data min = solutions.get(0).getObjectives().get(i);
                Data max = solutions.get(solutions.size() - 1).getObjectives().get(i);
                Data distance = Data.getZeroByType(min);
                for (int j = 1; j < solutions.size() - 1; j++) {
                    distance = solutions.get(j + 1).getObjectives().get(i)
                            .minus(solutions.get(j - 1).getObjectives().get(i));
                    distance = distance.div(max.minus(min));
                    distance = distance.plus(
                            (Data) solutions.get(j).getProperties().getOrDefault(getKey(), Data.getZeroByType(min)));
                    solutions.get(j).getProperties().put(getKey(), distance);
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    private Data maxValue(Solution solution) {
        return Data.initByRefType(solution.getObjectives().get(0), Double.POSITIVE_INFINITY);
    }

    @Override
    public String getKey() {
        return getClass().getName();
    }

    @Override
    public ArrayList<Solution> sort(ArrayList<Solution> solutions) {
        solutions.sort(new CrowdingDistanceComparator().reversed());
        return solutions;
    }

    public class CrowdingDistanceComparator implements Comparator<Solution> {

        @Override
        public int compare(Solution a, Solution b) {
            return ((Data) a.getProperties().get(getKey())).compareTo(((Data) b.getProperties().get(getKey())));
        }

    }

}