package com.castellanos94.indicators.impl;

import java.util.ArrayList;

import com.castellanos94.datatype.Data;
import com.castellanos94.indicators.Indicator;
import com.castellanos94.solutions.Solution;

public class InvertedGenerationalDistance implements Indicator<Solution> {
    private Data[][] reference;

    public InvertedGenerationalDistance(Data[][] referenceFront) {
        this.reference = referenceFront;
    }

    @Override
    public Data evaluate(ArrayList<Solution> solutions) {
        Data sum = Data.getZeroByType(solutions.get(0).getObjective(0));
        for (int i = 0; i < reference.length; i++) {
            sum = sum.addition(distanceToClosestPoint(reference[i], solutions).pow(2));
            // sum += Math.pow(distanceToClosestPoint(reference[i], solutions), 2);
        }

        sum = sum.pow(1.0 / 2);// Math.pow(sum, 1.0 / 2);

        return sum.division(reference.length);
    }

    private Data distanceToClosestPoint(Data[] point, ArrayList<Solution> front) {
        Data values[] = new Data[front.get(0).getObjectives().size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = front.get(0).getObjective(i);
        }
        Data minDistance = euclideanDistanceCompute(point, values);

        for (int i = 1; i < front.size(); i++) {
            for (int j = 0; j < values.length; j++) {
                values[j] = front.get(i).getObjective(j);
            }
            Data aux = euclideanDistanceCompute(point, values);
            if (aux.compareTo(minDistance) < 0) {
                minDistance = aux;
            }
        }

        return minDistance;
    }

    private Data euclideanDistanceCompute(Data[] vector1, Data[] vector2) {

        Data distance = Data.getZeroByType(vector1[0]);

        Data diff;
        for (int i = 0; i < vector1.length; i++) {
            diff = vector1[i].subtraction(vector2[i]);
            distance = distance.addition(diff.multiplication(diff));
        }

        return distance.sqr();
    }

}