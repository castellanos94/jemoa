package com.castellanos94.instances;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

import com.castellanos94.datatype.Data;

/*
*@author Castellanos Alvarez, Alejandro
*@since 22/03/202
*/
public abstract class Instance {
    protected HashMap<String, Object> params;
    protected String id;
    protected String path;

    public Instance(String path) {
        this.params = new HashMap<>();
        this.path = path;
        this.id = "";
    }

    public abstract Instance loadInstance() throws FileNotFoundException;

    public Instance loadInstance(InputStream is) throws IOException {
        throw new UnsupportedOperationException("Instance undefined method");
    }

    public void addParam(String key, Object value) {
        this.params.put(key, value);
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    public Data getData(String key) {
        return (Data) this.params.get(key);
    }

    public Data[] getDataVector(String key) {
        return (Data[]) this.getParams().get(key);
    }

    public Data[][] getDataMatrix(String key) {
        return (Data[][]) this.getParams().get(key);
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        String st = "FileName: " + this.getId() + "\n";
        Iterator<String> iterator = params.keySet().iterator();
        while (iterator.hasNext()) {
            String k = iterator.next();
            Object o = params.get(k);
            st += k + " : ";
            if (o.getClass().isArray()) {
                st += "\n";
                for (int i = 0; i < Array.getLength(o); i++) {
                    Object oa = Array.get(o, i);
                    if (oa.getClass().isArray()) {

                        st += "\t" + Arrays.deepToString((Object[]) oa) + "\n ";
                    } else {
                        st += oa + " ";
                    }
                }
                st += "\n";
            } else {
                st += o.toString() + "\n";
            }
        }
        return st;
    }
}