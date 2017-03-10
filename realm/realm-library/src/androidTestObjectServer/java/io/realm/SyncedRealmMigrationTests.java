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

package io.realm;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;

import io.realm.rule.TestRealmConfigurationFactory;
import io.realm.rule.TestSyncConfigurationFactory;
import io.realm.util.SyncTestUtils;

import static org.junit.Assert.fail;

/**
 * Testing methods around migrations for Realms using a {@link SyncConfiguration}.
 */
@RunWith(AndroidJUnit4.class)
public class SyncedRealmMigrationTests {

    @Rule
    public final TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Test
    public void migrateRealm_syncConfigurationThrows() {
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(SyncTestUtils.createTestUser(), "http://foo.com/auth").build();
        try {
            Realm.migrateRealm(config);
            fail();
        } catch (FileNotFoundException e) {
            fail(e.toString());
        } catch (IllegalArgumentException ignored) {
        }
    }

}
