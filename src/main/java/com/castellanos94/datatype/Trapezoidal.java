package com.castellanos94.datatype;

/**
 * Trapezoidal Fuzzy Number <br>
 * Work-based arithmetic : Kumar, V. Multi-Objective Fuzzy Optimization; Indian
 * Institute of Technology: Kharagpur, India, 2010.
 * 
 */
public class Trapezoidal extends FuzzyNumber {
    /**
     * a
     */
    protected double a;
    /**
     * b
     */
    protected double b;
    /**
     * alpha
     */
    protected double c;
    /**
     * beta
     */
    protected double d;

    /**
     * A trapezoidal fuzzy number construtctor
     * 
     * @param a a value
     * @param b b value
     * @param c alpha value
     * @param d beta value
     */
    public Trapezoidal(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    /**
     * pendiente revisar como funciona con reales.
     */
    @Override
    public int compareTo(Number b) {
        double gmi_a = GMIR(this);
        double gmi_b;
        if (b instanceof Trapezoidal) {
            gmi_b = GMIR((Trapezoidal) b);
        } else {
            gmi_b = GMIR(new Trapezoidal(b.doubleValue(), b.doubleValue(), b.doubleValue(), b.doubleValue()));
        }
        return Double.compare (gmi_a , gmi_b) ;/*{
            return -1;
        }
        if (gmi_a > gmi_b) {
            return 1;
        }
        return 0;*/
    }

    /**
     * Graded Mean Integration Representation is a method of comparing two fuzzy
     * numbers. The number with higher defuzzified value is larger
     * 
     * @param value trapezoidal number
     * @return the gmi value
     */
    public static double GMIR(Trapezoidal value) {
        return (3 * value.getA() + 3 * value.getB() + value.getD() - value.getC()) / 6.0;
    }

    @Override
    public double evaluate(double v) {

        if (v < a)
            return 0;
        if (a <= v && v <= b) {
            double lw = (a == b) ? Double.NaN : b - a;
            return (v - a) / lw;
        }
        if (b <= v && v < c)
            return 1;
        if (c <= v && v < d)
            return 1 - (v - c) / (d - c);
        return 0;

    }

    @Override
    public Data times(Number b) {
        if (b instanceof Trapezoidal) {
            Trapezoidal a2 = (Trapezoidal) b;
            double uns_a1 = Math.abs(a);
            double uns_b1 = Math.abs(this.b);
            double uns_a2 = Math.abs(a2.a);
            double uns_b2 = Math.abs(a2.b);
            return new Trapezoidal(a * a2.a, this.b * a2.b, uns_a1 * a2.c + c * uns_a2, uns_b1 * a2.d + d * uns_b2);
        }
        double v = b.doubleValue();
        return this.times(new Trapezoidal(v, v, v, v));
    }

    @Override
    public Data plus(Number b) {
        if (b instanceof Trapezoidal) {
            Trapezoidal a2 = (Trapezoidal) b;
            return new Trapezoidal(a + a2.a, this.b + a2.b, c + a2.c, d + a2.d);
        }
        double v = b.doubleValue();
        return new Trapezoidal(a + v, this.b + v, c + v, d + v);
    }

    @Override
    public Data minus(Number b) {
        if (b instanceof Trapezoidal) {
            Trapezoidal a2 = (Trapezoidal) b;
            return new Trapezoidal(a - a2.b, this.b - a2.a, c + a2.d, d + a2.b);
        }
        double v = b.doubleValue();
        return new Trapezoidal(a - v, this.b - v, c + v, d + v);
    }

    @Override
    public Data div(Number b) {
        if (b instanceof Trapezoidal) {
            Trapezoidal a2 = (Trapezoidal) b;

            double uns_a1 = Math.abs(a);
            double uns_b1 = Math.abs(this.b);
            double uns_a2 = Math.abs(a2.a);
            double uns_b2 = Math.abs(a2.b);
            double alpha = (uns_a1 * a2.d + c * uns_b2) / Math.pow(a2.b, 2);
            double beta = (uns_b1 * a2.c + d * uns_a2) / Math.pow(a2.a, 2);
            return new Trapezoidal(a / a2.b, this.b / a2.a, alpha, beta);
        }
        double v = b.doubleValue();
        return new Trapezoidal(v, v, v, v);
    }

    @Override
    public Data unaryMinsu() {
        return new Trapezoidal(-1, -1, -1, -1).times(this);
    }

    @Override
    public Number getData() {
        return this;
    }

    @Override
    public Data copy() {
        return new Trapezoidal(a, b, c, d);
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    /**
     * alpha
     * 
     * @return double value
     */
    public double getC() {
        return c;
    }

    /**
     * beta
     * 
     * @return double value
     */
    public double getD() {
        return d;
    }

    @Override
    public int intValue() {
        if (a == b && b == c && c == d) {
            return (int) a;
        }
        throw new IllegalArgumentException("The propierties must be equals");
    }

    /**
     * This using GMI by Ranking of LR type fuzzy numbers. <br>
     * Chen, S.-H., & Wang, C.-C. (2009). Fuzzy distance using fuzzy absolute value.
     * 2009 International Conference on Machine Learning and Cybernetics.
     * doi:10.1109/icmlc.2009.5212628
     */
    @Override
    public Data abs() {
        double gmir = Trapezoidal.GMIR(this);
        if (gmir < 0) {
            Trapezoidal img = new Trapezoidal(-a, -b, -c, -d);
            return img;
        }
        return new RealData(gmir);
    }

    @Override
    public String toString() {
        return a + " " + b + " " + c + " " + d;
    }
}
