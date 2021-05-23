package com.castellanos94.mcda;

import com.castellanos94.datatype.Interval;
import com.castellanos94.preferences.impl.IntervalOutrankingRelations;
import com.castellanos94.preferences.impl.OutrankingModel;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Classifier;

/**
 * INTERCLLASS-nB. The class is set as an attribute of type integer (int) and
 * corresponds to the index k of C_k. Fernández, E., Figueira, J. R., & Navarro,
 * J. (2020). Interval-based extensions of two outranking methods for
 * multi-criteria ordinal classification. Omega, 95, 102065.
 * https://doi.org/10.1016/j.omega.2019.05.001
 * 
 * @see OutrankingModel
 * @see IntervalOutrankingRelations
 * 
 */
public class INTERCLASSnB<S extends Solution<?>> extends Classifier<S> {

    public static enum RULE {
        PESSIMISTIC, OPTIMISTIC
    };

    protected Interval[][] referenceAction;
    protected final int numberOfReferenceActions;
    protected final int numberOfObjectives;
    protected RULE ruleForAssignment;
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
    public INTERCLASSnB(int numberOfObjectives, int[] objectiveTypes, OutrankingModel model,
            Interval[][] referenceAction, RULE ruleForAssignment) {
        this.referenceAction = referenceAction;
        this.ruleForAssignment = ruleForAssignment;
        this.numberOfReferenceActions = referenceAction.length;
        this.numberOfObjectives = numberOfObjectives;
        this.pref = new IntervalOutrankingRelations<>(numberOfObjectives, objectiveTypes, model);
    }

    /**
     * Classify the solution using the preference model associated with the dm. The
     * class is set as an attribute of type integer (int) and corresponds to the
     * index k of C_k.
     * 
     * @param x solution to classify
     */
    @Override
    public void classify(S x) {
        int val;
        if (this.ruleForAssignment == RULE.OPTIMISTIC) {
            val = ascRule(x);
            x.setAttribute(getAttributeKey(), (val != -1) ? val : 0);
        } else {
            val = descRule(x);
            x.setAttribute(getAttributeKey(), (val != -1) ? val : 0);
        }
    }

    /**
     * Pseudo-disjunctive procedure, also known as optimistic procedure :
     * B_kPr(delta,lambda)x
     * 
     * @param x
     * @return
     */
    @SuppressWarnings("unchecked")
    protected int ascRule(S x) {
        int clase = -1;
        S w = (S) x.copy();
        for (int i = 0; i < numberOfReferenceActions; i++) {
            loadObjectivesToFunction(w, referenceAction[i]);
            if (pref.compare(w, x) <= 0) {
                clase = i;
            }
        }
        return clase;
    }

    /**
     * Pseudo-conjunctive procedure, also known as pessimistic procedure:
     * xS(delta,lambda)B_k procedure
     * 
     * @param x
     * @return
     */
    @SuppressWarnings("unchecked")
    protected int descRule(S x) {
        int clase = -1;
        S w = (S) x.copy();
        for (int i = numberOfReferenceActions - 1; i >= 0; i--) {
            loadObjectivesToFunction(w, referenceAction[i]);
            if (pref.compare(x, w) <= 0) {
                return i;
            }
        }
        return clase;

    }

    private void loadObjectivesToFunction(S b, Interval[] action) {
        for (int j = 0; j < numberOfObjectives; j++) {
            b.setObjective(j, action[j]);
        }
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
}
