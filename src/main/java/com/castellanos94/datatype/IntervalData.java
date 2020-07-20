package com.castellanos94.datatype;

public class IntervalData extends Data {
    /**
     *
     */
    private static final long serialVersionUID = -4032868859966244473L;
    private RealData lower;
    private RealData upper;

    public IntervalData(Number n) {
        this.lower = new RealData(n);
        this.upper = new RealData(n);
    }

    public IntervalData(Number lower, Number upper) {
        this.lower = new RealData(lower);
        this.upper = new RealData(upper);
    }

    @Override
    public int compareTo(Number value) {
        IntervalData data;
        if (value instanceof IntervalData) {
            data = (IntervalData) value;

        } else {
            data = new IntervalData(value);
        }
        if (lower.compareTo(data.getLower()) == 0 && upper.compareTo(data.getUpper()) == 0)
            return 0;
        if (data.getLower().compareTo(upper) > 0)
            return 1;
        if (data.getLower().compareTo(lower) < 0)
            return -1;
        RealData v = (RealData) upper.subtraction(lower).addition(data.getUpper().subtraction(data.getLower()));
        RealData ped = (RealData) upper.subtraction(data.getLower()).division(v);

        return (ped.compareTo(0.5) > 0) ? 1 : (ped.compareTo(0.5) < 0) ? -1 : 0;
    }

    public RealData posibilityFunction(Number value) {
        IntervalData data;
        if (value instanceof IntervalData) {
            data = (IntervalData) value;

        } else {
            data = new IntervalData(value);
        }
        if (lower.compareTo(data.getLower()) == 0 && upper.compareTo(data.getUpper()) == 0)
            return RealData.ZERO;
        if (data.getLower().compareTo(upper) > 0)
            return RealData.ONE;
        if (data.getLower().compareTo(lower) < 0)
            return (RealData) RealData.ONE.multiplication(-1);
        RealData v = (RealData) upper.subtraction(lower).addition(data.getUpper().subtraction(data.getLower()));
        RealData ped = (RealData) upper.subtraction(data.getLower()).division(v);

        return (ped.compareTo(1) > 0) ? RealData.ONE : (ped.compareTo(0) <= 0) ? RealData.ZERO : ped;
    }

    @Override
    public Data multiplication(Number value) {
        IntervalData data;
        if (value instanceof IntervalData) {
            data = (IntervalData) value;

        } else {
            data = new IntervalData(value);
        }
        RealData a1 = (RealData) lower.multiplication(data.getLower());
        RealData a2 = (RealData) lower.multiplication(data.getUpper());
        RealData a3 = (RealData) upper.multiplication(data.getLower());
        RealData a4 = (RealData) upper.multiplication(data.getUpper());
        double b1 = Math.min(a1.doubleValue(), a2.doubleValue());
        double b2 = Math.min(a3.doubleValue(), a4.doubleValue());
        double low = Math.min(b1, b2);

        double b3 = Math.max(a1.doubleValue(), a2.doubleValue());
        double b4 = Math.max(a3.doubleValue(), a4.doubleValue());
        double up = Math.max(b3, b4);

        return new IntervalData(low, up);
    }

    @Override
    public Data addition(Number value) {
        IntervalData data;
        if (value instanceof IntervalData) {
            data = (IntervalData) value;

        } else {
            data = new IntervalData(value);
        }
        return new IntervalData(lower.addition(data.getLower()), upper.addition(data.getUpper()));
    }

    @Override
    public Data subtraction(Number value) {
        IntervalData data;
        if (value instanceof IntervalData) {
            data = (IntervalData) value;

        } else {
            data = new IntervalData(value);
        }
        return new IntervalData(lower.subtraction(data.getUpper()), upper.subtraction(data.getLower()));
    }

    @Override
    public Data division(Number value) {
        IntervalData vid;
        if (value instanceof IntervalData) {
            vid = (IntervalData) value;

        } else {
            vid = new IntervalData(value);
        }
        return this
                .multiplication(new IntervalData(1 / vid.getLower().doubleValue(), 1 / vid.getUpper().doubleValue()));
    }

    @Override
    public Number getData() {
        return new IntervalData(lower, upper);
    }

    /**
     * @return the lower
     */
    public RealData getLower() {
        return lower;
    }

    /**
     * @return the upper
     */
    public RealData getUpper() {
        return upper;
    }

    @Override
    public double doubleValue() {
        throw new UnsupportedOperationException("Undefined");
    }

    @Override
    public float floatValue() {
        throw new UnsupportedOperationException("Undefined");
    }

    @Override
    public int intValue() {
        throw new UnsupportedOperationException("Undefined");
    }

    @Override
    public long longValue() {
        throw new UnsupportedOperationException("Undefined");
    }

    @Override
    public String toString() {
        return String.format("[%s,%s]", lower, upper);
    }

    /*
     * public static void main(String args[]) { IntervalData carteras[][] = new
     * IntervalData[][] { { new IntervalData(100, 135), new IntervalData(15, 29),
     * new IntervalData(55, 65) }, { new IntervalData(190, 240), new
     * IntervalData(27, 48), new IntervalData(100, 120) }, { new IntervalData(100,
     * 140), new IntervalData(17, 33), new IntervalData(80, 100) } }; IntervalData
     * q[] = new IntervalData[] { new IntervalData(10, 20), new IntervalData(2, 4),
     * new IntervalData(7, 11) }; IntervalData w[] = new IntervalData[] { new
     * IntervalData(.2, .4), new IntervalData(.2, .4), new IntervalData(.3, .5) };
     * IntervalData v[] = new IntervalData[] { new IntervalData(100, 110), new
     * IntervalData(4, 6), new IntervalData(55, 65) };
     * 
     * for (IntervalData[] cartera : carteras) {
     * System.out.println(Arrays.toString(cartera)); }
     * System.out.println(Arrays.toString(q));
     * System.out.println(Arrays.toString(w));
     * System.out.println(Arrays.toString(v)); IntervalData c1 = (IntervalData)
     * carteras[0][0].subtraction(carteras[1][0]); IntervalData mq = (IntervalData)
     * q[0].multiplication(-1); System.out.println(c1); System.out.println(mq);
     * System.out.println(c1.compareTo(mq)); for (int i = 0; i < carteras.length;
     * i++) { IntervalData cj; IntervalData nq; for (int j = 0; j < carteras.length;
     * j++) { if (i != j){ RealData cjv[] = new RealData[v.length]; for (int k = 0;
     * k < v.length; k++) { cj = (IntervalData)
     * carteras[i][k].subtraction(carteras[j][k]); nq = (IntervalData)
     * q[k].multiplication(-1); cjv[k] = cj.posibilityFunction(nq);
     * //System.out.printf("C[%3d](%3d,%3d) = %s\n", k + 1, i + 1, j + 1,
     * cj.posibilityFunction(nq)); // System.out.printf("C[%3d](%3d,%3d) = %d\n", k
     * + 1, i + 1, j + 1, cj.compareTo(nq)); }
     * System.out.println("Cj("+i+", "+j+"): "+Arrays.toString(cjv)); } } } }
     */
}