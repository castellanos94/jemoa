package com.castellanos94.utils;

import java.util.List;
import java.util.Random;

import com.castellanos94.datatype.Interval;

public class Tools {
    private static Random random = new Random();

    public static Random getRandom() {
        return random;
    }

    public static void setSeed(Long seed) {
        random.setSeed(seed);
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

        // return random.nextDouble() * ((max - min) + 1) + min;
        return random.doubles(lowerBound.doubleValue(), upperBound.doubleValue()).findFirst().getAsDouble();
    }
}