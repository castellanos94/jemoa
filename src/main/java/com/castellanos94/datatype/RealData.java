package com.castellanos94.datatype;

/*
*@author Castellanos Alvarez, Alejandro
*@since 22/03/202
*/
public class RealData extends Data {

    private static final long serialVersionUID = 6987648766512291057L;
    public static RealData ONE = new RealData(1.0);
    public static RealData ZERO = new RealData(0.0);

    protected Double data;

    public RealData(Number n) {
        this.data = n.doubleValue();
    }

    @Override
    public Data multiplication(Number value) {
        // this.data *= value.doubleValue();
        return new RealData(data * value.doubleValue());
    }

    @Override
    public Data addition(Number value) {
        // this.data += value.doubleValue();
        return new RealData(data + value.doubleValue());
    }

    @Override
    public Data subtraction(Number value) {
        // this.data -= value.doubleValue();
        return new RealData(data - value.doubleValue());
    }

    @Override
    public Data division(Number value) {
        // this.data /= value.doubleValue();
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
        double a = this.data, b = value.doubleValue();
        return (a == b) ? 0 : (a > b) ? 1 : -1;
    }

    @Override
    public RealData clone() throws CloneNotSupportedException {
        return (RealData) super.clone();
    }

    @Override
    public String toString() {
        return String.format("%3.5f", data);
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

}