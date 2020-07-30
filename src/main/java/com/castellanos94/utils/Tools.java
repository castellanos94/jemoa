package com.castellanos94.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import com.castellanos94.datatype.IntervalData;
import com.castellanos94.instances.Instance;
import com.castellanos94.instances.KnapsackIntance;
import com.castellanos94.instances.PSPInstance;
import com.castellanos94.problems.ProblemType;

public class Tools {
    private static Random random = new Random();

    public static Random getRandom() {
        return random;
    }

    public static void setSeed(Long seed) {
        random.setSeed(seed);
    }

    public static Instance getInstanceFromResource(ProblemType type, String name) throws IOException {
        InputStream resourceAsStream;
        switch (type) {
            case Knapsack:
                resourceAsStream = ClassLoader.getSystemClassLoader()
                        .getResourceAsStream("resources/instances/knapsack/" + name);
                // .getResourceAsStream("resources" + File.separator +
                // "instances"+File.separator+"knapsack" + File.separator + name);
                return new KnapsackIntance().loadInstance(resourceAsStream);
            case PSP:
                resourceAsStream = ClassLoader.getSystemClassLoader().getResourceAsStream(
                        "resources" + File.separator + "instances" + File.separator + "psp" + File.separator + name);
                return new PSPInstance().loadInstance(resourceAsStream);
            default:
                return null;
        }
    }

    public static Number getRandomNumberInRange(Number lowerBound, Number upperBound) {
        if (lowerBound instanceof IntervalData) {
            IntervalData l = (IntervalData) lowerBound;
            IntervalData r = (IntervalData) upperBound;
            if (l.getLower().compareTo(l.getUpper()) != 0 && r.getLower().compareTo(r.getLower()) != 0) {
                return new IntervalData(getRandomNumberInRange(l.getLower().doubleValue(), l.getLower().doubleValue()),
                        getRandomNumberInRange(r.getLower().doubleValue(), r.getUpper().doubleValue()));
            }
            return new IntervalData(getRandomNumberInRange(l.getLower(), r.getUpper()));
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