package com.castellanos94.datatype;

import org.apache.commons.math3.util.Precision;

/*
*@author Castellanos Alvarez, Alejandro
*@since 22/03/202
*/
public class RealData extends Data {

    private static final long serialVersionUID = 6987648766512291057L;
    public static final double EPSILON = 1e-6;
    public static final RealData ONE = new RealData(1.0);
    public static final RealData ZERO = new RealData(0.0);

    protected Double data;

    public RealData(Number n) {
        this.data = n.doubleValue();
    }

    @Override
    public Data times(Number value) {
        return new RealData(data * value.doubleValue());
    }

    @Override
    public Data plus(Number value) {
        return new RealData(data + value.doubleValue());
    }

    @Override
    public Data minus(Number value) {
        return new RealData(data - value.doubleValue());
    }

    @Override
    public Data div(Number value) {
        return new RealData(data / value.doubleValue());
    }

    @Override
    public Number getData() {
        return data;
    }

    @Override
    public double doubleValue() {
        return data.doubleValue();
    }

    @Override
    public float floatValue() {
        return data.floatValue();
    }

    @Override
    public int intValue() {
        return data.intValue();
    }

    @Override
    public long longValue() {
        return data.longValue();
    }

    @Override
    public int compareTo(Number value) {
        if (value instanceof Interval) {
            return new Interval(this).compareTo(value);
        }
        double a = this.data, b = value.doubleValue();
        /*
         * if (a > 1.0 || b > 1.0) return (a == b) ? 0 : (a > b) ? 1 : -1; double v =
         * Math.abs(a - b); return (v <= THRESHOLD) ? 0 : (a > b) ? 1 : -1;
         */
        return Precision.compareTo(a, b, EPSILON);
        // return Double.compare(a, b);

    }

    @Override
    public String toString() {
        return data.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RealData other = (RealData) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        return true;
    }

    @Override
    public Data unaryMinsu() {
        return new RealData(-1 * data);
    }

    @Override
    public RealData copy() {
        return new RealData(data);
    }

}