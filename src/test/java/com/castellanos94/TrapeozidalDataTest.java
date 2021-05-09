package com.castellanos94;

import static org.junit.Assert.assertEquals;

import com.castellanos94.datatype.RealData;
import com.castellanos94.datatype.Trapezoidal;

import org.junit.Test;

public class TrapeozidalDataTest {
    @Test
    public void compare() {
        Trapezoidal a = new Trapezoidal(10, 13, 0.2, 0.5);
        Trapezoidal b = new Trapezoidal(3, 20, 1, 5);
        assertEquals(-1, a.compareTo(b));
    }

    @Test
    public void gmirTest() {
        Trapezoidal a = new Trapezoidal(10, 13, 0.2, 0.5);
        Trapezoidal b = new Trapezoidal(3, 20, 1, 5);

        assertEquals(11.55, Trapezoidal.GMIR(a), RealData.EPSILON);
        assertEquals(12.166666666666, Trapezoidal.GMIR(b), RealData.EPSILON);

    }

    @Test
    public void sum() {
        Trapezoidal a = new Trapezoidal(10, 13, 0.2, 0.5);
        Trapezoidal b = new Trapezoidal(3, 20, 1, 5);
        Trapezoidal result = new Trapezoidal(10 + 3, 13 + 20, 0.2 + 1, 0.5 + 5);
        assertEquals(result, a.plus(b));
    }

}
