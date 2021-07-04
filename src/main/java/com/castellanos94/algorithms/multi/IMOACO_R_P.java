package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import com.castellanos94.datatype.Data;
import com.castellanos94.mcda.SatClassifier;
import com.castellanos94.problems.GDProblem;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.ExtraInformation;
import com.castellanos94.utils.HeapSort;
import com.castellanos94.utils.Tools;

/**
 * IMOACO_R with Preferencen Incorporation, Satisfaction classifier with four
 * classes.
 * 
 * @see SatClassifier
 * @see IMOACO_R
 */
public class IMOACO_R_P<S extends DoubleSolution> extends IMOACO_R<S> implements ExtraInformation {
    protected SatClassifier<S> classifier;
    protected Comparator<S> comparisonOfFourTypes;
    protected final boolean isFirstRank;

    /**
     * 
     * @param problem       continuos problem
     * @param maxIterations G_max
     * @param q             diversification process control parameter
     * @param xi            convergence rate control parameter
     * @param h             proportional parameter, using for the construction of
     *                      the simplex-lattice on the SLD in order to create set of
     *                      N convex weight vectors. N is equally used as the number
     *                      of ants
     * @param isFirstRank   If true, the ranking follows this order in case of a tie
     *                      Class >> Rank >> U * >> L2 Norm otherwise Rank >> Class
     *                      >> U * >> L2 Norm
     * @see DoubleSolution
     */
    public IMOACO_R_P(Problem<S> problem, int maxIterations, double q, double xi, int h, boolean isFirstRank) {
        super(problem, maxIterations, q, xi, h);
        this.classifier = new SatClassifier<>((GDProblem<S>) problem);
        this.isFirstRank = isFirstRank;
        loadComparatorRank(isFirstRank);
    }

    /**
     * 
     * @param problem       continuos problem
     * @param maxIterations G_max
     * @param N             population size
     * @param q             diversification process control parameter
     * @param xi            convergence rate control parameter
     * @param h             proportional parameter, using for the construction of
     *                      the simplex-lattice on the SLD in order to create set of
     *                      N convex weight vectors. N is equally used as the number
     *                      of ants
     * @param isFirstRank   If true, the ranking follows this order in case of a tie
     *                      Class >> Rank >> U * >> L2 Norm otherwise Rank >> Class
     *                      >> U * >> L2 Norm
     * @see DoubleSolution
     */
    public IMOACO_R_P(Problem<S> problem, int maxIterations, int N, double q, double xi, int h, boolean isFirstRank) {
        super(problem, maxIterations, N, q, xi, h);
        this.classifier = new SatClassifier<>((GDProblem<S>) problem);
        this.isFirstRank = isFirstRank;
        /*
         * // (1) this.comparisonOfFourTypes = (a, b) -> Integer.compare(a.getRank(),
         * b.getRank()); // (2) this.comparisonOfFourTypes =
         * comparisonOfFourTypes.thenComparing((a, b) -> { Data ua = (Data)
         * a.getAttribute(BEST_UTILITY_KEY); Data ub = (Data)
         * b.getAttribute(BEST_UTILITY_KEY); return ua.compareTo(ub); }); // (3)
         * this.comparisonOfFourTypes = comparisonOfFourTypes.thenComparing((a, b) -> {
         * Data d1 = Tools.NORML2(a.getObjectives()); Data d2 =
         * Tools.NORML2(b.getObjectives()); return d1.compareTo(d2); });
         */
        loadComparatorRank(isFirstRank);
    }

    private void loadComparatorRank(boolean isFirstRank) {
        if (isFirstRank) {
            // (1)
            this.comparisonOfFourTypes = (a, b) -> {
                int integerClassA = getSatClass(a);
                int integerClassB = getSatClass(a);
                return Integer.compare(integerClassA, integerClassB);
            };
            // (2)
            this.comparisonOfFourTypes = this.comparisonOfFourTypes
                    .thenComparing((a, b) -> Integer.compare(a.getRank(), b.getRank()));
        } else {
            // (1)
            this.comparisonOfFourTypes = (a, b) -> Integer.compare(a.getRank(), b.getRank());
            // (2)
            this.comparisonOfFourTypes = this.comparisonOfFourTypes.thenComparing((a, b) -> {
                int integerClassA = getSatClass(a);
                int integerClassB = getSatClass(a);
                return Integer.compare(integerClassA, integerClassB);
            });
        }
        // (3)
        this.comparisonOfFourTypes = this.comparisonOfFourTypes.thenComparing((a, b) -> {
            Data ua = (Data) a.getAttribute(BEST_UTILITY_KEY);
            Data ub = (Data) b.getAttribute(BEST_UTILITY_KEY);
            return ua.compareTo(ub);
        });
        // (4)
        this.comparisonOfFourTypes = this.comparisonOfFourTypes.thenComparing((a, b) -> {
            Data d1 = Tools.NORML2(a.getObjectives());
            Data d2 = Tools.NORML2(b.getObjectives());
            return d1.compareTo(d2);
        });
    }

    @Override
    public String toString() {
        return "IMOACO_R-P [isFirstRank=" + isFirstRank + ", " + h + ", N=" + N + ", h=" + h + ", maxIterations="
                + maxIterations + ", q=" + q + ", xi=" + xi + ", MAX RECORD SIZE=" + MAX_RECORD_SIZE + "]";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        this.init_time = System.currentTimeMillis();
        ArrayList<ArrayList<Data>> LAMBDA = generateWeight();
        HeapSort<S> sorted4Criterial = new HeapSort<>(comparisonOfFourTypes);
        this.solutions = new ArrayList<>(this.N);
        // pheromenes T = solutions
        for (int index = 0; index < this.N; index++) {
            S randomSolution = problem.randomSolution();
            problem.evaluate(randomSolution);
            problem.evaluateConstraint(randomSolution);
            this.solutions.add(randomSolution);
        }
        this.idealPoint = new ArrayList<>(solutions.get(0).getObjectives());
        this.nadirPoint = new ArrayList<>(solutions.get(0).getObjectives());
        for (S p : solutions) {
            for (int index = 0; index < problem.getNumberOfObjectives(); index++) {
                if (idealPoint.get(index).compareTo(p.getObjective(index)) > 0) {
                    idealPoint.set(index, p.getObjective(index).copy());
                }
                if (nadirPoint.get(index).compareTo(p.getObjective(index)) < 0) {
                    nadirPoint.set(index, p.getObjective(index).copy());
                }
            }
        }
        normalize(this.solutions, idealPoint, nadirPoint);
        this.record = new ArrayList<>();
        saveRegisterInRecord(nadirPoint);
        classifySolutions(solutions);
        R2Ranking(solutions, idealPoint, LAMBDA);
        // this.solutions.sort((a, b) -> Integer.compare(a.getRank(), b.getRank()));
        sorted4Criterial.sort(solutions);
        ArrayList<Data> zmin = (ArrayList<Data>) idealPoint.clone();
        ArrayList<Data> zmax = (ArrayList<Data>) nadirPoint.clone();
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // The search engine method creates a new solution for each ant and returns all
            // the solutions made.
            ArrayList<S> ns = searchEngine(solutions);
            updateReferencePoint(zmin, zmax, ns, iteration);
            ArrayList<S> psi = new ArrayList<>(solutions);
            psi.addAll(ns);
            normalize(psi, idealPoint, nadirPoint);
            R2Ranking(psi, idealPoint, LAMBDA);
            // Classificamos y agregamos directo al las soluciones
            classifySolutions(psi);
            int indexSolution = 0;
            /*
             * for (; indexSolution < this.N && indexSolution < classifySolutions.size();
             * indexSolution++) { S tmp = (S) classifySolutions.get(indexSolution).copy();
             * tmp.setRank(1); this.solutions.set(indexSolution, tmp); }
             * psi.removeAll(classifySolutions);
             */
            // Ordenar PSI en forma creciente con respecto a los criterios (1) rank, (2) u*
            // y (3) norma L_2
            sorted4Criterial.sort(psi);
            // Copiar en Tau los primeros elementos de psi
            for (int i = 0; indexSolution < this.N; i++, indexSolution++) {
                this.solutions.set(indexSolution, (S) psi.get(i).copy());
            }
            R2Ranking(solutions, idealPoint, LAMBDA);
        }
        this.computeTime = System.currentTimeMillis() - this.init_time;
    }

    protected int getSatClass(S a) {
        return (int) a.getAttribute(getAttributeKey());
    }

    protected ArrayList<S> classifySolutions(ArrayList<S> solutions) {
        HashMap<String, ArrayList<S>> map = classifier.classify(solutions);
        ArrayList<S> toAdd = new ArrayList<>();
        map.forEach((key, v) -> {
            if (key.equals(SatClassifier.HSAT_CLASS_TAG)) {
                for (S _s : v) {
                    _s.setAttribute(getAttributeKey(), 1);
                    toAdd.add(_s);
                }
            } else if (key.equals(SatClassifier.SAT_CLASS_TAG)) {
                for (S _s : v) {
                    _s.setAttribute(getAttributeKey(), 2);
                    toAdd.add(_s);
                }
            } else if (key.equals(SatClassifier.DIS_CLASS_TAG)) {
                for (S _s : v) {
                    _s.setAttribute(getAttributeKey(), 3);
                }
            } else {
                for (S _s : v) {
                    _s.setAttribute(getAttributeKey(), 4);
                }
            }
        });
        return toAdd;
    }

    @Override
    public String getAttributeKey() {
        return "imoacorp-class";
    }

    @Override
    public IMOACO_R_P<S> copy() {
        return new IMOACO_R_P<>(problem, maxIterations, N, q, xi, h, isFirstRank);
    }
}
