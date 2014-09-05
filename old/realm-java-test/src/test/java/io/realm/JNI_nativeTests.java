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

package io.realm;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

import io.realm.internal.Util;


public class JNI_nativeTests {

    @Test
    public void testNativeExceptions() {
        String expect = "";
        for (Util.Testcase test: Util.Testcase.values()) {
            expect = test.expectedResult(0);
            try {
                test.execute(0);
            } catch (Exception e) {
                assertEquals(expect, e.toString());
            } catch (Error e) {
                assertEquals(expect, e.toString());
            }

        }
    }
}
