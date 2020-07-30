package com.castellanos94.datatype;

/*
*@author Castellanos Alvarez, Alejandro
*@since 22/03/202
*/
public abstract class Data extends Number implements Comparable<Number>, Cloneable {

    private static final long serialVersionUID = -7730849596544487990L;

    abstract public Data multiplication(Number value);

    abstract public Data addition(Number value);

    abstract public Data subtraction(Number value);

    abstract public Data division(Number value);

    abstract public Number getData();

    /**
     * @return -1 if this < b, 0 if this == b and 1 if this > 1
     */
    abstract public int compareTo(Number b);

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || !(obj instanceof Data))
            return false;
        Data data = (Data) obj;
        return this.compareTo(data) == 0;
    }

    public static Data getZeroByType(Number d) {
        if (d == null) {
            return null;
        }
        if (d instanceof Integer || d instanceof IntegerData) {
            return new IntegerData(0);
        }
        if (d instanceof Float || d instanceof Double || d instanceof RealData) {
            return new RealData(0);
        }
        if (d instanceof IntervalData) {
            return new IntervalData(0);
        }
        return null;
    }

    public static Data getOneByType(Number d) {
        if (d == null) {
            return null;
        }
        if (d instanceof Integer || d instanceof IntegerData) {
            return new IntegerData(1);
        }
        if (d instanceof Float || d instanceof Double || d instanceof RealData) {
            return new RealData(1);
        }
        if (d instanceof IntervalData) {
            return new IntervalData(1);
        }
        return null;
    }

    public Data abs() {

        if (this instanceof IntegerData) {
            return new IntegerData(Math.abs(this.intValue()));
        }
        if (this instanceof RealData) {
            return new RealData(Math.abs(this.doubleValue()));
        }
        if (this instanceof IntervalData) {
            double l = ((IntervalData) this).getLower().doubleValue();
            double u = ((IntervalData) this).getUpper().doubleValue();
            return new IntervalData(Math.abs(l), Math.abs(u));
        }
        return null;
    }

    public Data pow(Number exp) {
        if (this instanceof IntegerData) {
            return new RealData(Math.pow(this.doubleValue(), exp.doubleValue()));
        }
        if (this instanceof RealData) {
            return new RealData(Math.pow(this.doubleValue(), exp.doubleValue()));
        }
        if (this instanceof IntervalData) {
            double l = ((IntervalData) this).getLower().doubleValue();
            double u = ((IntervalData) this).getUpper().doubleValue();
            return new IntervalData(Math.pow(l, exp.doubleValue()), Math.pow(u, exp.doubleValue()));
        }
        return null;
    }

    public Data sqr() {
        if (this instanceof IntegerData) {
            return new RealData(Math.sqrt(this.intValue()));
        }
        if (this instanceof RealData) {
            return new RealData(Math.sqrt(this.doubleValue()));
        }
        if (this instanceof IntervalData) {
            double l = ((IntervalData) this).getLower().doubleValue();
            double u = ((IntervalData) this).getUpper().doubleValue();
            return new IntervalData(Math.sqrt(l), Math.sqrt(u));
        }
        return null;
    }

    public static Data initByRefType(Number var, Number value) {
        if (var == null) {
            return null;
        }
        if (var instanceof Integer || var instanceof IntegerData) {
            return new IntegerData(value);
        }
        if (var instanceof Float || var instanceof Double || var instanceof RealData) {
            return new RealData(value);
        }
        if (var instanceof IntervalData) {
            return new IntervalData(value);
        }
        return null;
    }

    public static boolean checkNaN(Number var) {
        if (var == null) {
            return false;
        }
        if (var instanceof Integer || var instanceof IntegerData) {
            return Double.isNaN(var.doubleValue());
        }
        if (var instanceof Float || var instanceof Double || var instanceof RealData) {
            return Double.isNaN(var.doubleValue());
        }
        if (var instanceof IntervalData) {
            IntervalData data = new IntervalData(var);
            return Double.isNaN(data.getLower().doubleValue()) || Double.isNaN(data.getUpper().doubleValue());
        }
        throw new IllegalArgumentException("is not number");
    }
}