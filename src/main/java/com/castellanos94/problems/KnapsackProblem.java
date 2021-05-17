package com.castellanos94.problems;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.castellanos94.datatype.IntegerData;
import com.castellanos94.instances.KnapsackIntance;
import com.castellanos94.solutions.BinarySolution;

public class KnapsackProblem extends Problem<BinarySolution> {
    protected IntegerData w[];
    protected IntegerData b[];
    protected IntegerData capacity;
    private List<Integer> index;

    public KnapsackProblem(KnapsackIntance instance) {
        this.instance = instance;
        this.numberOfDecisionVars = instance.getData("num_elements").intValue();
        this.numberOfConstrains = 1;
        this.numberOfObjectives = 1;

        this.objectives_type = new int[1];
        this.objectives_type[0] = Problem.MAXIMIZATION;

        this.w = (IntegerData[]) instance.getDataVector("weights");
        this.b = (IntegerData[]) instance.getDataVector("benefits");
        this.capacity = (IntegerData) instance.getData("capacity");
        this.index = IntStream.range(0, this.getNumberOfDecisionVars()).boxed().collect(Collectors.toList());

    }

    @Override
    public void evaluate(BinarySolution solution) {
        IntegerData current_w = new IntegerData(0);
        IntegerData current_b = new IntegerData(0);
        for (int i = 0; i < numberOfDecisionVars; i++) {
            if (solution.getVariable(0).get(i)) {
                current_w = (IntegerData) current_w.plus(w[i]);
                current_b = (IntegerData) current_b.plus(b[i]);
            }
        }
        solution.setResource(0, current_w);
        solution.setObjective(0, current_b);
    }

    @Override
    public void evaluateConstraint(BinarySolution sol) {

        IntegerData cw = (IntegerData) sol.getResources().get(0);
        if (cw.compareTo(capacity) > 0) {
            sol.setNumberOfPenalties(1);
            sol.setPenalties(capacity.minus(cw));
        } else {
            sol.setPenalties(new IntegerData(0));
        }
    }

    @Override
    public BinarySolution randomSolution() {
        BinarySolution solution = new BinarySolution(this);
        Collections.shuffle(index);
        IntegerData current_w = new IntegerData(0);
        IntegerData current_b = new IntegerData(0);

        for (int i = 0; i < numberOfDecisionVars; i++) {
            IntegerData tmp = (IntegerData) current_w.plus(w[index.get(i)]);
            if (tmp.compareTo(capacity) <= 0) {
                solution.getVariable(0).set(index.get(i));
                current_w = tmp;
                current_b = (IntegerData) current_b.plus(b[i]);
            }
        }
        solution.setObjective(0, current_b);
        solution.setResource(0, current_w);
        return solution;
    }

    @Override
    public BinarySolution getEmptySolution() {
        return new BinarySolution(this);
    }
}