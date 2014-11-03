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
import io.realm.entities.Dog;
import io.realm.internal.Row;


public class RealmObjectTest extends AndroidTestCase {

    protected Realm testRealm;

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());
    }


    // test io.realm.RealmObject Api

    // Row realmGetRow()
    public void testRealmGetRowReturnsValidRow() {

        testRealm.beginTransaction();
        RealmObject realmObject = testRealm.createObject(AllTypes.class);

        Row row = realmObject.row;

        testRealm.commitTransaction();
        assertNotNull("RealmObject.realmGetRow returns zero ", row);
        assertEquals("RealmObject.realmGetRow seems to return wrong row type: ", 8, row.getColumnCount());
    }

    public void testStringEncoding() {
        String[] strings = {"ABCD", "ÆØÅ", "Ö∫Ë", "ΠΑΟΚ", "Здравей"};

        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);

        for (String str : strings) {
            AllTypes obj1 = testRealm.createObject(AllTypes.class);
            obj1.setColumnString(str);
        }
        testRealm.commitTransaction();

        RealmResults<AllTypes> objects = testRealm.allObjects(AllTypes.class);
        assertEquals(strings.length, objects.size());
        int i = 0;
        for (AllTypes obj : objects) {
            String s = obj.getColumnString();
            assertEquals(strings[i], s);
            i++;
        }
    }

    public void testRemoveFromRealm() {
        Realm realm = Realm.getInstance(getContext());
        realm.beginTransaction();
        Dog rex = realm.createObject(Dog.class);
        rex.setName("Rex");
        Dog fido = realm.createObject(Dog.class);
        fido.setName("Fido");
        realm.commitTransaction();

        RealmResults<Dog> allDogsBefore = realm.where(Dog.class).equalTo("name", "Rex").findAll();
        assertEquals(1, allDogsBefore.size());

        realm.beginTransaction();
        rex.removeFromRealm();
        realm.commitTransaction();

        RealmResults<Dog> allDogsAfter = realm.where(Dog.class).equalTo("name", "Rex").findAll();
        assertEquals(0  , allDogsAfter.size());

        fido.getName();
        try {
            rex.getName();
            fail();
        } catch (IllegalStateException ignored) {}
    }
}
