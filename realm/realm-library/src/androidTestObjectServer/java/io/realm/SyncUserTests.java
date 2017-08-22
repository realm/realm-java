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
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.realm.entities.StringOnly;
import io.realm.internal.network.AuthenticateResponse;
import io.realm.internal.network.AuthenticationServer;
import io.realm.internal.objectserver.Token;
import io.realm.log.RealmLog;
import io.realm.objectserver.utils.StringOnlyModule;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.util.SyncTestUtils;

import static io.realm.util.SyncTestUtils.createTestAdminUser;
import static io.realm.util.SyncTestUtils.createTestUser;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SyncUserTests {

    private static final URL authUrl;
    private static final Constructor<SyncUser> SYNC_USER_CONSTRUCTOR;
    static {
        try {
            authUrl = new URL("http://localhost/auth");
            SYNC_USER_CONSTRUCTOR = SyncUser.class.getDeclaredConstructor(Token.class, URL.class);
            SYNC_USER_CONSTRUCTOR.setAccessible(true);
        } catch (MalformedURLException e) {
            throw new ExceptionInInitializerError(e);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    @BeforeClass
    public static void initUserStore() {
        Realm.init(InstrumentationRegistry.getInstrumentation().getContext());
        UserStore userStore = new RealmFileUserStore();
        SyncManager.setUserStore(userStore);
    }

    @Before
    public void setUp() {
        SyncManager.reset();
    }

    private static SyncUser createFakeUser(String id) {
        final Token token = new Token("token_value", id, "path_value", Long.MAX_VALUE, null);
        try {
            return SYNC_USER_CONSTRUCTOR.newInstance(token, authUrl);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            fail(e.getMessage());
        }
        return null;
    }

    @Test
    public void equals_validUser() {
        final SyncUser user1 = createFakeUser("id_value");
        final SyncUser user2 = createFakeUser("id_value");
        assertTrue(user1.equals(user2));
    }

    @Test
    public void equals_loggedOutUser() {
        final SyncUser user1 = createFakeUser("id_value");
        final SyncUser user2 = createFakeUser("id_value");
        user1.logout();
        user2.logout();
        assertTrue(user1.equals(user2));
    }

    @Test
    public void hashCode_validUser() {
        final SyncUser user = createFakeUser("id_value");
        assertNotEquals(0, user.hashCode());
    }

    @Test
    public void hashCode_loggedOutUser() {
        final SyncUser user = createFakeUser("id_value");
        user.logout();
        assertNotEquals(0, user.hashCode());
    }

    @Test
    public void toAndFromJson() {
        SyncUser user1 = createTestUser();
        SyncUser user2 = SyncUser.fromJson(user1.toJson());
        assertEquals(user1, user2);
    }

    // Tests that the UserStore does not return users that have expired
    @Test
    public void currentUser_returnsNullIfUserExpired() {
        // Add an expired user to the user store
        UserStore userStore = SyncManager.getUserStore();
        userStore.put(SyncTestUtils.createTestUser(Long.MIN_VALUE));

        // Invalid users should not be returned when asking the for the current user
        assertNull(SyncUser.currentUser());
    }

    @Test
    public void currentUser_throwsIfMultipleUsersLoggedIn() {
        AuthenticationServer originalAuthServer = SyncManager.getAuthServer();
        AuthenticationServer authServer = Mockito.mock(AuthenticationServer.class);
        SyncManager.setAuthServerImpl(authServer);

        try {
            // 1. Login two random users
            when(authServer.loginUser(any(SyncCredentials.class), any(URL.class))).thenAnswer(new Answer<AuthenticateResponse>() {
                @Override
                public AuthenticateResponse answer(InvocationOnMock invocationOnMock) throws Throwable {
                    return getNewRandomUser();
                }
            });
            SyncUser.login(SyncCredentials.facebook("foo"), "http:/test.realm.io/auth");
            SyncUser.login(SyncCredentials.facebook("foo"), "http:/test.realm.io/auth");

            // 2. Verify currentUser() now throws
            try {
                SyncUser.currentUser();
                fail();
            } catch (IllegalStateException ignore) {
            }
        } finally {
            SyncManager.setAuthServerImpl(originalAuthServer);
        }

    }

    private AuthenticateResponse getNewRandomUser() {
        String identity = UUID.randomUUID().toString();
        String userTokenValue = UUID.randomUUID().toString();
        return SyncTestUtils.createLoginResponse(userTokenValue, identity, Long.MAX_VALUE, false);
    }

    // Test that current user is cleared if it is logged out
    @Test
    public void currentUser_clearedOnLogout() {
        // Add 1 valid user to the user store
        SyncUser user = SyncTestUtils.createTestUser(Long.MAX_VALUE);
        UserStore userStore = SyncManager.getUserStore();
        userStore.put(user);

        SyncUser savedUser = SyncUser.currentUser();
        assertEquals(user, savedUser);
        assertNotNull(savedUser);
        savedUser.logout();
        assertNull(SyncUser.currentUser());
    }

    // `all()` returns an empty list if no users are logged in
    @Test
    public void all_empty() {
        Map<String, SyncUser> users = SyncUser.all();
        assertTrue(users.isEmpty());
    }

    // `all()` returns only valid users. Invalid users are filtered.
    @Test
    public void all_validUsers() {
        // Add 1 expired user and 1 valid user to the user store
        UserStore userStore = SyncManager.getUserStore();
        userStore.put(SyncTestUtils.createTestUser(Long.MIN_VALUE));
        userStore.put(SyncTestUtils.createTestUser(Long.MAX_VALUE));

        Map<String, SyncUser> users = SyncUser.all();
        assertEquals(1, users.size());
        assertTrue(users.entrySet().iterator().next().getValue().isValid());
    }

    @Test
    public void isAdmin() {
        SyncUser user1 = createTestUser();
        assertFalse(user1.isAdmin());

        SyncUser user2 = createTestAdminUser();
        assertTrue(user2.isAdmin());
    }

    @Test
    public void isAdmin_allUsers() {
        UserStore userStore = SyncManager.getUserStore();
        SyncUser user = SyncTestUtils.createTestAdminUser();
        assertTrue(user.isAdmin());
        userStore.put(user);

        Map <String, SyncUser> users = SyncUser.all();
        assertEquals(1, users.size());
        assertTrue(users.entrySet().iterator().next().getValue().isAdmin());
    }

    // Tests that the user store returns the last user to login
    @Ignore("This test fails because of wrong JSON string.")
    @Test
    public void currentUser_returnsUserAfterLogin() {
        AuthenticationServer authServer = Mockito.mock(AuthenticationServer.class);
        when(authServer.loginUser(any(SyncCredentials.class), any(URL.class))).thenReturn(SyncTestUtils.createLoginResponse(Long.MAX_VALUE));

        SyncUser user = SyncUser.login(SyncCredentials.facebook("foo"), "http://bar.com/auth");
        assertEquals(user, SyncUser.currentUser());
    }

    @Test
    public void getManagementRealm() {
        SyncUser user = SyncTestUtils.createTestUser();
        Realm managementRealm = user.getManagementRealm();
        assertNotNull(managementRealm);
        managementRealm.close();
    }

    @Test
    public void getManagementRealm_enforceTLS() throws URISyntaxException {
        // Non TLS
        SyncUser user = SyncTestUtils.createTestUser("http://objectserver.realm.io/auth");
        Realm managementRealm = user.getManagementRealm();
        SyncConfiguration config = (SyncConfiguration) managementRealm.getConfiguration();
        assertEquals(new URI("realm://objectserver.realm.io/" + user.getIdentity() + "/__management"), config.getServerUrl());
        managementRealm.close();

        // TLS
        user = SyncTestUtils.createTestUser("https://objectserver.realm.io/auth");
        managementRealm = user.getManagementRealm();
        config = (SyncConfiguration) managementRealm.getConfiguration();
        assertEquals(new URI("realms://objectserver.realm.io/" + user.getIdentity() + "/__management"), config.getServerUrl());
        managementRealm.close();
    }

    @Test
    public void toString_returnDescription() {
        SyncUser user = SyncTestUtils.createTestUser("http://objectserver.realm.io/auth");
        String str = user.toString();
        assertTrue(str != null && !str.isEmpty());
    }

    // Test that a login with an access token logs the user in directly without touching the network
    @Test
    public void login_withAccessToken() {
        AuthenticationServer authServer = Mockito.mock(AuthenticationServer.class);
        when(authServer.loginUser(any(SyncCredentials.class), any(URL.class))).thenThrow(new AssertionError("Server contacted."));
        AuthenticationServer originalServer = SyncManager.getAuthServer();
        SyncManager.setAuthServerImpl(authServer);
        try {
            SyncCredentials credentials = SyncCredentials.accessToken("foo", "bar");
            SyncUser user = SyncUser.login(credentials, "http://ros.realm.io/auth");
            assertTrue(user.isValid());
        } finally {
            SyncManager.setAuthServerImpl(originalServer);
        }
    }

    // Checks that `/auth` is correctly added to any URL without a path
    @Test
    public void login_appendAuthSegment() {
        AuthenticationServer authServer = Mockito.mock(AuthenticationServer.class);
        AuthenticationServer originalServer = SyncManager.getAuthServer();
        SyncManager.setAuthServerImpl(authServer);
        String[][] urls = {
                {"http://ros.realm.io", "http://ros.realm.io/auth"},
                {"http://ros.realm.io:8080", "http://ros.realm.io:8080/auth"},
                {"http://ros.realm.io/", "http://ros.realm.io/"},
                {"http://ros.realm.io/?foo=bar", "http://ros.realm.io/?foo=bar"},
                {"http://ros.realm.io/auth", "http://ros.realm.io/auth"},
                {"http://ros.realm.io/auth/", "http://ros.realm.io/auth/"},
                {"http://ros.realm.io/custom-path/", "http://ros.realm.io/custom-path/"}
        };

        try {
            for (String[] url : urls) {
                RealmLog.error(url[0]);
                String input = url[0];
                String normalizedInput = url[1];
                SyncCredentials credentials = SyncCredentials.accessToken("token", UUID.randomUUID().toString());
                SyncUser user = SyncUser.login(credentials, input);
                assertEquals(normalizedInput, user.getAuthenticationUrl().toString());
                user.logout();
            }
        } finally {
            SyncManager.setAuthServerImpl(originalServer);
        }
    }

    @Test
    public void changePassword_nullThrows() {
        SyncUser user = createTestUser();

        thrown.expect(IllegalArgumentException.class);
        //noinspection ConstantConditions
        user.changePassword(null);
    }

    @Test
    public void changePassword_admin_nullThrows() {
        SyncUser user = createTestUser();

        thrown.expect(IllegalArgumentException.class);
        //noinspection ConstantConditions
        user.changePassword(null, "new-password");
    }

    @Test
    public void changePasswordAsync_nonLooperThreadThrows() {
        SyncUser user = createTestUser();

        thrown.expect(IllegalStateException.class);
        user.changePasswordAsync("password", new SyncUser.Callback() {
            @Override
            public void onSuccess(SyncUser user) {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                fail();
            }
        });
    }

    @Test
    public void changePassword_admin_Async_nonLooperThreadThrows() {
        SyncUser user = createTestUser();

        thrown.expect(IllegalStateException.class);
        user.changePasswordAsync("user-id", "new", new SyncUser.Callback() {
            @Override
            public void onSuccess(SyncUser user) {
                fail();
            }

            @Override
            public void onError(ObjectServerError error) {
                fail();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void changePasswordAsync_nullCallbackThrows() {
        SyncUser user = createTestUser();

        thrown.expect(IllegalArgumentException.class);
        //noinspection ConstantConditions
        user.changePasswordAsync("new-password", null);
    }

    @Test
    @RunTestInLooperThread
    public void changePassword_admin_Async_nullCallbackThrows() {
        SyncUser user = createTestUser();

        thrown.expect(IllegalArgumentException.class);
        //noinspection ConstantConditions
        user.changePasswordAsync("user-id", "new-password", null);
    }

    @Test
    @RunTestInLooperThread
    public void changePassword_noneAdminThrows() {
        SyncUser user = createTestUser();

        thrown.expect(IllegalStateException.class);
        user.changePassword("user-id", "new-password");
    }

    @Test
    public void allSessions() {
        String url1 = "realm://objectserver.realm.io/default";
        String url2 = "realm://objectserver.realm.io/~/default";

        SyncUser user = createTestUser();
        assertEquals(0, user.allSessions().size());

        SyncConfiguration configuration1 = new SyncConfiguration.Builder(user, url1).build();
        Realm realm1 = Realm.getInstance(configuration1);
        List<SyncSession> allSessions = user.allSessions();
        assertEquals(1, allSessions.size());
        Iterator<SyncSession> iter = allSessions.iterator();
        SyncSession session = iter.next();
        assertEquals(user, session.getUser());
        assertEquals(url1, session.getServerUrl().toString());

        SyncConfiguration configuration2 = new SyncConfiguration.Builder(user, url2).build();
        Realm realm2 = Realm.getInstance(configuration2);
        allSessions = user.allSessions();
        assertEquals(2, allSessions.size());
        iter = allSessions.iterator();
        String individualUrl = url2.replace("~", user.getIdentity());
        int foundCount = 0;
        while (iter.hasNext()) {
            session = iter.next();
            assertEquals(user, session.getUser());
            if (individualUrl.equals(session.getServerUrl().toString())) {
                foundCount++;
            }
        }
        assertEquals(1, foundCount);
        realm1.close();

        allSessions = user.allSessions();
        assertEquals(1, allSessions.size());
        iter = allSessions.iterator();
        session = iter.next();
        assertEquals(user, session.getUser());
        assertEquals(individualUrl, session.getServerUrl().toString());

        realm2.close();
        assertEquals(0, user.allSessions().size());
    }

    // JSON format changed in 3.6.0 (removed unnecessary fields), this regression test
    // makes sure we can still deserialize a valid SyncUser from the old format.
    @Test
    public void fromJson_WorkWithRemovedObjectServerUser() {
        String oldSyncUserJSON = "{\"authUrl\":\"http:\\/\\/192.168.1.151:9080\\/auth\",\"userToken\":{\"token\":\"eyJpZGVudGl0eSI6IjY4OWQ5MGMxNDIyYTIwMmZkNTljNDYwM2M0ZTRmNmNjIiwiZXhwaXJlcyI6MTgxNjM1ODE4NCwiYXBwX2lkIjoiaW8ucmVhbG0ucmVhbG10YXNrcyIsImFjY2VzcyI6WyJyZWZyZXNoIl0sImlzX2FkbWluIjpmYWxzZSwic2FsdCI6MC4yMTEwMjQyNDgwOTEyMzg1NH0=:lEDa83o1zu8rkwdZVpTyunLHh1wmjxPPSGmZQNxdEM7xDmpbiU7V+8dgDWGevJNHMFluNDAOmrcAOI9TLfhI4rMDl70NI1K9rv\\/Aeq5uIOzq\\/Gf7JTeTUKY5Z7yRoppd8NArlNBKesLFxzdLRlfm1hflF9wH23xQXA19yUZ67JIlkhDPL5e3bau8O3Pr\\/St0unW3KzPOiZUk1l9KRrs2iMCCiXCfq4rf6rp7B2M7rBUMQm68GnB1Ot7l1CblxEWcREcbpyhBKTWIOFRGMwg2TW\\/zRR3cRNglx+ZC4FOeO0mfkX+nf+slyFODAnQkOzPZcGO8xc3I1emafX58Wl\\/Guw==\",\"token_data\":{\"identity\":\"689d90c1422a202fd59c4603c4e4f6cc\",\"path\":\"\",\"expires\":1816358184,\"access\":[\"unknown\"],\"is_admin\":false}},\"realms\":[]}";
        SyncUser syncUser = SyncUser.fromJson(oldSyncUserJSON);

        // Note: we can't call isValid() and expect it to be true
        //       since the user is not persisted in the UserStore
        //       isValid() requires SyncManager.getUserStore().isActive(identity)
        //       to return true as well.
        Token accessToken = syncUser.getAccessToken();
        assertNotNull(accessToken);
        // refresh token should expire in 10 years (July 23, 2027)
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(accessToken.expiresMs());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        assertEquals(23, day);
        assertEquals(Calendar.JULY, month);
        assertEquals(2027, year);

        assertEquals("http://192.168.1.151:9080/auth", syncUser.getAuthenticationUrl().toString());
    }

    @Test
    @Ignore("until https://github.com/realm/realm-java/issues/5097 is fixed")
    public void logoutUserShouldDeleteRealmAfterRestart() throws InterruptedException {
        SyncManager.reset();
        BaseRealm.applicationContext = null; // Required for Realm.init() to work
        Realm.init(InstrumentationRegistry.getTargetContext());

        SyncUser user = createTestUser();
        SyncConfiguration syncConfiguration = new SyncConfiguration
                .Builder(user, "realm://127.0.0.1:9080/~/tests")
                .modules(new StringOnlyModule())
                .build();

        Realm realm = Realm.getInstance(syncConfiguration);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(StringOnly.class).setChars("1");
            }
        });
        user.logout();
        realm.close();

        final File realmPath = new File (syncConfiguration.getPath());
        assertTrue(realmPath.exists());

        // simulate an app restart
        SyncManager.reset();
        BaseRealm.applicationContext = null;
        Realm.init(InstrumentationRegistry.getTargetContext());

        //now the file should be deleted
        assertFalse(realmPath.exists());
    }
}
