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

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import io.realm.objectserver.utils.StringOnlyModule;
import io.realm.objectserver.utils.UserFactory;
import io.realm.rule.TestRealmConfigurationFactory;

import static io.realm.SyncTestUtils.createTestUser;
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
                return Collections.emptySet();
            }

            @Override
            public boolean isActive(String identity, String authenticationUrl) {
                return true;
            }
        };
        SyncManager.reset();
    }

    @After
    public void tearDown() {
        UserFactory.logoutAllUsers();
        UserStore userStore = SyncManager.getUserStore();
        for (SyncUser syncUser : userStore.allUsers()) {
            userStore.remove(syncUser.getIdentity(), syncUser.getAuthenticationUrl().toString());
        }
        SyncManager.reset();
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
        BaseRealm.applicationContext = null;
        Realm.init(InstrumentationRegistry.getTargetContext());
        SyncUser user = createTestUser();
        String url = "realm://objectserver.realm.io/default";
        SyncConfiguration config = user.createConfiguration(url)
                .modules(new StringOnlyModule())
                .build();
        // This will trigger the creation of the session
        Realm realm = Realm.getInstance(config);
        SyncSession session = SyncManager.getSession(config);
        assertEquals(user, session.getUser()); // see also SessionTests

        realm.close();
    }

    private void tryCase(Runnable runnable) {
        try {
            runnable.run();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void setAuthorizationHeaderName_illegalArgumentsThrows() {
        //noinspection ConstantConditions
        tryCase(() -> SyncManager.setAuthorizationHeaderName(null));
        tryCase(() -> SyncManager.setAuthorizationHeaderName(""));
        //noinspection ConstantConditions
        tryCase(() -> SyncManager.setAuthorizationHeaderName(null, "myhost"));
        tryCase(() -> SyncManager.setAuthorizationHeaderName("", "myhost"));
        //noinspection ConstantConditions
        tryCase(() -> SyncManager.setAuthorizationHeaderName("myheader", null));
        tryCase(() -> SyncManager.setAuthorizationHeaderName("myheader", ""));
    }

    @Test
    public void setAuthorizationHeaderName() throws URISyntaxException {
        SyncManager.setAuthorizationHeaderName("foo");
        assertEquals("foo", SyncManager.getAuthorizationHeaderName(new URI("http://localhost")));
    }

    @Test
    public void setAuthorizationHeaderName_hostOverrideGlobal() throws URISyntaxException {
        SyncManager.setAuthorizationHeaderName("foo");
        SyncManager.setAuthorizationHeaderName("bar", "localhost");
        assertEquals("bar", SyncManager.getAuthorizationHeaderName(new URI("http://localhost")));
    }

    @Test
    public void getAuthorizationHeaderName_ignoreHostCasing() throws URISyntaxException {
        SyncManager.setAuthorizationHeaderName("foo", "lOcAlHoSt");
        assertEquals("foo", SyncManager.getAuthorizationHeaderName(new URI("http://localhost")));
        assertEquals("foo", SyncManager.getAuthorizationHeaderName(new URI("http://LOCALHOST")));
    }

    @Test
    public void addCustomRequestHeader_illegalArgumentThrows() {
        //noinspection ConstantConditions
        tryCase(() -> SyncManager.addCustomRequestHeader(null, "val"));
        tryCase(() -> SyncManager.addCustomRequestHeader("", "val"));
        //noinspection ConstantConditions
        tryCase(() -> SyncManager.addCustomRequestHeader("header", null));

        //noinspection ConstantConditions
        tryCase(() -> SyncManager.addCustomRequestHeader(null, "val", "localhost"));
        tryCase(() -> SyncManager.addCustomRequestHeader("", "val", "localhost"));
        //noinspection ConstantConditions
        tryCase(() -> SyncManager.addCustomRequestHeader("header", "value", null));
        tryCase(() -> SyncManager.addCustomRequestHeader("header", "value", ""));
    }

    @Test
    public void addCustomRequestHeaders_illegalArgumentThrows() {
        tryCase(() -> SyncManager.addCustomRequestHeaders(null));
        tryCase(() -> SyncManager.addCustomRequestHeaders(Collections.emptyMap(), null));
        tryCase(() -> SyncManager.addCustomRequestHeaders(Collections.emptyMap(), ""));
    }

    @Test
    public void addCustomRequestHeader() throws URISyntaxException {
        SyncManager.addCustomRequestHeader("header1", "val1");
        SyncManager.addCustomRequestHeader("header2", "val2");
        Map<String, String> headers = SyncManager.getCustomRequestHeaders(new URI("http://localhost"));
        assertEquals(2, headers.size());
        Map.Entry<String, String> header = headers.entrySet().iterator().next();
        assertEquals("header1", header.getKey());
        assertEquals("val1", header.getValue());
    }

    @Test
    public void addCustomRequestHeader_hostOverrideGlobal() throws URISyntaxException {
        SyncManager.addCustomRequestHeader("header1", "val1");
        SyncManager.addCustomRequestHeader("header1", "val2", "localhost");
        Map<String, String> headers = SyncManager.getCustomRequestHeaders(new URI("http://localhost"));
        assertEquals(1, headers.size());
        Map.Entry<String, String> header = headers.entrySet().iterator().next();
        assertEquals("header1", header.getKey());
        assertEquals("val2", header.getValue());
    }

    @Test
    public void addCustomRequestHeader_ignoreCasingForHost() throws URISyntaxException {
        SyncManager.addCustomRequestHeader("header1", "val1", "lOcAlHoSt");
        SyncManager.addCustomRequestHeader("header2", "val2", "LOCALHOST");
        Map<String, String> headers = SyncManager.getCustomRequestHeaders(new URI("http://localhost"));
        assertEquals(2, headers.size());
    }

    @Test
    public void addCustomHeaders() throws URISyntaxException {
        Map<String, String> inputHeaders = new LinkedHashMap<>();
        inputHeaders.put("header1", "value1");
        inputHeaders.put("header2", "value2");
        SyncManager.addCustomRequestHeaders(inputHeaders);
        Map<String, String> outputHeaders = SyncManager.getCustomRequestHeaders(new URI("http://localhost"));
        assertEquals(2, outputHeaders.size());
        Iterator<Map.Entry<String, String>> it = outputHeaders.entrySet().iterator();
        Map.Entry<String, String> header1 = it.next();
        assertEquals("header1", header1.getKey());
        assertEquals("value1", header1.getValue());
        Map.Entry<String, String> header2 = it.next();
        assertEquals("header2", header2.getKey());
        assertEquals("value2", header2.getValue());
    }

    @Test
    public void addCustomHeaders_hostOverrideGlobal() throws URISyntaxException {
        Map<String, String> inputHeaders = new LinkedHashMap<>();
        inputHeaders.put("header1", "val1");
        SyncManager.addCustomRequestHeaders(inputHeaders);
        inputHeaders.put("header1", "val2");
        SyncManager.addCustomRequestHeaders(inputHeaders, "localhost");
        Map<String, String> outputHeaders = SyncManager.getCustomRequestHeaders(new URI("http://localhost"));
        assertEquals(1, outputHeaders.size());
        Map.Entry<String, String> header = outputHeaders.entrySet().iterator().next();
        assertEquals("header1", header.getKey());
        assertEquals("val2", header.getValue());
    }

    @Test
    public void addCustomHeader_combinesSingleAndMultiple() throws URISyntaxException {
        Map<String, String> inputHeaders1 = new LinkedHashMap<>();
        inputHeaders1.put("header1", "val1");
        Map<String, String> inputHeaders2 = new LinkedHashMap<>();
        inputHeaders2.put("header2", "val2");

        SyncManager.addCustomRequestHeader("header3", "val3");
        SyncManager.addCustomRequestHeaders(inputHeaders1);
        SyncManager.addCustomRequestHeader("header4", "val4", "realm.io");
        SyncManager.addCustomRequestHeaders(inputHeaders2, "realm.io");

        Map<String, String> localhostHeaders = SyncManager.getCustomRequestHeaders(new URI("http://localhost"));
        assertEquals(2, localhostHeaders.size());
        Iterator<Map.Entry<String, String>> it = localhostHeaders.entrySet().iterator();
        Map.Entry<String, String> item = it.next();
        assertEquals("header3", item.getKey());
        assertEquals("val3", item.getValue());
        item = it.next();
        assertEquals("header1", item.getKey());
        assertEquals("val1", item.getValue());

        Map<String, String> realmioHeaders = SyncManager.getCustomRequestHeaders(new URI("http://realm.io"));
        it = realmioHeaders.entrySet().iterator();
        assertEquals(4, realmioHeaders.size());
        item = it.next();
        assertEquals("header3", item.getKey());
        assertEquals("val3", item.getValue());
        item = it.next();
        assertEquals("header1", item.getKey());
        assertEquals("val1", item.getValue());
        item = it.next();
        assertEquals("header4", item.getKey());
        assertEquals("val4", item.getValue());
        item = it.next();
        assertEquals("header2", item.getKey());
        assertEquals("val2", item.getValue());
    }
}
