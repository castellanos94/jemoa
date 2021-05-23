package com.castellanos94.mcda;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.Interval;
import com.castellanos94.preferences.impl.IntervalOutrankingRelations;
import com.castellanos94.preferences.impl.OutrankingModel;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Classifier;

/**
 * INTERCLLASS-nC. The class is set as an attribute of type integer (int) and
 * corresponds to the index k of C_k. Fernández, E., Figueira, J. R., & Navarro,
 * J. (2020). Interval-based extensions of two outranking methods for
 * multi-criteria ordinal classification. Omega, 95, 102065.
 * https://doi.org/10.1016/j.omega.2019.05.001
 * 
 * @see OutrankingModel
 * @see IntervalOutrankingRelations
 * 
 */
public class INTERCLASSnC<S extends Solution<?>> extends Classifier<S> {
    protected Interval[][] referenceAction;
    protected final int numberOfReferenceActions;
    protected final int numberOfObjectives;

    protected final IntervalOutrankingRelations<S> pref;

    /**
     * INTERCLASS-nC usefel when the dm has only a vague idea about the boundares
     * between adjacent classes but can easily identify several.
     * 
     * @param numberOfObjectives of problem
     * @param objectiveTypes     max/ min integer vector
     * @param model              oturanking model
     * @param referenceAction    subset of reference actions that characterize C_k,
     *                           k = 1, ... M, where {r_0, R_1, ... R_M, R_(M+1)}
     *                           are the anti-ideal and ideal actions.
     */
    public INTERCLASSnC(int numberOfObjectives, int[] objectiveTypes, OutrankingModel model,
            Interval[][] referenceAction) {
        this.referenceAction = referenceAction;
        this.numberOfReferenceActions = referenceAction.length;
        this.numberOfObjectives = numberOfObjectives;
        this.pref = new IntervalOutrankingRelations<>(numberOfObjectives, objectiveTypes, model);
    }

    /**
     * The object is a data primitve int
     * 
     * @return key to access data in solution attributes
     */
    @Override
    public String getAttributeKey() {
        return getClass().getName();
    }

    /**
     * Classify the solution using the preference model associated with the dm. The
     * class is set as an attribute of type integer (int) and corresponds to the
     * index k of C_k.
     * 
     * @param x solution to classify
     */
    public void classify(S x) {
        int asc = ascending_rule(x);
        int dsc = descending_rule(x);
        if (asc == dsc && dsc != -1) {
            x.setAttribute(getAttributeKey(), dsc);
        } else {
            x.setAttribute(getAttributeKey(), 0);
        }
    }

    /**
     * Ascending assigment rule acording to paper. R_kS(Delta,Lambda)x
     * 
     * @param x solution to classify
     * @return class c_k
     */
    @SuppressWarnings("unchecked")
    protected int ascending_rule(S x) {
        S b = (S) x.copy();
        Data lastFunctionI = null;
        int clase = -1;
        for (int i = 0; i < numberOfReferenceActions; i++) {
            loadObjectivesToFunction(b, referenceAction[i]);
            int val = pref.compare(b, x);
            // Si b_iD(alpha)x es falso y xD(alpha)b_i es cierto entonces
            // xS(Delta,Lambda)Lambda por Proposition 1.i xD(alpha)b_i ->
            // x(delta,lambda)b_i, dado que es comparacion indirecta es necesario verificar
            // si tienen una realacion
            if (val <= 0 || val == 2) {
                if (i == 0) {
                    clase = i;
                } else {
                    Data currentFunctionI = Data.getMin(pref.getSigmaXY(), pref.getSigmaYX());
                    if (currentFunctionI.compareTo(lastFunctionI) >= 0) {
                        clase = i;
                    } else {
                        clase = i - 1;
                    }
                }
            }
            lastFunctionI = Data.getMin(pref.getSigmaXY(), pref.getSigmaYX());
        }
        return clase;
    }

    /**
     * Descending assigment rule acording to paper. xS(Delta,Lambda)R_k
     * 
     * @param x solution to classify
     * @return class c_k
     */
    @SuppressWarnings("unchecked")
    protected int descending_rule(S x) {
        S b = (S) x.copy();
        for (int i = numberOfReferenceActions - 1; i >= 0; i--) {
            loadObjectivesToFunction(b, referenceAction[i]);
            int val = pref.compare(x, b);
            if (val <= 0) {
                if (i > 0 && i + 1 < numberOfReferenceActions) {
                    Data currentFunctionI = Data.getMin(pref.getSigmaXY(), pref.getSigmaYX());
                    loadObjectivesToFunction(b, referenceAction[i + 1]);
                    pref.compare(x, b);
                    Data nextFunctionI = Data.getMin(pref.getSigmaXY(), pref.getSigmaYX());
                    if (currentFunctionI.compareTo(nextFunctionI) >= 0) {
                        return i;
                    } else {
                        return i + 1;
                    }
                } else if (i == 0) {
                    return i;
                } else {
                    return numberOfReferenceActions - 1;
                }
            }
        }
        return -1;
    }

    private void loadObjectivesToFunction(S b, Interval[] action) {
        for (int j = 0; j < numberOfObjectives; j++) {
            b.setObjective(j, action[j]);
        }
    }

    public IntervalOutrankingRelations<S> getIntervalOutrankingRelation() {
        return pref;
    }
}