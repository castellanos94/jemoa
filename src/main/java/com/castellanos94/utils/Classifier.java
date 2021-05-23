package com.castellanos94.utils;

import com.castellanos94.solutions.Solution;

public abstract class Classifier<S extends Solution<?>> implements ExtraInformation {
    public abstract void classify(S x);

    public static String CLASS_KEY = "__CLASS__";

}