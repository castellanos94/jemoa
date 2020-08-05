package com.castellanos94.utils;

import java.util.ArrayList;
import java.util.List;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.solutions.Solution;
import com.google.common.math.BigIntegerMath;

import org.paukov.combinatorics3.Generator;

public class ReferenceHyperplane {
    protected long H;
    protected int number_of_objectives;
    protected int segmentations;
    protected Solution[] referenceSolutions;
    protected int[] pr_member_size;

    public ReferenceHyperplane(int number_of_objectives, int segmentations) {
        this.segmentations = segmentations;
        this.number_of_objectives = number_of_objectives;
        this.H = segmentations;

    }

    public void execute() {
        ArrayList<Data> elements = generateX();
        ArrayList<List<Data>> list = new ArrayList<>();
        Generator.combination(elements).simple(number_of_objectives - 1).forEach(list::add);

        ArrayList<List<Data>> references = dasAndDennis(list);
        debAndJains(references);
        referenceSolutions = new Solution[references.size()];
        pr_member_size = new int[references.size()];
        for (int i = 0; i < referenceSolutions.length; i++) {
            referenceSolutions[i] = new Solution(number_of_objectives, 0, null, null);
            for (int j = 0; j < number_of_objectives; j++) {
                referenceSolutions[i].setObjective(j, references.get(i).get(j));
            }
            pr_member_size[i] = 0;
        }
        list.clear();
        references.clear();

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
    public void resetCount(){
        for (int i = 0; i < pr_member_size.length; i++) {
            pr_member_size[i] = 0;
        }
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

    public Solution getReferenceSolution(int index) {
        return this.referenceSolutions[index];
    }

    public Solution[] getReferenceSolutions() {
        return referenceSolutions;
    }

    public int getRPMemberSize(int p) {
        return this.pr_member_size[p];
    }

    public void incrementRPMemberSize(int p) {
        this.pr_member_size[p]++;
    }

    public int getNumberOfReferencePoints() {
        return referenceSolutions.length;
    }

    @Override
    public String toString() {
        return "ReferenceHyperplane [H=" + H + ", number_of_objectives=" + number_of_objectives + ", segmentations="
                + segmentations + "]";
    }

}