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

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


// Tests for detailed change notification on RealmObject.
@RunWith(AndroidJUnit4.class)
public class ObjectChangeSetTests {

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    public static class PopulateOneAllTypes implements RunInLooperThread.RunnableBefore {

        @Override
        public void run(RealmConfiguration realmConfig) {
            Realm realm = Realm.getInstance(realmConfig);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    AllTypes allTypes = realm.createObject(AllTypes.class);
                    allTypes.setColumnRealmObject(realm.createObject(Dog.class));
                    allTypes.getColumnRealmList().add(realm.createObject(Dog.class));
                }
            });
            realm.close();
        }
    }

    private void checkDeleted(AllTypes allTypes) {
        allTypes.addChangeListener(new RealmObjectChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes object, ObjectChangeSet changeSet) {
                assertEquals(0, changeSet.getChangedFields().length);
                assertFalse(object.isValid());
                assertTrue(changeSet.isDeleted());
                looperThread.testComplete();
            }
        });
        looperThread.keepStrongReference.add(allTypes);
    }

    private void checkChangedField(AllTypes allTypes, final String... fieldNames) {
        assertNotNull(fieldNames);
        allTypes.addChangeListener(new RealmObjectChangeListener<RealmModel>() {
            @Override
            public void onChange(RealmModel object, ObjectChangeSet changeSet) {
                assertEquals(fieldNames.length, changeSet.getChangedFields().length);
                for (String name : fieldNames) {
                    for (String field : changeSet.getChangedFields()) {
                        if (field.equals(name)) {
                            break;
                        }
                        fail("Cannot find field " + name + " in field changes.");
                    }
                }
                looperThread.testComplete();
            }
        });
        looperThread.keepStrongReference.add(allTypes);
    }

    private void listenerShouldNotBeCalled(AllTypes allTypes) {
        allTypes.addChangeListener(new RealmObjectChangeListener<RealmModel>() {
            @Override
            public void onChange(RealmModel object, ObjectChangeSet changeSet) {
                fail();
            }
        });
        looperThread.postRunnableDelayed(new Runnable() {
            @Override
            public void run() {
                looperThread.testComplete();
            }
        }, 100);
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void objectDeleted() {
        Realm realm = looperThread.realm;
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkDeleted(allTypes);
        realm.beginTransaction();
        allTypes.deleteFromRealm();
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeIntField() {
        Realm realm = looperThread.realm;
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_LONG);
        realm.beginTransaction();
        allTypes.setColumnLong(42);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeStringField() {
        Realm realm = looperThread.realm;
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_STRING);
        realm.beginTransaction();
        allTypes.setColumnString("42");
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeFloatField() {
        Realm realm = looperThread.realm;
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_FLOAT);
        realm.beginTransaction();
        allTypes.setColumnFloat(42.0f);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeDoubleField() {
        Realm realm = looperThread.realm;
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_DOUBLE);
        realm.beginTransaction();
        allTypes.setColumnDouble(42.0d);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeBooleanField() {
        Realm realm = looperThread.realm;
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_BOOLEAN);
        realm.beginTransaction();
        allTypes.setColumnBoolean(true);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeDateField() {
        Realm realm = looperThread.realm;
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_DATE);
        realm.beginTransaction();
        allTypes.setColumnDate(new Date());
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeBinaryField() {
        Realm realm = looperThread.realm;
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_BINARY);
        realm.beginTransaction();
        allTypes.setColumnBinary(new byte[] { 42 });
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeLinkFieldSetNewObject() {
        Realm realm = looperThread.realm;
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_REALMOBJECT);
        realm.beginTransaction();
        allTypes.setColumnRealmObject(realm.createObject(Dog.class));
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeLinkFieldRemoveObject() {
        Realm realm = looperThread.realm;
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_REALMOBJECT);
        realm.beginTransaction();
        allTypes.getColumnRealmObject().deleteFromRealm();
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeLinkFieldOriginalObjectChanged_notTrigger() {
        Realm realm = looperThread.realm;
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        listenerShouldNotBeCalled(allTypes);
        realm.beginTransaction();
        allTypes.getColumnRealmObject().setAge(42);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeLinkListAddObject() {
        Realm realm = looperThread.realm;
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_REALMLIST);
        realm.beginTransaction();
        allTypes.getColumnRealmList().add(realm.createObject(Dog.class));
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeLinkListClear() {
        Realm realm = looperThread.realm;
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_REALMLIST);
        realm.beginTransaction();
        allTypes.getColumnRealmList().clear();
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void findFirstAsync_changeSetIsNullWhenQueryReturns() {
        Realm realm = looperThread.realm;
        AllTypes allTypes = realm.where(AllTypes.class).findFirstAsync();
        allTypes.addChangeListener(new RealmObjectChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes object, ObjectChangeSet changeSet) {
                assertTrue(object.isValid());
                assertNull(changeSet);
                looperThread.testComplete();
            }
        });
    }

    // Due to the fact that Object Store disallow adding notification block inside a transaction, the pending query
    // for findFirstAsync needs to be executed first then move the listener from collection to the object before begin
    // transaction.
    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void findFirstAsync_queryExecutedByLocalCommit() {
        Realm realm = looperThread.realm;
        final AtomicInteger listenerCounter = new AtomicInteger(0);
        final AllTypes allTypes = realm.where(AllTypes.class).findFirstAsync();
        allTypes.addChangeListener(new RealmObjectChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes object, ObjectChangeSet changeSet) {
                int counter = listenerCounter.getAndIncrement();
                switch (counter) {
                    case 0:
                        assertTrue(object.isValid());
                        assertNull(changeSet);
                        break;
                    case 1:
                        assertFalse(object.isValid());
                        assertTrue(changeSet.isDeleted());
                        assertEquals(0, changeSet.getChangedFields().length);
                        looperThread.testComplete();
                        break;
                    default:
                        fail();
                }
            }
        });
        realm.beginTransaction();
        allTypes.deleteFromRealm();
        realm.commitTransaction();
    }
}
