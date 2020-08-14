package com.castellanos94.indicators;

import java.util.List;

import com.castellanos94.datatype.Data;
import com.castellanos94.solutions.Solution;

public interface Indicator<S extends Solution<?>> {
    Data evaluate(List<S> solution);
}