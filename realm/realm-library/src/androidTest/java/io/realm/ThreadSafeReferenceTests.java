/*
 * Copyright 2017 Realm Inc.
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

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ThreadSafeReferenceTests {

    @Test(expected = IllegalArgumentException.class)
    public void create_nullThrows() {
        //noinspection ConstantConditions
        ThreadSafeReference.create(null);
    }

    @Test
    public void create_failInsideWriteTransaction() {
        fail();
    }

    @Test
    public void isValid() {
        fail();
    }

    @Test
    void isValid_closedRealm() {
        fail();
    }

    @Test
    public void close() {
        fail();
    }

    @Test
    public void realmObject_resolveSameVersion() {
        fail("Verify that object can be moved across threads");
    }

    @Test
    public void realmObject_resolveOlderVersion() {
        fail("Verify that we can resolve on an thread with an older version");
    }

    @Test
    public void realmObject_resolveNewerVersion() {
        fail("Verify that we can resolve on a thread with a never version");
    }




}
