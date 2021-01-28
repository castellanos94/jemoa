package com.castellanos94.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class BorderRanking {

    public static void main(String[] args) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
        String fileName = "/home/thinkpad/Documents/jemoa/experiments/NSGA3_last/stact-result.json";
        Gson gson = new GsonBuilder().create();

        HashMap<String, Double> doRankingBorder = doRankingBorder(
                (Map<String, Object>) gson.fromJson(new FileReader(fileName), Object.class));
        System.out.println(doRankingBorder);
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Double> doRankingBorder(Map<String, Object> stacResultObject) {
        HashMap<String, Double> rs = new HashMap<>();
        if (stacResultObject.containsKey("post_hoc")) {
            Map<String, Object> post_hocranking = (Map<String, Object>) stacResultObject.get("ranking");
            ArrayList<String> _names = (ArrayList<String>) post_hocranking.get("names");
            HashMap<String, ArrayList<String>> comparisonHashMap = new HashMap<>();
            _names.forEach(name -> comparisonHashMap.put(name, new ArrayList<>()));

            Map<String, Object> post_hoc = (Map<String, Object>) stacResultObject.get("post_hoc");
            ArrayList<String> _comparisonsPostHoc = (ArrayList<String>) post_hoc.get("comparisons");
            ArrayList<Double> _resultPostHoc = (ArrayList<Double>) post_hoc.get("result");
            for (int i = 0; i < _resultPostHoc.size(); i++) {
                if (_resultPostHoc.get(i) == 1) {
                    String a = null, b = null;
                    for (String name : _names) {
                        if (_comparisonsPostHoc.get(i).contains(name)) {
                            if (a == null) {
                                a = name;
                            } else if (b == null) {
                                b = name;
                                break;
                            }
                        }
                    }
                    comparisonHashMap.get(a).add(b);
                    comparisonHashMap.get(b).add(a);
                }
            }           
            while (!comparisonHashMap.isEmpty()) {
                ArrayList<String> max = getMax(comparisonHashMap);
                ArrayList<Integer> index = new ArrayList<>();
                ArrayList<String> keys = new ArrayList<>();
                for (int i = 0; i < _names.size(); i++) {
                    String string = _names.get(i);
                    if (comparisonHashMap.containsKey(string) && comparisonHashMap.get(string).size() == max.size()) {
                        comparisonHashMap.remove(string);
                        index.add(i + 1);
                        keys.add(string);
                    }
                }
                double sum = 0;
                for (Integer integer : index) {
                    sum += integer;
                }
                for (int i = 0; i < index.size(); i++) {
                    rs.put(keys.get(i), sum / index.size());
                }
            }

        }
        return rs;
    }

    private static ArrayList<String> getMax(HashMap<String, ArrayList<String>> comparisonHashMap) {
        String key = null;
        ArrayList<String> result = null;
        Iterator<String> iterator = comparisonHashMap.keySet().iterator();
        while (iterator.hasNext()) {
            key = iterator.next();
            if (result == null) {
                result = comparisonHashMap.get(key);
            } else if (result.size() < comparisonHashMap.get(key).size()) {
                result = comparisonHashMap.get(key);
            }
        }
        return result;
    }
}
