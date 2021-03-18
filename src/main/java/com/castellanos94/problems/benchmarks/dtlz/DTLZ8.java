package com.castellanos94.problems.benchmarks.dtlz;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.solutions.DoubleSolution;

/**
 * DTLZ8
 * <p>
 * Problem define in: Deb, K., Thiele, L., Laumanns, M., & Zitzler, E. (n.d.).
 * Scalable Test Problems for Evolutionary Multiobjective Optimization.
 * Evolutionary Multiobjective Optimization, 105–145.
 * doi:10.1007/1-84628-137-7_6
 */
public class DTLZ8 extends DTLZ {

    public DTLZ8(int numberOfObjectives, int numberOfVariables) {
        super(numberOfObjectives, numberOfVariables);
        this.numberOfConstrains = numberOfObjectives;
    }

    public DTLZ8() {
        super(3, 10 * 3);
    }

    @Override
    public double[][] getParetoOptimal3Obj() throws FileNotFoundException {
        return null;
    }

    @Override
    public void evaluate(DoubleSolution solution) {
        double factorNM = (this.numberOfDecisionVars * 1.0) / this.numberOfObjectives;
        for (int j = 0; j < this.numberOfObjectives; j++) {
            int lower = (int) Math.floor(j * factorNM);
            int upper = (int) Math.floor((j + 1.0) * factorNM);
            double sum = 0;
            for (int i = lower; i < upper; i++) {
                /*
                 * int index = 0; if (i != 0) { index = i - 1; } if (index >=
                 * this.numberOfDecisionVars) index = this.numberOfDecisionVars - 1;
                 */
                sum += solution.getVariable(i);
            }
            solution.setObjective(j, new RealData(sum / factorNM));
        }
    }
    @Override
    public void evaluateConstraint(DoubleSolution solution) {

        // Constraint evaluated
        int numberOfPenaltieViolated = 0;
        Data min = null;
        for (int i = 0; i < this.numberOfObjectives - 1; i++) {
            for (int j = 0; j < this.numberOfObjectives - 1; j++) {
                if (i != j) {
                    Data sumInnerGM = solution.getObjective(i).plus(solution.getObjective(j));
                    if (min == null) {
                        min = sumInnerGM.copy();
                    } else if (min.compareTo(sumInnerGM) > 0) {
                        min = sumInnerGM;
                    }
                }
            }
        }
        Data accumulatedConstraint = RealData.ZERO;
        ArrayList<Data> res = new ArrayList<>();
        for (int i = 0; i < this.numberOfObjectives; i++) {
            res.add(null);
        }
        solution.setResources(res);
        Data fm = solution.getObjective(this.numberOfObjectives - 1);

        for (int i = 0; i < this.numberOfObjectives - 1; i++) {
            Data gj = fm.plus(solution.getObjective(i).times(4)).minus(-1);
            solution.setResource(i, gj);
            if (gj.compareTo(0) < 0) {
                numberOfPenaltieViolated++;
                accumulatedConstraint = accumulatedConstraint.plus(gj);
            }
        }
        solution.setResource(this.numberOfObjectives - 1,
                solution.getObjective(this.numberOfObjectives - 1).times(2).plus(min).minus(1));
        if (solution.getResource(this.numberOfObjectives - 1).compareTo(0) < 0) {
            numberOfPenaltieViolated++;
            accumulatedConstraint = accumulatedConstraint.plus(solution.getResource(this.numberOfObjectives - 1));
        }
        solution.setNumberOfPenalties(numberOfPenaltieViolated);
        if (accumulatedConstraint == null) {
            accumulatedConstraint = RealData.ZERO.copy();
        }
        solution.setPenalties(accumulatedConstraint);
        int cn = numberOfPenaltieViolated;
        double v = accumulatedConstraint.doubleValue();
        for (int i = 0; i < numberOfDecisionVars; i++) {
            if (solution.getVariable(i).compareTo(lowerBound[i].doubleValue()) < 0) {
                cn++;
                v += lowerBound[i].doubleValue() - solution.getVariable(i).doubleValue();
            } else if (solution.getVariable(i).compareTo(upperBound[i].doubleValue()) > 0) {
                cn++;
                v += upperBound[i].doubleValue() - solution.getVariable(i).doubleValue();
            }
        }
        solution.setPenalties(new RealData(v));
        solution.setNumberOfPenalties(cn);
    }
    
}
