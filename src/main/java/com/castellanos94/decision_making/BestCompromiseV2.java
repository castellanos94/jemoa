package com.castellanos94.decision_making;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.castellanos94.datatype.RealData;
import com.castellanos94.instances.DTLZ_Instance;
import com.castellanos94.preferences.impl.ITHDM_Preference;
import com.castellanos94.problems.preferences.dtlz.DTLZ1_P;
import com.castellanos94.problems.preferences.dtlz.DTLZ2_P;
import com.castellanos94.problems.preferences.dtlz.DTLZ3_P;
import com.castellanos94.problems.preferences.dtlz.DTLZ4_P;
import com.castellanos94.problems.preferences.dtlz.DTLZ5_P;
import com.castellanos94.problems.preferences.dtlz.DTLZ6_P;
import com.castellanos94.problems.preferences.dtlz.DTLZ7_P;
import com.castellanos94.problems.preferences.dtlz.DTLZPreferences;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

public class BestCompromiseV2 {
    private static final String NETSCORE_KEY = "FLUJO_NETO";
    private static final String WEAKNESS_kEY = "DEBILIDAD_FUERZA";
    protected int MAX_T = 5 * 1000;
    protected DTLZPreferences problem;
    protected ITHDM_Preference<DoubleSolution> preference;

    public BestCompromiseV2(DTLZPreferences problem) {
        this.problem = problem;
        this.preference = new ITHDM_Preference<>(problem, problem.getInstance().getPreferenceModel(0));
    }

    /**
     * Weakness strength sigma(y,x) > beta count_s_i + =1 BestCompromise
     * min(count_s)
     * 
     * @return
     */
    public ArrayList<DoubleSolution> execute() {
        ArrayList<DoubleSolution> sample = problem.generateSampleNonDominated(MAX_T);
        DoubleSolution best_compromise = null;
        System.out.println("Executing ...");
        for (int i = 0; i < sample.size(); i++) {
            double step = sample.size() / 10.0;
            if (i != 0 && i % step == 0) {
                System.out.printf("Iteration (%3.2f) %6d of %6d ...\n", 1.0 * i / sample.size(), i, sample.size());
            }
            RealData sigma_out = RealData.ZERO, sigma_in = RealData.ZERO;
            int sigmaYXGreaterThanBeta = 0;
            for (int j = 0; j < sample.size(); j++) {
                // int value = dominance.compare(sample.get(i), sample.get(j));
                if (i != j) {
                    preference.compare(sample.get(i), sample.get(j));
                    sigma_out = (RealData) sigma_out.plus(preference.getSigmaXY());
                    sigma_in = (RealData) sigma_in.plus(preference.getSigmaYX());
                    //if (preference.getSigmaYX().compareTo(preference.getModel().getBeta()) > 0) {
                    if(preference.getModel().getBeta().compareTo(sigma_out) < 0 && sigma_in.compareTo(0.5) < 0){
                        sigmaYXGreaterThanBeta++;
                    }
                }
            }
            RealData net_score = (RealData) sigma_out.minus(sigma_in);
            sample.get(i).setAttribute(NETSCORE_KEY, net_score);
            sample.get(i).setAttribute(WEAKNESS_kEY, sigmaYXGreaterThanBeta);
        }
        RealData bestNetScore = (RealData) sample.get(0).getAttribute(NETSCORE_KEY);
        int indexBestNetScore = 0, indexWeakness = -1;
        ArrayList<Integer> indexCandidatos = new ArrayList<>();
        for (int i = 0; i < sample.size(); i++) {
            DoubleSolution solution = sample.get(i);
            if (((RealData) solution.getAttribute(NETSCORE_KEY)).compareTo(bestNetScore) > 0) {
                bestNetScore = (RealData) solution.getAttribute(NETSCORE_KEY);
                indexBestNetScore = i;
            }
            if (((int) solution.getAttribute(WEAKNESS_kEY)) == 0) {
                indexCandidatos.add(i);
            }
        }
        RealData best = null;
        if (indexCandidatos.size() == 1) {
            indexWeakness = indexCandidatos.get(0);
        } else if (indexCandidatos.size() > 1) {

            for (Integer index : indexCandidatos) {
                RealData tmp = (RealData) sample.get(indexCandidatos.get(index)).getAttribute(NETSCORE_KEY);
                if (best == null || best.compareTo(tmp) >= 0) {
                    best = tmp;
                    indexBestNetScore = index;
                }
            }
        }
        if (indexWeakness != -1) {
            System.out.println("Best Compromise : " + sample.get(indexWeakness));
            best_compromise = sample.get(indexWeakness);
        } else {
            System.out.println("Best Compromise by netscore " + bestNetScore + " : " + sample.get(indexBestNetScore));
            best_compromise = sample.get(indexBestNetScore);
        }
        ArrayList<DoubleSolution> roi = new ArrayList<>();
        roi.add(best_compromise);

        for (int i = 0; i < sample.size() && roi.size() < 200; i++) {
            if (indexWeakness != -1) {
                if (((int) sample.get(i).getAttribute(WEAKNESS_kEY)) == 0) {
                    if (!roi.contains(sample.get(i)))
                        roi.add(sample.get(i));
                }
            } else {
                if (((RealData) sample.get(i).getAttribute(NETSCORE_KEY)).compareTo(best) > 0) {
                    if (!roi.contains(sample.get(i)))
                        roi.add(sample.get(i));
                }
            }
        }
        System.out.println("ROI : " + roi.size());
        if (roi.size() == 1) {
            int count = 0;
            while (count < 4) {
                int index = Tools.getRandom().nextInt(sample.size());
                if (!roi.contains(sample.get(index))) {
                    roi.add(sample.get(index));
                }
                count++;
            }
        }
        return roi;
    }

    public void setMAX_T(int mAX_T) {
        MAX_T = mAX_T;
    }

    public static void main(String[] args) throws IOException {
        String path = "src/main/resources/DTLZ_INSTANCES/DTLZ1_Instance.txt";
        DTLZ_Instance instance = (DTLZ_Instance) new DTLZ_Instance(path).loadInstance();
        Tools.setSeed(8435L);
        System.out.println(instance);

        DTLZPreferences problem = new DTLZ1_P(instance);
        System.out.println(problem);
        BestCompromiseV2 compromiseV2 = new BestCompromiseV2(problem);
        
        ArrayList<DoubleSolution> roi = compromiseV2.execute();
        Solution.writSolutionsToFile("bestCompromise" + File.separator + "bestCompromiseV3_" + problem.getName(),
                new ArrayList<>(roi));
    }
}
