package com.castellanos94.preferences;

import com.castellanos94.solutions.Solution;

public interface Classifier {
    public void classify(Solution x);

    public static String getAttributeKey() {
        throw new UnsupportedOperationException("the method is not defined yet.");
    }
}