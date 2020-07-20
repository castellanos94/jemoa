package com.castellanos94.utils;

import java.util.Random;

public class Tools {
    private static Random random = new Random();

    public static Random getRandom() {
        return random;
    }

    public static double getRandomNumberInRange(double min, Double max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        return random.nextDouble() * ((max - min) + 1) + min;
    }

    public static void setSeed(Long seed) {
        random.setSeed(seed);
    }

}