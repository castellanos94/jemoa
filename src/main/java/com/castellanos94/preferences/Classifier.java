package com.castellanos94.preferences;

import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.ExtraInformation;

public abstract class Classifier implements ExtraInformation {
    public abstract void classify(Solution x);

}