package com.castellanos94.utils;

import tech.tablesaw.api.Table;

public interface Plotter {
    void plot();
    Table getTable();
}