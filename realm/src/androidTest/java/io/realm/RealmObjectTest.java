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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.realm.entities.AllTypes;
import io.realm.entities.CyclicType;
import io.realm.entities.Dog;
import io.realm.internal.Row;


public class RealmObjectTest extends AndroidTestCase {

    protected Realm testRealm;

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());
    }

    // test io.testRealm.RealmObject Api

    // Row realmGetRow()
    public void testRealmGetRowReturnsValidRow() {

        testRealm.beginTransaction();
        RealmObject realmObject = testRealm.createObject(AllTypes.class);

        Row row = realmObject.row;

        testRealm.commitTransaction();
        assertNotNull("RealmObject.realmGetRow returns zero ", row);
        assertEquals(8, row.getColumnCount());
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

        // deleting rex twice should fail
        realm.beginTransaction();
        try {
            rex.removeFromRealm();      
            fail();
        } catch (IllegalStateException ignored) {}
        realm.commitTransaction();
    }

    public boolean methodWrongThread(final boolean callGetter) throws ExecutionException, InterruptedException {
        Realm realm = Realm.getInstance(getContext());
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
        final AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    if (callGetter) {
                        allTypes.getColumnFloat();
                    } else {
                        allTypes.setColumnFloat(1.0f);
                    }
                    return false;
                } catch (IllegalStateException ignored) {
                    return true;
                }
            }
        });

        return future.get();
    }

    public void testGetSetWrongThread() throws ExecutionException, InterruptedException {
        assertTrue(methodWrongThread(true));
        assertTrue(methodWrongThread(false));
    }

    public void testEquals() {
        testRealm.beginTransaction();
        CyclicType ct = testRealm.createObject(CyclicType.class);
        ct.setName("Foo");
        testRealm.commitTransaction();

        CyclicType ct1 = testRealm.where(CyclicType.class).findFirst();
        CyclicType ct2 = testRealm.where(CyclicType.class).findFirst();

        assertTrue(ct1.equals(ct1));
        assertTrue(ct2.equals(ct2));
    }

    public void testEquals_afterModification() {
        testRealm.beginTransaction();
        CyclicType ct = testRealm.createObject(CyclicType.class);
        ct.setName("Foo");
        testRealm.commitTransaction();

        CyclicType ct1 = testRealm.where(CyclicType.class).findFirst();
        CyclicType ct2 = testRealm.where(CyclicType.class).findFirst();

        testRealm.beginTransaction();
        ct1.setName("Baz");
        testRealm.commitTransaction();

        assertTrue(ct1.equals(ct1));
        assertTrue(ct2.equals(ct2));
    }

    public void testEquals_standAlone() {
        testRealm.beginTransaction();
        CyclicType ct1 = testRealm.createObject(CyclicType.class);
        ct1.setName("Foo");
        testRealm.commitTransaction();

        CyclicType ct2 = new CyclicType();
        ct2.setName("Bar");

        assertFalse(ct1.equals(ct2));
        assertFalse(ct2.equals(ct1));
    }

    public void testCyclicEquals() {
        Realm realm = Realm.getInstance(getContext());
        realm.beginTransaction();
        realm.clear(CyclicType.class);
        CyclicType foo = realm.createObject(CyclicType.class);
        foo.setName("Foo");
        CyclicType bar = realm.createObject(CyclicType.class);
        bar.setName("Bar");

        // Setup cycle on normal object references
        foo.setObject(bar);
        bar.setObject(foo);

        realm.commitTransaction();

        assertEquals(foo, realm.where(CyclicType.class).equalTo("name", "Foo").findFirst());
    }

    public void testCyclicToString() {
        testRealm.beginTransaction();
        testRealm.clear(CyclicType.class);
        CyclicType foo = testRealm.createObject(CyclicType.class);
        foo.setName("Foo");
        CyclicType bar = testRealm.createObject(CyclicType.class);
        bar.setName("Bar");

        // Setup cycle on normal object references
        foo.setObject(bar);
        bar.setObject(foo);

        testRealm.commitTransaction();

        assertEquals("", foo.toString());
    }

    public void testCyclicHashCode() {
        testRealm.beginTransaction();
        testRealm.clear(CyclicType.class);
        CyclicType foo = testRealm.createObject(CyclicType.class);
        foo.setName("Foo");
        CyclicType bar = testRealm.createObject(CyclicType.class);
        bar.setName("Bar");

        // Setup cycle on normal object references
        foo.setObject(bar);
        bar.setObject(foo);

        testRealm.commitTransaction();

        assertEquals(0, foo.hashCode());
    }
}
