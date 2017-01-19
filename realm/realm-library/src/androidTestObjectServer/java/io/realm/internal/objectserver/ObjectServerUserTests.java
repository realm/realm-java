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
package io.realm.internal.objectserver;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ObjectServerUserTests {

    private static final URL authUrl;

    static {
        try {
            authUrl = new URL("http://localhost/auth");
        } catch (MalformedURLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static ObjectServerUser createFakeUser(String id) {
        final Token token = new Token("token_value", id, "path_value", Long.MAX_VALUE, null);
        return new ObjectServerUser(token, authUrl);
    }

    @Test
    public void equals_validUser() {
        final ObjectServerUser user1 = createFakeUser("id_value");
        final ObjectServerUser user2 = createFakeUser("id_value");
        assertTrue(user1.equals(user2));
    }

    @Test
    public void equals_loggedOutUser() {
        final ObjectServerUser user1 = createFakeUser("id_value");
        final ObjectServerUser user2 = createFakeUser("id_value");
        user1.clearTokens();
        user2.clearTokens();
        assertTrue(user1.equals(user2));
    }

    @Test
    public void hashCode_validUser() {
        final ObjectServerUser user = createFakeUser("id_value");
        assertNotEquals(0, user.hashCode());
    }

    @Test
    public void hashCode_loggedOutUser() {
        final ObjectServerUser user = createFakeUser("id_value");
        user.clearTokens();
        assertNotEquals(0, user.hashCode());
    }
}
