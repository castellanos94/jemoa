package com.castellanos94.decision_making;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.castellanos94.components.impl.CrowdingDistance;
import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.datatype.RealData;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.preferences.impl.ITHDM_Preference;
import com.castellanos94.problems.preferences.dtlz.*;
import com.castellanos94.problems.preferences.dtlz.DTLZPreferences;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class BestCompromiseDTLZ {
    protected int MAX_T = 1000;
    protected DTLZPreferences problem;
    protected ITHDM_Preference<DoubleSolution> preference;

    public BestCompromiseDTLZ(DTLZPreferences problem) {
        this.problem = problem;
        this.preference = new ITHDM_Preference<>(problem, problem.getInstance().getPreferenceModel(0));
    }

    /**
     * Solo se toma encuenta el dm 1 para realizar el mejor compromiso.
     * 
     * @return
     */
    public ArrayList<DoubleSolution> execute() {
        // System.out.println("call sampling");
        ArrayList<DoubleSolution> sample;
        // sample = problem.generateSampleNonDominated(MAX_T);
        // sample = problem.generateSample(MAX_T);
        sample = problem.generateRandomSample(MAX_T);
        // System.out.println("Sample size: " + sample.size());
        RealData sigma_out, sigma_in, best = RealData.ZERO, bestPref = RealData.ZERO;
        ArrayList<Pair<DoubleSolution, RealData>> candidatos = new ArrayList<>();
        for (int i = 0; i < sample.size() - 1; i++) {
            for (int j = 1; j < sample.size(); j++) {
                int value = preference.compare(sample.get(i), sample.get(j));
                sigma_out = preference.getSigmaXY();
                sigma_in = preference.getSigmaYX();
                RealData tmp = (RealData) sigma_out.minus(sigma_in);
                ImmutablePair<DoubleSolution, RealData> c;
                if (value == -2 && tmp.compareTo(bestPref) >= 0) {
                    bestPref = tmp;
                    c = new ImmutablePair<DoubleSolution, RealData>(sample.get(i), bestPref);
                    if (!candidatos.contains(c))
                        candidatos.add(c);
                } else if (value == -1) {
                    if (tmp.compareTo(best) >= 0) {
                        best = (RealData) tmp;
                        c = new ImmutablePair<DoubleSolution, RealData>(sample.get(i), best);
                        if (!candidatos.contains(c))
                            candidatos.add(c);
                    }
                }
            }

        }

        RealData bestf = (bestPref.compareTo(RealData.ZERO) != 0) ? bestPref : best;
        candidatos.removeIf(c -> c.getRight().compareTo(bestf) < 0);

        ArrayList<DoubleSolution> solutions = new ArrayList<>();
        candidatos.forEach(c -> {
            solutions.add(c.getLeft());
        });
        Iterator<DoubleSolution> iterator = solutions.iterator();
        System.out.println(solutions.size());
        while (iterator.hasNext()) {
            DoubleSolution next = iterator.next();
            for (int i = 0; i < candidatos.size(); i++) {
                DoubleSolution other = candidatos.get(i).getLeft();
                if (!next.equals(other)) {
                    int v = preference.getDominance().compare(next, other);
                    if (v > 0) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        return solutions;

    }

    public void setMAX_T(int mAX_T) {
        MAX_T = mAX_T;
    }

    public int getMAX_T() {
        return MAX_T;
    }

    public ITHDM_Preference<DoubleSolution> getPreference() {
        return preference;
    }

    public static void main(String[] args) throws IOException {
        // Tools.setSeed(8435L);

        String path = "src/main/resources/instances/dtlz/DTLZInstance.txt";
        // path = "src/main/resources/instances/dtlz/PreferenceDTLZ1_Instance_01.txt";
        DTLZ_Instance instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();
        System.out.println(instance);

        DTLZ1_P problem = new DTLZ1_P(instance, null);
        System.out.println(problem);
        BestCompromiseDTLZ bestCompromiseDTLZ = new BestCompromiseDTLZ(problem);
        ArrayList<DoubleSolution> bag = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            System.out.println("inteto " + (i + 1) + " bag: " + bag.size());
            ArrayList<DoubleSolution> candidatos = bestCompromiseDTLZ.execute();
            System.out.println("\tsize? " + candidatos.size());
            Iterator<DoubleSolution> otheIterator = candidatos.iterator();
            Iterator<DoubleSolution> iterator = bag.iterator();
            DominanceComparator<DoubleSolution> dominanceComparator = new DominanceComparator<>();
            while (iterator.hasNext()) {
                DoubleSolution a = iterator.next();
                while (otheIterator.hasNext()) {
                    DoubleSolution b = otheIterator.next();
                    if (a.equals(b)) {
                        otheIterator.remove();
                        break;
                    }
                    int v = dominanceComparator.compare(a, b);
                    // int vb = dominanceComparator.compare(b, a);
                    if (v == -1) {
                        otheIterator.remove();
                    } else if (v == 1) {
                        iterator.remove();
                        break;
                    }
                }
            }
            bag.addAll(candidatos);
            if (bag.size() > 500) {
                break;
            }
        }
        /**
         * Propuesta crear soluciones de referencia con el metodo de mejor compromiso y
         * usando una metrica de dispersion.
         */
        System.out.println("Candidatos : " + bag.size());
        CrowdingDistance<DoubleSolution> distance = new CrowdingDistance<>();
        System.out.println("Sin crowding");
        System.out.println("After crowindg");
        distance.compute(bag);
        distance.sort(bag);
        for (DoubleSolution solution : bag) {
            System.out.println(solution + " " + solution.getAttribute(distance.getAttributeKey()));
        }
        System.out.println("Candidatos : " + bag.size());

        Solution.writSolutionsToFile("bestCompromise_" + problem.getName(), new ArrayList<>(bag));
    }

}