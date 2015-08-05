/*
 * Copyright 2015 Realm Inc.
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
 *
 */

package io.realm;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import io.realm.entities.AllJavaTypes;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@RunWith(AndroidJUnit4.class)
public class DynamicRealmTest {

    private Realm realm;

    @Before
    public void setUp() {
        RealmConfiguration config = new RealmConfiguration.Builder(InstrumentationRegistry.getTargetContext()).build();
        Realm.deleteRealm(config);
        realm = Realm.getInstance(config);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    // Test that the SharedGroupManager is reused across Realm/DynamicRealm on the same thread
    @Test
    public void testSharedGroupManageReused() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(AllJavaTypes.class);
            }
        });

        assertThat(realm.allObjects(AllJavaTypes.class).size(), is(1));
    }
}
