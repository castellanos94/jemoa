package com.castellanos94.preferences.impl;

import java.util.Arrays;
import java.util.List;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.preferences.Preference;
import com.castellanos94.solutions.Solution;

public class ELECTRE_Preference<S extends Solution<?>> extends Preference<S> {
    protected ElectrePreferenceModel model;
    protected final int numberOfObjectives;

    public static enum RULE {
        PESSIMISTIC, OPTIMISTIC
    };

    public ELECTRE_Preference(ElectrePreferenceModel model, int numberOfObjectives) {
        this.model = model;
        this.numberOfObjectives = numberOfObjectives;
    }

    /**
     * xPy -1, xIy 0, yPx 1, xRy 2
     */
    @Override
    public int compare(S x, S y) {
        //System.out.println("cxy");
        Data cXY = computeDegreeOfCredibility(x.getObjectives(), y.getObjectives());
       // System.out.println("cyx");
        Data cYX = computeDegreeOfCredibility(y.getObjectives(), x.getObjectives());
        boolean bSa = cYX.compareTo(model.getLambda()) >= 0;
        boolean aSb = cXY.compareTo(model.getLambda()) >= 0;
        /*System.out.println(String.format("\taSb = %3s (%.3f), bSa = %3s (%.3f) \t-> ", (aSb) ? "Yes" : "No", cXY.doubleValue(),
                (bSa) ? "Yes" : "No", cYX.doubleValue()));*/
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

    private Data computeDegreeOfCredibility(List<Data> a, List<Data> b) {
        Data[] partial_concordance_index = computePartialConcordanceIndex(a, b);
        Data concordanceIndex = computeConcordance(a, partial_concordance_index);
        Data[] discordanceIndex = computeDiscordanceIndex(a, b);
        //System.out.println("\tcj = "+Arrays.toString(partial_concordance_index));
        //System.out.println("\tdj = "+Arrays.toString(discordanceIndex));
        //System.out.println("\tC = "+concordanceIndex);
        Data rs = RealData.ONE;
        for (int i = 0; i < numberOfObjectives; i++) {
            int val =discordanceIndex[i].compareTo(concordanceIndex);
            if(val >0){
                
                Data p = RealData.ONE.copy().minus(discordanceIndex[i]);
                Data q = RealData.ONE.copy().minus(concordanceIndex);
                rs = rs.times(p.div(q));
            }else{
                return concordanceIndex;
            }
            /*
            if (discordanceIndex[i].compareTo(concordanceIndex) <= 0) {
                // rs = rsRealData.ONE;
                return concordanceIndex;
            } else {
                Data p = RealData.ONE.copy().minus(discordanceIndex[i]);
                Data q = RealData.ONE.copy().minus(concordanceIndex);
                rs = rs.times(p.div(q));
            }*/
        }
        return concordanceIndex.times(rs);
    }

    private Data[] computeDiscordanceIndex(List<Data> a, List<Data> b) {
        Data[] dj = new Data[numberOfObjectives];
        for (int i = 0; i < dj.length; i++) {
            dj[i] = RealData.ZERO;
        }
        for (int index = 0; index < numberOfObjectives; index++) {
            // Data diff = bh.get(index).minus(a.get(index));
            if (a.get(index).compareTo(b.get(index).minus(model.getPreference(index))) >= 0) {
                dj[index] = dj[index].plus(RealData.ZERO);
            } else if (a.get(index).compareTo(b.get(index).minus(model.getVeto(index))) <= 0) {
                dj[index] = dj[index].plus(RealData.ONE);
            } else {
                // es + o -?
                // Data p =
                // bh.get(index).minus(x.getObjective(index)).minus(model.getPreference(index));
                // Data p = a.get(index).minus(bh.get(index)).minus(model.getPreference(index));
                Data p = b.get(index).minus(model.getPreference(index)).minus(a.get(index));
                Data q = model.getVeto(index).minus(model.getPreference(index));
                dj[index] = dj[index].plus(p.div(q));
            }
        }
        return dj;
    }

    private Data computeConcordance(List<Data> x, Data[] partial_concordance_index) {
        Data c = RealData.ZERO;
        Data w = RealData.ZERO;
        for (int index = 0; index < numberOfObjectives; index++) {
            c = c.plus(model.getWeight(index).times(partial_concordance_index[index]));
            w = w.plus(model.getWeight(index));
        }
        return c.div(w);
    }

    private Data[] computePartialConcordanceIndex(List<Data> a, List<Data> b) {
        Data[] cj = new Data[numberOfObjectives];
        for (int index = 0; index < numberOfObjectives; index++) {
            // Data diff = b.get(index).minus(a.get(index));
            if (a.get(index).compareTo(b.get(index).minus(model.getPreference(index))) <= 0) {
                cj[index] = RealData.ZERO;
            } else if (a.get(index).compareTo(b.get(index).minus(model.getIndiference(index))) >= 0) {
                cj[index] = RealData.ONE;
            } else {
                // Data p = model.getPreference(index).plus(a.get(index).minus(b.get(index)));
                Data p = a.get(index).minus(b.get(index)).plus(model.getPreference(index));
                Data q = model.getPreference(index).minus(model.getIndiference(index));
                cj[index] = p.div(q);
            }
        }

        return cj;
    }

}
