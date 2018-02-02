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

import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.entities.AllTypes;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.sync.permissions.ClassPermissions;
import io.realm.sync.permissions.RealmPermissions;
import io.realm.sync.permissions.RealmPrivileges;

import static io.realm.util.SyncTestUtils.createTestUser;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ObjectLevelPermissionsTest {

    private static String REALM_URI = "realm://objectserver.realm.io/~/default";

    private SyncConfiguration configuration;
    private SyncUser user;

    @Rule
    public final TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();
    private Realm realm;

    @Before
    public void setUp() {
        user = createTestUser();
        configuration = new SyncConfiguration.Builder(user, REALM_URI)
                .partialRealm()
                .build();
        realm = Realm.getInstance(configuration);
    }

    @After
    public void tearDown() {
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }

    @Test
    public void getPrivileges_realm_localDefaults() {
        RealmPrivileges privs = realm.getPrivileges();
        // FIXME Not sure about the semantics here
        assertTrue(privs.canCreate());
        assertTrue(privs.canRead());
        assertTrue(privs.canUpdate());
        assertTrue(privs.canDelete());
        assertTrue(privs.canQuery());
        assertTrue(privs.canSetPermissions());
        assertTrue(privs.canModifySchema());
    }

    @Test
    public void getPrivileges_class_localDefaults() {
        // FIXME Not sure about the semantics here
    }

    @Test
    public void getPrivileges_object_localDefaults() {
        // FIXME Not sure about the semantics here
    }
}
