package com.castellanos94.operators;

import java.util.ArrayList;

public interface SelectionOperator<Source> extends Operator<ArrayList<Source>, Void> {

    public ArrayList<Source> getParents();

    public void setPopulationSize(int size);
}