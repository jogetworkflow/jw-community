package org.joget.apps.app.lib;

import org.junit.Test;
import static org.junit.Assert.*;

public class ExpressionHashVariableTest {

    @Test
    public void testMathematicalOperators() {
        ExpressionHashVariable ehv = new ExpressionHashVariable();
        assertEquals("2", ehv.processHashVariable("1 + 1"));
        assertEquals("20", ehv.processHashVariable("10 * 2"));
        assertEquals("5", ehv.processHashVariable("10 / 2"));
        assertEquals("1", ehv.processHashVariable("10 % 3"));
    }

    @Test
    public void testLogicalOperators() {
        ExpressionHashVariable ehv = new ExpressionHashVariable();
        assertEquals("false", ehv.processHashVariable("true and false"));
        assertEquals("true", ehv.processHashVariable("true or false"));
        assertEquals("false", ehv.processHashVariable("!true"));
    }

    @Test
    public void testRelationalOperators() {
        ExpressionHashVariable ehv = new ExpressionHashVariable();
        assertEquals("true", ehv.processHashVariable("1 < 2"));
        assertEquals("true", ehv.processHashVariable("10 == 10"));
        assertEquals("false", ehv.processHashVariable("10 != 10"));
        assertEquals("true", ehv.processHashVariable("5 >= 5"));
    }

    @Test
    public void testTernaryOperator() {
        ExpressionHashVariable ehv = new ExpressionHashVariable();
        assertEquals("yes", ehv.processHashVariable("true ? 'yes' : 'no'"));
        assertEquals("no", ehv.processHashVariable("false ? 'yes' : 'no'"));
    }

    @Test
    public void testStringMethods() {
        ExpressionHashVariable ehv = new ExpressionHashVariable();
        // Test simple property
        assertEquals("5", ehv.processHashVariable("'hello'.length"));

        // Test method invocation
        assertEquals("HELLO", ehv.processHashVariable("'hello'.toUpperCase()"));
        assertEquals("ell", ehv.processHashVariable("'hello'.substring(1, 4)"));
        assertEquals("true", ehv.processHashVariable("'hello'.contains('ell')"));
    }

    @Test
    public void testIsParsedFunction() {
        ExpressionHashVariable ehv = new ExpressionHashVariable();
        // Assuming test inputs for isParsed logic.
        // The implementation checks if input starts/ends with # or { }
        assertEquals("true", ehv.processHashVariable("#isParsed('normalString')"));
        assertEquals("false", ehv.processHashVariable("#isParsed('#hashvar#')"));
    }

    @Test
    public void testMathFunctions() {
        ExpressionHashVariable ehv = new ExpressionHashVariable();
        // Syntax requires $ for functions as per code regex replacement
        String result = ehv.processHashVariable("$max(10, 20)");
        // SpEL might return 20.0 (double) or 20 (int) depending on Method match order.
        // Accepting both.
        assertTrue("Expected 20 or 20.0 but got " + result, "20".equals(result) || "20.0".equals(result));
        assertEquals("100.0", ehv.processHashVariable("$pow(10, 2)"));
    }

    @Test
    public void testRceAttempts() {
        ExpressionHashVariable ehv = new ExpressionHashVariable();

        // 1. T(...) access
        assertNull("T(Runtime) should be blocked", ehv.processHashVariable("T(java.lang.Runtime).getRuntime()"));

        // 2. new Object access
        assertNull("new Object should be blocked", ehv.processHashVariable("new java.lang.String('test')"));

        // 3. getClass() access
        assertNull("getClass() should be blocked", ehv.processHashVariable("\"\".getClass().getName()"));
        assertNull("RCE via getClass() should be blocked",
                ehv.processHashVariable("\"\".getClass().forName('java.lang.Runtime')"));
    }

    @Test
    public void testSecurityHardening() {
        ExpressionHashVariable ehv = new ExpressionHashVariable();

        // 1. Bean Access
        assertNull("Bean access should be blocked", ehv.processHashVariable("@someBean"));

        // 2. Constructor Access (redundant with NoTypeLocator but verifying)
        assertNull("Constructor access should be blocked", ehv.processHashVariable("new java.lang.String('test')"));

        // 3. getClassLoader Access
        assertNull("classLoader property access should be blocked", ehv.processHashVariable("''.class.classLoader"));
        assertNull("getClassLoader() method access should be blocked",
                ehv.processHashVariable("''.getClass().getClassLoader()"));
    }
}
