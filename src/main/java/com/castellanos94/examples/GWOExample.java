package com.castellanos94.examples;

import java.util.ArrayList;

import com.castellanos94.algorithms.single.GWO;
import com.castellanos94.datatype.RealData;
import com.castellanos94.operators.impl.RandomRepair;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.Tools;

public class GWOExample {
    public static void main(String[] args) {
        Tools.setSeed(1L);
        GreyWolfOptimizerTestProblem problem = new GreyWolfOptimizerTestProblem(18);
        int populationSize = 20;
        int MAX_ITERATIONS = 10000;
        System.out.println(problem);
        int runs = 31;
        ArrayList<Double> values = new ArrayList<>();
        GWO<DoubleSolution> algorithm = null;
        double time = 0;
        for (int i = 0; i < runs; i++) {
            algorithm = new GWO<>(problem, populationSize, MAX_ITERATIONS, new RandomRepair());
            algorithm.execute();
            time += algorithm.getComputeTime();
            values.add(algorithm.getSolutions().get(0).getObjective(0).doubleValue());
        }
        double mean = 0;
        for (int i = 0; i < runs; i++) {
            mean += values.get(i);
        }
        mean /= runs;
        double std = 0;
        for (Double xi : values) {
            std += (xi - mean) * (xi - mean);
        }
        std = Math.sqrt(std / runs);
        System.out.println("Compute time : " + ((time / runs) / 1000.0));
        System.out.println("Mean : " + mean);
        System.out.println("STD : " + std);

    }

    /**
     * Check original paper table 2 and 3
     */
    public static class GreyWolfOptimizerTestProblem extends Problem<DoubleSolution> {
        private final int fij;

        /**
         * Problems 1(0), 8(-418.9829*5) & 18(3)
         * 
         * @param f_ij number of problem to evalute
         */
        public GreyWolfOptimizerTestProblem(int f_ij) {
            this.fij = f_ij;
            this.numberOfObjectives = 1;
            this.numberOfConstrains = 0;
            this.objectives_type = new int[1];
            this.objectives_type[0] = Problem.MINIMIZATION;
            switch (f_ij) {
            case 1:
                this.numberOfDecisionVars = 30;

                this.lowerBound = new RealData[this.numberOfDecisionVars];
                this.upperBound = new RealData[this.numberOfDecisionVars];
                for (int i = 0; i < this.numberOfDecisionVars; i++) {
                    lowerBound[i] = new RealData(-100);
                    upperBound[i] = new RealData(100);
                }
                setName("F1-Unimodal");
                break;
            case 8:
                this.numberOfDecisionVars = 30;
                this.lowerBound = new RealData[this.numberOfDecisionVars];
                this.upperBound = new RealData[this.numberOfDecisionVars];
                for (int i = 0; i < this.numberOfDecisionVars; i++) {
                    lowerBound[i] = new RealData(-500);
                    upperBound[i] = new RealData(500);
                }
                setName("F8-MultiModal (Generalized Schwefel's Problem)");

                break;
            case 18:

                this.numberOfDecisionVars = 2;

                this.lowerBound = new RealData[this.numberOfDecisionVars];
                this.upperBound = new RealData[this.numberOfDecisionVars];
                for (int i = 0; i < this.numberOfDecisionVars; i++) {
                    lowerBound[i] = new RealData(-2);
                    upperBound[i] = new RealData(2);
                }
                setName("F18-Fixed-Dimension-MultiModal (Gold stein)");
                break;
            default:
                throw new IllegalArgumentException("Problem not implented yet");
            }

        }

        @Override
        public void evaluate(DoubleSolution solution) {
            double sum = 0;

            switch (fij) {
            case 1:
                for (Double data : solution.getVariables()) {
                    sum += data * data;
                }
                break;
            case 8:

                for (int i = 0; i < this.numberOfDecisionVars; i++) {
                    sum += solution.getVariable(i) * Math.sin(Math.sqrt(Math.abs(solution.getVariable(i))));
                }
                sum = -sum;
                break;
            case 18:
                double x[] = new double[2];
                for (int i = 0; i < x.length; i++) {
                    x[i] = solution.getVariable(i);
                }
                double first = 0.0;
                double second = 0.0;
                first = (1.0 + (x[0] + x[1] + 1.0) * (x[0] + x[1] + 1.0) * (19.0 - 14.0 * x[0] + 3.0 * x[0] * x[0]
                        - 14.0 * x[1] + 6.0 * x[0] * x[1] + 3.0 * x[1] * x[1]));
                second = 30.0 + (2.0 * x[0] - 3.0 * x[1]) * (2.0 * x[0] - 3.0 * x[1]) * (18.0 - 32.0 * x[0]
                        + 12.0 * x[0] * x[0] + 48.0 * x[1] - 36.0 * x[0] * x[1] + 27 * x[1] * x[1]);
                sum = first * second;
                break;
            default:
                break;
            }
            solution.setObjective(0, new RealData(sum));
        }

        @Override
        public void evaluateConstraint(DoubleSolution solution) {
            int npenalties = 0;
            double acum = 0;
            for (int i = 0; i < this.numberOfDecisionVars; i++) {
                double var = solution.getVariable(i);
                if (lowerBound[i].compareTo(var) > 0) {
                    npenalties++;
                    acum += lowerBound[i].doubleValue() - var;
                } else if (upperBound[i].compareTo(var) < 0) {
                    npenalties++;
                    acum += upperBound[i].doubleValue() - var;
                }
            }
            if (Double.isNaN(solution.getObjective(0).doubleValue())) {
                npenalties += 100;
                acum *= 100;
            }
            solution.setNumberOfPenalties(npenalties);
            solution.setPenalties(new RealData(-Math.abs(acum)));

        }

        @Override
        public DoubleSolution randomSolution() {
            DoubleSolution solution = new DoubleSolution(this);
            for (int i = 0; i < this.numberOfDecisionVars; i++) {
                solution.setVariable(i, Tools.getRandomNumberInRange(lowerBound[i], upperBound[i]).doubleValue());
            }
            return solution;
        }

        @Override
        public String toString() {
            return this.name + " " + numberOfDecisionVars + ", " + numberOfObjectives + ", " + numberOfConstrains;
        }

        @Override
        public DoubleSolution getEmptySolution() {
            return null;
        }

    }
}
