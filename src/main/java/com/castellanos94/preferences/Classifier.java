package com.castellanos94.preferences;

import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.ExtraInformation;

public abstract class Classifier<S extends Solution<?>> implements ExtraInformation {
    public abstract void classify(S x) throws CloneNotSupportedException;

    public static String CLASS_KEY = "__CLASS__";

}