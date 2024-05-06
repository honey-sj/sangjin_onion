package main;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.assertEquals;
public class CalculatorTest {
    private Calculator cal;

    @Before
    public void setUp() throws Exception {
        cal = new Calculator();
    }
    @Test
    public void add() {
        assertEquals(9,cal.add(6,3));
    }

    @Test
    public void subtract() {
        assertEquals(3,cal.subtract(6,3));

    }

    @Test
    public void multiply() {
        assertEquals(18,cal.multiply(6,3));

    }

    @Test
    public void divide() {
        assertEquals(2,cal.divide(6,3));
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("tearDown");
    }
}