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
 */
package io.realm;

import android.test.AndroidTestCase;

import java.util.Date;

import io.realm.dynamic.DynamicRealmList;
import io.realm.dynamic.DynamicRealmObject;
import io.realm.entities.AllJavaTypes;
import io.realm.entities.Dog;

public class DynamicRealmListTest extends AndroidTestCase {

    private Realm realm;
    private DynamicRealmObject dynamicObject;
    private DynamicRealmList dynamicList;

    @Override
    protected void setUp() throws Exception {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getContext()).schema(AllJavaTypes.class, Dog.class).build();
        Realm.deleteRealm(realmConfig);
        realm = Realm.getInstance(realmConfig);
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
        obj.setFieldString("str");
        obj.setFieldShort((short) 1);
        obj.setFieldInt(1);
        obj.setFieldLong(1);
        obj.setFieldFloat(1.23f);
        obj.setFieldDouble(1.234d);
        obj.setFieldBinary(new byte[]{1, 2, 3});
        obj.setFieldBoolean(true);
        obj.setFieldDate(new Date(1000));
        obj.setFieldObject(obj);
        obj.getFieldList().add(obj);
        dynamicObject = new DynamicRealmObject(realm, obj.row);
        dynamicList = dynamicObject.getRealmList(AllJavaTypes.FIELD_LIST);
        realm.commitTransaction();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        realm.close();
    }

    public void testAddNullObjectThrows() {
        realm.beginTransaction();
        try {
            dynamicList.add(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    public void testAddWrongTableClassThrows() {
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        try {
            dynamicList.add(new DynamicRealmObject(realm, dog.row));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    public void testAddFromWrongRealmThrows() {
        RealmConfiguration otherConfig = new RealmConfiguration.Builder(getContext()).name("realm2").build();
        Realm.deleteRealm(otherConfig);
        Realm realm2 = Realm.getInstance(otherConfig);
        realm2.beginTransaction();
        AllJavaTypes realm2Object = realm2.createObject(AllJavaTypes.class);
        realm2.commitTransaction();

        realm.beginTransaction();
        try {
            dynamicList.add(new DynamicRealmObject(realm2, realm2Object.row));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
            realm2.close();
        }
    }

    public void testAddObject() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
        dynamicList.add(new DynamicRealmObject(realm, obj.row));
        realm.commitTransaction();

        assertEquals(2, dynamicList.size());
    }

    public void testClear() {
        realm.beginTransaction();
        dynamicList.clear();
        realm.commitTransaction();

        assertEquals(0, dynamicList.size());
    }

    public void testGetIllegalIndexThrows() {
        int[] indexes = new int[] { -1, 1 };
        for (int i : indexes) {
            try {
                dynamicList.get(i);
                fail("Could retrieve from index" + i);
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
    }

    public void testGet() {
        DynamicRealmObject listObject = dynamicList.get(0);
        assertEquals(dynamicObject, listObject);
    }

    public void testSetIllegalLocationThrows() {
        int[] indexes = new int[] { -1, 1 };
        for (int i : indexes) {
            try {
                dynamicList.set(i, dynamicObject);
                fail("Could set index out of bounds " + i);
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
    }

    public void testSetNullThrows() {
        realm.beginTransaction();
        try {
            dynamicList.set(0, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    public void testSetWrongTableClassThrows() {
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        try {
            dynamicList.set(0, new DynamicRealmObject(realm, dog.row));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    public void testSetWrongRealmThrows() {
        RealmConfiguration otherConfig = new RealmConfiguration.Builder(getContext()).name("realm2").build();
        Realm.deleteRealm(otherConfig);
        Realm realm2 = Realm.getInstance(otherConfig);
        realm2.beginTransaction();
        AllJavaTypes realm2Object = realm2.createObject(AllJavaTypes.class);
        realm2.commitTransaction();

        realm.beginTransaction();
        try {
            dynamicList.set(0, new DynamicRealmObject(realm2, realm2Object.row));
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
            realm2.close();
        }
    }

    public void testSet() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
        obj.setFieldLong(2);
        dynamicList.set(0, new DynamicRealmObject(realm, obj.row));
        realm.commitTransaction();

        assertEquals(1, dynamicList.size());
        assertEquals(new DynamicRealmObject(realm, obj.row), dynamicList.get(0));
    }

    public void testSize() {
        assertEquals(1, dynamicList.size());
    }
}
