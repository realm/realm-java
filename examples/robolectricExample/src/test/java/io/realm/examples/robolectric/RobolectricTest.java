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

package io.realm.examples.robolectric;

import android.app.Activity;
import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmRobolectricRule;
import io.realm.examples.robolectric.entities.Person;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class RobolectricTest {

    private Realm realm;
    private Activity context;
    private RealmConfiguration config;

    private Context getContext() {
        return context;
    }

    @Rule
    public ExternalResource realmRobolectricRule = new RealmRobolectricRule();

    @Before
    public void setUp() throws Exception {
        context = Robolectric.setupActivity(MainActivity.class);
        config = new RealmConfiguration.Builder(getContext().getFilesDir()).name(Realm.DEFAULT_REALM_NAME).build();
        Realm.deleteRealm(config);
        realm = Realm.getInstance(config);
    }

    @After
    public void tearDown() throws Exception {
        if (realm != null) {
            realm.close();
        }
    }

    @Test
    public void testIsEmpty() {
        assertTrue(realm.isEmpty());
        Person person = new Person();
        person.setName("Brad Pitt");
        person.setAge(52);
        realm.beginTransaction();
        realm.copyToRealm(person);
        realm.commitTransaction();
        assertFalse(realm.isEmpty());
    }
}
