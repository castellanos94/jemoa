package com.castellanos94.mcda;

import java.util.ArrayList;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.preferences.impl.CrispOutrankingRelations;
import com.castellanos94.preferences.impl.ElectrePreferenceModel;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;
import com.castellanos94.utils.Classifier;

/**
 * Electre Tri implementation (Degree of credibility) based on Fontana, M. E., &
 * Cavalcante, C. A. V. (2013). Electre tri method used to storage location
 * assignment into categories. Pesquisa Operacional, 33(2), 283–303.
 * https://doi.org/10.1590/s0101-74382013000200009
 * 
 * @see CrispOutrankingRelations
 * @see ElectrePreferenceModel
 */
public class ElectreTri<S extends Solution<?>> extends Classifier<S> {
    protected ElectrePreferenceModel model;
    protected final int numberOfObjectives;
    protected final ArrayList<ArrayList<Data>> referenceProfile;
    protected CrispOutrankingRelations<S> preference;
    protected RULE ruleForAssignment;

    public static enum RULE {
        PESSIMISTIC, OPTIMISTIC
    };

    public ElectreTri(ElectrePreferenceModel model, int numberOfObjectives, ArrayList<ArrayList<Data>> referenceProfile,
            RULE ruleForAssignment) {
        this.model = model;
        this.numberOfObjectives = numberOfObjectives;
        this.referenceProfile = referenceProfile;
        this.preference = new CrispOutrankingRelations<>(model, numberOfObjectives);
        this.ruleForAssignment = ruleForAssignment;
    }

    @Override
    public void classify(S x) {
        if (this.ruleForAssignment == RULE.PESSIMISTIC) {
            x.setAttribute(getAttributeKey(), String.format("%c", 'A' + pessimisticRule(x)));
        } else {
            x.setAttribute(getAttributeKey(), String.format("%c", 'A' + optimisticRule(x)));
        }
    }

    @SuppressWarnings("unchecked")
    protected int optimisticRule(S x) {
        S y = (S) x.copy();
        for (int r = 0; r < this.referenceProfile.size(); r++) {
            for (int index = 0; index < numberOfObjectives; index++) {
                y.setObjective(index, this.referenceProfile.get(r).get(index));
            }
            int val = this.preference.compare(y, x);
            if (val == -1) {
                return r;
            }
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    protected int pessimisticRule(S x) {
        S y = (S) x.copy();
        for (int r = this.referenceProfile.size() - 1; r >= 0; r--) {
            for (int index = 0; index < numberOfObjectives; index++) {
                y.setObjective(index, this.referenceProfile.get(r).get(index));
            }
            int val = this.preference.compare(x, y);
            if (val == -1) {
                return r + 1;
            }
        }
        return 0;
    }

    @Override
    public String getAttributeKey() {
        return "eletre-tri-key";
    }

    public static void main(String[] args) {
        int n = 5;
        Data q[] = new Data[n];
        q[0] = new RealData(15);
        q[1] = new RealData(80);
        q[2] = new RealData(1);
        q[3] = new RealData(0.5);
        q[4] = new RealData(1);
        Data p[] = new Data[n];
        p[0] = new RealData(40);
        p[1] = new RealData(350);
        p[2] = new RealData(3);
        p[3] = new RealData(3.5);
        p[4] = new RealData(5);
        Data v[] = new Data[n];
        v[0] = new RealData(100);
        v[1] = new RealData(850);
        v[2] = new RealData(5);
        v[3] = new RealData(4.5);
        v[4] = new RealData(8);
        Data w[] = new Data[n];
        w[0] = new RealData(0.25);
        w[1] = new RealData(0.45);
        w[2] = new RealData(0.10);
        w[3] = new RealData(0.12);
        w[4] = new RealData(0.8);
        RealData lambda = new RealData(0.75);
        ElectrePreferenceModel model = new ElectrePreferenceModel(p, w, q, v, lambda);
        DoubleSolution x1 = new DoubleSolution(n, 0, 0, null);
        x1.setObjective(0, new RealData(-120));
        x1.setObjective(1, new RealData(-284));
        x1.setObjective(2, new RealData(5));
        x1.setObjective(3, new RealData(3.5));
        x1.setObjective(4, new RealData(18));
        DoubleSolution x2 = new DoubleSolution(n, 0, 0, null);
        x2.setObjective(0, new RealData(-150));
        x2.setObjective(1, new RealData(-269));
        x2.setObjective(2, new RealData(2));
        x2.setObjective(3, new RealData(4.5));
        x2.setObjective(4, new RealData(24));
        DoubleSolution x3 = new DoubleSolution(n, 0, 0, null);
        x3.setObjective(0, new RealData(-100));
        x3.setObjective(1, new RealData(-414));
        x3.setObjective(2, new RealData(4));
        x3.setObjective(3, new RealData(5.5));
        x3.setObjective(4, new RealData(17));
        DoubleSolution x4 = new DoubleSolution(n, 0, 0, null);
        x4.setObjective(0, new RealData(-60));
        x4.setObjective(1, new RealData(-596));
        x4.setObjective(2, new RealData(6));
        x4.setObjective(3, new RealData(8));
        x4.setObjective(4, new RealData(20));
        DoubleSolution x5 = new DoubleSolution(n, 0, 0, null);
        x5.setObjective(0, new RealData(-30));
        x5.setObjective(1, new RealData(-1321));
        x5.setObjective(2, new RealData(8));
        x5.setObjective(3, new RealData(7.5));
        x5.setObjective(4, new RealData(16));

        DoubleSolution x6 = new DoubleSolution(n, 0, 0, null);
        x6.setObjective(0, new RealData(-80));
        x6.setObjective(1, new RealData(-734));
        x6.setObjective(2, new RealData(5));
        x6.setObjective(3, new RealData(4));
        x6.setObjective(4, new RealData(21));

        DoubleSolution x7 = new DoubleSolution(n, 0, 0, null);
        x7.setObjective(0, new RealData(-45));
        x7.setObjective(1, new RealData(-982));
        x7.setObjective(2, new RealData(7));
        x7.setObjective(3, new RealData(8.5));
        x7.setObjective(4, new RealData(13));

        ArrayList<Data> b2 = new ArrayList<>();
        b2.add(new RealData(-50));
        b2.add(new RealData(-500));
        b2.add(new RealData(7));
        b2.add(new RealData(7));
        b2.add(new RealData(20));
        ArrayList<Data> b1 = new ArrayList<>();
        b1.add(new RealData(-100));
        b1.add(new RealData(-1000));
        b1.add(new RealData(4));
        b1.add(new RealData(4));
        b1.add(new RealData(15));
        ArrayList<ArrayList<Data>> b = new ArrayList<>();
        b.add(b1);
        b.add(b2);
        ElectreTri<DoubleSolution> eTri = new ElectreTri<>(model, n, b, ElectreTri.RULE.OPTIMISTIC);
        System.out.print("a1 ");
        eTri.classify(x1);
        System.out.println(x1.getAttribute(eTri.getAttributeKey()));
        System.out.print("a2 ");
        eTri.classify(x2);
        System.out.println(x2.getAttribute(eTri.getAttributeKey()));
        System.out.print("a3 ");
        eTri.classify(x3);
        System.out.println(x3.getAttribute(eTri.getAttributeKey()));
        System.out.print("a4 ");
        eTri.classify(x4);
        System.out.println(x4.getAttribute(eTri.getAttributeKey()));
        System.out.print("a5 ");
        eTri.classify(x5);
        System.out.println(x5.getAttribute(eTri.getAttributeKey()));
        System.out.print("a6 ");
        eTri.classify(x6);
        System.out.println(x6.getAttribute(eTri.getAttributeKey()));
        System.out.print("a7 ");
        eTri.classify(x7);
        System.out.println(x7.getAttribute(eTri.getAttributeKey()));

    }

}
