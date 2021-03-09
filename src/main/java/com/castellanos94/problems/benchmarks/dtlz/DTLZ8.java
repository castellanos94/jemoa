package com.castellanos94.problems.benchmarks.dtlz;

import java.io.FileNotFoundException;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.solutions.DoubleSolution;

public class DTLZ8 extends DTLZ {

    public DTLZ8(int numberOfObjectives, int numberOfVariables) {
        super(numberOfObjectives, numberOfVariables);
        this.numberOfConstrains = numberOfObjectives;
    }

    @Override
    public double[][] getParetoOptimal3Obj() throws FileNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void evaluate(DoubleSolution solution) {
        double factorNM = (this.numberOfDecisionVars*1.0)/this.numberOfObjectives;
        for (int j = 1; j <=this.numberOfDecisionVars; j++) {
            int lower = (int) Math.floor((j-1.0)*(factorNM));
            int upper = (int) Math.floor(j*factorNM);
            double sum = 0;
            for (int i = lower; i < upper; i++) {
                sum += solution.getVariable(i-1);
            }
            sum = (1.0/(factorNM))*sum;
            solution.setObjective(j, new RealData(sum));
        }
        // Constraint evaluated
        Data fm = solution.getObjective(this.numberOfObjectives-1);
        int numberOfPenaltieViolated = 0;
        Data min = null;
        for (int i = 0; i < this.numberOfObjectives - 1; i++) {
            for (int j = 0; j < this.numberOfObjectives -1; j++) {
                if(i!=j){
                    Data sumInnerGM = solution.getObjective(i).plus(solution.getObjective(j));
                    if(min==null){
                        min = sumInnerGM.copy();
                    }else if(min.compareTo(sumInnerGM) > 0){
                        min = sumInnerGM;
                    }
                }
            }
        }
        Data accumulatedConstraint = RealData.ZERO;
        for (int i = 0; i < this.numberOfObjectives - 1; i++) {
            Data gj = fm.plus(solution.getObjective(i).times(4)).minus(-1);
            solution.setResource(i, gj);
            if(gj.compareTo(0) < 0){
                numberOfPenaltieViolated ++;
                accumulatedConstraint = accumulatedConstraint.plus(gj);
            }            
        }
        solution.setResource(this.numberOfObjectives-1, solution.getObjective(this.numberOfObjectives-1).times(2).plus(min).minus(1));
        if(solution.getResource(this.numberOfObjectives-1).compareTo(0) < 0){
            numberOfPenaltieViolated ++;
            accumulatedConstraint = accumulatedConstraint.plus(solution.getResource(this.numberOfObjectives-1));
        }          
        solution.setNumberOfPenalties(numberOfPenaltieViolated);
        if(accumulatedConstraint==null){
            accumulatedConstraint = RealData.ZERO;
        }
        solution.setPenalties(accumulatedConstraint);
    }
}
