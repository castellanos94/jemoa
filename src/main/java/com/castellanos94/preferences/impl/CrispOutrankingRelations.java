package com.castellanos94.preferences.impl;

import java.util.List;

import com.castellanos94.datatype.Data;
import com.castellanos94.preferences.Preference;
import com.castellanos94.solutions.Solution;

/**
 * Sigma implementation (Degree of credibility) based on Fontana, M. E., &
 * Cavalcante, C. A. V. (2013). Electre tri method used to storage location
 * assignment into categories. Pesquisa Operacional, 33(2), 283–303.
 * https://doi.org/10.1590/s0101-74382013000200009
 */
public class CrispOutrankingRelations<S extends Solution<?>> extends Preference<S> {
    protected ElectrePreferenceModel model;
    protected final int numberOfObjectives;

    public CrispOutrankingRelations(ElectrePreferenceModel model, int numberOfObjectives) {
        this.model = model;
        this.numberOfObjectives = numberOfObjectives;
    }

    /**
     * xPy -1, xIy 0, yPx 1, xRy 2
     */
    @Override
    public int compare(S x, S y) {
        double cXY = computeDegreeOfCredibility(x.getObjectives(), y.getObjectives());
        double cYX = computeDegreeOfCredibility(y.getObjectives(), x.getObjectives());
        boolean bSa = cYX >= model.getLambda().doubleValue();
        boolean aSb = cXY >= model.getLambda().doubleValue();
        // System.out.println(String.format("\taSb = %3s (%.4f), bSa = %3s (%.4f) \t->
        // ", (aSb) ? "Yes" : "No", cXY,(bSa) ? "Yes" : "No", cYX));
        if (aSb && bSa) {
            return 0;
        }
        if (aSb && !bSa) {
            return -1;
        }

        if (!aSb && bSa) {
            return 1;
        }

        return 2;
    }

    private double computeDegreeOfCredibility(List<Data> a, List<Data> b) {
        double[] partial_concordance_index = computePartialConcordanceIndex(a, b);
        double concordanceIndex = computeConcordance(a, partial_concordance_index);
        double[] discordanceIndex = computeDiscordanceIndex(a, b);
        double rs = 1;
        for (int i = 0; i < numberOfObjectives; i++) {
            if (discordanceIndex[i] > concordanceIndex) {
                rs *= (1 - discordanceIndex[i]) / (1 - concordanceIndex);
            }
        }
        if (rs < 0)
            return 0;
        return concordanceIndex * rs;
    }

    private double[] computeDiscordanceIndex(List<Data> a, List<Data> b) {
        double[] dj = new double[numberOfObjectives];

        for (int index = 0; index < numberOfObjectives; index++) {
            Data diff = b.get(index).minus(a.get(index));
            if (diff.compareTo(model.getPreference(index)) <= 0) {
                dj[index] = 0;
            } else if (diff.compareTo(model.getVeto(index)) > 0) {
                dj[index] = 1;
            } else {
                Data p = b.get(index).minus(model.getPreference(index)).minus(a.get(index));
                Data q = model.getVeto(index).minus(model.getPreference(index));
                dj[index] = p.div(q).doubleValue();
            }
        }
        return dj;
    }

    private double computeConcordance(List<Data> x, double[] partial_concordance_index) {
        double c = 0, w = 0;
        for (int index = 0; index < numberOfObjectives; index++) {
            c += model.getWeight(index).doubleValue() * partial_concordance_index[index];
            w += model.getWeight(index).doubleValue();
        }
        return c / w;
    }

    private double[] computePartialConcordanceIndex(List<Data> a, List<Data> b) {
        double[] cj = new double[numberOfObjectives];
        for (int index = 0; index < numberOfObjectives; index++) {
            Data diff = b.get(index).minus(a.get(index));
            if (diff.compareTo(model.getPreference(index)) >= 0) {
                cj[index] = 0;
            } else if (diff.compareTo(model.getIndiference(index)) <= 0) {
                cj[index] = 1;
            } else {
                Data p = model.getPreference(index).plus(a.get(index).minus(b.get(index)));
                Data q = model.getPreference(index).minus(model.getIndiference(index));
                cj[index] = p.div(q).doubleValue();
            }
        }

        return cj;
    }

}
