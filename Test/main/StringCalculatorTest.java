package main;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StringCalculatorTest {
    private StringCalculator cal;
    @Before
    public void setUp() throws Exception {
        cal = new StringCalculator();
        System.out.println("Test setup");
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("Test tearDown");
    }

    @Test
    public void add() {
        assertEquals(0, cal.add(""));
        assertEquals(0, cal.add(null));

        assertEquals(1, cal.add("1"));
        assertEquals(3, cal.add("1,2"));
        assertEquals(6, cal.add("//;\n1;2;3"));
        assertEquals(3, cal.add("1,2"));
        assertThrows(RuntimeException.class, () -> cal.add("-1,2,3"));
    }


}