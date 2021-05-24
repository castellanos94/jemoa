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

    public IMOACO_R_P(Problem<S> problem, int maxIterations, double q, double xi, int h) {
        super(problem, maxIterations, q, xi, h);
        this.classifier = new SatClassifier<>((GDProblem) problem);
    }

    @Override
    public String toString() {
        return "IMOACO_R-P [N=" + N + ", h=" + h + ", maxIterations=" + maxIterations + ", q=" + q + ", xi=" + xi
                + ", MAX RECORD SIZE=" + MAX_RECORD_SIZE + "]";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        this.init_time = System.currentTimeMillis();
        ArrayList<ArrayList<Data>> LAMBDA = generateWeight();

        Comparator<S> cmp = (a, b) -> {
            int integerClassA = getSatClass(a);
            int integerClassB = getSatClass(a);
            return Integer.compare(integerClassA, integerClassB);
        };
        cmp = cmp.thenComparing((a, b) -> Integer.compare(a.getRank(), b.getRank()));
        cmp = cmp.thenComparing((a, b) -> {
            Data ua = (Data) a.getAttribute(BEST_UTILITY_KEY);
            Data ub = (Data) b.getAttribute(BEST_UTILITY_KEY);
            return ua.compareTo(ub);
        });
        cmp.thenComparing((a, b) -> {
            Data d1 = Tools.NORML2(a.getObjectives());
            Data d2 = Tools.NORML2(b.getObjectives());
            return d1.compareTo(d2);
        });
        HeapSort<S> sorted4Criterial = new HeapSort<>(cmp);
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
        R2Ranking(solutions, idealPoint, LAMBDA);
        this.solutions.sort((a, b) -> Integer.compare(a.getRank(), b.getRank()));
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
            classifySolutions(psi);
            // Ordenar PSI en forma creciente con respecto a los criterios (1) class, (2)
            // rank, (3) u* y (4) norma L_2
            sorted4Criterial.sort(psi);
            // Copiar en Tau los primeros elementos de psi
            for (int i = 0; i < this.N; i++) {
                this.solutions.set(i, (S) psi.get(i).copy());
            }
            R2Ranking(solutions, idealPoint, LAMBDA);
        }
        this.computeTime = System.currentTimeMillis() - this.init_time;
    }

    protected int getSatClass(S a) {
        return (int) a.getAttribute(getAttributeKey());
    }

    protected void classifySolutions(ArrayList<S> solutions) {
        HashMap<String, ArrayList<S>> map = classifier.classify(solutions);

        map.forEach((key, v) -> {
            if (key.equals(SatClassifier.HSAT_CLASS_TAG)) {
                for (S _s : v) {
                    _s.setAttribute(getAttributeKey(), 1);
                }
            } else if (key.equals(SatClassifier.SAT_CLASS_TAG)) {
                for (S _s : v) {
                    _s.setAttribute(getAttributeKey(), 2);
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
    }

    @Override
    public String getAttributeKey() {
        return "imoacorp-class";
    }
}
