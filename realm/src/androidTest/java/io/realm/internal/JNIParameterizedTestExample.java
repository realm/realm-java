/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
