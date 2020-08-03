package com.castellanos94.preferences;

import java.util.Comparator;

import com.castellanos94.datatype.Data;
import com.castellanos94.solutions.Solution;

public abstract class Preference<S extends Solution> implements Comparator<S>{

    public int comparae(Data[] a, Data[]b){
        throw new UnsupportedOperationException(getClass().getSimpleName()+" not implemented yey.");
    }
}