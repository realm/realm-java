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

package io.realm;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collection;

import io.realm.rule.TestRealmConfigurationFactory;

import static io.realm.util.SyncTestUtils.createTestUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SyncManagerTests {

    private UserStore userStore;

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        userStore = new UserStore() {
            @Override
            public void put(SyncUser user) {}

            @Override
            public SyncUser getCurrent() {
                return null;
            }

            @Override
            public SyncUser get(String identity, String authenticationUrl) {
                return null;
            }

            @Override
            public void remove(String identity, String authenticationUrl) {
            }

            @Override
            public Collection<SyncUser> allUsers() {
                return null;
            }

            @Override
            public boolean isActive(String identity, String authenticationUrl) {
                return true;
            }
        };
    }

    @Test
    public void set_userStore() {
        SyncManager.setUserStore(userStore);
        assertTrue(userStore.equals(SyncManager.getUserStore()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void set_userStore_null() {
        SyncManager.setUserStore(null);
    }

    @Test
    public void authListener() {
        SyncUser user = createTestUser();
        final int[] counter = {0, 0};

        AuthenticationListener authenticationListener = new AuthenticationListener() {
            @Override
            public void loggedIn(SyncUser user) {
                counter[0]++;
            }

            @Override
            public void loggedOut(SyncUser user) {
                counter[1]++;
            }
        };

        SyncManager.addAuthenticationListener(authenticationListener);
        SyncManager.notifyUserLoggedIn(user);
        SyncManager.notifyUserLoggedOut(user);
        assertEquals(1, counter[0]);
        assertEquals(1, counter[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void authListener_null() {
        SyncManager.addAuthenticationListener(null);
    }

    @Test
    public void authListener_remove() {
        SyncUser user = createTestUser();
        final int[] counter = {0, 0};

        AuthenticationListener authenticationListener = new AuthenticationListener() {
            @Override
            public void loggedIn(SyncUser user) {
                counter[0]++;
            }

            @Override
            public void loggedOut(SyncUser user) {
                counter[1]++;
            }
        };

        SyncManager.addAuthenticationListener(authenticationListener);

        SyncManager.removeAuthenticationListener(authenticationListener);

        SyncManager.notifyUserLoggedIn(user);
        SyncManager.notifyUserLoggedOut(user);

        // no listener to update counters
        assertEquals(0, counter[0]);
        assertEquals(0, counter[1]);
    }

    @Test
    public void session() throws IOException {
        SyncUser user = createTestUser();
        String url = "realm://objectserver.realm.io/default";
        SyncConfiguration config = new SyncConfiguration.Builder(user, url)
                .build();
        // This will trigger the creation of the session
        Realm realm = Realm.getInstance(config);
        SyncSession session = SyncManager.getSession(config);
        assertEquals(user, session.getUser()); // see also SessionTests

        realm.close();
        SyncManager.reset();
    }
}
