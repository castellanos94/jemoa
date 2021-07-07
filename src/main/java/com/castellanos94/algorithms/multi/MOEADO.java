package com.castellanos94.algorithms.multi;

import java.util.ArrayList;

import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.datatype.Data;
import com.castellanos94.operators.CrossoverOperator;
import com.castellanos94.operators.MutationOperator;
import com.castellanos94.operators.RepairOperator;
import com.castellanos94.preferences.impl.IntervalOutrankingRelations;
import com.castellanos94.preferences.impl.OutrankingModel;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

/**
 * E. Fernández, N. Rangel-Valdez, L. Cruz-Reyes, C. Gomez-Santillan, and C. A.
 * Coello-Coello. Preference incorporation into moea/d using an outranking
 * approach with imprecisemodel parameters. to appear, 2021
 */
public class MOEADO<S extends Solution<?>> extends MOEAD<S> {
    /**
     * DM's value system (w, v, lambda, beta)
     */
    protected OutrankingModel model;
    protected IntervalOutrankingRelations<S> intervalOutrankingApproach;
    protected final int variant;

    /**
     * 
     * @param problem             MOP
     * @param MAX_ITERATIONS      a stopping criterion
     * @param N                   the numbre of the subproblems considered in MOEA/D
     * @param weightVectors       a uniform spread of N weight vectors
     * @param T                   the number of the weight vectors in the
     *                            neighborhood of each weight vector
     * @param model               DM's value system (w, v, lambda, beta)
     * @param variant             MOEA/D/O variants of the scalarizing functions
     * @param crossoverOperator   genetic operator
     * @param mutationOperator    genetic operator
     * @param repairOperator      repair or improvement operator
     * @param dominanceComparator @see {@link DominanceComparator}
     * @param apporachUsed        Approach to evaluete: @see {@link APPROACH}
     */
    public MOEADO(Problem<S> problem, int MAX_ITERATIONS, int N, ArrayList<ArrayList<Data>> weightVectors, int T,
            OutrankingModel model, int variant, CrossoverOperator<S> crossoverOperator,
            MutationOperator<S> mutationOperator, RepairOperator<S> repairOperator,
            DominanceComparator<S> dominanceComparator, APPROACH apporachUsed) {
        super(problem, MAX_ITERATIONS, N, weightVectors, T, crossoverOperator, mutationOperator, repairOperator,
                dominanceComparator, apporachUsed);
        this.model = model;
        this.intervalOutrankingApproach = new IntervalOutrankingRelations<>(problem.getNumberOfObjectives(),
                problem.getObjectives_type(), model);
        this.variant = variant;

    }

    

    @Override
    @SuppressWarnings("unchecked")
    protected void updateNeighboring(int i, ArrayList<S> FV, ArrayList<S> child) {
        for (int j = 0; j < T; j++) {
            for (int index = 0; index < child.size(); index++) {
                S y = child.get(index);
                S x = FV.get(b[i][j]);
                if (g(y, lambda.get(b[i][j])).compareTo(g(x, lambda.get(b[i][j]))) <= 0 && scalarizingFunction(y, x)) {
                    FV.set(b[i][j], (S) y.copy());
                }
            }
        }
    }

    /**
     * MOEA/D/O variants of the scalarizing function: MOEAD = 0, VAR_1 = 1, ...,
     * VAR_6 = 6
     * 
     * @param y solution to evaluate if replace
     * @param x solution
     * @return
     */
    protected boolean scalarizingFunction(S y, S x) {
        if (variant >= 1 && variant < 6) {
            return ioaRelation(variant, y, x);
        } else if (variant == 6) {
            for (int i = 1; i <= 5; i++) {
                if (ioaRelation(i, y, x)) {
                    return true;
                }
            }
        }
        return true;
    }

    /**
     * Binary preferences relation to evaluate
     * 
     * @param y
     * @param x
     * @return
     */
    protected boolean ioaRelation(int relation, S y, S x) {
        this.intervalOutrankingApproach.compare(y, x);
        switch (relation) {
            case 1:
                return this.intervalOutrankingApproach.getSigmaYX()
                        .compareTo(this.intervalOutrankingApproach.getSigmaXY()) > 0;
            case 2:
                return this.intervalOutrankingApproach.getSigmaYX().compareTo(model.getBeta()) >= 0;
            case 3:
                return this.intervalOutrankingApproach.getSigmaYX().compareTo(model.getBeta()) >= 0
                        && this.intervalOutrankingApproach.getSigmaXY().compareTo(model.getBeta()) < 0;
            case 4:
                return this.intervalOutrankingApproach.getSigmaYX()
                        .compareTo(this.intervalOutrankingApproach.getSigmaXY()) > 0
                        && this.intervalOutrankingApproach.getSigmaYX().compareTo(0.5) > 0;
            case 5:
                return this.intervalOutrankingApproach.getSigmaYX().compareTo(model.getBeta()) >= 0
                        && this.intervalOutrankingApproach.getSigmaXY().compareTo(0.5) < 0;
            default:
                throw new IllegalArgumentException("Invalid relation: " + relation);
        }
    }

    @Override
    public MOEADO<S> copy() {
        return new MOEADO<>(problem, MAX_ITERATIONS, N, lambda, T, model, variant, crossoverOperator, mutationOperator,
                repairOperator, dominanceComparator, apporachUsed);
    }

    @Override
    public String toString() {
        return "MOEADO [MAX_ITERATIONS=" + MAX_ITERATIONS + ", N=" + N + ", T=" + T + ", apporachUsed=" + apporachUsed
                + ", variant=" + variant + "]";
    }
}
