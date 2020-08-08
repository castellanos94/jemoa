package com.castellanos94.preferences.impl;

import java.util.ArrayList;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.Interval;
import com.castellanos94.datatype.RealData;
import com.castellanos94.preferences.Preference;
import com.castellanos94.problems.Problem;
import com.castellanos94.solutions.Solution;
/**
 * Pendiente de revisar paper, implementacion base rara
 */
public class ITHDM_Preference extends Preference<Solution> {
    protected ITHDM_Dominance dominance;
    protected OutrankModel model;
    protected Problem p;
    private int[] coalition;

    public ITHDM_Preference(Problem p, OutrankModel model) {
        this.model = model;
        this.p = p;
        this.dominance = new ITHDM_Dominance((RealData) model.getAlpha());
        coalition = new int[p.getNumberOfObjectives()];
    }

    @Override
    public int compare(Solution x, Solution y) {
        if (dominance.compare(x, y) == -1)// x outranks y
            return -1;
        if (dominance.compare(y, x) == -1) // y outranks x
            return 1;
        RealData cxy, cyx;
        try {
            cxy = credibility_index(x, y);
            cyx = credibility_index(y, x);
            boolean outxy = cxy.compareTo(model.getBeta()) >= 0;
            boolean outyx = cyx.compareTo(model.getBeta()) >= 0;
            if (outxy && !outyx)// xPry
                return -2;
            if (!outxy && outyx)//yPrx
                return 2;
            if()

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private RealData credibility_index(Solution x, Solution y) throws CloneNotSupportedException {
        ArrayList<RealData> omegas = new ArrayList<>();
        RealData[] eta_gamma = new RealData[x.getObjectives().size()];
        RealData max_discordance;
        RealData non_discordance;
        RealData max_eta_gamma = new RealData(Double.MIN_VALUE);
        Interval ci;
        RealData[] dj = new RealData[eta_gamma.length];
        for (int i = 0; i < dj.length; i++) {
            omegas.add(compute_alpha_ij(x, y, i));
            dj[i] = compute_discordance_ij(x, y, i);
        }
        for (int i = 0; i < dj.length; i++) {
            RealData gamma = omegas.get(i);
            ci = this.concordance_index(gamma, omegas);
            RealData poss = ci.possGreaterThanOrEq((Interval) model.getLambda());
            max_discordance = new RealData(Double.MIN_VALUE);
            for (int j = 0; j < dj.length; j++) {
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
        Interval[] weights = (Interval[]) p.getInstance().getDataVector("weights");
        for (int i = 0; i < weights.length; i++) {
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

    private RealData compute_discordance_ij(Solution x, Solution y, int criteria) {
        Data veto = p.getInstance().getDataVector("veto")[criteria];
        RealData res;
        Interval value_x = (Interval) x.getObjective(criteria);
        Interval value_y = (Interval) y.getObjective(criteria);
        if (p.getObjectives_type()[criteria] == Problem.MAXIMIZATION) {
            res = value_y.possGreaterThanOrEq((Interval) value_x.plus(veto));
        } else {
            res = value_y.possSmallerThanOrEq((Interval) value_x.plus(veto));
        }

        return res;
    }

    private RealData compute_alpha_ij(Solution x, Solution y, int criteria) {
        RealData res;
        Interval value_x = (Interval) x.getObjective(criteria);
        Interval value_y = (Interval) y.getObjective(criteria);
        if (p.getObjectives_type()[criteria] == Problem.MAXIMIZATION) {
            res = value_x.possGreaterThanOrEq(value_y);
        } else {
            res = value_x.possSmallerThanOrEq(value_y);
        }

        return res;
    }
}