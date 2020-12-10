package com.castellanos94.decision_making;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.datatype.RealData;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.preferences.impl.ITHDM_Dominance;
import com.castellanos94.preferences.impl.ITHDM_Preference;
import com.castellanos94.problems.preferences.dtlz.*;
import com.castellanos94.problems.preferences.dtlz.DTLZPreferences;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.ExtraInformation;
import com.castellanos94.utils.Tools;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class BestCompromiseDTLZ implements ExtraInformation {
    protected int MAX_T = 5000;
    protected DTLZPreferences problem;
    protected ITHDM_Preference<DoubleSolution> preference;
    private DominanceComparator<DoubleSolution> dominance;

    public BestCompromiseDTLZ(DTLZPreferences problem) {
        this.problem = problem;
        this.preference = new ITHDM_Preference<>(problem, problem.getInstance().getPreferenceModel(0));
        dominance = preference.getDominance();
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
        RealData best = RealData.ZERO, bestPref = new RealData(Double.MIN_VALUE);
        ArrayList<Pair<DoubleSolution, RealData>> candidatos = new ArrayList<>();
        DoubleSolution best_compromise = null;
        for (int i = 0; i < sample.size() - 1; i++) {
            RealData sigma_out = RealData.ZERO, sigma_in = RealData.ZERO;
            for (int j = 1; j < sample.size(); j++) {
                // int value = dominance.compare(sample.get(i), sample.get(j));
                preference.compare(sample.get(i), sample.get(j));
                sigma_out = (RealData) sigma_out.plus(preference.getSigmaXY());
                sigma_in = (RealData) sigma_in.plus(preference.getSigmaYX());

                /*
                 * if (value == -2 && tmp.compareTo(bestPref) >= 0) { bestPref = tmp; c = new
                 * ImmutablePair<DoubleSolution, RealData>(sample.get(i), bestPref); if
                 * (!candidatos.contains(c)) candidatos.add(c); } else if (value == -1) { if
                 * (tmp.compareTo(best) >= 0) { best = (RealData) tmp; c = new
                 * ImmutablePair<DoubleSolution, RealData>(sample.get(i), best); if
                 * (!candidatos.contains(c)) candidatos.add(c); } }
                 */
            }
            RealData net_score = (RealData) sigma_out.minus(sigma_in);
            if (best_compromise == null || net_score.compareTo(bestPref) > 0) {
                best_compromise = sample.get(i);
                ImmutablePair<DoubleSolution, RealData> c = new ImmutablePair<DoubleSolution, RealData>(sample.get(i),
                        bestPref.copy());
                bestPref = net_score;
                if (!candidatos.contains(c)) {
                    candidatos.add(c);
                }
            }

        }

        // RealData bestf = (bestPref.compareTo(RealData.ZERO) != 0) ? bestPref : best;
        // candidatos.removeIf(c -> c.getRight().compareTo(bestf) < 0);

        ArrayList<DoubleSolution> solutions = new ArrayList<>();
        candidatos.forEach(c -> {
            // if (c.getRight().compareTo(0) > 0)
            solutions.add(c.getLeft());
        });
        /*
         * Iterator<DoubleSolution> iterator = solutions.iterator();
         * System.out.println(solutions.size()); while (iterator.hasNext()) {
         * DoubleSolution next = iterator.next(); for (int i = 0; i < candidatos.size();
         * i++) { DoubleSolution other = candidatos.get(i).getLeft(); if
         * (!next.equals(other)) { int v = preference.getDominance().compare(next,
         * other); if (v > 0) { iterator.remove(); break; } } } }
         */
        return solutions;

    }

    public DoubleSolution getBestCompromise(ArrayList<DoubleSolution> solutions) {
        DoubleSolution best_compromise = null;
        RealData best = new RealData(Double.MIN_VALUE);
        for (int i = 0; i < solutions.size() - 1; i++) {
            RealData sigma_out = RealData.ZERO, sigma_in = RealData.ZERO;
            for (int j = 1; j < solutions.size(); j++) {
                // int value = dominance.compare(solutions.get(i), solutions.get(j));
                preference.compare(solutions.get(i), solutions.get(j));
                sigma_out = (RealData) sigma_out.plus(preference.getSigmaXY());
                sigma_in = (RealData) sigma_in.plus(preference.getSigmaYX());

            }
            RealData net_score = (RealData) sigma_out.minus(sigma_in);
            if (best_compromise == null || net_score.compareTo(best) > 0) {
                solutions.get(i).setAttribute(getAttributeKey(), net_score.copy());
                best_compromise = solutions.get(i);
                best = net_score;
            }
        }

        return best_compromise;
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
        // Tools.setSeed(1L);
        String path = "src/main/resources/DTLZ_INSTANCES/DTLZ6_Instance.txt";
        DTLZ_Instance instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();
        System.out.println(instance);

        DTLZPreferences problem = new DTLZ6_P(instance);
        System.out.println(problem);
        BestCompromiseDTLZ bestCompromiseDTLZ = new BestCompromiseDTLZ(problem);
        ArrayList<DoubleSolution> bag = new ArrayList<>();       
        for (int i = 0; i < 300 && bag.size() < 500; i++) {
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
            Solution.writSolutionsToFile("bestCompromise" + File.separator + "bestCompromise_" + problem.getName(),
                new ArrayList<>(bag));
        }
        // Looking for best comprimise
        System.out.println("Best compromise");
        DoubleSolution best = bestCompromiseDTLZ.getBestCompromise(bag);
        System.out.println(best.getAttribute(bestCompromiseDTLZ.getAttributeKey()) + " - " + best.getObjectives());
        System.out.println();
        System.out.println("Candidatas");
        for (DoubleSolution s : bag) {
            if (s.getAttribute(bestCompromiseDTLZ.getAttributeKey()) != null)
                System.out.println(s.getAttribute(bestCompromiseDTLZ.getAttributeKey()) + " - " + s);

        }
        /**
         * Propuesta crear soluciones de referencia con el metodo de mejor compromiso y
         * usando una metrica de dispersion.
         */
        /*
         * System.out.println("Candidatos : " + bag.size());
         * CrowdingDistance<DoubleSolution> distance = new CrowdingDistance<>();
         * System.out.println("Sin crowding"); System.out.println("After crowindg");
         * distance.compute(bag); distance.sort(bag); for (DoubleSolution solution :
         * bag) { System.out.println(solution + " " +
         * solution.getAttribute(distance.getAttributeKey())); }
         * System.out.println("Candidatos : " + bag.size());
         */
        Solution.writSolutionsToFile("bestCompromise" + File.separator + "bestCompromise_" + problem.getName(),
                new ArrayList<>(bag));
    }

    @Override
    public String getAttributeKey() {
        return "NET_SCORE";
    }

}