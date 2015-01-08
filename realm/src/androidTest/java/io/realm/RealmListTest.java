/*
 * Copyright 2014 Realm Inc.
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

import android.test.AndroidTestCase;

import io.realm.entities.AllTypes;
import io.realm.exceptions.RealmException;

public class RealmListTest extends AndroidTestCase{

    protected Realm testRealm;

    protected void setUp() {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        testRealm.close();
    }

    public void testPublicNoArgConstructor() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        assertNotNull(list);
    }

    public void testUnavailableMethodsInNonManagedMode() {
        RealmList<AllTypes> list = new RealmList<AllTypes>();
        try {
            list.move(0, 1);
            fail("move() should fail in non-managed mode.");
        } catch (RealmException ignore) {
        }
        try {
            list.where();
            fail("where() should fail in non-managed mode.");
        } catch (RealmException ignore) {
        }
    }
}
