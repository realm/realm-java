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

package io.realm.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.KeyStoreException;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.SyncUser;
import io.realm.UserStore;
import io.realm.rule.TestRealmConfigurationFactory;

import static io.realm.util.SyncTestUtils.createTestUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class UserStoreTest {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private Realm realm;

    @Before
    public void setUp() {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    @Ignore("See https://github.com/realm/realm-java/issues/3555")
    @Test
    public void encrypt_decrypt_UsingAndroidKeyStoreUserStore() throws KeyStoreException {
        SyncUser user = createTestUser();
        UserStore userStore = new SecureUserStore(InstrumentationRegistry.getTargetContext());
        SyncUser savedUser = userStore.put("crypted_entry", user);
        assertNull(savedUser);
        SyncUser decrypted_entry = userStore.get("crypted_entry");
        assertEquals(user, decrypted_entry);
     }
}
