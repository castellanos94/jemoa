package com.castellanos94.datatype;
/**
 * Interval arithmetic (Moore, 1979) and Introduction to interval analysis
 */
public class Interval extends Data {
    /**
     *
     */
    private static final long serialVersionUID = -4032868859966244473L;
    private RealData lower;
    private RealData upper;

    public Interval(Number n) {
        if (n instanceof Interval) {
            Interval t = (Interval) n;
            this.lower = new RealData(t.getLower());
            this.upper = new RealData(t.getUpper());
        } else {
            this.lower = new RealData(n);
            this.upper = new RealData(n);

        }
    }

    public Interval(Number lower, Number upper) {
        this.lower = new RealData(lower);
        this.upper = new RealData(upper);
    }

    public RealData possibility(Number value) {
        Interval data;
        if (value instanceof Interval) {
            data = (Interval) value;

        } else {
            data = new Interval(value);
        }
        if (lower.compareTo(data.getLower()) == 0 && upper.compareTo(data.getUpper()) == 0)
            return RealData.ZERO;
        if (data.getLower().compareTo(upper) > 0)
            return RealData.ONE;
        if (data.getLower().compareTo(lower) < 0)
            return (RealData) RealData.ONE.times(-1);
        RealData v = (RealData) upper.minus(lower).plus(data.getUpper().minus(data.getLower()));
        RealData ped = (RealData) upper.minus(data.getLower()).div(v);

        return (ped.compareTo(1) > 0) ? RealData.ONE : (ped.compareTo(0) <= 0) ? RealData.ZERO : ped;
    }

    @Override
    public Data times(Number value) {
        Interval data;
        if (value instanceof Interval) {
            data = (Interval) value;

        } else {
            data = new Interval(value);
        }
        RealData a1 = (RealData) lower.times(data.getLower());
        RealData a2 = (RealData) lower.times(data.getUpper());
        RealData a3 = (RealData) upper.times(data.getLower());
        RealData a4 = (RealData) upper.times(data.getUpper());
        double b1 = Math.min(a1.doubleValue(), a2.doubleValue());
        double b2 = Math.min(a3.doubleValue(), a4.doubleValue());
        double low = Math.min(b1, b2);

        double b3 = Math.max(a1.doubleValue(), a2.doubleValue());
        double b4 = Math.max(a3.doubleValue(), a4.doubleValue());
        double up = Math.max(b3, b4);

        return new Interval(low, up);
    }

    @Override
    public Data plus(Number value) {
        Interval data;
        if (value instanceof Interval) {
            data = (Interval) value;

        } else {
            data = new Interval(value);
        }
        return new Interval(lower.plus(data.getLower()), upper.plus(data.getUpper()));
    }

    @Override
    public Data minus(Number value) {
        Interval data;
        if (value instanceof Interval) {
            data = (Interval) value;

        } else {
            data = new Interval(value);
        }
        return new Interval(lower.minus(data.getUpper()), upper.minus(data.getLower()));
    }

    @Override
    public Data div(Number value) {
        Interval vid;
        if (value instanceof Interval) {
            vid = (Interval) value;

        } else {
            vid = new Interval(value);
        }
        double c = vid.getLower().doubleValue();
        double d = vid.getUpper().doubleValue();
        if (c == 0 && d > 0)
            return new Interval(1 / d, Double.POSITIVE_INFINITY);
        if (c < d && d == 0)
            return new Interval(Double.NEGATIVE_INFINITY, 1 / c);

        return this.times(new Interval(1 / c, 1 / d));
    }

    @Override
    public Number getData() {
        return new Interval(lower, upper);
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

    @Override
    public int compareTo(Number b) {
        Interval c;
        if (b instanceof Interval) {
            c = (Interval) b;

        } else {
            c = new Interval(b);
        }
        double lower = this.lower.doubleValue();
        double upper = this.upper.doubleValue();
        double lowerB = c.getLower().doubleValue();
        double upperB = c.getUpper().doubleValue();

        if (Double.compare(lower, lowerB) == 0 && Double.compare(upper, upperB) == 0)
            return 0;

        int ped = this.possibility(c).compareTo(0.5);
        return (ped == 0) ? 0 : (ped < 0.5) ? -1 : 1;
    }

    @Override
    public Data unaryMinsu() {
        return new Interval(-1).times(this);
    }

    public Data exp() {
        return new Interval(Math.exp(lower.doubleValue()), Math.exp(upper.doubleValue()));
    }

    public Data log() {
        return new Interval(Math.log(lower.doubleValue()), Math.log(upper.doubleValue()));
    }

    @Override
    public Data pow(Number exp) {

        double l = ((Interval) this).getLower().doubleValue();
        double u = ((Interval) this).getUpper().doubleValue();
        double n = exp.doubleValue();
        if (l > 0 || n % 2 != 0)
            return new Interval(Math.pow(l, n), Math.pow(u, n));
        if (u < 0 && n % 2 == 0)
            return new Interval(Math.pow(u, n), Math.pow(l, n));
        if (u == 0 || l == 0 && n % 2 == 0) {
            l = Math.pow(l, n);
            u = Math.pow(u, n);
            return new Interval(0, Math.max(l, u));
        }
        if (n == 0.5 && l < 0) {
            return new Interval(Math.pow(l, n), Math.pow(u, -n));
        }
        l = Math.pow(l, n);
        u = Math.pow(u, n);
        return new Interval(Math.min(l, u), Math.max(l, u));
    }

    @Override
    public Data sqr() {
        return this.plus(0.5);
    }

    public Data sin() {
        double l = lower.doubleValue();
        double u = upper.doubleValue();
        return new Interval(Math.min(Math.sin(l), Math.sin(u)), Math.max(Math.sin(l), Math.sin(u)));
    }

    /**
     * x ∪ y = [min(lx,ly),max(ux,uy)]
     * 
     * @param y other interval
     * @return interval result
     */
    public Data union(Interval y) {
        double lx = lower.doubleValue();
        double rx = upper.doubleValue();
        double ly = y.getLower().doubleValue();
        double ry = y.getUpper().doubleValue();
        return new Interval(Math.min(lx, ly), Math.max(rx, ry));
    }

    /**
     * x ∩ y = [max(lx,ly), min(ux,y)]
     * 
     * @param y other interval
     * @return interval result
     */
    public Data intersection(Interval y) {
        double lx = lower.doubleValue();
        double ux = upper.doubleValue();
        double ly = y.getLower().doubleValue();
        double uy = y.getUpper().doubleValue();
        if (uy < lx || ux < ly)
            return new Interval(0);
        return new Interval(Math.max(lx, ly), Math.min(ux, uy));
    }

    /**
     * m(x) = 0.5*(l + u)
     * 
     * @return a real data
     */
    public Data mindPoint() {
        double l = ((Interval) this).getLower().doubleValue();
        double u = ((Interval) this).getUpper().doubleValue();
        return new RealData(0.5 * (l + u));
    }

    /**
     * |x| = max(|l|,|u|)
     * 
     * @return a real data
     */
    public Data mag() {
        double l = ((Interval) this).getLower().doubleValue();
        double u = ((Interval) this).getUpper().doubleValue();
        return new RealData(Math.max(Math.abs(l), Math.abs(u)));
    }

    @Override
    public Data abs() {

        double l = ((Interval) this).getLower().doubleValue();
        double u = ((Interval) this).getUpper().doubleValue();
        if (l >= 0)
            return new Interval(l, u);
        if (u <= 0)
            return new Interval(Math.min(Math.abs(l), Math.abs(u)), Math.max(Math.abs(l), Math.abs(u)));
        if (Math.abs(l) > u)
            return new Interval(0, Math.abs(l));
        return new Interval(0, Math.abs(u));
    }

    /**
     * w(x) = u - l
     * 
     * @return a real data
     */
    public Data width() {
        double l = ((Interval) this).getLower().doubleValue();
        double u = ((Interval) this).getUpper().doubleValue();
        return new RealData(u - l);
    }

}