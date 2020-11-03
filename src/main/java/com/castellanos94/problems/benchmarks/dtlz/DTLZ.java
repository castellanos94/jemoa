package com.castellanos94.problems.benchmarks.dtlz;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.Tools;

public abstract class DTLZ extends Problem<DoubleSolution> {
    protected int k;
    protected final double THRESHOLD = 10e-3;

    public DTLZ(int numberOfObjectives, int numberOfVariables) {
        this.numberOfObjectives = numberOfObjectives;
        this.numberOfDecisionVars = numberOfVariables;
        numberOfConstrains = 0;
        k = this.numberOfDecisionVars - this.numberOfObjectives + 1;

        objectives_type = new int[numberOfObjectives];
        for (int i = 0; i < numberOfObjectives; i++) {
            objectives_type[i] = Problem.MINIMIZATION;
        }
        loadBoundarys();
        setName(this.getClass().getSimpleName());
    }

    private void loadBoundarys() {
        lowerBound = new Data[numberOfDecisionVars];
        upperBound = new Data[numberOfDecisionVars];
        for (int i = 0; i < lowerBound.length; i++) {
            lowerBound[i] = new RealData(0);
            upperBound[i] = new RealData(1);
        }
    }

    /**
     * G for problem DTLZ1, DTLZ3
     * 
     * @param x vars array
     * @return g value
     */
    protected double g(double x[]) {
        double g = 0.0;
        for (int i = this.numberOfDecisionVars - k; i < this.numberOfDecisionVars; i++) {
            g += (x[i] - 0.5) * (x[i] - 0.5) - Math.cos(20.0 * Math.PI * (x[i] - 0.5));
        }

        g = 100 * (k + g);
        return g;
    }

    /**
     * G for problem DTLZ2, DTLZ4, DTLZ5
     * 
     * @param x vars array
     * @return g value
     */
    protected double g2(double x[]) {
        double g = 0.0;
        for (int i = numberOfDecisionVars - k; i < numberOfDecisionVars; i++) {
            g += (x[i] - 0.5) * (x[i] - 0.5);
        }
        return g;
    }

    public abstract double[][] getParetoOptimal3Obj() throws FileNotFoundException;

    @Override
    public void evaluateConstraint(DoubleSolution solution) {
        int cn = 0;
        double v = 0;
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

    public DTLZ setK(int k) {
        this.k = k;
        this.numberOfDecisionVars = this.k + this.numberOfObjectives - 1;
        loadBoundarys();
        return this;
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
    public String toString() {
        return String.format("%s, number_of_variables = %d, number_of_objectives = %d, k = %d", name,
                numberOfDecisionVars, numberOfObjectives, k);
    }

    /**
     * https://github.com/MOEAFramework/MOEAFramework/issues/49
     * 
     * @return
     */
    public DoubleSolution generate() {
        DoubleSolution solution = new DoubleSolution(this);

        for (int i = 0; i < numberOfObjectives - 1; i++) {
            solution.setVariable(i, Tools.getRandom().nextDouble());
        }

        for (int i = numberOfObjectives - 1; i < numberOfDecisionVars; i++) {
            solution.setVariable(i, 0.5);
        }

        evaluate(solution);

        solution.setNumberOfPenalties(0);
        solution.setPenalties(RealData.ZERO);
        return solution;
    }

    public ArrayList<DoubleSolution> generateRandomSample(int size) {
        ArrayList<DoubleSolution> solutions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            solutions.add(generate());
        }
        return solutions;
    }

    /**
     * Usando Latin Hypercube Sampling para las primera M variables de cada
     * solucion, se genera la lista de soluciones muestra del frente aproximado. No
     * se garantiza que todas estas soluciones sean no dominadas.
     * 
     * @param size sampling size
     * @return solutions uniform dist
     */
    public ArrayList<DoubleSolution> generateSample(int size) {
        ArrayList<DoubleSolution> solutions = new ArrayList<>();
        double matrix[][] = Tools.LHS(size, numberOfObjectives);
        for (int i = 0; i < size; i++) {
            DoubleSolution solution = new DoubleSolution(this);
            for (int ii = 0; ii < numberOfObjectives - 1; ii++) {
                solution.setVariable(ii, matrix[i][ii]);
            }

            for (int ii = numberOfObjectives - 1; ii < numberOfDecisionVars; ii++) {
                solution.setVariable(ii, 0.5);
            }
            evaluate(solution);
            // evaluateConstraint(solution);
            solution.setNumberOfPenalties(0);
            solution.setPenalties(RealData.ZERO);
            solutions.add(solution);
        }

        return solutions;
    }

    public ArrayList<DoubleSolution> generateSampleNonDominated(int size) {
        ArrayList<DoubleSolution> bag = generateSample(size);
        DominanceComparator<DoubleSolution> dominanceComparator = new DominanceComparator<>();
        dominanceComparator.computeRanking(bag);
        while (dominanceComparator.getSubFront(0).size() < size) {
            bag = dominanceComparator.getSubFront(0);
            bag.addAll(generateSample((bag.size() > size / 2) ? size / 4 : size / 2));
        }
        return new ArrayList<>(dominanceComparator.getSubFront(0).subList(0, size));

    }
    @Override
    public DoubleSolution generateFromVarString(String string) {
        DoubleSolution solution = new DoubleSolution(this);
        String split[] = string.trim().split(",");
        for (int i = 0; i < this.numberOfDecisionVars; i++) {
            solution.setVariable(i, Double.parseDouble(split[i]));
        }
        this.evaluate(solution);
        this.evaluateConstraint(solution);
        return solution;
    }
}