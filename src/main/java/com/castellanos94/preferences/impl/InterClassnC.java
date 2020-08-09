package com.castellanos94.preferences.impl;

import java.io.FileNotFoundException;
import java.util.Arrays;

import com.castellanos94.datatype.Interval;
import com.castellanos94.instances.PspIntervalInstance_GD;
import com.castellanos94.preferences.Classifier;
import com.castellanos94.problems.PSPI_GD;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Tools;

/**
 * Clasifica una solucion con respecto a los conjuntos R2, R2. Guarda el vector
 * [HSat, Sat, Dis, HDis] de clasicacion en los atributos extra de la solucion.
 * Recuperar con getAttributeKey().
 * 
 * @see com.castellanos94.problems.PSPI_GD
 * 
 */
public class InterClassnC implements Classifier {
    protected PSPI_GD problem;
    protected PspIntervalInstance_GD instance;

    public InterClassnC(PSPI_GD problem) {
        this.problem = problem;
        this.instance = (PspIntervalInstance_GD) problem.getInstance();

    }

    public static String getAttributeKey() {
        return InterClassnC.class.getName();
    }

    /**
     * Classify the solution using the preference model associated with the dm.
     * 
     * @param x solution to classify
     */
    public void classify(Solution x) {
        int hsat = 0, sat = 0, dis = 0, hdis = 0;
        for (int dm = 0; dm < instance.getNumDMs(); dm++) {
            if (instance.getTypesOfDMs()[dm].compareTo(0) == 0) {// Dm con modelo de outranking
                int asc = asc_rule(x, dm);
                int dsc = desc_rule(x, dm);
                if (asc == dsc) {
                    if (asc < instance.getR2()[dm].length) {
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
            } else if (instance.getTypesOfDMs()[dm].compareTo(1) == 0) { // DM UF
                boolean bsat = isSatWithXUF(x, dm);
                boolean bhsat = isHighSatWithXUF(x, dm);
                boolean flag = false;
                if (bhsat && bsat) {
                    hsat++;
                } else if (!bhsat && bsat) {
                    sat++;
                } else {
                    flag = true;
                }
                boolean bdis = isDisWithXUF(x, dm);
                boolean bhdis = isHighSatWithXUF(x, dm);
                if (bhdis && bdis) {
                    hdis++;
                } else if (!bhdis && bdis) {
                    dis++;
                } else if (flag) {
                    hdis++;
                }

            } else {
                throw new IllegalArgumentException(
                        String.format("The dm (%3d) does not have a compatible preference model.", (dm + 1)));
            }
        }
        // System.out.printf("\tHSat = %2d, Sat = %2d, Dis = %2d, HDis = %2d\n", hsat,
        // sat, dis, hdis);
        int[] iclass = new int[4];
        iclass[0] = hsat;
        iclass[1] = sat;
        iclass[2] = dis;
        iclass[3] = hdis;
        x.setAttribute(getAttributeKey(), iclass);
    }

    /**
     * Pendiente verificar.
     * 
     * @param x
     * @param dm
     * @return
     */
    protected int asc_rule(Solution x, int dm) {
        ITHDM_Preference pref = new ITHDM_Preference(problem, problem.getPreferenceModel(dm));
        Interval[][] r2 = instance.getR2()[dm];
        int clase = -1;
        Solution w = new Solution(problem);
        w.setPenalties(Interval.ZERO);
        w.setN_penalties(0);
        for (int i = 0; i < r2.length && clase == -1; i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r2[i][j]);
            }
            if (pref.compare(w, x) <= -1) {
                clase = i;
            }
        }
        if (clase != -1)
            return clase;
        Interval[][] r1 = instance.getR1()[dm];
        for (int i = 0; i < r1.length && clase == -1; i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r1[i][j]);
            }
            if (pref.compare(w, x) <= -1) {
                clase = i;
            }
        }
        return (clase == -1) ? clase : clase + r2.length;

    }

    /**
     * Verificar
     * 
     * @param x
     * @param dm
     * @return
     */
    protected int desc_rule(Solution x, int dm) {
        ITHDM_Preference pref = new ITHDM_Preference(problem, problem.getPreferenceModel(dm));
        Interval[][] r1 = instance.getR1()[dm];
        int clase = -1;
        Solution w = new Solution(problem);
        w.setPenalties(Interval.ZERO);
        w.setN_penalties(0);
        for (int i = r1.length - 1; i > 0 && clase == -1; i--) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r1[i][j]);
            }
            if (pref.compare(x, w) <= -1) {
                clase = i;
            }
        }
        if (clase != -1)
            return clase + r1.length;

        Interval[][] r2 = instance.getR2()[dm];
        for (int i = r2.length - 1; i > 0 && clase == -1; i--) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r2[i][j]);
            }
            if (pref.compare(x, w) <= -1) {
                clase = i;
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
    protected boolean isHighSat(Solution x, int dm) {
        ITHDM_Preference pref = new ITHDM_Preference(problem, problem.getPreferenceModel(dm));
        Interval[][] r2 = instance.getR2()[dm];
        Solution w = new Solution(problem);
        w.setPenalties(Interval.ZERO);
        w.setN_penalties(0);
        for (int i = 0; i < r2.length; i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r2[i][j]);
            }
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
    protected boolean isHighDis(Solution x, int dm) {
        ITHDM_Preference pref = new ITHDM_Preference(problem, problem.getPreferenceModel(dm));
        Interval[][] r1 = instance.getR1()[dm];
        Solution w = new Solution(problem);
        w.setPenalties(Interval.ZERO);
        w.setN_penalties(0);
        for (int i = 0; i < r1.length; i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r1[i][j]);
            }
            if (pref.compare(w, x) > -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Def 18: DM is compatible with weighted-sum function model, The DM is said to
     * be sat with a feasible solution x iff the following conditions are fulfilled:
     * i) For all w belonging to R1, x is alpha-preferred to w. ii) Theres is no z
     * belonging to R2 such that z is alpha-preferred to x.
     * 
     * @param x
     * @param dm
     * @return
     */
    public boolean isSatWithXUF(Solution x, int dm) {
        UF_ITHDM_Preference pref = new UF_ITHDM_Preference(problem, problem.getPreferenceModel(dm));
        Interval[][] r1 = instance.getR1()[dm];
        Solution w = new Solution(problem);
        w.setPenalties(Interval.ZERO);
        w.setN_penalties(0);
        for (int i = 0; i < r1.length; i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r1[i][j]);
            }
            if (pref.compare(x, w) > -1)
                return false;
        }

        Interval[][] r2 = instance.getR2()[dm];
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
     * be dissatisfied with a feasible solution x if at least one of the following
     * conditions is fullfilled: i) For all w belonging to R2, w is alpha-pref to x;
     * ii) There is no z belonging to R1 such that x is alpha-pref to z.
     * 
     * @param x
     * @param dm
     * @return
     */
    public boolean isDisWithXUF(Solution x, int dm) {
        UF_ITHDM_Preference pref = new UF_ITHDM_Preference(problem, problem.getPreferenceModel(dm));
        Interval[][] r2 = instance.getR2()[dm];
        Solution w = new Solution(problem);
        w.setPenalties(Interval.ZERO);
        w.setN_penalties(0);
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
        Interval[][] r1 = instance.getR1()[dm];
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
    public boolean isHighSatWithXUF(Solution x, int dm) {
        UF_ITHDM_Preference pref = new UF_ITHDM_Preference(problem, problem.getPreferenceModel(dm));
        Interval[][] r2 = instance.getR2()[dm];
        Solution w = new Solution(problem);
        w.setPenalties(Interval.ZERO);
        w.setN_penalties(0);
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
     * Def 21: Suppose that the DM is dissat with a solution x, We say that the DM
     * is highly dissatisfied with x if the following condition is also fulfilled -
     * For all w belonging to R1, w is alpha-pref to x.
     * 
     * @param x
     * @param dm
     * @return
     */
    public boolean isHighDisWithXUF(Solution x, int dm) {
        UF_ITHDM_Preference pref = new UF_ITHDM_Preference(problem, problem.getPreferenceModel(dm));
        Interval[][] r1 = instance.getR2()[dm];
        Solution w = new Solution(problem);
        w.setPenalties(Interval.ZERO);
        w.setN_penalties(0);
        for (int i = 0; i < r1.length; i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r1[i][j]);
            }
            if (pref.compare(w, x) > -1)
                return false;
        }
        return true;

    }

    public static void main(String[] args) throws FileNotFoundException {
        PspIntervalInstance_GD ins = (PspIntervalInstance_GD) new PspIntervalInstance_GD(
                "src/main/resources/instances/gd/GD_ITHDM-UFCA.txt").loadInstance();
        PSPI_GD problem = new PSPI_GD(ins);
        Solution[] solutions = new Solution[20];
        Tools.setSeed(1L);
        for (int i = 0; i < solutions.length; i++) {
            solutions[i] = problem.randomSolution();
            problem.evaluate(solutions[i]);
            problem.evaluateConstraints(solutions[i]);
            // System.out.println(solutions[i]);
        }
        ITHDM_Preference preference = new ITHDM_Preference(problem, problem.getPreferenceModel(1));
        System.out.println("Evaluando el sistema de preferencia del DM 1");
        int[][] matrix = new int[solutions.length][solutions.length];

        for (int i = 0; i < solutions.length; i++) {
            for (int j = 0; j < solutions.length; j++) {
                // System.out.printf("Solucion %d vs %d : %d\n", (i + 1), (j + 1),
                // preference.compare(solutions[i], solutions[j]));
                if (i != j) {
                    matrix[i][j] = preference.compare(solutions[i], solutions[j]);
                }
            }
        }
        System.out.println("Matrix de (S, λ-relation)");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                System.out.printf("%3d, ", matrix[i][j]);
            }
            System.out.println();
        }
        System.out.println("Clasifcando soluciones con InterClass-nC");
        InterClassnC classificator = new InterClassnC(problem);
        for (int i = 0; i < solutions.length; i++) {
            // System.out.println("Clasificando solucion: " + i);
            classificator.classify(solutions[i]);
        }
        for (Solution s : solutions) {
            System.out.println(
                    s.getObjectives() + " " + Arrays.toString((int[]) s.getAttribute(InterClassnC.getAttributeKey())));
        }

    }
}