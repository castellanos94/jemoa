package com.castellanos94.datatype;

/*
*@author Castellanos Alvarez, Alejandro
*@since 22/03/202
*/
public class IntegerData extends Data {

    private static final long serialVersionUID = 6987648766520291057L;
    protected Integer data;

    public IntegerData(Number data) {
        this.data = data.intValue();
    }

    @Override
    public Data multiplication(Number value) {
        // this.data *= value.intValue();
        return new IntegerData(data * value.intValue());
    }

    @Override
    public Data addition(Number value) {
        // this.data += value.intValue();
        return new IntegerData(data + value.intValue());
    }

    @Override
    public Data subtraction(Number value) {
        // this.data -= value.intValue();
        return new IntegerData(data - value.intValue());
    }

    @Override
    public Data division(Number value) {
        // this.data /= value.intValue();
        return new RealData(data.doubleValue() / value.doubleValue());
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
        double a = this.doubleValue(), b = value.doubleValue();
        return (a == b) ? 0 : (a > b) ? 1 : -1;
    }

    @Override
    public String toString() {
        return String.format("%d", data);
    }

    @Override
    public Integer getData() {
        return this.data;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return (IntegerData) super.clone();
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
        IntegerData other = (IntegerData) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        return true;
    }
}