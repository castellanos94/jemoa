package com.castellanos94;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Arrays;

import com.castellanos94.datatype.Data;
import com.castellanos94.datatype.RealData;
import com.castellanos94.utils.Distance;

public class DistanceTest {
    @Test
    public void euclideanDistance() {
        RealData[] x = new RealData[] { new RealData(3), new RealData(4), new RealData(5) };
        RealData[] y = new RealData[] { new RealData(6), new RealData(3), new RealData(-1) };
        Data result = Distance.euclideanDistance(Arrays.asList(x), Arrays.asList(y));
        assertEquals("6.78233", String.format("%6.5f", result.doubleValue()));
    }

    @Test
    public void manhattanDistance() {
        RealData[] x = new RealData[] { new RealData(3), new RealData(4), new RealData(5) };
        RealData[] y = new RealData[] { new RealData(6), new RealData(3), new RealData(-1) };
        Data result = Distance.manhattanDistance(Arrays.asList(x), Arrays.asList(y));
        assertEquals("10.00000", String.format("%6.5f", result.doubleValue()));
    }

    @Test
    public void chebyshevDistance() {
        RealData[] x = new RealData[] { new RealData(3), new RealData(4), new RealData(5) };
        RealData[] y = new RealData[] { new RealData(6), new RealData(3), new RealData(-1) };
        Data result = Distance.chebyshevDistance(Arrays.asList(x), Arrays.asList(y));
        assertEquals("6.00000", String.format("%6.5f", result.doubleValue()));
    }

    @Test
    public void canberraDistance() {
        RealData[] x = new RealData[] { new RealData(0), new RealData(3), new RealData(4), new RealData(5) };
        RealData[] y = new RealData[] { new RealData(7), new RealData(6), new RealData(3), new RealData(-1) };
        Data result = Distance.canberraDistance(Arrays.asList(x), Arrays.asList(y));
        assertEquals("2.476", String.format("%4.3f", result.doubleValue()));
    }
}
