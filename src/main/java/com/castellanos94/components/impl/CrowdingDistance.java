package com.castellanos94.components.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.castellanos94.components.DensityEstimator;
import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.solutions.Solution;

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
            // bubbleSort(i, solutions);

            Data min = solutions.get(0).getObjectives().get(i);
            Data max = min.copy();
            for (int j = 1; j < solutions.size(); j++) {
                Data current = solutions.get(j).getObjective(i);
                if (current.compareTo(min) < 0) {
                    min = current;
                }
                if (current.compareTo(max) > 0) {
                    max = current;
                }
            }
            if (min.compareTo(max) != 0) {
                final int ii = i;
                Collections.sort(solutions, new Comparator<S>() {

                    @Override
                    public int compare(S a, S b) {
                        double i1 = a.getObjective(ii).doubleValue();
                        double i2 = b.getObjective(ii).doubleValue();
                        return (int) Math.signum(i1 - i2);
                    }

                });
                solutions.get(0).getAttributes().put(getAttributeKey(), MaxValue.copy());
                solutions.get(solutions.size() - 1).getAttributes().put(getAttributeKey(), (Data) MaxValue.copy());

                for (int j = 1; j < solutions.size() - 1; j++) {
                    double p = solutions.get(j - 1).getObjective(i).doubleValue();
                    double n = solutions.get(j + 1).getObjective(i).doubleValue();
                    S s = solutions.get(j);
                    Data current = (Data) s.getAttributes().getOrDefault(getAttributeKey(), RealData.ZERO);
                    s.setAttribute(getAttributeKey(), current.plus(n - p).div(max.minus(min)));
                    /*
                     * distance = solutions.get(j + 1).getObjectives().get(i) .minus(solutions.get(j
                     * - 1).getObjectives().get(i)); distance = distance.div(max.minus(min));
                     * distance = distance.plus((Data)
                     * solutions.get(j).getAttributes().getOrDefault(getAttributeKey(),
                     * Data.getZeroByType(min)));
                     * solutions.get(j).getAttributes().put(getAttributeKey(), distance);
                     */
                }
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
        solutions.sort(new CrowdingDistanceComparator().reversed());
        return solutions;
    }

    public Comparator<S> getComparator() {
        return new CrowdingDistanceComparator();
    }

    public class CrowdingDistanceComparator implements Comparator<S> {

        @Override
        public int compare(S a, S b) {
            Data aa = (Data) a.getAttributes().get(getAttributeKey());
            Data bb = ((Data) b.getAttributes().get(getAttributeKey()));

            return (aa).compareTo(bb);
        }

    }

}