package com.castellanos94.preferences.impl;

import java.util.ArrayList;

import com.castellanos94.components.impl.DominanceComparator;
import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.Interval;
import com.castellanos94.datatype.RealData;
import com.castellanos94.preferences.Preference;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

/**
 * Interval Outranking Relations: This class determines what kind of outranking
 * relationship exists between two solutions: x, y.
 * Fernández,J.R.FigueiraandJ.Navarro,Interval-based extensions of two
 * outranking methods for multi-criteria ordinal classification, Omega,
 * https://doi.org/10.1016/j.omega.2019.05.001
 */
public class IntervalOutrankingRelations<S extends Solution<?>> extends Preference<S> {
    protected DominanceComparator<S> dominance;
    protected OutrankingModel model;
    protected final int numberOfObjectives;
    protected final int[] objectiveTypes;
    private int[] coalition;

    private RealData sigmaXY, sigmaYX;

    public IntervalOutrankingRelations(int numberOfObjectives, int objectiveTypes[], OutrankingModel model) {
        this.model = model;
        this.numberOfObjectives = numberOfObjectives;
        this.objectiveTypes = objectiveTypes;
        this.dominance = new EtaDominance<>((RealData) model.getAlpha());
        coalition = new int[numberOfObjectives];
    }

    public IntervalOutrankingRelations(int numberOfObjectives, int objectiveTypes[], OutrankingModel model,
            DominanceComparator<S> dominanceComparator) {
        this.model = model;
        this.objectiveTypes = objectiveTypes;
        this.numberOfObjectives = numberOfObjectives;
        this.dominance = dominanceComparator;
        coalition = new int[numberOfObjectives];
    }

    /**
     * * Definition 3. Relatiopships: xS(δ,λ)y in [-2], xP(δ,λ)y in [-1], xI(δ,λ)y
     * in [0], xR(δ,λ)y in [1], yS(δ,λ)x in [2]
     * 
     * @param x a solution
     * @param y another solution
     * @return x(S, λ-relation)y
     */
    @Override
    public int compare(S x, S y) {

        sigmaXY = credibility_index(x, y);
        sigmaYX = credibility_index(y, x);
        int v = dominance.compare(x, y);
        if (v == -1)// x outranks y
            return -1;
        if (v == 1)
            return 2;
        boolean booleanXSDelta = sigmaXY.compareTo(model.getDelta()) >= 0;
        int ySDelta = sigmaYX.compareTo(model.getDelta());
        if (booleanXSDelta) {
            if (ySDelta < 0) {
                return -1;
            } else if (ySDelta >= 0) {
                return 0;
            }
            return -2;
        }

        return 1;
    }

    private RealData credibility_index(S x, S y) {
        ArrayList<RealData> omegas = new ArrayList<>();
        RealData[] eta_gamma = new RealData[numberOfObjectives];
        RealData max_discordance;
        RealData non_discordance;
        RealData max_eta_gamma = new RealData(Double.MIN_VALUE);
        Interval ci;
        RealData[] dj = new RealData[eta_gamma.length];
        for (int i = 0; i < numberOfObjectives; i++) {
            omegas.add(compute_alpha_ij(x, y, i));
            dj[i] = compute_discordance_ij(x, y, i);
        }

        for (int i = 0; i < numberOfObjectives; i++) {
            RealData gamma = omegas.get(i);
            ci = this.concordance_index(gamma, omegas);
            RealData poss = ci.possGreaterThanOrEq((Interval) model.getLambda());
            max_discordance = new RealData(Double.MIN_VALUE);
            for (int j = 0; j < numberOfObjectives; j++) {
                if (this.coalition[j] == 0 && dj[j].compareTo(max_discordance) > 0) {
                    max_discordance = dj[j];
                }
            }
            non_discordance = (RealData) RealData.ONE.minus(max_discordance);
            eta_gamma[i] = gamma.copy();
            if (eta_gamma[i].compareTo(poss) > 0) {
                eta_gamma[i] = poss.copy();
            }
            if (eta_gamma[i].compareTo(non_discordance) > 0) {
                eta_gamma[i] = non_discordance.copy();
            }
            if (max_eta_gamma.compareTo(eta_gamma[i]) < 0) {
                max_eta_gamma = eta_gamma[i].copy();
            }
        }
        return max_eta_gamma;
    }

    private Interval concordance_index(RealData gamma, ArrayList<RealData> omegas) {
        double cl = 0, cu = 0, dl = 0, du = 0;
        double lower = 0, upper = 0;
        Interval[] weights = (Interval[]) model.getWeights();
        for (int i = 0; i < numberOfObjectives; i++) {
            if (omegas.get(i).compareTo(gamma) >= 0) {
                coalition[i] = 1;
                cl += weights[i].getLower();
                cu += weights[i].getUpper();
            } else {
                coalition[i] = 0;
                dl += weights[i].getLower();
                du += weights[i].getUpper();
            }
        }
        if (cl + du >= 1) {
            lower = cl;
        } else {
            lower = 1 - du;
        }
        if (cu + dl <= 1) {
            upper = cu;
        } else {
            upper = 1 - dl;
        }

        return new Interval(lower, upper);
    }

    private RealData compute_discordance_ij(S x, S y, int criteria) {
        Data veto = model.getVetos()[criteria];
        RealData res;
        Interval value_x = x.getObjective(criteria).toInterval();
        Interval value_y = y.getObjective(criteria).toInterval();

        if (objectiveTypes[criteria] == Problem.MAXIMIZATION) {
            res = value_y.possGreaterThanOrEq((Interval) value_x.plus(veto));
        } else {
            res = value_y.possSmallerThanOrEq((Interval) value_x.minus(veto));
        }

        return res;
    }

    private RealData compute_alpha_ij(S x, S y, int criteria) {
        RealData res;
        Interval value_x = x.getObjective(criteria).toInterval();
        Interval value_y = y.getObjective(criteria).toInterval();
        if (value_x.getLower().compareTo(value_x.getUpper()) == 0) {
            if (objectiveTypes[criteria] == Problem.MAXIMIZATION) {
                res = (value_x.getLower() >= value_y.getLower()) ? new RealData(1) : new RealData(0);
            } else {
                res = (value_x.getLower() <= value_y.getLower()) ? new RealData(1) : new RealData(0);
            }
        } else {
            if (objectiveTypes[criteria] == Problem.MAXIMIZATION) {
                res = value_x.possGreaterThanOrEq(value_y);
            } else {
                res = value_x.possSmallerThanOrEq(value_y);
            }
        }
        return res;
    }

    public RealData getSigmaXY() {
        return sigmaXY;
    }

    public RealData getSigmaYX() {
        return sigmaYX;
    }

    public DominanceComparator<S> getDominance() {
        return dominance;
    }

    public OutrankingModel getModel() {
        return model;
    }
}