package com.castellanos94.datatype;

/**
 * Interval arithmetic (Moore, 1979) and Introduction to interval analysis Ramon
 * E. Moore, R. Baker Kearfott, and Michael J. Cloud. 2009. Introduction to
 * Interval Analysis. Society for Industrial and Applied Mathematics, USA.
 * 
 * 
 */
public class Interval extends Data {

    /**
     *
     */
    private static final long serialVersionUID = -4032868859966244473L;
    private Double lower;
    private Double upper;
    public static final Interval ONE = new Interval(1);
    public static final Interval ZERO = new Interval(0);

    public Interval(Number n) {
        if (n instanceof Interval) {
            Interval t = (Interval) n;
            this.lower = t.getLower();
            this.upper = t.getUpper();
        } else {
            this.lower = n.doubleValue();
            this.upper = n.doubleValue();

        }
    }

    public Interval(Number lower, Number upper) {
        this.lower = lower.doubleValue();
        this.upper = upper.doubleValue();
    }

    public RealData possGreaterThanOrEq(Interval e) {
        RealData r = this.possibility(e);
        if (r.compareTo(0) <= 0)
            return RealData.ZERO;
        if (r.compareTo(1) >= 0)
            return RealData.ONE;
        return r;
    }

    public RealData possSmallerThanOrEq(Interval e) {
        return (RealData) RealData.ONE.minus(this.possGreaterThanOrEq(e));
    }

    public RealData possibility(Number value) {
        Interval other;
        if (value instanceof Interval) {
            other = (Interval) value;

        } else {
            other = new Interval(value);
        }
        if (lower.compareTo(other.getLower()) == 0 && upper.compareTo(other.getUpper()) == 0)
            return RealData.ZERO;
        if (lower == upper && other.getLower() == other.getUpper()) {
            if (lower >= other.getUpper())
                return RealData.ONE;
            return RealData.ZERO;
        }
        double v = (upper - lower) + (other.getUpper() - other.getLower());
        RealData ped = new RealData((upper - other.getLower()) / (v));

        return (ped.compareTo(1.0) > 0) ? RealData.ONE : (ped.compareTo(0.0) <= 0) ? RealData.ZERO : ped;
    }

    @Override
    public Data times(Number value) {
        Interval data;
        if (value instanceof Interval) {
            data = (Interval) value;

        } else {
            data = new Interval(value);
        }
        double a, b, c, d;
        a = lower * data.getLower();
        b = lower * data.getUpper();
        c = upper * data.getLower();
        d = upper * data.getUpper();

        double low = Math.min(Math.min(a, b), Math.min(c, d));

        double up = Math.max(Math.max(a, b), Math.max(c, d));

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
        return new Interval(lower + data.getLower(), upper + data.getUpper());
    }

    @Override
    public Data minus(Number value) {
        Interval data;
        if (value instanceof Interval) {
            data = (Interval) value;

        } else {
            data = new Interval(value);
        }
        return new Interval(lower - data.getUpper(), upper - data.getLower());
    }

    @Override
    public Data div(Number value) {
        Interval vid;
        if (value instanceof Interval) {
            vid = (Interval) value;

        } else {
            vid = new Interval(value);
        }
        double c = vid.getLower();
        double d = vid.getUpper();
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
    public Double getLower() {
        return lower;
    }

    /**
     * @return the upper
     */
    public Double getUpper() {
        return upper;
    }

    @Override
    public String toString() {
        return String.format("%s %s", lower, upper);
    }

    @Override
    public int compareTo(Number b) {
        Interval c;
        if (b instanceof Interval) {
            c = (Interval) b;

        } else {
            c = new Interval(b);
        }
        double lower = this.lower;
        double upper = this.upper;
        double lowerB = c.getLower();
        double upperB = c.getUpper();

        if (Double.compare(lower, upper) == 0 && Double.compare(lowerB, upperB) == 0) {
            return Double.compare(upper, upperB);
        }

        int ped = this.possibility(c).compareTo(0.5);
        return (ped == 0) ? 0 : (ped < 0.5) ? -1 : 1;
    }

    @Override
    public Data unaryMinsu() {
        return new Interval(-1).times(this);
    }

    public Data exp() {
        return new Interval(Math.exp(lower), Math.exp(upper));
    }

    public Data log() {
        return new Interval(Math.log(lower), Math.log(upper));
    }

    @Override
    public Data pow(Number exp) {

        double l = this.lower;
        double u = this.upper;
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
    public Data sqrt() {
        return this.plus(0.5);
    }

    public Data sin() {
        double l = lower;
        double u = upper;
        return new Interval(Math.min(Math.sin(l), Math.sin(u)), Math.max(Math.sin(l), Math.sin(u)));
    }

    /**
     * x ∪ y = [min(lx,ly),max(ux,uy)]
     * 
     * @param y other interval
     * @return interval result
     */
    public Data union(Interval y) {
        double lx = lower;
        double rx = upper;
        double ly = y.getLower();
        double ry = y.getUpper();
        return new Interval(Math.min(lx, ly), Math.max(rx, ry));
    }

    /**
     * x ∩ y = [max(lx,ly), min(ux,y)]
     * 
     * @param y other interval
     * @return interval result
     */
    public Data intersection(Interval y) {
        double lx = lower;
        double ux = upper;
        double ly = y.getLower();
        double uy = y.getUpper();
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
        double l = this.lower;
        double u = this.upper;
        return new RealData(0.5 * (l + u));
    }

    /**
     * |x| = max(|l|,|u|)
     * 
     * @return a real data
     */
    public Data mag() {
        double l = this.lower;
        double u = this.upper;
        return new RealData(Math.max(Math.abs(l), Math.abs(u)));
    }

    @Override
    public Data abs() {

        double l = this.lower;
        double u = this.upper;
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
        double l = this.lower;
        double u = this.upper;
        return new RealData(u - l);
    }

    @Override
    public Interval copy() {
        return new Interval(lower, upper);
    }
    /*
     * public static void main(String[] args) { Interval a = new Interval(3, 4);
     * Interval b = new Interval(4, 5); System.out.println(a.times(b));
     * System.out.println(a); System.out.println(a.plus(b));
     * System.out.println(a.minus(b)); System.out.println(a.div(b));
     * System.out.println("A compare to B : " + a.compareTo(b));
     * System.out.println("A possibility to B : " + a.possibility(b));
     * System.out.println("A possibility greather that to B : " +
     * a.possGreaterThanOrEq(b));
     * System.out.println("A possibility smaller that to B : " +
     * a.possSmallerThanOrEq(b));
     * 
     * }
     */
}