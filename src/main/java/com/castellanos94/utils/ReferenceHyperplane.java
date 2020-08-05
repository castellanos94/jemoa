package com.castellanos94.utils;

import java.util.ArrayList;
import java.util.List;
import com.google.common.math.BigIntegerMath;

import org.paukov.combinatorics3.Generator;

public class ReferenceHyperplane {
    protected long H;
    protected int number_of_objectives;
    protected int segmentations;
    protected ArrayList<ArrayList<Double>> references;

    public ReferenceHyperplane(int number_of_objectives, int segmentations) {
        this.segmentations = segmentations;
        this.number_of_objectives = number_of_objectives;
        this.H = segmentations;

    }

    public void execute() {
        ArrayList<Double> elements = generateX();
        ArrayList<List<Double>> list = new ArrayList<>();
        Generator.combination(elements).simple(number_of_objectives - 1).forEach(list::add);
        references = dasAndDennis(list);
        debAndJains(references);

    }

    public ArrayList<ArrayList<Double>> dasAndDennis(ArrayList<List<Double>> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = 1; j < list.get(i).size(); j++) {
                double xij = list.get(i).get(j);
                xij = xij - (1.0) / ((double) H);
                list.get(i).set(j, xij);
            }
        }
        ArrayList<ArrayList<Double>> s = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            ArrayList<Double> v = new ArrayList<>();
            for (int j = 0; j < number_of_objectives; j++) {
                double sij;
                if (j == 0) {
                    sij = list.get(i).get(j);
                } else if (j < list.get(i).size() - 2) {
                    sij = list.get(i).get(j) - list.get(i).get(j - 1);
                } else {
                    sij = 1 - list.get(i).get(j - 1);
                }
                v.add(sij);
            }
            s.add(v);
        }
        return s;
    }

    public void debAndJains(ArrayList<ArrayList<Double>> s) {
        ArrayList<ArrayList<Double>> s2 = new ArrayList<>();
        for (int i = 0; i < s.size(); i++) {
            ArrayList<Double> v = new ArrayList<>();
            for (int j = 0; j < number_of_objectives; j++) {
                v.add((0.5) * s.get(i).get(j) + 1.0 / (2 * number_of_objectives));
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

    protected ArrayList<Double> generateX() {
        ArrayList<Double> list = new ArrayList<>();

        for (int i = 0; i <= H + number_of_objectives - 2; i++) {
            double tmp = (double) i / H;
            // if (!list.contains(tmp))
            list.add(tmp);
        }
        return list;
    }

    public static void main(String[] args) {
        ReferenceHyperplane rf = new ReferenceHyperplane(10, 3);
        System.out.println(rf);
        rf.execute();
    }

    @Override
    public String toString() {
        return "ReferenceHyperplane [H=" + H + ", number_of_objectives=" + number_of_objectives + ", segmentations="
                + segmentations + "]";
    }

    public ArrayList<ArrayList<Double>> getReferences() {
        return references;
    }
}