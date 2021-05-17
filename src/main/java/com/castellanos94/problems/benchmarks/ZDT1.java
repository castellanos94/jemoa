package com.castellanos94.problems.benchmarks;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.IntegerData;
import com.castellanos94.datatype.RealData;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.Tools;

public class ZDT1 extends Problem<DoubleSolution> {
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
    public void evaluate(DoubleSolution solution) {
        // Double f1 = solution.getDecision_vars()[0].doubleValue();

        solution.setObjective(0, new RealData(solution.getVariable(0)));

        RealData g = new RealData(0);
        RealData f2 = new RealData(0);
        RealData sum = new RealData(0);

        for (int i = 1; i < numberOfDecisionVars; i++) {
            sum = (RealData) sum.plus(solution.getVariables().get(i));
        }
        // g = (RealData) RealData.ONE.addition(new RealData(9).multiplication(g));
        Data tmp = new RealData(9).div(solution.getVariables().size() - 1);
        g = (RealData) RealData.ONE.plus(tmp.times(sum));
        // h = (RealData)
        // RealData.ONE.subtraction(solution.getObjectives().get(0).division(g).sqr());
        tmp = solution.getObjectives().get(0).div(g);
        f2 = (RealData) g.times(RealData.ONE.minus(tmp.sqrt()));
        solution.setObjective(1, f2);

        evaluateConstraint(solution);
    }

    @Override
    public void evaluateConstraint(DoubleSolution solution) {
        int count = 0;
        for (int i = 0; i < numberOfDecisionVars; i++) {
            if (solution.getVariables().get(i).compareTo(0.0) < 0
                    || solution.getVariables().get(i).compareTo(1.0) > 0) {
                count++;
            }
        }
        solution.setNumberOfPenalties(count);
        solution.setPenalties(new IntegerData(0));
    }

    @Override
    public DoubleSolution randomSolution() {
        DoubleSolution solution = new DoubleSolution(this);
        for (int i = 0; i < this.numberOfDecisionVars; i++) {
            solution.setVariable(i, Tools.getRandom().nextDouble());
        }
        return solution;
    }

    @Override
    public DoubleSolution getEmptySolution() {
        return new DoubleSolution(this);
    }

}