package com.castellanos94.preferences.impl;

import com.castellanos94.datatype.Interval;
import com.castellanos94.preferences.Classifier;
import com.castellanos94.problems.GDProblem;
import com.castellanos94.solutions.Solution;

/**
 * INTERCLLASS-nB for GroupDecision Problem and two classes.
 */
public class INTERCLASSnB<S extends Solution<?>> extends Classifier<S> {
    protected final int numberOfReferenceActions;
    protected final Interval[][][] referenceAction;
    protected GDProblem<S> problem;

    public INTERCLASSnB(GDProblem<S> problem) {
        this.problem = problem;
        numberOfReferenceActions = this.problem.getR1()[0].length + this.problem.getR1()[0].length;
        this.referenceAction = new Interval[this.problem.getNumDMs()][numberOfReferenceActions][this.problem
                .getNumberOfObjectives()];

        for (int dm = 0; dm < this.problem.getNumDMs(); dm++) {
            Interval src[][] = this.problem.getR1()[dm];
            int index = 0;
            for (int i = 0; i < src.length; i++) {
                this.referenceAction[dm][index] = new Interval[problem.getNumberOfObjectives()];
                System.arraycopy(src[i], 0, this.referenceAction[dm][index++], 0, this.problem.getNumberOfObjectives());
            }
            src = this.problem.getR2()[dm];
            for (int i = 0; i < src.length; i++) {
                this.referenceAction[dm][index] = new Interval[problem.getNumberOfObjectives()];
                System.arraycopy(src[i], 0, this.referenceAction[dm][index++], 0, this.problem.getNumberOfObjectives());
            }
        }
    }

    @Override
    public void classify(S x) {
        int hsat = 0, sat = 0, dis = 0, hdis = 0;

        for (int dm = 0; dm < problem.getNumDMs(); dm++) {
            if (!problem.getPreferenceModel(dm).isSupportsUtilityFunction()) {// Dm con modelo de outranking
                int asc = ascRule(x, dm);
                int dsc = descRule(x, dm);// desc_rule(x, dm);
                // System.out.println(String.format("\tOld : asc = %2d, desc = %2d", asc, dsc));
                // System.out.println(String.format("\tPaper : asc = %2d, desc = %2d",
                // ascending_rule(x, dm), descengind_rule(x, dm)));
                if (asc == dsc && asc != -1) {
                    if (asc >= problem.getR1()[dm].length) {
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
            // System.out.println(problem.getPreferenceModel(dm).isSupportsUtilityFunction()+
            // " "+hsat+" "+sat+" "+dis+" "+hdis);
        }
        setPenalties(x, hsat, sat, dis, hdis);

    }

    private void setPenalties(S x, int hsat, int sat, int dis, int hdis) {
        int[] iclass = new int[4];
        iclass[0] = hsat;
        iclass[1] = sat;
        iclass[2] = dis;
        iclass[3] = hdis;
        x.setAttribute(getAttributeKey(), iclass);
    }

    /**
     * Pseudo-disjunctive procedure
     * 
     * @param x
     * @param dm
     * @return
     */
    @SuppressWarnings("unchecked")
    protected int ascRule(S x, int dm) {
        ITHDM_Preference<S> pref = new ITHDM_Preference<>(problem, problem.getPreferenceModel(dm));
        int clase = -1;
        S w = (S) x.copy();
        for (int i = 0; i < numberOfReferenceActions; i++) {
            loadObjectivesToFunction(w, referenceAction[dm][i]);
            if (pref.compare(x, w) <= 0) {
                clase = i;
            }
        }
        return clase;

    }

    /**
     * Pseudo-conjunctive procedure
     * 
     * @param x
     * @param dm
     * @return
     */
    @SuppressWarnings("unchecked")
    protected int descRule(S x, int dm) {
        ITHDM_Preference<S> pref = new ITHDM_Preference<>(problem, problem.getPreferenceModel(dm));
        int clase = -1;
        S w = (S) x.copy();
        for (int i = numberOfReferenceActions - 1; i >= 0; i--) {
            loadObjectivesToFunction(w, referenceAction[dm][i]);
            if (pref.compare(x, w) <= 0) {
                return i;
            }
        }
        return clase;

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
        ITHDM_Preference<S> pref = new ITHDM_Preference<>(problem, problem.getPreferenceModel(dm));
        Interval[][] r2 = problem.getR2()[dm];
        S w = (S) x.copy();
        for (int i = 0; i < r2.length; i++) {
            loadObjectivesToFunction(w, r2[dm]);

            if (pref.compare(x, w) > -1) {
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
        ITHDM_Preference<S> pref = new ITHDM_Preference<>(problem, problem.getPreferenceModel(dm));
        Interval[][] r1 = problem.getR1()[dm];
        S w = (S) x.copy();
        for (int i = 0; i < r1.length; i++) {
            loadObjectivesToFunction(w, r1[i]);
            if (pref.compare(w, x) > -1) {
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
            loadObjectivesToFunction(w, r1[i]);
            if (pref.compare(x, w) > -1)
                return false;
        }

        Interval[][] r2 = problem.getR2()[dm];
        int count = 0;
        for (int i = 0; i < r2.length; i++) {
            loadObjectivesToFunction(w, r2[i]);
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
            loadObjectivesToFunction(w, r2[i]);
            if (pref.compare(w, x) == -1)
                count++;
        }
        if (count == r2.length)
            return true;

        count = 0;
        Interval[][] r1 = problem.getR1()[dm];
        for (int i = 0; i < r1.length; i++) {
            loadObjectivesToFunction(w, r1[i]);
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
            loadObjectivesToFunction(w, r2[i]);
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
        for (int i = 0; i < r1.length; i++) {
            loadObjectivesToFunction(w, r1[i]);
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

    /**
     * The data is a vector of integers, such that: [hsat, sat, dis, hdis]
     * 
     * @return key to access data in solution attributes
     */
    @Override
    public String getAttributeKey() {
        return getClass().getName();
    }
}
