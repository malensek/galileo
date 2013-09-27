package galileo.test.stat;

import static org.junit.Assert.*;
import org.junit.Test;

import galileo.stat.RunningStatistics;

/**
 * Tests the RunningStatistics implementation based on the algorithm outlined by
 * B P Welford.
 */
public class Welford {

    private static double EPSILON = 0.000001;

    @Test
    public void noValues() {
        RunningStatistics z1 = new RunningStatistics();
        assertEquals("no values, mean", 0.0, z1.mean(), EPSILON);
        assertEquals("no values, var", Double.NaN, z1.var(), EPSILON);
        assertEquals("no values, std", Double.NaN, z1.std(), EPSILON);

        RunningStatistics z2 = new RunningStatistics(0.0);
        assertEquals("zero, mean", 0.0, z2.mean(), EPSILON);
        assertEquals("zero, var", 0.0, z2.var(), EPSILON);
        assertEquals("zero, std", 0.0, z2.std(), EPSILON);

        RunningStatistics z3 = new RunningStatistics(0.0, 0.0, 0.0, 0.0, 0.0);
        assertEquals("many zeros, mean", 0.0, z3.mean(), EPSILON);
        assertEquals("many zeros, var", 0.0, z3.var(), EPSILON);
        assertEquals("many zeros, std", 0.0, z3.std(), EPSILON);
    }

    @Test
    public void presetValues1() {
        double d[] = { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 20.0 };
        RunningStatistics rs = new RunningStatistics(d);
        assertEquals("mean of vals: " + stringify(d),
                5.8571428571428568, rs.mean(), EPSILON);
        assertEquals("var of vals: " + stringify(d),
                35.836734693877546, rs.var(), EPSILON);
        assertEquals("std of vals: " + stringify(d),
                5.9863790970734172, rs.std(), EPSILON);
     }

    @Test
    public void presetValues2() {
        double d[] = { 1.0, 2.0, 3.0 };
        RunningStatistics rs = new RunningStatistics(d);
        assertEquals("mean of vals: " + stringify(d),
                2.0, rs.mean(), EPSILON);
        assertEquals("var of vals: " + stringify(d),
                0.66666666666666663, rs.var(), EPSILON);
        assertEquals("std of vals: " + stringify(d),
                0.81649658092772603, rs.std(), EPSILON);
     }

    @Test
    public void presetValues3() {
        double d[] = { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
        RunningStatistics rs = new RunningStatistics(d);
        assertEquals("mean of vals: " + stringify(d),
                3.5, rs.mean(), EPSILON);
        assertEquals("var of vals: " + stringify(d),
                2.9166666666666665, rs.var(), EPSILON);
        assertEquals("std of vals: " + stringify(d),
                1.707825127659933, rs.std(), EPSILON);
     }

    /**
     * Test sample stats instead of population stats (ddof = 1).
     */
    @Test
    public void sampleStats() {
        double d[] = { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
        RunningStatistics rs = new RunningStatistics(d);
        assertEquals("mean of vals: " + stringify(d),
                3.5, rs.mean(), EPSILON);
        assertEquals("var of vals: " + stringify(d),
                3.5, rs.var(1.0), EPSILON);
        assertEquals("std of vals: " + stringify(d),
                1.8708286933869707, rs.std(1.0), EPSILON);
     }

    private String stringify(double[] ds) {
        String s = "";
        for (double d : ds) {
            s += d + " ";
        }
        return s;
    }
}
