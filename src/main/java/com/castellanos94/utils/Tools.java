package com.castellanos94.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.Interval;
import com.castellanos94.solutions.BinarySolution;
import com.castellanos94.solutions.DoubleSolution;
import com.castellanos94.solutions.Solution;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class Tools {
    private static Random random = new Random();
    public static final int PLACES = 6;

    public static Random getRandom() {
        return random;
    }

    public static void setSeed(Long seed) {
        random.setSeed(seed);
    }

    /**
     * Generates sequences using Latin hypercube sampling (LHS). Each axis is
     * divided into {@code N} stripes and exactly one point may exist in each
     * stripe. MOEA
     * <p>
     * References:
     * <ol>
     * <li>McKay M.D., Beckman, R.J., and Conover W.J. "A Comparison of Three
     * Methods for Selecting Values of Input Variables in the Analysis of Output
     * from a Computer Code." Technometrics, 21(2):239-245, 1979.
     * </ol>
     */
    public static double[][] LHS(int N, int D) {
        double[][] result = new double[N][D];
        List<Double> temp = new ArrayList<>();
        double d = 1.0 / N;
        for (int i = 0; i < N; i++) {
            temp.add(0.0);
        }
        for (int i = 0; i < D; i++) {
            for (int j = 0; j < N; j++) {
                temp.set(j, random.doubles(j * d, (j + 1) * d).findFirst().getAsDouble());
            }

            shuffle(temp);

            for (int j = 0; j < N; j++) {
                result[j][i] = temp.get(j);
            }
        }

        return result;
    }

    /**
     * This method is used to shuffle lists instead of using the Collections.shuffle
     * method because if we set a seed, we never have full control with that method.
     * 
     * @param positions list to shuffle
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void shuffle(List positions) {
        for (int i = 0; i < positions.size(); i++) {
            int randomIndexToSwap = random.nextInt(positions.size());
            Object tmp = positions.get(randomIndexToSwap);
            positions.set(randomIndexToSwap, positions.get(i));
            positions.set(i, tmp);
        }

    }

    public static Number getRandomNumberInRange(Number lowerBound, Number upperBound) {
        if (lowerBound instanceof Interval) {
            Interval l = (Interval) lowerBound;
            Interval r = (Interval) upperBound;
            if (l.getLower().compareTo(l.getUpper()) != 0 && r.getLower().compareTo(r.getLower()) != 0) {
                return new Interval(getRandomNumberInRange(l.getLower().doubleValue(), l.getLower().doubleValue()),
                        getRandomNumberInRange(r.getLower().doubleValue(), r.getUpper().doubleValue()));
            }
            return new Interval(getRandomNumberInRange(l.getLower(), r.getUpper()));
        }
        if (lowerBound.doubleValue() > upperBound.doubleValue()) {
            throw new IllegalArgumentException("max must be greater than min");
        } else if (lowerBound.doubleValue() == upperBound.doubleValue()) {
            return lowerBound.doubleValue();
        }
        return random.doubles(lowerBound.doubleValue(), upperBound.doubleValue()).findFirst().getAsDouble();
    }

    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @SuppressWarnings("rawtypes")
    public static void SOLUTIONS_TO_FILE_DOUBLE(String path, ArrayList<DoubleSolution> solutions) throws IOException {
        ArrayList<Solution> _s = new ArrayList<>();
        solutions.forEach(_s::add);
        SOLUTIONS_TO_FILE_(path, _s);
    }

    @SuppressWarnings("rawtypes")
    public static void SOLUTIONS_TO_FILE_BINARY(String path, ArrayList<BinarySolution> solutions) throws IOException {
        ArrayList<Solution> _s = new ArrayList<>();
        solutions.forEach(_s::add);
        SOLUTIONS_TO_FILE_(path, _s);
    }

    @SuppressWarnings("rawtypes")
    private static void SOLUTIONS_TO_FILE_(String path, List<Solution> solutions) throws IOException {
        Solution solution = solutions.get(0);
        if (!path.contains(".csv"))
            path += ".csv";
        Table table = Table.create(solution.getProblem().getName());
        for (int i = 0; i < solution.getProblem().getNumberOfDecisionVars(); i++) {
            StringColumn column = StringColumn.create("Var-" + (i + 1));
            for (Solution solution_ : solutions)
                column.append(solution_.getVariable(i).toString());

            table.addColumns(column);
        }
        for (int i = 0; i < solution.getProblem().getNumberOfObjectives(); i++) {
            StringColumn column = StringColumn.create("F-" + (i + 1));
            for (Solution solution_ : solutions)
                column.append(solution_.getObjective(i).toString());
            table.addColumns(column);
        }
        for (int i = 0; i < solution.getProblem().getNumberOfConstrains(); i++) {
            StringColumn column = StringColumn.create("Res-" + (i + 1));
            for (Solution solution_ : solutions)
                column.append(solution_.getResource(i).toString());

            table.addColumns(column);
        }
        DoubleColumn column = DoubleColumn.create("Penalties");
        for (Solution solution_ : solutions)
            column.append(solution_.getPenalties());

        DoubleColumn column_rank = DoubleColumn.create("Rank");
        for (Solution solution_ : solutions)
            column_rank.append(solution_.getRank());

        table.addColumns(column, column_rank);
        table.write().csv(path);
    }

    /**
     * Generate a vector with random permutation
     * 
     * @param perm vector of permutations to save
     * @param size of vector
     */
    public static void randomPermutation(int[] perm, int size) {

        int[] index = new int[size];
        boolean[] flag = new boolean[size];

        for (int n = 0; n < size; n++) {
            index[n] = n;
            flag[n] = true;
        }

        int num = 0;
        while (num < size) {
            int start = Tools.random.nextInt(size);
            while (true) {
                if (flag[start]) {
                    perm[num] = index[start];
                    flag[start] = false;
                    num++;
                    break;
                }
                if (start == (size - 1)) {
                    start = 0;
                } else {
                    start++;
                }
            }
        }
    }

    /**
     * Simulación del lanzamiento de una moneda con probabilidad prob.
     * 
     * @param prob probabilidad del flip.
     * @return Valor 0 ó 1 dependiendo del flip.
     */
    public static boolean flip(double prob) {
        return (random.nextInt(101) / 100.0 <= prob);
    }

    /**
     * Norma para un vector
     * 
     * @param objectives
     * @return
     */
    public static Data NORML2(List<Data> objectives) {
        Data norm = Data.getZeroByType(objectives.get(0));
        for (int i = 0; i < objectives.size(); i++) {
            norm = norm.plus(objectives.get(i).pow(2));
        }
        return norm.sqrt();
    }

}