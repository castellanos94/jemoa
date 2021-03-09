package com.castellanos94.problems.benchmarks.dtlz;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.solutions.DoubleSolution;

/**
 * DTLZ9
 * <p>
 * Problem define in: Deb, K., Thiele, L., Laumanns, M., & Zitzler, E. (n.d.).
 * Scalable Test Problems for Evolutionary Multiobjective Optimization.
 * Evolutionary Multiobjective Optimization, 105–145.
 * doi:10.1007/1-84628-137-7_6
 */
public class DTLZ9 extends DTLZ {
    public DTLZ9() {
        super(3, 3 * 10);
    }

    public DTLZ9(int numberOfObjectives, int numberOfVariables) {
        super(numberOfObjectives, numberOfVariables);
        this.numberOfConstrains = this.numberOfObjectives;
    }

    @Override
    public double[][] getParetoOptimal3Obj() throws FileNotFoundException {
        return null;
    }

    @Override
    public void evaluate(DoubleSolution solution) {
        double factorNM = this.numberOfDecisionVars * 1.0 / this.numberOfObjectives;
        for (int j = 0; j < this.numberOfObjectives; j++) {
            double sum = 0;
            for (int i = (int) (j * factorNM); i < (j + 1) * factorNM; i++) {
                sum += Math.pow(solution.getVariable(i), 0.1);
            }
            solution.setObjective(j, new RealData(sum));
        }
    }
    
    @Override
    public void evaluateConstraint(DoubleSolution solution) {

        // Constraint evaluation
        Data accumulatedConstraint = RealData.ZERO;
        ArrayList<Data> res = new ArrayList<>();
        for (int i = 0; i < this.numberOfObjectives; i++) {
            res.add(RealData.ZERO.copy());
        }
        solution.setResources(res);
        int numberOfPenaltieViolated = 0;
        Data fm = solution.getObjective(this.numberOfObjectives - 1).pow(2);
        for (int i = 0; i < this.numberOfObjectives - 1; i++) {
            Data gj = fm.plus(solution.getObjective(i).pow(2)).minus(1);
            solution.setResource(i, gj);
            if (gj.compareTo(0) < 0) {
                numberOfPenaltieViolated++;
                accumulatedConstraint = accumulatedConstraint.plus(gj);
            }
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
