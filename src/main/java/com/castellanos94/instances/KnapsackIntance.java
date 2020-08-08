package com.castellanos94.instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import com.castellanos94.datatype.IntegerData;

public class KnapsackIntance extends Instance {

    public KnapsackIntance(String path) {
        super(path);
    }

    @Override
    public Instance loadInstance() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(path));

        int num_elements = Integer.parseInt(sc.nextLine().trim());
        int capacity = Integer.parseInt(sc.nextLine().trim());
        IntegerData w[] = new IntegerData[num_elements];
        IntegerData b[] = new IntegerData[num_elements];
        for (int i = 0; i < num_elements; i++) {
            String[] line = sc.nextLine().trim().split(" ");
            w[i] = new IntegerData(Integer.parseInt(line[0].trim()));
            b[i] = new IntegerData(Integer.parseInt(line[1].trim()));
        }
        sc.close();
        this.addParam("capacity", new IntegerData(capacity));
        this.addParam("num_elements", new IntegerData(num_elements));
        this.addParam("weights", w);
        this.addParam("benefits", b);
        return this;
    }

    @Override
    public Instance loadInstance(InputStream is) throws IOException {
        Scanner sc = new Scanner(is);

        int num_elements = Integer.parseInt(sc.nextLine().trim());
        int capacity = Integer.parseInt(sc.nextLine().trim());
        IntegerData w[] = new IntegerData[num_elements];
        IntegerData b[] = new IntegerData[num_elements];
        for (int i = 0; i < num_elements; i++) {
            String[] line = sc.nextLine().trim().split(" ");
            w[i] = new IntegerData(Integer.parseInt(line[0].trim()));
            b[i] = new IntegerData(Integer.parseInt(line[1].trim()));
        }
        sc.close();
        this.addParam("capacity", new IntegerData(capacity));
        this.addParam("num_elements", new IntegerData(num_elements));
        this.addParam("weights", w);
        this.addParam("benefits", b);
        return this;
    }

}