package com.castellanos94.preferences;

import com.castellanos94.datatype.Data;
import com.castellanos94.solutions.Solution;

public abstract class Preference<S extends Solution> {
    public abstract int compare(S x, S y);

    public int compare(Data[] a, Data[] b) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " not implemented yey.");
    }
}