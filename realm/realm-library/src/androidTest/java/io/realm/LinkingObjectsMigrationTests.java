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
import org.junit.runner.RunWith;

import java.io.IOException;

import io.realm.entities.AllJavaTypesModule;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class LinkingObjectsMigrationTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private Realm realm;
    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getContext();

        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    /**
     * The asset file backlinks-migration.realm contains a realm that has three objects in it,
     * all of the type AllJavaTypes.  The version of AllJavaTypes used to create the objects,
     * though, has no LinkingObjects fields
     *
     * This test verifies that backlinks are not part of the persistent realm file and are,
     * thus, migration proof.
     */
    @Test
    public void linkingObjects_newFieldsDoNotRequireMigrationgit () {
        final String realmName = "backlinks-migration.realm";

        RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .name(realmName)
                .modules(new AllJavaTypesModule())
                .build();
        try {
            configFactory.copyRealmFromAssets(context, realmName, realmName);
            Realm localRealm = Realm.getInstance(realmConfig);
            localRealm.close();
        } catch (IOException e) {
            fail("Failed copying realm");
        } catch (RealmMigrationNeededException expected) {
            fail("No migration should have been required");
        } finally {
            Realm.deleteRealm(realmConfig);
        }
    }
}
