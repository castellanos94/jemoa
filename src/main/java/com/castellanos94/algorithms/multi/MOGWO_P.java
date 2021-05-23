package com.castellanos94.algorithms.multi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.castellanos94.mcda.INTERCLASSnC;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.DoubleSolution;

public class MOGWO_P<S extends DoubleSolution> extends MOGWO_V<S> {
    protected INTERCLASSnC<S> classifier;

    public MOGWO_P(Problem<S> problem, int populationSize, int MAX_ITERATIONS, int nGrid,
            RepairOperator<S> repairOperator) {
        super(problem, populationSize, MAX_ITERATIONS, nGrid, repairOperator);
        classifier = new INTERCLASSnC<>(problem);
    }

    @Override
    protected void selectLeader(ArrayList<S> solutions) {
        // Filter
        HashMap<String, ArrayList<S>> map = classifier.classify(solutions);
        ArrayList<S> cHSat = map.get(INTERCLASSnC.HSAT_CLASS_TAG);
        ArrayList<S> cSat = map.get(INTERCLASSnC.SAT_CLASS_TAG);
        ArrayList<S> cDis = map.get(INTERCLASSnC.DIS_CLASS_TAG);
        ArrayList<S> cHDis = map.get(INTERCLASSnC.HDIS_CLASS_TAG);

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
        alphaWolf = iterator.next();
        iterator.remove();

        // Select beta and remove to exclude
        _solutions = new ArrayList<>();
        String[] classAttribute = { INTERCLASSnC.HSAT_CLASS_TAG, INTERCLASSnC.SAT_CLASS_TAG, INTERCLASSnC.DIS_CLASS_TAG,
                INTERCLASSnC.HDIS_CLASS_TAG };
        int indexClass = 0;
        while (_solutions.size() < 2 && indexClass < classAttribute.length) {
            for (S sol : (!parents.isEmpty()) ? parents : solutions) {
                if (((String) sol.getAttribute(INTERCLASSnC.CLASS_KEY)).equalsIgnoreCase(classAttribute[indexClass])) {
                    if (!sol.equals(alphaWolf))
                        _solutions.add(sol);
                }
            }
            indexClass++;
        }
        // Select Beta
        this.selectionOperator.execute(_solutions);
        parents = this.selectionOperator.getParents();
        iterator = parents.iterator();
        boolean isBetaWolf = false, isDeltaWolf = false;
        if (iterator.hasNext()) {
            betaWolf = iterator.next();
            iterator.remove();
        } else {
            S _c = null;
            this.selectionOperator.execute(wolves);
            int index = 0;
            do {
                _c = selectionOperator.getParents().get(index++);
            } while (_c.equals(alphaWolf) && index < selectionOperator.getParents().size());
            betaWolf = (S) _c.copy();
            isBetaWolf = true;
        }
        // Select delta and remove to exclude
        indexClass = 0;
        while (_solutions.size() < 3 && indexClass < classAttribute.length) {
            for (S sol : (!parents.isEmpty()) ? parents : solutions) {
                if (((String) sol.getAttribute(INTERCLASSnC.CLASS_KEY)).equalsIgnoreCase(classAttribute[indexClass])) {
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
            deltaWolf = iterator.next();
            iterator.remove();
        } else {
            S _c = null;
            this.selectionOperator.execute(wolves);
            int index = 0;
            do {
                _c = selectionOperator.getParents().get(index++);
            } while (_c.equals(betaWolf) || _c.equals(alphaWolf) && index < selectionOperator.getParents().size());
            deltaWolf = (S) wolves.get(index).copy();
            isDeltaWolf = true;
        }

        // add back alpha, beta and deta to the archive

        if (!this.archiveSelection.getParents().contains(alphaWolf))
            this.archiveSelection.getParents().add(alphaWolf);
        if (!this.archiveSelection.getParents().contains(betaWolf) && !isBetaWolf)
            this.archiveSelection.getParents().add(betaWolf);
        if (!this.archiveSelection.getParents().contains(deltaWolf) && !isDeltaWolf)
            this.archiveSelection.getParents().add(deltaWolf);

    }

    @Override
    public String toString() {
        return "MOGWO-P [MAX_ITERATIONS=" + MAX_ITERATIONS + ", nGrid=" + nGrid + ", Problem=" + this.problem.toString()
                + "]";
    }
}
