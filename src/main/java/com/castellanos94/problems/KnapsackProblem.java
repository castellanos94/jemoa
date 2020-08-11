package com.castellanos94.problems;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.castellanos94.datatype.IntegerData;
import com.castellanos94.instances.KnapsackIntance;
import com.castellanos94.solutions.Solution;

public class KnapsackProblem extends Problem {
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
    public void evaluate(Solution solution) {
        IntegerData current_w = new IntegerData(0);
        IntegerData current_b = new IntegerData(0);
        for (int i = 0; i < numberOfDecisionVars; i++) {
            if (solution.getVariables().get(i).compareTo(1) == 0) {
                current_w = (IntegerData) current_w.plus(w[i]);
                current_b = (IntegerData) current_b.plus(b[i]);
            }
        }
        solution.setResource(0, current_w);
        solution.setObjective(0, current_b);
        if (current_w.compareTo(capacity) > 0) {
            solution.setN_penalties(1);
            solution.setPenalties(capacity.minus(current_w));
        } else {
            solution.setPenalties(new IntegerData(0));
        }
    }

    @Override
    public Solution randomSolution() {
        Solution solution = new Solution(this);
        Collections.shuffle(index);
        IntegerData current_w = new IntegerData(0);
        IntegerData current_b = new IntegerData(0);

        for (int i = 0; i < numberOfDecisionVars; i++) {
            IntegerData tmp = (IntegerData) current_w.plus(w[index.get(i)]);
            if (tmp.compareTo(capacity) <= 0) {
                solution.setVariables(index.get(i), new IntegerData(1));
                current_w = tmp;
                current_b = (IntegerData) current_b.plus(b[i]);
            } else {
                solution.setVariables(index.get(i), new IntegerData(0));
            }
        }
        solution.setObjective(0, current_b);
        solution.setResource(0, current_w);
        return solution;
    }

}