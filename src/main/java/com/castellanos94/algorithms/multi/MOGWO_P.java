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
                x.setAttribute("class-nc", "hsat");
            } else if (iclass[1] > 0) {
                cSat.add(x);
                x.setAttribute("class-nc", "sat");
            } else if (iclass[2] > 0) {
                cDis.add(x);
                x.setAttribute("class-nc", "dis");
            } else {
                cHDis.add(x);
                x.setAttribute("class-nc", "hdis");
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
        alphaWolf = (S) iterator.next().copy();
        iterator.remove();

        // Select beta and remove to exclude
        if (!cHSat.isEmpty()) {
            _solutions = cHSat;
        }
        if (!cSat.isEmpty() || cHSat.size() < 2) {
            _solutions = cSat;
        }
        if (!cDis.isEmpty() || cSat.size() < 2) {
            _solutions = cDis;
            _solutions.addAll(cHDis);
        }
        this.selectionOperator.execute(parents);
        parents = this.selectionOperator.getParents();
        iterator = parents.iterator();
        boolean isBetaWolf = false, isDeltaWolf = false;
        if (iterator.hasNext()) {
            betaWolf = (S) iterator.next().copy();
            iterator.remove();
        } else {
            int index = -1;
            do {
                index = Tools.getRandomNumberInRange(0, wolves.size()).intValue();
            } while (wolves.get(index).equals(alphaWolf));
            betaWolf = (S) wolves.get(index).copy();
            isBetaWolf = true;
        }
        // Select delta and remove to exclude
        if (!cHSat.isEmpty()) {
            _solutions = cHSat;
        }
        if (!cSat.isEmpty() || cHSat.size() < 3) {
            _solutions = cSat;
        }
        if (!cDis.isEmpty() || cSat.size() < 3) {
            _solutions = cDis;
            _solutions.addAll(cHDis);
        }
        this.selectionOperator.execute(parents);
        parents = this.selectionOperator.getParents();
        iterator = parents.iterator();
        if (iterator.hasNext()) {
            deltaWolf = (S) iterator.next().copy();
            iterator.remove();
        } else {
            int index = -1;
            do {
                index = Tools.getRandomNumberInRange(0, wolves.size()).intValue();
            } while (wolves.get(index).equals(betaWolf) || wolves.get(index).equals(alphaWolf));
            deltaWolf = (S) wolves.get(index).copy();
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

    @Override
    public String toString() {
        return "MOGWO-P [MAX_ITERATIONS=" + MAX_ITERATIONS + ", nGrid=" + nGrid + ", Problem=" + this.problem.toString()
                + "]";
    }
}
