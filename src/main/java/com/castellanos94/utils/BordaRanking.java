package com.castellanos94.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class BordaRanking {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
        String fileName = "/home/thinkpad/Documents/jemoa/experiments/NSGA3_last/stact-result.json";
        Gson gson = new GsonBuilder().create();

        HashMap<String, Double> doRankingBorder = doRankingBorda(
                (Map<String, Object>) gson.fromJson(new FileReader(fileName), Object.class));
        System.out.println(doRankingBorder);
    }

    /**
     * Realiza rankeo borda de un estadisitico multi
     * 
     * @param stacResultObject map devuelto por stac
     * @return Un map de todos los objetos con su rankeo, no ordenado.
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, Double> doRankingBorda(Map<String, Object> stacResultObject) {
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
            // System.out.println(comparisonHashMap);
            while (!comparisonHashMap.isEmpty()) {
                ArrayList<String> max = getMax(comparisonHashMap);
                ArrayList<Integer> index = new ArrayList<>();
                ArrayList<String> keys = new ArrayList<>();
                boolean wasZero = max.size() == 0;
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
                if (wasZero) {
                    double tmp = -1;
                    int lastIndex = -1;
                    double tmpSum = sum / index.size();
                    boolean exist = false;
                    for (int i = 0; i < _names.size(); i++) {
                        if (rs.containsKey(_names.get(i))) {
                            lastIndex = i + 1;
                            tmp = rs.get(_names.get(i));
                            if (!exist && tmpSum == tmp) {
                                exist = true;
                            }
                        }
                    }
                    if (exist) {
                        while (lastIndex <= tmp) {
                            lastIndex++;
                        }
                        sum = lastIndex;
                    } else {
                        sum /= index.size();
                    }
                } else {
                    sum /= index.size();
                }
                for (int i = 0; i < index.size(); i++) {
                    rs.put(keys.get(i), sum);
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

    /**
     * Realiza rankeo de todos los casos de una metrica particular.
     * 
     * @param rankList Map con rakeo Borda.
     * @return retorna el rankeo global de los objetos trasladados, no ordenado.
     */
    public static HashMap<String, Double> doGlobalRanking(ArrayList<HashMap<String, Double>> rankList) {
        HashMap<String, Double> rs = new HashMap<>();

        for (HashMap<String, Double> map : rankList) {
            Iterator<String> keys = map.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                if (rs.containsKey(key)) {
                    rs.put(key, rs.get(key) + map.get(key));
                } else {
                    rs.put(key, map.get(key));
                }
            }
        }
        Iterator<String> iterator = rs.keySet().iterator();
        Double[] array = rs.values().toArray(new Double[rs.values().size()]);
        Arrays.sort(array);
        ArrayList<Double> rank = new ArrayList<>();
        for (Double tmp : array) {
            if (!rank.contains(tmp)) {
                rank.add(tmp);
            }
        }
        System.out.println(Arrays.toString(array) + " -> " + rank);
        while (iterator.hasNext()) {
            String key = iterator.next();
            int _index = 0;
            for (int i = 0; i < rank.size(); i++) {
                if (Double.compare(rs.get(key), rank.get(i)) == 0) {
                    _index = i + 1;
                    break;
                }
            }
            rs.put(key, (1.0) * _index);
        }
        return rs;
    }

    /**
     * Suma de todos los borda ranks.
     * 
     * @param metricList lista con borda ranks a sumar.
     * @return Map con suma de ranking ordenado.
     */
    public static HashMap<String, Double> makeSumRank(HashMap<String, ArrayList<HashMap<String, Double>>> metricList) {
        HashMap<String, Double> rs = new HashMap<>();
        Iterator<String> iterator = metricList.keySet().iterator();
        while (iterator.hasNext()) {
            String metric = iterator.next();
            for (HashMap<String, Double> map : metricList.get(metric)) {
                Iterator<String> keyIterator = map.keySet().iterator();
                while (keyIterator.hasNext()) {
                    String key = keyIterator.next();
                    if (rs.containsKey(key)) {
                        rs.put(key, rs.get(key) + map.get(key));
                    } else {
                        rs.put(key, map.get(key));
                    }
                }
            }
        }
        return sortByValue(rs);
    }

    // function to sort hashmap by values
    public static HashMap<String, Double> sortByValue(HashMap<String, Double> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
}
