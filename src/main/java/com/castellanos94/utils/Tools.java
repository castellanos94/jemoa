package com.castellanos94.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.castellanos94.datatype.Interval;

public class Tools {
    private static Random random = new Random();
    public static final int PLACES = 6;

    public static Random getRandom() {
        return random;
    }

    public static void setSeed(Long seed) {
        random.setSeed(seed);
    }

    /**
     * Generates sequences using Latin hypercube sampling (LHS). Each axis is
     * divided into {@code N} stripes and exactly one point may exist in each
     * stripe. MOEA
     * <p>
     * References:
     * <ol>
     * <li>McKay M.D., Beckman, R.J., and Conover W.J. "A Comparison of Three
     * Methods for Selecting Values of Input Variables in the Analysis of Output
     * from a Computer Code." Technometrics, 21(2):239-245, 1979.
     * </ol>
     */
    public static double[][] LHS(int N, int D) {
        double[][] result = new double[N][D];
        List<Double> temp = new ArrayList<>();
        double d = 1.0 / N;
        for (int i = 0; i < N; i++) {
            temp.add(0.0);
        }
        for (int i = 0; i < D; i++) {
            for (int j = 0; j < N; j++) {
                temp.set(j, random.doubles(j * d, (j + 1) * d).findFirst().getAsDouble());
            }

            shuffle(temp);

            for (int j = 0; j < N; j++) {
                result[j][i] = temp.get(j);
            }
        }

        return result;
    }

    /**
     * This method is used to shuffle lists instead of using the Collections.shuffle
     * method because if we set a seed, we never have full control with that method.
     * 
     * @param positions list to shuffle
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void shuffle(List positions) {
        for (int i = 0; i < positions.size(); i++) {
            int randomIndexToSwap = random.nextInt(positions.size());
            Object tmp = positions.get(randomIndexToSwap);
            positions.set(randomIndexToSwap, positions.get(i));
            positions.set(i, tmp);
        }

    }

    public static Number getRandomNumberInRange(Number lowerBound, Number upperBound) {
        if (lowerBound instanceof Interval) {
            Interval l = (Interval) lowerBound;
            Interval r = (Interval) upperBound;
            if (l.getLower().compareTo(l.getUpper()) != 0 && r.getLower().compareTo(r.getLower()) != 0) {
                return new Interval(getRandomNumberInRange(l.getLower().doubleValue(), l.getLower().doubleValue()),
                        getRandomNumberInRange(r.getLower().doubleValue(), r.getUpper().doubleValue()));
            }
            return new Interval(getRandomNumberInRange(l.getLower(), r.getUpper()));
        }
        if (lowerBound.doubleValue() > upperBound.doubleValue()) {
            throw new IllegalArgumentException("max must be greater than min");
        } else if (lowerBound.doubleValue() == upperBound.doubleValue()) {
            return lowerBound.doubleValue();
        }
        return random.doubles(lowerBound.doubleValue(), upperBound.doubleValue()).findFirst().getAsDouble();
    }

    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}