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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import io.realm.android.SharedPrefsUserStore;
import io.realm.rule.RunInLooperThread;
import io.realm.util.SyncTestUtils;

import static io.realm.util.SyncTestUtils.createTestUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class UserTests {

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Before
    public void setUp() {
        Realm.init(InstrumentationRegistry.getTargetContext());
        SyncManager.getUserStore().clear();
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
        UserStore userStore = new SharedPrefsUserStore(InstrumentationRegistry.getContext());
        SyncManager.setUserStore(userStore);
        userStore.put(UserStore.CURRENT_USER_KEY, SyncTestUtils.createTestUser(Long.MIN_VALUE));

        // Invalid users should not be returned when asking the for the current user
        assertNull(SyncUser.currentUser());
    }

    // Test that current user is cleared if it is logged out
    @Test
    public void currentUser_clearedOnLogout() {
        // Add an expired user to the user store
        SyncUser user = SyncTestUtils.createTestUser(Long.MAX_VALUE);
        UserStore userStore = new SharedPrefsUserStore(InstrumentationRegistry.getContext());
        SyncManager.setUserStore(userStore);
        userStore.put(UserStore.CURRENT_USER_KEY, user);

        SyncUser savedUser = SyncUser.currentUser();
        assertEquals(user, savedUser);
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
        UserStore userStore = new SharedPrefsUserStore(InstrumentationRegistry.getContext());
        SyncManager.setUserStore(userStore);
        userStore.put(UserStore.CURRENT_USER_KEY, SyncTestUtils.createTestUser(Long.MIN_VALUE));
        userStore.put(UserStore.CURRENT_USER_KEY, SyncTestUtils.createTestUser(Long.MAX_VALUE));

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
}
