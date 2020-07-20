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
            return new IntegerData(0);
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
}