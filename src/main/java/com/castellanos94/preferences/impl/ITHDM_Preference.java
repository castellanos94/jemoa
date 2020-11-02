package com.castellanos94.preferences.impl;

import java.util.ArrayList;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.Interval;
import com.castellanos94.datatype.RealData;
import com.castellanos94.preferences.Preference;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;

/**
 * This class determines what kind of outranking relationship exists between two
 * solutions: x, y. Fernández,J.R.FigueiraandJ.Navarro,Interval-based extensions
 * of two outranking methods for multi-criteria ordinal classification, Omega,
 * https://doi.org/10.1016/j.omega.2019.05.001
 */
public class ITHDM_Preference<S extends Solution<?>> extends Preference<S> {
    protected ITHDM_Dominance<S> dominance;
    protected OutrankingModel model;
    protected Problem<?> p;
    private int[] coalition;
    private RealData sigmaXY, sigmaYX;

    public ITHDM_Preference(Problem<?> p, OutrankingModel model) {
        this.model = model;
        this.p = p;
        this.dominance = new ITHDM_Dominance<>((RealData) model.getAlpha());
        coalition = new int[p.getNumberOfObjectives()];
    }

    /**
     * * Definition 3. Relatiopships: xS(δ,λ)y in [-2], xP(δ,λ)y in [-1], xI(δ,λ)y
     * in [0], xR(δ,λ)y in [1]
     * 
     * @param x a solution
     * @param y another solution
     * @return x(S, λ-relation)y
     */
    @Override
    public int compare(S x, S y) {

        try {
            sigmaXY = credibility_index(x, y);
            sigmaYX = credibility_index(y, x);
            /*
             * System.out.println(x.getObjectives()); System.out.println(y.getObjectives());
             * System.out.println(sigmaXY+" "+sigmaYX);
             */
            int v= dominance.compare(x, y) ;
            if (v == -1)// x outranks y
                return -2;
            if( v== 1)
                return 2;
            if (sigmaXY.compareTo(model.getBeta()) >= 0 && model.getBeta().compareTo(sigmaYX) > 0)
                return -1;
            if( sigmaXY.compareTo(model.getBeta())>= 0 && sigmaYX.compareTo(model.getBeta()) >=0)
                return 0;
            if (sigmaXY.compareTo(model.getBeta()) < 0 && sigmaYX.compareTo(0) < 0)
            return 1;
            

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private RealData credibility_index(S x, S y) throws CloneNotSupportedException {
        ArrayList<RealData> omegas = new ArrayList<>();
        RealData[] eta_gamma = new RealData[p.getNumberOfObjectives()];
        RealData max_discordance;
        RealData non_discordance;
        RealData max_eta_gamma = new RealData(Double.MIN_VALUE);
        Interval ci;
        RealData[] dj = new RealData[eta_gamma.length];
        for (int i = 0; i < p.getNumberOfObjectives(); i++) {
            omegas.add(compute_alpha_ij(x, y, i));
            dj[i] = compute_discordance_ij(x, y, i);
        }

        for (int i = 0; i < p.getNumberOfObjectives(); i++) {
            RealData gamma = omegas.get(i);
            ci = this.concordance_index(gamma, omegas);
            RealData poss = ci.possGreaterThanOrEq((Interval) model.getLambda());
            max_discordance = new RealData(Double.MIN_VALUE);
            for (int j = 0; j < p.getNumberOfObjectives(); j++) {
                if (this.coalition[j] == 0 && dj[j].compareTo(max_discordance) > 0) {
                    max_discordance = dj[j];
                }
            }
            non_discordance = (RealData) RealData.ONE.minus(max_discordance);
            eta_gamma[i] = gamma.clone();
            if (eta_gamma[i].compareTo(poss) > 0) {
                eta_gamma[i] = poss.clone();
            }
            if (eta_gamma[i].compareTo(non_discordance) > 0) {
                eta_gamma[i] = non_discordance.clone();
            }
            if (max_eta_gamma.compareTo(eta_gamma[i]) < 0) {
                max_eta_gamma = eta_gamma[i].clone();
            }
        }
        return max_eta_gamma;
    }

    private Interval concordance_index(RealData gamma, ArrayList<RealData> omegas) {
        double cl = 0, cu = 0, dl = 0, du = 0;
        double lower = 0, upper = 0;
        Interval[] weights = (Interval[]) model.getWeights();
        for (int i = 0; i < p.getNumberOfObjectives(); i++) {
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
        if (p.getObjectives_type()[criteria] == Problem.MAXIMIZATION) {
            res = value_y.possGreaterThanOrEq((Interval) value_x.plus(veto));
        } else {
            res = value_y.possSmallerThanOrEq((Interval) value_x.plus(veto));
        }

        return res;
    }

    private RealData compute_alpha_ij(S x, S y, int criteria) {
        RealData res;
        Interval value_x = x.getObjective(criteria).toInterval();
        Interval value_y = y.getObjective(criteria).toInterval();
        if (p.getObjectives_type()[criteria] == Problem.MAXIMIZATION) {
            res = value_x.possGreaterThanOrEq(value_y);
        } else {
            res = value_x.possSmallerThanOrEq(value_y);
        }
        return res;
    }

    public RealData getSigmaXY() {
        return sigmaXY;
    }

    public RealData getSigmaYX() {
        return sigmaYX;
    }

    public ITHDM_Dominance<S> getDominance() {
        return dominance;
    }
}