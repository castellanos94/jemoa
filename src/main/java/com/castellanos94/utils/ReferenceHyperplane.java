package com.castellanos94.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.solutions.Solution;
import com.google.common.math.BigIntegerMath;

import org.paukov.combinatorics3.Generator;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class ReferenceHyperplane<S extends Solution<?>> {
    protected long H;
    protected int number_of_objectives;
    protected int segmentations;
    protected ArrayList<ReferencePointC<S>> references;

    public ReferenceHyperplane(int number_of_objectives, int segmentations) {
        this.segmentations = segmentations;
        this.number_of_objectives = number_of_objectives;
        this.H = segmentations;

    }

    public void transformToReferencePoint(ArrayList<S> solutions) {
        if (references == null)
            references = new ArrayList<>();
        else
            references.clear();
        for (S solution : solutions) {
            references.add(new ReferencePointC<S>(solution.getObjectives()));
        }
    }

    public void execute() {
        ArrayList<Data> elements = generateX();
        ArrayList<List<Data>> list = new ArrayList<>();
        Generator.combination(elements).simple(number_of_objectives - 1).forEach(list::add);

        list = dasAndDennis(list);
        debAndJains(list);
        list = new ArrayList<>(list.stream().distinct().collect(Collectors.toList()));
        references = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            references.add(new ReferencePointC<>(new ArrayList<>(list.get(i))));
        }

    }

    public ArrayList<ArrayList<Data>> transformToData() {
        ArrayList<ArrayList<Data>> list = new ArrayList<>();
        for (int i = 0; i < references.size(); i++) {
            ReferencePointC<S> rf = this.references.get(i);
            list.add(new ArrayList<>(rf.getPoint()));
        }
        return list;
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

    @SuppressWarnings("unused")
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
            list.add(new RealData(tmp));
        }
        return list;
    }

    public int getNumberOfPoints() {
        return references.size();
    }

    public ReferencePointC<S> get(int index) {
        return this.references.get(index);
    }

    @Override
    public String toString() {
        return "ReferenceHyperplane [H=" + H + ", number_of_objectives=" + number_of_objectives + ", segmentations="
                + segmentations + "]";
    }

    public void remove(int min_rp) {
        this.references.remove(min_rp);
    }

    public ArrayList<ReferencePointC<S>> getReferences() {
        return references;
    }

    public void setReferences(ArrayList<ReferencePointC<S>> references) {
        this.references = references;
    }

    public ReferenceHyperplane<S> copy() {
        ReferenceHyperplane<S> referenceHyperplane = new ReferenceHyperplane<>(this.number_of_objectives,
                this.segmentations);
        ArrayList<ReferencePointC<S>> pointCs = new ArrayList<>();
        for (ReferencePointC<S> p : this.references) {
            pointCs.add(new ReferencePointC<>(p.getPoint()));
        }
        referenceHyperplane.setReferences(pointCs);
        return referenceHyperplane;
    }

    public static class ReferencePointC<S extends Solution<?>> {
        private List<Data> point;
        private List<Pair<S, Data>> members;
        private int potentialMembers;

        public ReferencePointC(List<Data> point) {
            this.point = point;
            this.members = new ArrayList<>();
            this.potentialMembers = 0;
        }

        public List<Pair<S, Data>> getMembers() {
            return members;
        }

        public List<Data> getPoint() {
            return point;
        }

        public int getPotentialMembers() {
            return potentialMembers;
        }

        public void incrementPotentialMembers() {
            this.potentialMembers += 1;
        }

        public void addMember(S s, Data distance) {
            this.members.add(new ImmutablePair<S, Data>(s, distance));
        }

        public void removeMember(S s) {
            Iterator<Pair<S, Data>> iterator = this.members.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getLeft().equals(s)) {
                    iterator.remove();
                    break;
                }
            }
        }

        public S FindClosestMember() {
            Data minDistance = new RealData(Double.MAX_VALUE);
            S closetMember = null;
            for (Pair<S, Data> p : this.members) {
                if (p.getRight().compareTo(minDistance) < 0) {
                    minDistance = p.getRight();
                    closetMember = p.getLeft();
                }
            }

            return closetMember;
        }

        public S RandomMember() {
            int i = this.members.size() > 1 ? Tools.getRandom().nextInt(this.members.size() - 1) : 0;
            Pair<S, Data> p = this.members.get(i);
            if (p == null)
                return null;
            return this.members.get(i).getLeft();
        }

        public void RemovePotentialMember(S solution) {
            Iterator<Pair<S, Data>> it = this.members.iterator();
            while (it.hasNext()) {
                Pair<S, Data> next = it.next();
                if (next != null && next.getLeft().equals(solution)) {
                    it.remove();
                    break;
                }
            }
        }

        public boolean HasPotentialMember() {
            return members.size() > 0;
        }

        public Object copy() {
            return new ReferencePointC<>(this.point);
        }

        @Override
        public String toString() {
            return "Members: " + this.members.size() + ", potential: " + potentialMembers + ", " + point;
        }
    }
}