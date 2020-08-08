package com.castellanos94.problems;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.IntegerData;
import com.castellanos94.datatype.Interval;
import com.castellanos94.instances.PspIntervalInstance_GD;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

public class Psp_Interval_GD extends Problem {
    protected PspIntervalInstance_GD instance;
    private List<Integer> positions;

    public Psp_Interval_GD(PspIntervalInstance_GD instance_GD) {
        this.instance = instance_GD;
        this.numberOfObjectives = this.instance.getNumObjectives();
        this.numberOfConstrains = 1;
        this.numberOfDecisionVars = this.instance.getNumProjects();
        this.lowerBound = new Data[this.numberOfDecisionVars];
        this.upperBound = new Data[this.numberOfDecisionVars];

        for (int i = 0; i < lowerBound.length; i++) {
            lowerBound[i] = IntegerData.ZERO;
            upperBound[i] = IntegerData.ONE;
        }
        positions = IntStream.range(0, numberOfDecisionVars).boxed().collect(Collectors.toList());
    }

    @Override
    public void evaluate(Solution solution) {
        // TODO Auto-generated method stub

    }

    @Override
    public int evaluateConstraints(Solution solution) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Solution randomSolution() {
        Solution sol = new Solution(this);

        Collections.shuffle(positions);
        Interval[][] projects = instance.getProjects();
        Interval budget = instance.getBudget();
        Interval current_budget = new Interval(0);
        for (int i = 0; i < positions.size(); i++) {
            if (projects[positions.get(i)][0].plus(current_budget).compareTo(budget) <= 0) {
                sol.setDecisionVar(positions.get(i), new IntegerData(1));
                current_budget = (Interval) current_budget.plus(projects[positions.get(i)][0]);
            } else {
                sol.setDecisionVar(positions.get(i), new IntegerData(0));
            }
        }
        sol.setResource(0, current_budget);
        return sol;
    }

    public static void main(String[] args) throws FileNotFoundException {
        PspIntervalInstance_GD ins = (PspIntervalInstance_GD) new PspIntervalInstance_GD(
                "src/main/resources/instances/gd/GD_ITHDM-UFCA.txt").loadInstance();
        Psp_Interval_GD problem = new Psp_Interval_GD(ins);
        Solution[] solutions = new Solution[4];
        for (int i = 0; i < solutions.length; i++) {
            solutions[i] = problem.randomSolution();
            System.out.println(solutions[i]);
        }

    }
}