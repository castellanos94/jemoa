package com.castellanos94.mcda;

import java.util.ArrayList;
import java.util.HashMap;

import com.castellanos94.datatype.Interval;
import com.castellanos94.preferences.impl.IntervalOutrankingRelations;
import com.castellanos94.preferences.impl.UF_ITHDM_Preference;
import com.castellanos94.problems.GDProblem;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Classifier;

/***
 * Satisfaction classifier with four classes: HSat, Sat, Dis and HDis. This
 * classifier is compatible only with group decision problems. <br>
 * Castellanos-Alvarez A, Cruz-Reyes L, Fernandez E, Rangel-Valdez N,
 * Gómez-Santillán C, Fraire H, Brambila-Hernández JA. A Method for Integration
 * of Preferences to a Multi-Objective Evolutionary Algorithm Using Ordinal
 * Multi-Criteria Classification. Mathematical and Computational Applications.
 * 2021; 26(2):27. https://doi.org/10.3390/mca26020027
 */
public class SatClassifier<S extends Solution<?>> extends Classifier<S> {
    protected GDProblem<S> problem;
    public static String HSAT_CLASS_TAG = "_CLASS_HSAT";
    public static String SAT_CLASS_TAG = "_CLASS_SAT";
    public static String DIS_CLASS_TAG = "_CLASS_DIS";
    public static String HDIS_CLASS_TAG = "_CLASS_HDIS";
    protected INTERCLASSnC<S>[] internalClassifier;

    @SuppressWarnings("unchecked")
    public SatClassifier(GDProblem<S> problem) {
        this.problem = problem;
        this.internalClassifier = new INTERCLASSnC[problem.getNumDMs()];
        for (int i = 0; i < problem.getNumDMs(); i++) {
            internalClassifier[i] = new INTERCLASSnC<>(problem.getNumberOfObjectives(), problem.getObjectives_type(),
                    problem.getPreferenceModel(i), transformData(problem, i));
        }
    }

    private Interval[][] transformData(GDProblem<?> problem, int dm) {
        Interval[][] referenceAction = new Interval[problem.getR1()[dm].length + problem.getR2()[dm].length][problem
                .getNumberOfObjectives()];
        Interval src[][] = problem.getR1()[dm];
        int index = 0;
        for (int i = 0; i < src.length; i++) {
            referenceAction[index] = new Interval[problem.getNumberOfObjectives()];
            System.arraycopy(src[i], 0, referenceAction[index++], 0, problem.getNumberOfObjectives());
        }
        src = problem.getR2()[dm];
        for (int i = 0; i < src.length; i++) {
            referenceAction[index] = new Interval[problem.getNumberOfObjectives()];
            System.arraycopy(src[i], 0, referenceAction[index++], 0, problem.getNumberOfObjectives());
        }
        return referenceAction;
    }

    /**
     * Classify the solution x for all decision makers, grouping the result by
     * class, the classes are saved as an integer vector with the following
     * structure [hsat, sat, dis, hdis] in the attributes of x.
     * 
     * @param x solution to classify
     */
    @Override
    public void classify(S x) {
        int hsat = 0, sat = 0, dis = 0, hdis = 0;
        for (int dm = 0; dm < problem.getNumDMs(); dm++) {
            if (!problem.getPreferenceModel(dm).isSupportsUtilityFunction()) {// Dm con modelo de outranking
                this.internalClassifier[dm].classify(x);
                int _class = (int) x.getAttribute(this.internalClassifier[dm].getAttributeKey());
                if (_class != -1) {
                    if (_class >= problem.getR1()[dm].length) {
                        if (isHighSat(x, dm)) {
                            hsat++;
                        } else {
                            sat++;
                        }
                    } else {
                        if (isHighDis(x, dm)) {
                            hdis++;
                        } else {
                            dis++;
                        }
                    }
                } else {
                    hdis++;
                }
            } else { // DM UF
                boolean bsat = isSatWithXUF(x, dm);
                boolean bhsat = isHighSatWithXUF(x, dm);
                if (bhsat && bsat) {
                    hsat++;
                } else if (!bhsat && bsat) {
                    sat++;
                } else {
                    boolean bdis = isDisWithXUF(x, dm);
                    boolean bhdis = isHighDisWithXUF(x, dm);
                    if (bhdis && bdis) {
                        hdis++;
                    } else if (!bhdis && bdis) {
                        dis++;
                    } else {
                        hdis++;
                    }
                }

            }
        }
        setPenalties(x, hsat, sat, dis, hdis);
    }

    /**
     * IMPORTAT: this method is only for 1 dm
     * 
     * @param front front to classify
     * @return solutions
     */
    public HashMap<String, ArrayList<S>> classify(ArrayList<S> front) {
        HashMap<String, ArrayList<S>> map = new HashMap<>();
        map.put(HSAT_CLASS_TAG, new ArrayList<>());
        map.put(SAT_CLASS_TAG, new ArrayList<>());
        map.put(DIS_CLASS_TAG, new ArrayList<>());
        map.put(HDIS_CLASS_TAG, new ArrayList<>());
        for (S x : front) {
            classify(x);
            int[] iclass = (int[]) x.getAttribute(getAttributeKey());
            if (iclass[0] > 0) {
                x.setAttribute(CLASS_KEY, HSAT_CLASS_TAG);
                map.get(HSAT_CLASS_TAG).add(x);
            } else if (iclass[1] > 0) {
                x.setAttribute(CLASS_KEY, SAT_CLASS_TAG);
                map.get(SAT_CLASS_TAG).add(x);
            } else if (iclass[2] > 0) {
                x.setAttribute(CLASS_KEY, DIS_CLASS_TAG);
                map.get(DIS_CLASS_TAG).add(x);
            } else {
                x.setAttribute(CLASS_KEY, HDIS_CLASS_TAG);
                map.get(HDIS_CLASS_TAG).add(x);
            }
        }
        return map;
    }

    /**
     * The Dm is highly satisfied with a satisfactory x if for each action w in R2
     * we have xPr(B,Lambda)w
     * 
     * @param x  solution to classificate
     * @param dm prefence model
     * @return True if isHighSat otherwise false
     */
    @SuppressWarnings("unchecked")
    protected boolean isHighSat(S x, int dm) {
        IntervalOutrankingRelations<S> pref = this.internalClassifier[dm].getIntervalOutrankingRelation();
        Interval[][] r2 = problem.getR2()[dm];
        S w = (S) x.copy();
        for (int i = 0; i < r2.length; i++) {
            loadObjectivesToFunction(w, r2[i]);
            int val = pref.compare(x, w);
            if (val > -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * The DM is strongly dissatisfied with x if for each w in R1 we have
     * wP(betha,Lambda)x
     * 
     * @param x  solution to class
     * @param dm model preference
     * @return true if is high dis otherwise false
     */
    @SuppressWarnings("unchecked")
    protected boolean isHighDis(S x, int dm) {
        IntervalOutrankingRelations<S> pref = this.internalClassifier[dm].getIntervalOutrankingRelation();

        Interval[][] r1 = problem.getR1()[dm];
        S w = (S) x.copy();
        for (int i = 0; i < r1.length; i++) {
            loadObjectivesToFunction(w, r1[i]);
            int val = pref.compare(w, x);
            if (val > -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Def 18: DM is compatible with weighted-sum function model, The DM is said to
     * be sat with a feasible S x iff the following conditions are fulfilled: i) For
     * all w belonging to R1, x is alpha-preferred to w. ii) Theres is no z
     * belonging to R2 such that z is alpha-preferred to x.
     * 
     * @param x
     * @param dm
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean isSatWithXUF(S x, int dm) {
        UF_ITHDM_Preference<S> pref = new UF_ITHDM_Preference<>(problem, problem.getPreferenceModel(dm));
        Interval[][] r1 = problem.getR1()[dm];
        S w = (S) x.copy();
        for (int i = 0; i < r1.length; i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r1[i][j]);
            }
            if (pref.compare(x, w) > -1)
                return false;
        }

        Interval[][] r2 = problem.getR2()[dm];
        int count = 0;
        for (int i = 0; i < r2.length; i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r2[i][j]);
            }
            if (pref.compare(w, x) == -1)
                count++;
        }
        return (count == 0);
    }

    /**
     * Def 19: DM is compatible with weighted-sum function model, The DM is said to
     * be dissatisfied with a feasible S x if at least one of the following
     * conditions is fullfilled: i) For all w belonging to R2, w is alpha-pref to x;
     * ii) There is no z belonging to R1 such that x is alpha-pref to z.
     * 
     * @param x
     * @param dm
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean isDisWithXUF(S x, int dm) {
        UF_ITHDM_Preference<S> pref = new UF_ITHDM_Preference<>(problem, problem.getPreferenceModel(dm));
        Interval[][] r2 = problem.getR2()[dm];
        S w = (S) x.copy();
        int count = 0;
        for (int i = 0; i < r2.length; i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r2[i][j]);
            }
            if (pref.compare(w, x) == -1)
                count++;
        }
        if (count == r2.length)
            return true;

        count = 0;
        Interval[][] r1 = problem.getR1()[dm];
        for (int i = 0; i < r1.length; i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r1[i][j]);
            }
            if (pref.compare(x, w) == -1)
                count++;
        }

        return count == 0;
    }

    /**
     * Def 20: If the DM is sat with x, we say that the DM is high sat with x iff
     * the following condition is also fulfilled: - For all w belonging to R2, x is
     * alph-pref to w.
     * 
     * @param x
     * @param dm
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean isHighSatWithXUF(S x, int dm) {
        UF_ITHDM_Preference<S> pref = new UF_ITHDM_Preference<>(problem, problem.getPreferenceModel(dm));
        Interval[][] r2 = problem.getR2()[dm];
        S w = (S) x.copy();
        for (int i = 0; i < r2.length; i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r2[i][j]);
            }
            if (pref.compare(x, w) > -1)
                return false;
        }
        return true;
    }

    /**
     * Def 21: Suppose that the DM is dissat with a S x, We say that the DM is
     * highly dissatisfied with x if the following condition is also fulfilled - For
     * all w belonging to R1, w is alpha-pref to x.
     * 
     * @param x
     * @param dm
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean isHighDisWithXUF(S x, int dm) {
        UF_ITHDM_Preference<S> pref = new UF_ITHDM_Preference<>(problem, problem.getPreferenceModel(dm));
        Interval[][] r1 = problem.getR1()[dm];
        S w = (S) x.copy();
        w.setPenalties(Interval.ZERO);
        w.setNumberOfPenalties(0);
        for (int i = 0; i < r1.length; i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r1[i][j]);
            }
            if (pref.compare(w, x) > -1)
                return false;
        }
        return true;

    }

    private void loadObjectivesToFunction(S b, Interval[] action) {
        for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
            b.setObjective(j, action[j]);
        }
    }

    private void setPenalties(S x, int hsat, int sat, int dis, int hdis) {
        int[] iclass = new int[4];
        iclass[0] = hsat;
        iclass[1] = sat;
        iclass[2] = dis;
        iclass[3] = hdis;
        x.setAttribute(getAttributeKey(), iclass);
    }

    @Override
    public String getAttributeKey() {
        return getClass().getCanonicalName();
    }

}
