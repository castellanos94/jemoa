package com.castellanos94.datatype;

/*
*@author Castellanos Alvarez, Alejandro
*@since 22/03/202
*/
public abstract class Data extends Number implements Comparable<Number> {

    private static final long serialVersionUID = -7730849596544487990L;

    abstract public Data times(Number b);

    abstract public Data plus(Number b);

    abstract public Data minus(Number b);

    abstract public Data div(Number b);

    abstract public Data unaryMinsu();

    abstract public Number getData();

    /**
     * @return -1 if this < b, 0 if this == b and 1 if this > b
     */
    public int compareTo(Number b) {
        if (b instanceof Interval && !(this instanceof Interval)) {
            return new Interval(this).compareTo(b);
        }

        return this.compareTo(b);
    }

    public abstract Data copy();

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
        if (d instanceof Interval) {
            return new Interval(0);
        }
        if (d instanceof Trapezoidal) {
            return new Trapezoidal(1, 1, 1, 1);
        }
        throw new UnsupportedOperationException("Operation not defined.");
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
        if (d instanceof Interval) {
            return new Interval(1);
        }
        if (d instanceof Trapezoidal) {
            return new Trapezoidal(1, 1, 1, 1);
        }
        throw new UnsupportedOperationException("Operation not defined.");
    }

    /**
     * For interval used the definition of ISSN1360-1725
     * 
     * @return absolute value
     */
    public Data abs() {

        if (this instanceof IntegerData) {
            return new IntegerData(Math.abs(this.intValue()));
        }
        if (this instanceof RealData) {
            return new RealData(Math.abs(this.doubleValue()));
        }
        throw new UnsupportedOperationException("Operation not defined.");
    }

    public Data pow(Number exp) {
        if (this instanceof IntegerData) {
            return new RealData(Math.pow(this.doubleValue(), exp.doubleValue()));
        }
        if (this instanceof RealData) {
            return new RealData(Math.pow(this.doubleValue(), exp.doubleValue()));
        }
        if (this instanceof Trapezoidal) {
            if (exp.intValue() >= 1) {
                Data rs = (Trapezoidal) this;
                for (int i = 1; i <= exp.intValue(); i++) {
                    rs = rs.times(this);
                }
                return rs;
            } else {
                return new Trapezoidal(0, 0, 0, 0);
            }
        }
        throw new UnsupportedOperationException("Operation not defined.");
    }

    public Data sqrt() {
        if (this instanceof IntegerData) {
            return new RealData(Math.sqrt(this.intValue()));
        }
        if (this instanceof RealData) {
            return new RealData(Math.sqrt(this.doubleValue()));
        }
        if (this instanceof Trapezoidal) {
            Trapezoidal a = (Trapezoidal) this;
            return new Trapezoidal(Math.sqrt(a.getA()), Math.sqrt(a.getB()), a.c / 2 * Math.sqrt(a.getA()),
                    a.d / Math.sqrt(a.getB()));
        }
        throw new UnsupportedOperationException("Operation not defined.");
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
        if (var instanceof Interval) {
            return new Interval(value);
        }
        if (var instanceof Trapezoidal) {
            return new Trapezoidal(value.doubleValue(), value.doubleValue(), value.doubleValue(), value.doubleValue());
        }
        throw new UnsupportedOperationException("Operation not defined.");

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
        if (var instanceof Interval) {
            Interval data = new Interval(var);
            return Double.isNaN(data.getLower().doubleValue()) || Double.isNaN(data.getUpper().doubleValue());
        }
        throw new IllegalArgumentException("is not number");
    }

    public Interval toInterval() {
        if (this instanceof Interval) {
            return (Interval) this;
        }
        return new Interval(this);
    }

    @Override
    public int intValue() {
        throw new UnsupportedOperationException("Operation not defined.");
    }

    @Override
    public long longValue() {
        throw new UnsupportedOperationException("Operation not defined.");
    }

    @Override
    public float floatValue() {
        throw new UnsupportedOperationException("Operation not defined.");
    }

    @Override
    public double doubleValue() {
        throw new UnsupportedOperationException("Operation not defined.");
    }

    public static Data getMin(Data a, Data b) {
        int val = a.compareTo(b);
        if (val < 0) {
            return a.copy();
        } else if (val > 0) {
            return b.copy();
        }
        return a.copy();
    }
    public static Data getMax(Data a, Data b) {
        int val = a.compareTo(b);
        if (val > 0) {
            return a.copy();
        } else if (val < 0) {
            return b.copy();
        }
        return a.copy();
    }

}