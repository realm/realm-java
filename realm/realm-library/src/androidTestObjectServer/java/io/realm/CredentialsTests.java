package io.realm;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class CredentialsTests {

    // See https://github.com/realm/realm-sync-services/blob/master/doc/index.apib for a description of the fields
    // needed by each identity provider.

    @Test
    public void getUserInfo_isUnmodifiable() {
        SyncCredentials creds = SyncCredentials.custom("foo", "customProvider", null);
        Map<java.lang.String, Object> userInfo = creds.getUserInfo();
        try {
            userInfo.put("boom", null);
            fail();
        } catch (UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void facebook() {
        SyncCredentials creds = SyncCredentials.facebook("foo");

        assertEquals(SyncCredentials.IdentityProvider.FACEBOOK, creds.getIdentityProvider());
        assertEquals("foo", creds.getUserIdentifier());
        assertTrue(creds.getUserInfo().isEmpty());
    }

    @Test
    public void facebook_invalidInput() {
        String[] invalidInput = {null, ""};
        for (String input : invalidInput) {
            try {
                SyncCredentials.facebook(input);
                fail(input + " should have failed");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void google() {
        SyncCredentials creds = SyncCredentials.google("foo");

        assertEquals(SyncCredentials.IdentityProvider.GOOGLE, creds.getIdentityProvider());
        assertEquals("foo", creds.getUserIdentifier());
        assertTrue(creds.getUserInfo().isEmpty());
    }

    @Test
    public void google_invalidInput() {
        String[] invalidInput = {null, ""};
        for (String input : invalidInput) {
            try {
                SyncCredentials.google(input);
                fail(input + " should have failed");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void jwt() {
        SyncCredentials creds = SyncCredentials.jwt("foo");

        assertEquals(SyncCredentials.IdentityProvider.JWT, creds.getIdentityProvider());
        assertEquals("foo", creds.getUserIdentifier());
        assertTrue(creds.getUserInfo().isEmpty());
    }

    @Test
    public void jwt_invalidInput() {
        String[] invalidInput = {null, ""};
        for (String input : invalidInput) {
            try {
                SyncCredentials.jwt(input);
                fail(input + " should have failed");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void anonymous() {
        SyncCredentials creds = SyncCredentials.anonymous();
        assertEquals(SyncCredentials.IdentityProvider.ANONYMOUS, creds.getIdentityProvider());
        assertTrue(creds.getUserInfo().isEmpty());
    }

    @Test
    public void nickname() {
        SyncCredentials creds = SyncCredentials.nickname("foo", false);
        assertEquals(SyncCredentials.IdentityProvider.NICKNAME, creds.getIdentityProvider());
        assertFalse(creds.getUserInfo().isEmpty());
        assertFalse((Boolean) creds.getUserInfo().get("is_admin"));
    }

    @Test
    public void nickname_invalidInput() {
        String[] invalidInput = {null, ""};
        for (String input : invalidInput) {
            try {
                SyncCredentials.nickname(input, false);
                fail(input + " should have failed");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void usernamePassword_register() {
        SyncCredentials creds = SyncCredentials.usernamePassword("foo", "bar", true);
        assertUsernamePassword(creds, "foo", "bar", true);
    }

    @Test
    public void usernamePassword_noRegister() {
        SyncCredentials creds = SyncCredentials.usernamePassword("foo", "bar", false);
        assertUsernamePassword(creds, "foo", "bar", false);
    }

    @Test
    public void usernamePassword_defaultRegister() {
        SyncCredentials creds = SyncCredentials.usernamePassword("foo", "bar");
        assertUsernamePassword(creds, "foo", "bar", false);
    }

    // Only validate username. All passwords are allowed
    @Test
    public void usernamePassword_invalidUserName() {
        String[] invalidInput = {null, ""};
        for (String input : invalidInput) {
            try {
                SyncCredentials.usernamePassword(input, "bar", true);
                fail(input + " should have failed");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    // Null passwords are allowed
    @Test
    public void usernamePassword_nullPassword() {
        SyncCredentials creds = SyncCredentials.usernamePassword("foo", null, true);
        assertUsernamePassword(creds, "foo", null, true);
    }

    @Test
    public void custom() {
        Map<java.lang.String, Object> userInfo = new HashMap<String, Object>();
        userInfo.put("custom", "property");
        SyncCredentials creds = SyncCredentials.custom("foo", "customProvider", userInfo);

        assertEquals("foo", creds.getUserIdentifier());
        assertEquals("customProvider", creds.getIdentityProvider());
        assertEquals(1, creds.getUserInfo().size());
        assertEquals("property", creds.getUserInfo().get("custom"));
    }

    @Test
    public void custom_invalidUserName() {
        Map<String, Object> userInfo = new HashMap<String, Object>();

        String[] invalidInput = {null, ""};
        for (String username : invalidInput) {
            try {
                SyncCredentials.custom(username, SyncCredentials.IdentityProvider.FACEBOOK, userInfo);
                fail();
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void custom_invalidProvider() {
        Map<String, Object> userInfo = new HashMap<String, Object>();

        try {
            SyncCredentials.custom("foo", null, userInfo);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void assertUsernamePassword(SyncCredentials creds, String username, String password, boolean register) {
        assertEquals(username, creds.getUserIdentifier());

        Map<String, Object> userInfo = creds.getUserInfo();
        assertEquals(SyncCredentials.IdentityProvider.USERNAME_PASSWORD, creds.getIdentityProvider());

        assertEquals(password, userInfo.get("password"));

        Boolean registerActual = (Boolean) userInfo.get("register");
        if (registerActual == null) {
            registerActual = Boolean.FALSE;
        }
        assertEquals(register, registerActual);
    }
}
