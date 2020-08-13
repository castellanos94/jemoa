package com.castellanos94.operators;

public interface Operator<Source, Result> {
    Result execute(Source source);
}