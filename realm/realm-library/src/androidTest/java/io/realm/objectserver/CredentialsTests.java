package io.realm.objectserver;
/*
 * Copyright 2016 Realm Inc.
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

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class CredentialsTests {

    // See https://github.com/realm/realm-sync-services/blob/master/doc/index.apib for a description of the fields
    // needed by each identity provider.

    @Test
    public void getUserInfo_isUnmodifiable() {
        Credentials creds = Credentials.custom("foo", "bar", null);
        Map<java.lang.String, Object> userInfo = creds.getUserInfo();
        try {
            userInfo.put("boom", null);
            fail();
        } catch (UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void facebook() {
        Credentials creds = Credentials.facebook("foo");

        assertEquals(Credentials.IdentityProvider.FACEBOOK, creds.getIdentityProvider());
        assertEquals("foo", creds.getUserIdentifier());
        assertTrue(creds.getUserInfo().isEmpty());
    }

    @Test
    public void facebook_invalidInput() {
        String[] invalidInput = { null, ""};
        for (String input : invalidInput) {
            try {
                Credentials.facebook(input);
                fail(input + " should have failed");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void usernamePassword() {
        Credentials creds = Credentials.usernamePassword("foo", "bar", true);
        assertEquals("foo", creds.getUserIdentifier());
        Map<String, Object> userInfo = creds.getUserInfo();

        assertEquals(Credentials.IdentityProvider.USERNAME_PASSWORD, creds.getIdentityProvider());
        assertEquals("bar", userInfo.get("password"));
        assertTrue((Boolean) userInfo.get("register"));
    }

    // Only validate username. All passwords are allowed
    @Test
    public void usernamePassword_invalidUserName() {
        String[] invalidInput = { null, ""};
        for (String input : invalidInput) {
            try {
                Credentials.usernamePassword(input, "bar", true);
                fail(input + " should have failed");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void custom() {
        Map<java.lang.String, Object> userInfo = new HashMap<String, Object>();
        userInfo.put("custom", "property");
        Credentials creds = Credentials.custom("customProvider", "foo", userInfo);

        assertEquals("foo", creds.getUserIdentifier());
        assertEquals("customProvider", creds.getIdentityProvider());
        assertEquals(1, creds.getUserInfo().size());
        assertEquals("property", creds.getUserInfo().get("custom"));
    }
}
