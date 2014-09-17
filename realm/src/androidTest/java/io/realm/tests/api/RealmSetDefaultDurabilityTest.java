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

package io.realm.tests.api;

import junit.framework.TestCase;

import io.realm.Realm;
import io.realm.internal.SharedGroup;


public class RealmSetDefaultDurabilityTest extends TestCase {

    //Test Realm API

    // setDefaultDurability(SharedGroup.Durability durability)
    public void testShouldSetDurabilityFull() {
        Realm.setDefaultDurability(SharedGroup.Durability.FULL);
    }

    // setDefaultDurability(SharedGroup.Durability durability)
    public void testShouldSetDurabilityFullByName() {
        Realm.setDefaultDurability(SharedGroup.Durability.valueOf("FULL"));
    }

    // setDefaultDurability(SharedGroup.Durability durability)
    public void testShouldSetDurabilityWithInvalidNameFail() {
        try {
            Realm.setDefaultDurability(SharedGroup.Durability.valueOf("INVALID"));
            fail("Expected IllegalArgumentException when providing illegal Durability value");
        } catch (IllegalArgumentException ioe) {
        }
    }

    // setDefaultDurability(SharedGroup.Durability durability)
    public void testShouldSetDurabilityMemOnly() {
        Realm.setDefaultDurability(SharedGroup.Durability.MEM_ONLY);
    }
}
