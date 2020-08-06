package com.castellanos94.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.solutions.Solution;
import com.google.common.math.BigIntegerMath;

import org.paukov.combinatorics3.Generator;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class ReferenceHyperplane {
    protected long H;
    protected int number_of_objectives;
    protected int segmentations;
    protected ArrayList<List<Data>> references;
    protected ArrayList<ArrayList<Pair<Solution, Data>>> potentialMembers;

    public ReferenceHyperplane(int number_of_objectives, int segmentations) {
        this.segmentations = segmentations;
        this.number_of_objectives = number_of_objectives;
        this.H = segmentations;

    }

    public void execute() {
        ArrayList<Data> elements = generateX();
        ArrayList<List<Data>> list = new ArrayList<>();
        Generator.combination(elements).simple(number_of_objectives - 1).forEach(list::add);

        references = dasAndDennis(list);
        debAndJains(references);

        potentialMembers = new ArrayList<>();
        for (int i = 0; i < references.size(); i++) {
            potentialMembers.add(new ArrayList<>());
        }

    }

    protected ArrayList<List<Data>> dasAndDennis(ArrayList<List<Data>> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = 1; j < list.get(i).size(); j++) {
                Data xij = list.get(i).get(j);
                xij = xij.minus((j - 1.0) / ((double) H));
                list.get(i).set(j, xij);
            }
        }
        ArrayList<List<Data>> s = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            ArrayList<Data> v = new ArrayList<>();
            for (int j = 0; j < number_of_objectives; j++) {
                Data sij;
                if (j == 0) {
                    sij = list.get(i).get(j);
                } else if (j < list.get(i).size() - 2) {
                    sij = list.get(i).get(j).minus(list.get(i).get(j - 1));
                    // sij = list.get(i).get(j) - list.get(i).get(j - 1);
                } else {
                    // sij = 1 - list.get(i).get(j - 1);
                    sij = RealData.ONE.minus(list.get(i).get(j - 1));
                }
                v.add(sij);
            }
            s.add(v);
        }
        return s;
    }

    protected void debAndJains(ArrayList<List<Data>> s) {
        ArrayList<List<Data>> s2 = new ArrayList<>();
        for (int i = 0; i < s.size(); i++) {
            ArrayList<Data> v = new ArrayList<>();
            for (int j = 0; j < number_of_objectives; j++) {
                v.add(new RealData(0.5).times(s.get(i).get(j)).plus(1.0 / (2 * number_of_objectives)));
                // v.add((0.5) * s.get(i).get(j) + 1.0 / (2 * number_of_objectives));
            }
            s2.add(v);
        }
        s.addAll(s);
    }

    private long calculateH() {
        int n = number_of_objectives + segmentations - 1;
        int p = segmentations;
        return BigIntegerMath.factorial(n).divide(BigIntegerMath.factorial(p).multiply(BigIntegerMath.factorial(n - p)))
                .longValue();

    }

    protected ArrayList<Data> generateX() {
        ArrayList<Data> list = new ArrayList<>();

        for (int i = 0; i <= H + number_of_objectives - 2; i++) {
            double tmp = (double) i / H;
            // if (!list.contains(tmp))
            list.add(new RealData(tmp));
        }
        return list;
    }

    public int getNumberOfPoints() {
        return references.size();
    }

    public boolean HasPotentialMember(int index) {
        return potentialMembers.get(index).size() > 0;
    }

    public void clear() {
        this.potentialMembers = new ArrayList<>();
        for (int i = 0; i < references.size(); i++) {
            potentialMembers.add(new ArrayList<>());
        }
    }

    public void AddPotentialMember(int index, Solution member_ind, Data distance) {
        this.potentialMembers.get(index).add(new ImmutablePair<Solution, Data>(member_ind, distance));
    }

    public int getPotentialMemberSize(int index) {
        return potentialMembers.get(index).size();
    }

    public ArrayList<ArrayList<Pair<Solution, Data>>> getPotentialMembers() {
        return potentialMembers;
    }

    public Solution FindClosestMember(int index) {
        Data minDistance = new RealData(Double.MAX_VALUE);
        Solution closetMember = null;
        for (Pair<Solution, Data> p : this.potentialMembers.get(index)) {
            if (p.getRight().compareTo(minDistance) < 0) {
                minDistance = p.getRight();
                closetMember = p.getLeft();
            }
        }

        return closetMember;
    }

    public Solution RandomMember(int index) {
        int i = this.potentialMembers.get(index).size() > 1
                ? Tools.getRandom().nextInt(this.potentialMembers.get(index).size() - 1)
                : 0;
        Pair<Solution, Data> p = this.getPotentialMembers().get(index).get(i);
        if (p == null)
            return null;
        return this.potentialMembers.get(index).get(i).getLeft();
    }

    public void RemovePotentialMember(int index, Solution solution) {
        Iterator<Pair<Solution, Data>> it = this.potentialMembers.get(index).iterator();
        while (it.hasNext()) {
            Pair<Solution, Data> next = it.next();
            if (next != null && next.getLeft().equals(solution)) {                
                it.remove();
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "ReferenceHyperplane [H=" + H + ", number_of_objectives=" + number_of_objectives + ", segmentations="
                + segmentations + "]";
    }

    public List<Data> getPoint(int r) {
        return references.get(r);
    }

    public void addMember(int min_rp) {
        //this.potentialMembers.get(min_rp).add(null);
    }

   public void remove(int min_rp) {
        this.references.remove(min_rp);
        this.potentialMembers.remove(min_rp);
    }

    public ArrayList<List<Data>> getReferences() {
        return references;
    }

    public void setPotentialMembers(ArrayList<ArrayList<Pair<Solution, Data>>> potentialMembers) {
        this.potentialMembers = potentialMembers;
    }

    public void setReferences(ArrayList<List<Data>> references) {
        this.references = references;
    }

    public ReferenceHyperplane copy() {
        this.clear();
        ReferenceHyperplane referenceHyperplane = new ReferenceHyperplane(this.number_of_objectives,
                this.segmentations);
        referenceHyperplane
                .setPotentialMembers((ArrayList<ArrayList<Pair<Solution, Data>>>) this.potentialMembers.clone());
        referenceHyperplane.setReferences((ArrayList<List<Data>>) this.references.clone());
        return referenceHyperplane;
    }

}