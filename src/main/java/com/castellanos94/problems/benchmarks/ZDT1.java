package com.castellanos94.problems.benchmarks;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.IntegerData;
import com.castellanos94.datatype.RealData;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

public class ZDT1 extends Problem {
    // private List<Integer> index;
    public ZDT1() {
        this(30);
    }

    public ZDT1(int numberOfDecisionVars) {
        this.numberOfDecisionVars = numberOfDecisionVars;
        this.numberOfObjectives = 2;
        this.numberOfConstrains = 0;
        // this.index = IntStream.range(0,
        // this.getnDecisionVars()).boxed().collect(Collectors.toList());
        this.objectives_type = new int[numberOfObjectives];
        for (int i = 0; i < objectives_type.length; i++) {
            objectives_type[i] = Problem.MINIMIZATION;
        }
        this.lowerBound = new RealData[numberOfDecisionVars];
        this.upperBound = new RealData[numberOfDecisionVars];
        for (int i = 0; i < numberOfDecisionVars; i++) {
            this.lowerBound[i] = RealData.ZERO;
            this.upperBound[i] = RealData.ONE;
        }
    }

    @Override
    public void evaluate(Solution solution) {
        // Double f1 = solution.getDecision_vars()[0].doubleValue();

        try {
            solution.setObjective(0, (Data) solution.getDecision_vars().get(0).clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        RealData g = new RealData(0);
        RealData f2 = new RealData(0);
        RealData sum = new RealData(0);

        for (int i = 1; i < numberOfDecisionVars; i++) {
            sum = (RealData) sum.addition(solution.getDecision_vars().get(i));
        }
        // g = (RealData) RealData.ONE.addition(new RealData(9).multiplication(g));
        Data tmp = new RealData(9).division(solution.getDecision_vars().size() - 1);
        g = (RealData) RealData.ONE.addition(tmp.multiplication(sum));
        // h = (RealData)
        // RealData.ONE.subtraction(solution.getObjectives().get(0).division(g).sqr());
        tmp = solution.getObjectives().get(0).division(g);
        f2 = (RealData) g.multiplication(RealData.ONE.subtraction(tmp.sqr()));
        solution.setObjective(1, f2);

        evaluateConstraints(solution);
    }

    @Override
    public int evaluateConstraints(Solution solution) {
        int count = 0;
        for (int i = 0; i < numberOfDecisionVars; i++) {
            if (solution.getDecision_vars().get(i).compareTo(0.0) < 0
                    || solution.getDecision_vars().get(i).compareTo(1.0) > 0) {
                count++;
            }
        }
        solution.setN_penalties(count);
        solution.setPenalties(new IntegerData(0));
        return 0;
    }

    @Override
    public Solution randomSolution() {
        Solution solution = new Solution(this);
        for (int i = 0; i < this.numberOfDecisionVars; i++) {
            solution.setDecisionVar(i, new RealData(Tools.getRandom().nextDouble()));
        }
        return solution;
    }

}