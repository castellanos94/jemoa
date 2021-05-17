package com.castellanos94.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.utils.ReferenceHyperplane;
import com.google.common.math.BigIntegerMath;

import org.paukov.combinatorics3.Generator;

public class IMOACO_Example {
    public static void main(String[] args) {
        int H = 14;
        int number_of_objectives = 3;
        ArrayList<Data> elements = generateX(H);
        System.out.println(elements.size()+" "+elements);
        int n =BigIntegerMath.factorial(number_of_objectives + H - 1).divide(
            BigIntegerMath.factorial(H).multiply(BigIntegerMath.factorial(number_of_objectives - 1)))
            .intValueExact();
            System.out.println("N="+n);
        ArrayList<List<Data>> list = new ArrayList<>();
        Generator.combination(elements).simple(number_of_objectives).forEach(list::add);
        
        System.out.println("Comb: "+ list.size());
        System.out.println(elements.size());
        ReferenceHyperplane<DoubleSolution> referenceHyperplane = new ReferenceHyperplane<>(number_of_objectives, H);
        referenceHyperplane.execute();
        System.out.println(referenceHyperplane);
        System.out.println(referenceHyperplane.getNumberOfPoints());
        ArrayList<ArrayList<Data>> transformToData = referenceHyperplane.transformToData();
        System.out.println("TF "+transformToData.size());
        List<ArrayList<Data>> distinct = transformToData.stream().distinct().collect(Collectors.toList());
        System.out.println(distinct.size());
    }

    private static ArrayList<Data> generateX(int H) {
        ArrayList<Data> list = new ArrayList<>();

        for (int i = 0; i <= H; i++) {
            double tmp = (double) i / H;
            list.add(new RealData(tmp));
        }
        return list;
    }
}
