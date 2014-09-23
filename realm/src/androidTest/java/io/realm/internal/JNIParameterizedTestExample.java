package io.realm.internal;

import junit.framework.Test;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collection;

public class JNIParameterizedTestExample extends TestCase {

    private int input_one;
    private int input_two;
    private int expected_value;

    //set up parameters for tests
    public static Collection<Object[]> parameters() {
        return Arrays.asList(
                new Object[]{2, 1, 3},
                new Object[]{1, 1, 2},
                new Object[]{-2, 1, -1}
        );
    }

    // {2, 1, 2} will be set to 2= input_one, 1= input_two and 3=expected_value
    public JNIParameterizedTestExample(int input_one, int input_two, int expected_value) {
        this.input_one = input_one;
        this.input_two = input_two;
        this.expected_value = expected_value;
    }

    // The test suite is set to method name must start with 'test',return type void and empty parameters.
    public void testIntAdd() {
        final int actual_value = input_one + input_two;
        System.out.println(expected_value + "+" + actual_value);
        assertEquals(expected_value, actual_value);
    }

    //Returns the test class and parameters
    public static Test suite() {
        return new JNITestSuite(JNIParameterizedTestExample.class, parameters());

    }


}
