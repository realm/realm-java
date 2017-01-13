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
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.UUID;

import io.realm.internal.network.AuthenticateResponse;
import io.realm.internal.network.AuthenticationServer;
import io.realm.rule.RunInLooperThread;
import io.realm.util.SyncTestUtils;

import static io.realm.util.SyncTestUtils.createTestUser;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SyncUserTests {

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @BeforeClass
    public static void initUserStore() {
        Realm.init(InstrumentationRegistry.getInstrumentation().getContext());
        UserStore userStore = new RealmFileUserStore(InstrumentationRegistry.getTargetContext().getFilesDir().getPath());
        SyncManager.setUserStore(userStore);
    }

    @After
    public void tearDown() {
        RealmFileUserStore.nativeResetForTesting();
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
        return SyncTestUtils.createLoginResponse(userTokenValue, identity, Long.MAX_VALUE);
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
        Collection<SyncUser> users = SyncUser.all();
        assertTrue(users.isEmpty());
    }

    // `all()` returns only valid users. Invalid users are filtered.
    @Test
    public void all_validUsers() {
        // Add 1 expired user and 1 valid user to the user store
        UserStore userStore = SyncManager.getUserStore();
        userStore.put(SyncTestUtils.createTestUser(Long.MIN_VALUE));
        userStore.put(SyncTestUtils.createTestUser(Long.MAX_VALUE));

        Collection<SyncUser> users = SyncUser.all();
        assertEquals(1, users.size());
        assertTrue(users.iterator().next().isValid());
    }

    // Tests that the user store returns the last user to login
    /* FIXME: This test fails because of wrong JSON string.
    @Test
    public void currentUser_returnsUserAfterLogin() {
        AuthenticationServer authServer = Mockito.mock(AuthenticationServer.class);
        when(authServer.loginUser(any(Credentials.class), any(URL.class))).thenReturn(SyncTestUtils.createLoginResponse(Long.MAX_VALUE));

        User user = User.login(Credentials.facebook("foo"), "http://bar.com/auth");
        assertEquals(user, User.currentUser());
    }
    */

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

}
