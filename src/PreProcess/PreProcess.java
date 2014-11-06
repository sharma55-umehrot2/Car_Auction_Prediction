package PreProcess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by manshu on 11/5/14.
 */
public class PreProcess {
    ArrayList<String[]> lines = null;
    String features[];
    HashSet<String> missing_values = new HashSet<String>(Arrays.asList(new String[]{"NULL", "NOT AVAILABLE", "", "NOT AVAIL"}));
    Integer bad_nominal_cols[] = {3, 6, 7, 8, 9, 10, 11, 13, 15, 16, 17, 26, 27, 32};
    Integer bad_numerical_cols[] = {4, 14, 18, 19, 20, 21, 22, 23, 24, 25, 33};
    HashSet<Integer> neglect_cols = new HashSet<Integer>(Arrays.asList(new Integer[]{0, 2, 5, 12, 28, 29, 30, 31}));
    Integer Class_Col = 1;
    Integer numeric_bins = 100;

    public void printFile(String file_name) throws IOException{
        PrintWriter p = new PrintWriter(file_name);
        StringBuilder sb = new StringBuilder();

        String word = "";

        for (int i = 0; i < features.length; i++){
            if (neglect_cols.contains(i))
                continue;
            word = features[i];
            sb.append(word + ",");
        }
        sb.deleteCharAt(sb.length() - 1);
        p.println(sb.toString());

        for (String[] line : lines){
            sb = new StringBuilder();
            for (int i = 0; i < line.length; i++) {
                if (neglect_cols.contains(i))
                    continue;
                word = line[i];
                sb.append(word + ",");
            }
            sb.deleteCharAt(sb.length() - 1);
            p.println(sb.toString());
        }
        p.close();
    }

    private void writeMap(HashMap<Integer, String> hm, String file_name) throws IOException{
        PrintWriter p = new PrintWriter(file_name);
        for (Integer i : hm.keySet())
            p.println(String.valueOf(i) + ":" + hm.get(i));
        p.close();
    }

    public void binData(int col_num) {
        HashSet<String> uniq_data = new HashSet<String>();
        for (int i = 0; i < lines.size(); i++) {
            uniq_data.add(lines.get(i)[col_num]);
        }
        HashMap<Integer, String> hm = new HashMap<Integer, String>();
        HashMap<String, Integer> rm = new HashMap<String, Integer>();
        Iterator<String> data = uniq_data.iterator();
        int j = 0;
        String val = "";
        while (data.hasNext()){
            val = data.next();
            rm.put(val, j);
            hm.put(j++, val);
        }
        try{
            writeMap(hm, String.valueOf(col_num) + "-vocab.txt");
        }
        catch(IOException e){
            e.printStackTrace();
        }
        for (int i = 0; i < lines.size(); i++)
            lines.get(i)[col_num] = String.valueOf(rm.get(lines.get(i)[col_num]));
    }
    public void binData(int col_nums, int max, int min){
        int range = (max - min) / numeric_bins;
        HashMap<Integer, String> hm = new HashMap<Integer, String>();
        HashMap<Integer, Integer> rm = new HashMap<Integer, Integer>();

        for (int i = 0; i < lines.size(); i++) {

            Integer.parseInt(lines.get(i)[col_nums]);
        }

    }

    public void impute(int col_num, String technique){
        if (technique.equalsIgnoreCase("most_frequent")){
            HashMap<Integer, HashMap<String, Integer>> hm = new HashMap<Integer, HashMap<String, Integer>>();
            hm.put(1, new HashMap<String, Integer>());
            hm.put(0, new HashMap<String, Integer>());
            String value = "";
            HashMap<String, Integer> temp_hm;
            for (int i = 0; i < lines.size(); i++){
                temp_hm = hm.get(Integer.parseInt(lines.get(i)[Class_Col]));
                value = lines.get(i)[col_num];
                if (missing_values.contains(value.toUpperCase()))
                    continue;
                if (temp_hm.containsKey(value))
                    temp_hm.put(value, temp_hm.get(value) + 1);
                else
                    temp_hm.put(value, 1);
            }
//            System.out.println();
//            for (String key : hm.get(0).keySet()){
//                System.out.println(key + " " + hm.get(0).get(key));
//            }
//            System.out.println();
//            for (String key : hm.get(1).keySet()){
//                System.out.println(key + " " + hm.get(1).get(key));
//            }
            HashMap<Integer, String> most_freq_map = new HashMap<Integer, String>();
            int max_score = 0;
            String max_key = "";
            temp_hm = hm.get(0);
            for (String key : temp_hm.keySet()){
                if (temp_hm.get(key) > max_score){
                    max_key = key;
                    max_score = temp_hm.get(key);
                }
            }
            most_freq_map.put(0, max_key);

            max_score = 0;
            max_key = "";
            temp_hm = hm.get(1);
            for (String key : temp_hm.keySet()){
                if (temp_hm.get(key) > max_score){
                    max_key = key;
                    max_score = temp_hm.get(key);
                }
            }
            most_freq_map.put(1, max_key);
            Integer data_class = 0;
            for (int i = 0; i < lines.size(); i++){
                data_class = Integer.parseInt(lines.get(i)[Class_Col]);
                value = lines.get(i)[col_num];
                if (missing_values.contains(value.toUpperCase())){
                    lines.get(i)[col_num] = most_freq_map.get(data_class);
                }
            }
            //binData(col_num);
        }
        if (technique.equalsIgnoreCase("mean")){
            HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
            HashMap<Integer, Integer> hnum = new HashMap<Integer, Integer>();
            hm.put(0, 0);
            hm.put(1, 0);
            hnum.put(0, 0);
            hnum.put(1, 0);
            String value = "";
            Integer data_class = 0;
            for (int i = 0; i < lines.size(); i++){
                data_class = Integer.parseInt(lines.get(i)[Class_Col]);
                value = lines.get(i)[col_num];
                if (missing_values.contains(value.toUpperCase()))
                    continue;
                try{
                    hm.put(data_class, hm.get(data_class) + Integer.parseInt(value));
                }catch (NumberFormatException nfe){
                    System.out.println("Cannot apply mean imputation technique to nominal variables");
                    return;
                }
                hnum.put(data_class, hnum.get(data_class) + 1);
            }
            hm.put(0, hm.get(0) / hnum.get(0));
            hm.put(1, hm.get(1) / hnum.get(1));
            for (int i = 0; i < lines.size(); i++) {
                data_class = Integer.parseInt(lines.get(i)[Class_Col]);
                value = lines.get(i)[col_num];
                if (missing_values.contains(value.toUpperCase()))
                    lines.get(i)[col_num] = String.valueOf(hm.get(data_class));
            }
        }

    }
    public void readFile(String file_name) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(file_name));
        String line = "";
        lines = new ArrayList<String[]>();
        while ((line = br.readLine()) != null){
            lines.add(line.split(","));
        }
        features = lines.get(0);
        lines.remove(0);
        br.close();
    }

    public static void main(String args[]){
        String file = "training.csv";
        PreProcess pp = new PreProcess();
        try{
            pp.readFile(file);
            pp.impute(19, "mean");
            for (Integer i : pp.bad_nominal_cols)
                pp.impute(i, "most_frequent");
            for (Integer i : pp.bad_numerical_cols)
                pp.impute(i, "mean");
            pp.printFile("a.csv");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
