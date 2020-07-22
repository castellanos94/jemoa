package com.castellanos94.preferences;

import com.castellanos94.datatype.Data;
import com.castellanos94.solutions.Solution;

public abstract class Preference<S extends Solution> {

    public abstract Integer evaluate(S a, S b);

    public Integer evaluate(Data[] a, Data[] b) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }
}