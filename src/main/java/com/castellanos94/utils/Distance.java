package com.castellanos94.utils;

import java.util.ArrayList;
import java.util.List;

import com.castellanos94.datatype.Data;
import com.castellanos94.solutions.Solution;

/**
 * Performance the distance between solutions or two vector of points,
 * 
 * @see com.castellanos94.utils.Distance.Metric
 * @since November, 2020.
 */
public class Distance<S extends Solution<?>> implements ExtraInformation {

    private Metric metric;

    public Distance(Metric metric) {
        this.metric = metric;
    }

    /**
     * Performance the distance between the observed solutions to predicted
     * solutions.
     * 
     * @param observed  solution list
     * @param predicted solution list
     * @return list with all distance from observed objectives to predicted
     *         objectives.
     */
    public List<Data> evaluate(List<S> observed, List<S> predicted) {
        ArrayList<Data> rs = new ArrayList<>();
        switch (metric) {
            case EUCLIDEAN_DISTANCE:
                for (S observed_ : observed) {
                    for (S predicted_ : predicted) {
                        rs.add(euclideanDistance(observed_.getObjectives(), predicted_.getObjectives()));
                    }
                }
                return rs;
            case MANHATTAN_DISTANCE:
                for (S observed_ : observed) {
                    for (S predicted_ : predicted) {
                        rs.add(manhattanDistance(observed_.getObjectives(), predicted_.getObjectives()));
                    }
                }
                return rs;
            case CANBERRA_DISTANCE:
                for (S observed_ : observed) {
                    for (S predicted_ : predicted) {
                        rs.add(canberraDistance(observed_.getObjectives(), predicted_.getObjectives()));
                    }
                }
                return rs;
            case CHEBYSHEV_DISTANCE:
                for (S observed_ : observed) {
                    for (S predicted_ : predicted) {
                        rs.add(chebyshevDistance(observed_.getObjectives(), predicted_.getObjectives()));
                    }
                }
                return rs;
            default:
                throw new IllegalArgumentException("Metric not implemented yet.");
        }
    }

    /**
     * The Chebyshev distance (or <bold>Tchebychev </bold>) between two vectors or
     * points x and y.
     * <p>
     * DChebyshev(x,y) = max_i ( |x_i - y_i|)
     * <p>
     * 
     * @param x points
     * @param y points
     * @return chebyshev distance between x, y
     * @throws IllegalArgumentException If the vectors have different length
     */
    public static Data chebyshevDistance(List<Data> x, List<Data> y) {
        if (x.size() != y.size()) {
            throw new IllegalArgumentException("The vectors must have the same length");
        }
        Data rs = Data.getZeroByType(x.get(0));
        for (int i = 0; i < x.size(); i++) {
            Data tmp = x.get(i).minus(y.get(i)).abs();
            if (rs.compareTo(tmp) < 0) {
                rs = tmp.copy();
            }
        }
        return rs;
    }

    /**
     * The Canberra distance between two vectors or points x and y.
     * <p>
     * DCanberra(x,y) = Sum_i^N |x_i - y_i| / (|x_i| + |y_i|)
     * </p>
     * 
     * @param x points
     * @param y points
     * @return canberra distance between x, y
     * @throws IllegalArgumentException If the vectors have different length
     */
    public static Data canberraDistance(List<Data> x, List<Data> y) {
        if (x.size() != y.size()) {
            throw new IllegalArgumentException("The vectors must have the same length");
        }
        Data rs = Data.getZeroByType(x.get(0));
        for (int i = 0; i < x.size(); i++) {
            Data pData = x.get(i);
            Data oData = y.get(i);
            rs = rs.plus(pData.minus(oData).abs().div(pData.abs().plus(oData.abs())));

        }
        return rs;
    }

    /**
     * The Manhattan distance between two vectors or points x and y.
     * <p>
     * DManhattan(x,y) = Sum_i^N |x_i - y_i|
     * </p>
     * 
     * @param x points
     * @param y points
     * @return Manhattan distance between x, y
     * @throws IllegalArgumentException If the vectors have different length
     */
    public static Data manhattanDistance(List<Data> x, List<Data> y) {
        if (x.size() != y.size()) {
            throw new IllegalArgumentException("The vectors must have the same length");
        }
        Data rs = Data.getZeroByType(x.get(0));
        for (int i = 0; i < x.size(); i++) {
            rs = rs.plus(x.get(i).minus(y.get(i)).abs());
        }
        return rs;
    }

    /**
     * The Euclidean distance between two vectors or points x and y.
     * <p>
     * DEuclidean(x,y) = Sum_i^N sqrt((x_i - y_i)^2)
     * </p>
     * 
     * @param x points
     * @param y points
     * @return Euclidean distance between x, y
     * @throws IllegalArgumentException If the vectors have different length
     */
    public static Data euclideanDistance(List<Data> x, List<Data> y) {
        if (x.size() != y.size()) {
            throw new IllegalArgumentException("The vectors must have the same length");
        }
        Data rs = Data.getZeroByType(x.get(0));
        for (int i = 0; i < x.size(); i++) {
            rs = rs.plus(x.get(i).minus(y.get(i)).pow(2));

        }
        return rs.sqrt();
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    @Override
    public String getAttributeKey() {
        return getClass().getCanonicalName();
    }

    public static enum Metric {
        EUCLIDEAN_DISTANCE, MANHATTAN_DISTANCE, CANBERRA_DISTANCE, CHEBYSHEV_DISTANCE;
    }

    /**
     * Performance the distance between the solutions to the point.
     * 
     * @param observed solution list
     * @param point    reference point
     * @return list with all distance from observed objectives to point.
     */
    public List<Data> evaluateSolutionsToPoint(final List<S> observed, List<Data> point) {
        ArrayList<Data> rs = new ArrayList<>();
        switch (metric) {
            case EUCLIDEAN_DISTANCE:
                for (S observed_ : observed) {
                    List<Data> _p = observed_.getObjectives();
                    rs.add(euclideanDistance(_p, point));
                }

                return rs;
            case MANHATTAN_DISTANCE:
                for (S observed_ : observed) {
                    rs.add(manhattanDistance(observed_.getObjectives(), point));
                }

                return rs;
            case CANBERRA_DISTANCE:
                for (S observed_ : observed) {
                    rs.add(canberraDistance(observed_.getObjectives(), point));
                }

                return rs;
            case CHEBYSHEV_DISTANCE:
                for (S observed_ : observed) {
                    rs.add(chebyshevDistance(observed_.getObjectives(), point));
                }

                return rs;
            default:
                throw new IllegalArgumentException("Metric not implemented yet.");
        }
    }
}
