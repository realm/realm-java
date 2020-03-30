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

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import io.realm.entities.StringOnlyModule;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.TestRealmConfigurationFactory;

import static io.realm.SyncTestUtils.*;
import static org.junit.Assert.*;

@Ignore("FIXME: RealmApp refactor")
@RunWith(AndroidJUnit4.class)
public class SyncManagerTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        // SyncManager.reset();
    }

    @After
    public void tearDown() {
        UserFactory.logoutAllUsers();
        // SyncManager.reset();
        BaseRealm.applicationContext = null; // Required for Realm.init() to work
        Realm.init(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @Test
    public void authListener() {
        RealmUser user = createTestUser();
        final int[] counter = {0, 0};

        AuthenticationListener authenticationListener = new AuthenticationListener() {
            @Override
            public void loggedIn(RealmUser user) {
                counter[0]++;
            }

            @Override
            public void loggedOut(RealmUser user) {
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
        RealmUser user = createTestUser();
        final int[] counter = {0, 0};

        AuthenticationListener authenticationListener = new AuthenticationListener() {
            @Override
            public void loggedIn(RealmUser user) {
                counter[0]++;
            }

            @Override
            public void loggedOut(RealmUser user) {
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
        BaseRealm.applicationContext = null;
        Realm.init(InstrumentationRegistry.getInstrumentation().getTargetContext());
        RealmUser user = createTestUser();
        String url = "realm://objectserver.realm.io/default";
        SyncConfiguration config = user.createSyncConfiguration()
                .modules(new StringOnlyModule())
                .build();
        // This will trigger the creation of the session
        Realm realm = Realm.getInstance(config);
        SyncSession session = SyncManager.getSession(config);
        assertEquals(user, session.getUser()); // see also SessionTests

        realm.close();
    }
}
