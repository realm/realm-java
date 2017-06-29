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

import io.realm.permissions.UserCondition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
public class UserConditionTests {

    @Test
    public void username_nullOrEmptyThrows() {
        String[] illegalValues = { null, ""};
        for (String value : illegalValues) {
            try {
                UserCondition.username(value);
                fail();
            } catch (IllegalArgumentException ignore) {
            }
        }
    }

    @Test
    public void userId_nullOrEmptyThrows() {
        String[] illegalValues = { null, ""};
        for (String value : illegalValues) {
            try {
                UserCondition.userId(value);
                fail();
            } catch (IllegalArgumentException ignore) {
            }
        }
    }

    @Test
    public void keyValue_nullOrEmptyThrows() {
        // Keys
        String[] illegalKeys = { null, ""};
        for (String key : illegalKeys) {
            try {
                UserCondition.keyValue(key, "value");
                fail();
            } catch (IllegalArgumentException ignore) {
            }
        }

        // Values
        try {
            UserCondition.keyValue("key", null);
            fail();
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void username() {
        UserCondition condition = UserCondition.username("a@b.c");
        assertEquals("a@b.c", condition.getValue());
        assertEquals("email", condition.getKey());
        assertEquals(UserCondition.MatcherType.METADATA, condition.getType());
    }

    @Test
    public void userId() {
        UserCondition condition = UserCondition.userId("foo");
        assertEquals("foo", condition.getValue());
        assertEquals("", condition.getKey());
        assertEquals(UserCondition.MatcherType.USER_ID, condition.getType());
    }

    @Test
    public void keyValue() {
        UserCondition condition = UserCondition.keyValue("key", "value");
        assertEquals("value", condition.getValue());
        assertEquals("key", condition.getKey());
        assertEquals(UserCondition.MatcherType.METADATA, condition.getType());
    }

    @Test
    public void nonExistingPermissions() {
        UserCondition condition = UserCondition.noExistingPermissions();
        assertEquals("*", condition.getValue());
        assertEquals("", condition.getKey());
        assertEquals(UserCondition.MatcherType.USER_ID, condition.getType());
    }

    @Test
    public void equals() {
        UserCondition c1 = UserCondition.username("a@b.c");
        UserCondition c2 = UserCondition.username("a@b.c");

        assertTrue(c1.equals(c2));
        assertTrue(c2.equals(c1));
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void notEquals() {
        UserCondition c1 = UserCondition.username("a@b.c");
        UserCondition c2 = UserCondition.username("a@b.d");

        assertFalse(c1.equals(c2));
        assertFalse(c2.equals(c1));
        assertNotEquals(c1.hashCode(), c2.hashCode());
    }
}
