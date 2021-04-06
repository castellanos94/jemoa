package com.castellanos94.utils;

public class MatrixArithmetic {
    /**
     * 
     * @return hadamard product
     */
    public static double[] entrywiseProduct(double scalar, double[] vector) {
        double[] c = new double[vector.length];
        for (int i = 0; i < c.length; i++) {
            c[i] = scalar * vector[i];
        }
        return c;
    }

    /**
     * Absolute value for each element
     * 
     * @param vector
     */
    public static double[] abs(double[] vector) {
        for (int i = 0; i < vector.length; i++) {
            vector[i] = Math.abs(vector[i]);
        }
        return vector;
    }

}
