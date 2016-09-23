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

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import io.realm.android.SharedPrefsUserStore;
import io.realm.internal.network.AuthenticateResponse;
import io.realm.internal.network.AuthenticationServer;
import io.realm.internal.objectserver.Token;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.util.SyncTestUtils;

import static io.realm.util.SyncTestUtils.createTestUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class UserTests {

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Test
    public void toAndFromJson() {
        User user1 = createTestUser();
        User user2 = User.fromJson(user1.toJson());
        assertEquals(user1, user2);
    }

    // Tests that the UserStore does not return users that have expired
    @Test
    public void currentUser_returnsNullIfUserExpired() {
        // Add an expired user to the user store
        UserStore userStore = new SharedPrefsUserStore(InstrumentationRegistry.getContext());
        SyncManager.setUserStore(userStore);
        userStore.put(UserStore.CURRENT_USER_KEY, SyncTestUtils.createTestUser(Long.MIN_VALUE));

        // Invalid users should not be returned when asking the for the current user
        assertNull(User.currentUser());
    }

    // Tests that the user store returns the last user to login
    @Test
    public void currentUser_returnsUserAfterLogin() {
        AuthenticationServer authServer = Mockito.mock(AuthenticationServer.class);
        when(authServer.loginUser(any(Credentials.class), any(URL.class))).thenReturn(SyncTestUtils.createLoginResponse(Long.MAX_VALUE));

        User user = User.login(Credentials.facebook("foo"), "http://bar.com/auth");
        assertEquals(user, User.currentUser());
    }

    // Tests that if a user logs in, the refreshToken is refreshed before it expires.
    @RunTestInLooperThread
    @Test
    public void login_refreshWhenExpiring() {
        // Setup server responses
        // Expires in 30 seconds and Refresh starts 30 seconds before it expires. This should trigger a refresh
        // immediately.
        long expires = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);
        AuthenticationServer authServer = Mockito.mock(AuthenticationServer.class);
        when(authServer.loginUser(any(Credentials.class), any(URL.class))).thenReturn(SyncTestUtils.createLoginResponse(expires));
        when(authServer.refreshUser(any(Token.class), any(URL.class))).then(new Answer<AuthenticateResponse>() {
            @Override
            public AuthenticateResponse answer(InvocationOnMock invocation) throws Throwable {
                looperThread.testComplete();
                return SyncTestUtils.createRefreshResponse();
            }
        });

        // Login (which will trigger a refreshUser)
        SyncManager.setAuthServerImpl(authServer);
        User.login(Credentials.facebook("foo"), "http://bar.com/auth");
    }

    // Tests that if a user is loaded from storage, it will still be refreshed when expiring.
    @RunTestInLooperThread
    @Test
    public void currentUser_refreshWhenExpiring() {
        // Setup
        // Expires in 30 seconds and Refresh starts 30 seconds before it expires. This should trigger a refresh
        // immediately.
        long expires = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);
        AuthenticationServer authServer = Mockito.mock(AuthenticationServer.class);
        when(authServer.refreshUser(any(Token.class), any(URL.class))).then(new Answer<AuthenticateResponse>() {
            @Override
            public AuthenticateResponse answer(InvocationOnMock invocation) throws Throwable {
                looperThread.testComplete();
                return SyncTestUtils.createRefreshResponse();
            }
        });

        SyncManager.setUserStore(new SharedPrefsUserStore(InstrumentationRegistry.getContext()));
        SyncManager.setAuthServerImpl(authServer);
        User testUser = SyncTestUtils.createTestUser(expires);
        SyncManager.getUserStore().put(UserStore.CURRENT_USER_KEY, testUser);

        // Load user from storage. This should also trigger a refresh when the user expires
        User user = User.currentUser();
        assertEquals(testUser, user);
    }
}
