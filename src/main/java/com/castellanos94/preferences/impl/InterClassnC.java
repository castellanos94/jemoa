package com.castellanos94.preferences.impl;

import java.io.FileNotFoundException;
import java.util.Arrays;

import com.castellanos94.datatype.Interval;
import com.castellanos94.instances.PSPI_Instance;
import com.castellanos94.preferences.Classifier;
import com.castellanos94.problems.PSPI_GD;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.BinarySolution;
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
@SuppressWarnings("unchecked")

public class InterClassnC<S extends Solution<?>> extends Classifier<S> {
    protected GDProblem<S> problem;
    protected S w;

    public InterClassnC(Problem<S> problem) {
        this.problem = (GDProblem<S>) problem;
        this.w = problem.randomSolution();
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

    /**
     * Classify the solution using the preference model associated with the dm.
     * 
     * @param x solution to classify
     */
    public void classify(S x) {
        int hsat = 0, sat = 0, dis = 0, hdis = 0;
        for (int dm = 0; dm < problem.getNumDMs(); dm++) {
            if (!problem.getPreferenceModel(dm).isSupportsUtilityFunction()) {// Dm con modelo de outranking
                int asc = asc_rule(x, dm);
                int dsc = desc_rule(x, dm);
                // System.out.println(asc+" "+dsc);
                if (asc == dsc && asc != -1) {
                    if (asc < problem.getR2()[dm].length) {
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

        /*
         * if (hsat < sat) { penaltie = penaltie.plus((hsat - sat) / 3.0); violated++; }
         * else if (hsat == sat) { penaltie = penaltie.plus(-sat / 3.0); violated++; }
         * if (sat < dis) { penaltie = penaltie.plus((double) sat - dis); violated++; }
         * else if (sat == dis) { penaltie = penaltie.plus(-dis / 2.0); violated++; } if
         * (dis < hdis) { penaltie = penaltie.plus((dis - hdis) * 2.0); violated++; }
         * x.setPenalties(penaltie); x.setNumberOfPenalties(violated);
         */
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
     * @throws CloneNotSupportedException
     */
    protected int asc_rule(S x, int dm) {
        ITHDM_Preference<S> pref = new ITHDM_Preference<>(problem, problem.getPreferenceModel(dm));
        Interval[][] r2 = problem.getR2()[dm];
        int clase = -1;
        w.setPenalties(Interval.ZERO);
        w.setNumberOfPenalties(0);
        for (int i = 0; i < r2.length; i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r2[i][j]);
            }
            int v = pref.compare(w, x);
            if (v <= -1 || v == 2) {
                clase = i;
                break;
            }
        }
        if (clase != -1)
            return clase;
        Interval[][] r1 = problem.getR1()[dm];
        for (int i = 0; i < r1.length; i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r1[i][j]);
            }
            if (pref.compare(w, x) <= -1) {
                clase = i;
                break;
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
     * @throws CloneNotSupportedException
     */
    protected int desc_rule(S x, int dm) {
        ITHDM_Preference<S> pref = new ITHDM_Preference<>(problem, problem.getPreferenceModel(dm));
        Interval[][] r1 = problem.getR1()[dm];
        int clase = -1;
        w.setPenalties(Interval.ZERO);
        w.setNumberOfPenalties(0);
        /*
         * for (int i = r1.length - 1; i > 0; i--) { for (int j = 0; j <
         * problem.getNumberOfObjectives(); j++) { w.setObjective(j, r1[i][j]); } if
         * (pref.compare(x, w) <= -1) { clase = i; } }
         */
        for (int i = 0; i < r1.length; i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r1[i][j]);
            }
            if (pref.compare(x, w) <= -1) {
                clase = i;
                break;
            }
        }
        if (clase != -1)
            clase = +r1.length;

        Interval[][] r2 = problem.getR2()[dm];
        /*
         * for (int i = r2.length - 1; i > 0; i--) { for (int j = 0; j <
         * problem.getNumberOfObjectives(); j++) { w.setObjective(j, r2[i][j]); } if
         * (pref.compare(x, w) <= -1) { clase = i; } }
         */
        for (int i = 0; i < r2.length; i++) {
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                w.setObjective(j, r2[i][j]);
            }
            if (pref.compare(x, w) <= -1) {
                clase = i;
                break;
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
    protected boolean isHighSat(S x, int dm) {
        ITHDM_Preference<S> pref = new ITHDM_Preference<>(problem, problem.getPreferenceModel(dm));
        Interval[][] r2 = problem.getR2()[dm];
        S w = (S) x.copy();
        w.setPenalties(Interval.ZERO);
        w.setNumberOfPenalties(0);
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
    protected boolean isHighDis(S x, int dm) {
        ITHDM_Preference<S> pref = new ITHDM_Preference<>(problem, problem.getPreferenceModel(dm));
        Interval[][] r1 = problem.getR1()[dm];
        S w = (S) x.copy();
        w.setPenalties(Interval.ZERO);
        w.setNumberOfPenalties(0);
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
     * be sat with a feasible S x iff the following conditions are fulfilled: i) For
     * all w belonging to R1, x is alpha-preferred to w. ii) Theres is no z
     * belonging to R2 such that z is alpha-preferred to x.
     * 
     * @param x
     * @param dm
     * @return
     */
    public boolean isSatWithXUF(S x, int dm) {
        UF_ITHDM_Preference<S> pref = new UF_ITHDM_Preference<>(problem, problem.getPreferenceModel(dm));
        Interval[][] r1 = problem.getR1()[dm];
        S w = (S) x.copy();
        w.setPenalties(Interval.ZERO);
        w.setNumberOfPenalties(0);
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
    public boolean isDisWithXUF(S x, int dm) {
        UF_ITHDM_Preference<S> pref = new UF_ITHDM_Preference<>(problem, problem.getPreferenceModel(dm));
        Interval[][] r2 = problem.getR2()[dm];
        S w = (S) x.copy();
        w.setPenalties(Interval.ZERO);
        w.setNumberOfPenalties(0);
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
    public boolean isHighSatWithXUF(S x, int dm) {
        UF_ITHDM_Preference<S> pref = new UF_ITHDM_Preference<>(problem, problem.getPreferenceModel(dm));
        Interval[][] r2 = problem.getR2()[dm];
        S w = (S) x.copy();
        w.setPenalties(Interval.ZERO);
        w.setNumberOfPenalties(0);
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

    public static void main(String[] args) throws FileNotFoundException {
        PSPI_Instance ins = (PSPI_Instance) new PSPI_Instance(
                "/home/thinkpad/Dropbox/GDM/GroupDecision_202001/_instances/GD_ITHDM-UFCC.txt").loadInstance();
        PSPI_GD problem = new PSPI_GD(ins);
        System.out.println(ins);
        System.out.println(problem);
        BinarySolution[] solutions = new BinarySolution[20];
        Tools.setSeed(1L);

        Interval[][] solutionIntervals = (Interval[][]) ins.getParams().get("Solutions");
        solutions = new BinarySolution[solutionIntervals.length + 10];
        for (int i = 0; i < solutions.length; i++) {
            if (i < solutionIntervals.length)
                solutions[i] = problem.createFromString(solutionIntervals[i]);
            else
                solutions[i] = problem.randomSolution();
            problem.evaluate(solutions[i]);
            problem.evaluateConstraint(solutions[i]);
            // System.out.println(solutions[i].getPenalties()+" "+solutions[i]);
        }
        ITHDM_Preference<BinarySolution> preference = new ITHDM_Preference<>(problem, problem.getPreferenceModel(1));
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
        InterClassnC<BinarySolution> classificator = new InterClassnC<>(problem);
        for (int i = 0; i < solutions.length; i++) {
            // System.out.println("Clasificando solucion: " + i);
            classificator.classify(solutions[i]);
        }
        for (BinarySolution s : solutions) {
            System.out.println(s.getPenalties() + ", " + s.getResources() + ", " + s.getResources() + " "
                    + Arrays.toString((int[]) s.getAttribute(classificator.getAttributeKey())));
            // System.out.println(s.getObjectives());
        }

    }
}