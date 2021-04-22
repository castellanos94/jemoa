package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.Iterator;

import com.castellanos94.operators.RepairOperator;
import com.castellanos94.preferences.impl.InterClassnC;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.Tools;

public class MOGWO_P<S extends DoubleSolution> extends MOGWO<S> {
    protected InterClassnC<S> classifier;

    public MOGWO_P(Problem<S> problem, int populationSize, int MAX_ITERATIONS, int nGrid,
            RepairOperator<S> repairOperator) {
        super(problem, populationSize, MAX_ITERATIONS, nGrid, repairOperator);
        classifier = new InterClassnC<>(problem);
    }

    @Override
    protected void selectLeader(ArrayList<S> solutions) {
        // Filter
        ArrayList<S> cHSat = new ArrayList<>();
        ArrayList<S> cSat = new ArrayList<>();
        ArrayList<S> cDis = new ArrayList<>();
        ArrayList<S> cHDis = new ArrayList<>();
        for (S x : solutions) {
            classifier.classify(x);
            int[] iclass = (int[]) x.getAttribute(classifier.getAttributeKey());
            if (iclass[0] > 0) {
                cHSat.add(x);
                x.setAttribute(keyClass(), "hsat");
            } else if (iclass[1] > 0) {
                cSat.add(x);
                x.setAttribute(keyClass(), "sat");
            } else if (iclass[2] > 0) {
                cDis.add(x);
                x.setAttribute(keyClass(), "dis");
            } else {
                cHDis.add(x);
                x.setAttribute(keyClass(), "hdis");
            }
        }
        ArrayList<S> _solutions = null;
        if (!cHSat.isEmpty()) {
            _solutions = cHSat;
        } else if (!cSat.isEmpty()) {
            _solutions = cSat;
        } else if (!cDis.isEmpty()) {
            _solutions = cDis;
        } else {
            _solutions = cHDis;
        }
        // Select Leader with roulette
        this.selectionOperator.execute(_solutions);
        ArrayList<S> parents = this.selectionOperator.getParents();
        Iterator<S> iterator = parents.iterator();

        // Select alfa and remove to exclude
        alphaWolf =  iterator.next();
        iterator.remove();

        // Select beta and remove to exclude
        _solutions = new ArrayList<>();
        String[] classAttribute = { "hsat", "sat", "dis", "hdis" };
        int indexClass = 0;
        while (_solutions.size() < 2 && indexClass < classAttribute.length) {
            for (S sol : (!parents.isEmpty()) ? parents : solutions) {
                if (((String) sol.getAttribute(keyClass())).equalsIgnoreCase(classAttribute[indexClass])) {
                    if (!sol.equals(alphaWolf))
                        _solutions.add(sol);
                }
            }
            indexClass++;
        }
        this.selectionOperator.execute(_solutions);
        parents = this.selectionOperator.getParents();
        iterator = parents.iterator();
        boolean isBetaWolf = false, isDeltaWolf = false;
        if (iterator.hasNext()) {
            betaWolf = iterator.next();
            iterator.remove();
        } else {
            int index = -1;
            do {
                index = Tools.getRandomNumberInRange(0, wolves.size()).intValue();
            } while (wolves.get(index).equals(alphaWolf));
            betaWolf =  wolves.get(index);
            isBetaWolf = true;
        }
        // Select delta and remove to exclude
        indexClass = 0;
        while (_solutions.size() < 3 && indexClass < classAttribute.length) {
            for (S sol : (!parents.isEmpty()) ? parents : solutions) {
                if (((String) sol.getAttribute(keyClass())).equalsIgnoreCase(classAttribute[indexClass])) {
                    if (!sol.equals(alphaWolf) && !sol.equals(betaWolf))
                        _solutions.add(sol);
                }
            }
            indexClass++;
        }
        this.selectionOperator.execute(_solutions);
        parents = this.selectionOperator.getParents();
        iterator = parents.iterator();
        if (iterator.hasNext()) {
            deltaWolf =  iterator.next();
            iterator.remove();
        } else {
            int index = -1;
            do {
                index = Tools.getRandomNumberInRange(0, wolves.size()).intValue();
            } while (wolves.get(index).equals(betaWolf) || wolves.get(index).equals(alphaWolf));
            deltaWolf =  wolves.get(index);
            isDeltaWolf = true;
        }

        // add back alpha, beta and deta to the archive
        if (!solutions.contains(alphaWolf))
            solutions.add(alphaWolf);
        if (!solutions.contains(betaWolf) && !isBetaWolf)
            solutions.add(betaWolf);
        if (!solutions.contains(deltaWolf) && !isDeltaWolf)
            solutions.add(deltaWolf);

    }

    protected String keyClass() {
        return "class-nc";
    }

    @Override
    public String toString() {
        return "MOGWO-P [MAX_ITERATIONS=" + MAX_ITERATIONS + ", nGrid=" + nGrid + ", Problem=" + this.problem.toString()
                + "]";
    }
}
