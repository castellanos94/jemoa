package com.castellanos94.decision_making;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import com.castellanos94.datatype.RealData;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.preferences.impl.ITHDM_Preference;
import com.castellanos94.problems.preferences.dtlz.DTLZ1_P;
import com.castellanos94.problems.preferences.dtlz.DTLZPreferences;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class BestCompromiseDTLZ {
    protected int MAX_T = 1000;
    protected DTLZPreferences problem;
    protected ITHDM_Preference preference;

    public BestCompromiseDTLZ(DTLZPreferences problem) {
        this.problem = problem;
        this.preference = new ITHDM_Preference(problem, problem.getInstance().getPreferenceModel(0));
    }

    /**
     * Solo se toma encuenta el dm 1 para realizar el mejor compromiso.
     * 
     * @param path to save instance
     * @return
     */
    public Solution execute(String path) {
        System.out.println("call sampling");
        ArrayList<Solution> sample = problem.generateSampleNonDominated(MAX_T);
        System.out.println("Sample size: " + sample.size());
        RealData sigma_out, sigma_in, best = RealData.ZERO, bestPref = RealData.ZERO;
        int bestIndex = -1, bestIndexPref = -1;
        ArrayList<Pair<Solution, RealData>> candidatos = new ArrayList<>();
        for (int i = 0; i < sample.size() - 1; i++) {
            for (int j = 1; j < sample.size(); j++) {
                int value = preference.compare(sample.get(i), sample.get(j));
                sigma_out = preference.getSigmaXY();
                sigma_in = preference.getSigmaYX();
                RealData tmp = (RealData) sigma_out.minus(sigma_in);

                if (value == -2 && tmp.compareTo(bestPref) >= 0) {
                    System.out.println("Last best i: " + bestIndexPref + " , value : " + bestPref + ", objs : "
                            + sample.get(i).getObjectives());
                    bestIndexPref = i;
                    bestPref = tmp;
                    candidatos.add(new ImmutablePair<Solution, RealData>(sample.get(i), best));
                } else if (value == -1) {
                    if (tmp.compareTo(best) >= 0) {
                        /*
                         * System.out.println("Last i: " + bestIndex + " , value : " + best +
                         * ", objs : " + sample.get(i).getObjectives());
                         */
                        bestIndex = i;
                        best = (RealData) tmp;
                        candidatos.add(new ImmutablePair<Solution, RealData>(sample.get(i), best));
                    }
                }
            }
        }
        System.out.println("Candidatos : " + candidatos.size());

        RealData bestf = (bestPref.compareTo(RealData.ZERO) != 0) ? bestPref : best;
        candidatos.removeIf(c -> c.getRight().compareTo(bestf) < 0);
        System.out.println("Candidatos : " + candidatos.size());
        for (Pair<Solution, RealData> pair : candidatos) {
            System.out.println(pair.getLeft() + " : " + pair.getRight());
        }
        return null;

    }

    public void setMAX_T(int mAX_T) {
        MAX_T = mAX_T;
    }

    public int getMAX_T() {
        return MAX_T;
    }

    public static void main(String[] args) throws FileNotFoundException {
        Tools.setSeed(8435L);
        String path = "src/main/resources/instances/dtlz/DTLZInstance.txt";
       //  path = "src/main/resources/instances/dtlz/PreferenceDTLZ1_Instance_01.txt";
        DTLZ_Instance instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();
        System.out.println(instance);
        DTLZ1_P problem = new DTLZ1_P(instance, null);
        BestCompromiseDTLZ bestCompromiseDTLZ = new BestCompromiseDTLZ(problem);
        bestCompromiseDTLZ.execute("path");
    }

}