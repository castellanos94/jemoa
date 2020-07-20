package com.castellanos94.instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.castellanos94.datatype.IntegerData;


public class KnapsackIntance extends Instance {

    @Override
    public Instance loadInstance(String path) throws FileNotFoundException {
        KnapsackIntance instance = new KnapsackIntance();
        Scanner sc = new Scanner(new File(path));
        
        int num_elements = Integer.parseInt(sc.nextLine().trim());
        int capacity = Integer.parseInt(sc.nextLine().trim());
        IntegerData w []= new IntegerData[num_elements];
        IntegerData b [] = new IntegerData[num_elements];
        for (int i = 0; i < num_elements; i++) {
            String[] line = sc.nextLine().trim().split(" ");
            w[i] =new IntegerData( Integer.parseInt(line[0].trim()));
            b[i] = new IntegerData(Integer.parseInt(line[1].trim()));
        }
        sc.close();
        instance.addParam("capacity", new IntegerData(capacity));
        instance.addParam("num_elements", new IntegerData(num_elements));
        instance.addParam("weights", w);
        instance.addParam("benefits", b);
        
        return instance;
    }

}