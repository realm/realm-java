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

import io.realm.permissions.AccessLevel;
import io.realm.permissions.PermissionRequest;
import io.realm.permissions.UserCondition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
public class PermissionRequestTests {

    @Test
    public void nullArgumentsThrows() {
        try {
            new PermissionRequest(null, "*", AccessLevel.ADMIN);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Non-null 'condition' required."));
        }

        try {
            new PermissionRequest(UserCondition.userId("id"), null, AccessLevel.ADMIN);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Non-empty 'realmUrl' required."));
        }

        try {
            new PermissionRequest(UserCondition.userId("id"), "*", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Non-null 'accessLevel' required."));
        }
    }

    @Test
    public void url_throwsOnInvalidURIs() {
        String[] invalidUrls = { "", "\\", "<foo>" };
        for (String url : invalidUrls) {
            try {
                new PermissionRequest(UserCondition.userId("id"), url, AccessLevel.ADMIN);
                fail(url + " should have thrown");
            } catch (IllegalArgumentException ignore) {
            }
        }
    }

    @Test
    public void url_validURIs() {
        // We support "*" and valid URI's
        // We don't attempt to do more validation than that and leaves that up to ROS
        String[] validUrls = {
                "*",
                "http://foo/bar/baz",
                "https://foo/bar/baz",
                "realm://foo.bar/~/default",
                "realms://foo.bar/~/default"
        };
        for (String url : validUrls) {
            PermissionRequest request = new PermissionRequest(UserCondition.userId("id"), url, AccessLevel.ADMIN);
            assertEquals(url, request.getUrl());
        }
    }

    @Test
    public void getters() {
        UserCondition condition = UserCondition.userId("id");
        String url = "*";
        AccessLevel accessLevel = AccessLevel.ADMIN;

        PermissionRequest request = new PermissionRequest(condition, url, accessLevel);

        assertEquals(condition, request.getCondition());
        assertEquals(url, request.getUrl());
        assertEquals(accessLevel, request.getAccessLevel());
    }

    @Test
    public void equals() {
        PermissionRequest r1 = new PermissionRequest(UserCondition.userId("id"), "*", AccessLevel.ADMIN);
        PermissionRequest r2 = new PermissionRequest(UserCondition.userId("id"), "*", AccessLevel.ADMIN);

        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));
        assertEquals(r1.hashCode(), r2.hashCode());
    }

}
