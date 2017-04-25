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

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.entities.Owner;
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
        looperThread.keepStrongReference(allTypes);
    }

    private void checkChangedField(AllTypes allTypes, final String... fieldNames) {
        assertNotNull(fieldNames);
        allTypes.addChangeListener(new RealmObjectChangeListener<RealmModel>() {
            @Override
            public void onChange(RealmModel object, ObjectChangeSet changeSet) {
                assertEquals(fieldNames.length, changeSet.getChangedFields().length);
                List<String> changedFields = Arrays.asList(changeSet.getChangedFields());
                for (String name : fieldNames) {
                    assertTrue(changeSet.isFieldChanged(name));
                    assertFalse(changeSet.isFieldChanged(name + "NotThere"));
                    if (!changedFields.contains(name)) {
                        fail("Cannot find field " + name + " in field changes.");
                    }
                }
                looperThread.testComplete();
            }
        });
        looperThread.keepStrongReference(allTypes);
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
        Realm realm = looperThread.getRealm();
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkDeleted(allTypes);
        realm.beginTransaction();
        allTypes.deleteFromRealm();
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeLongField() {
        Realm realm = looperThread.getRealm();
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_LONG);
        realm.beginTransaction();
        allTypes.setColumnLong(42);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeStringField() {
        Realm realm = looperThread.getRealm();
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_STRING);
        realm.beginTransaction();
        allTypes.setColumnString("42");
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeFloatField() {
        Realm realm = looperThread.getRealm();
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_FLOAT);
        realm.beginTransaction();
        allTypes.setColumnFloat(42.0f);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeDoubleField() {
        Realm realm = looperThread.getRealm();
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_DOUBLE);
        realm.beginTransaction();
        allTypes.setColumnDouble(42.0d);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeBooleanField() {
        Realm realm = looperThread.getRealm();
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_BOOLEAN);
        realm.beginTransaction();
        allTypes.setColumnBoolean(true);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeDateField() {
        Realm realm = looperThread.getRealm();
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_DATE);
        realm.beginTransaction();
        allTypes.setColumnDate(new Date());
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeBinaryField() {
        Realm realm = looperThread.getRealm();
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_BINARY);
        realm.beginTransaction();
        allTypes.setColumnBinary(new byte[] { 42 });
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeLinkFieldSetNewObject() {
        Realm realm = looperThread.getRealm();
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_REALMOBJECT);
        realm.beginTransaction();
        allTypes.setColumnRealmObject(realm.createObject(Dog.class));
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeLinkFieldSetNull() {
        Realm realm = looperThread.getRealm();
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_REALMOBJECT);
        realm.beginTransaction();
        allTypes.setColumnRealmObject(null);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeLinkFieldRemoveObject() {
        Realm realm = looperThread.getRealm();
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_REALMOBJECT);
        realm.beginTransaction();
        allTypes.getColumnRealmObject().deleteFromRealm();
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeLinkFieldOriginalObjectChanged_notTrigger() {
        Realm realm = looperThread.getRealm();
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        listenerShouldNotBeCalled(allTypes);
        realm.beginTransaction();
        allTypes.getColumnRealmObject().setAge(42);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeLinkListAddObject() {
        Realm realm = looperThread.getRealm();
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_REALMLIST);
        realm.beginTransaction();
        allTypes.getColumnRealmList().add(realm.createObject(Dog.class));
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeLinkListClear() {
        Realm realm = looperThread.getRealm();
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_REALMLIST);
        realm.beginTransaction();
        allTypes.getColumnRealmList().clear();
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeAllFields() {
        Realm realm = looperThread.getRealm();
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        checkChangedField(allTypes, AllTypes.FIELD_LONG, AllTypes.FIELD_REALMLIST, AllTypes.FIELD_REALMOBJECT,
                AllTypes.FIELD_DOUBLE, AllTypes.FIELD_FLOAT, AllTypes.FIELD_STRING, AllTypes.FIELD_BOOLEAN,
                AllTypes.FIELD_BINARY, AllTypes.FIELD_DATE);
        realm.beginTransaction();
        allTypes.setColumnLong(42);
        allTypes.getColumnRealmList().add(realm.createObject(Dog.class));
        allTypes.setColumnRealmObject(realm.createObject(Dog.class));
        allTypes.setColumnDouble(42.0d);
        allTypes.setColumnFloat(42.0f);
        allTypes.setColumnString("42");
        allTypes.setColumnBoolean(true);
        allTypes.setColumnBinary(new byte[] { 42 });
        allTypes.setColumnDate(new Date());
        realm.commitTransaction();
    }

    // Relevant to https://github.com/realm/realm-java/issues/4437
    // When the object listener triggered at the 2nd time, the local ref m_field_names_array has not been reset and it
    // contains an invalid local ref which has been released before.
    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void changeDifferentFieldOneAfterAnother() {
        Realm realm = looperThread.getRealm();
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        final AtomicBoolean stringChanged = new AtomicBoolean(false);
        final AtomicBoolean longChanged = new AtomicBoolean(false);
        final AtomicBoolean floatChanged = new AtomicBoolean(false);

        allTypes.addChangeListener(new RealmObjectChangeListener<RealmModel>() {
            @Override
            public void onChange(RealmModel object, ObjectChangeSet changeSet) {
                assertEquals(1, changeSet.getChangedFields().length);
                if (changeSet.isFieldChanged(AllTypes.FIELD_STRING)) {
                    assertFalse(stringChanged.get());
                    stringChanged.set(true);
                } else if (changeSet.isFieldChanged(AllTypes.FIELD_LONG)) {
                    assertFalse(longChanged.get());
                    longChanged.set(true);
                } else if (changeSet.isFieldChanged(AllTypes.FIELD_FLOAT)) {
                    assertTrue(stringChanged.get());
                    assertTrue(longChanged.get());
                    assertFalse(floatChanged.get());
                    floatChanged.set(true);
                    looperThread.testComplete();
                } else {
                    fail();
                }
            }
        });

        realm.beginTransaction();
        allTypes.setColumnString("42");
        realm.commitTransaction();

        realm.beginTransaction();
        allTypes.setColumnLong(42);
        realm.commitTransaction();

        realm.beginTransaction();
        allTypes.setColumnFloat(42.0f);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void findFirstAsync_changeSetIsNullWhenQueryReturns() {
        Realm realm = looperThread.getRealm();
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
        Realm realm = looperThread.getRealm();
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

    // When there are more than 512 fields change, the JNI local ref table size limitation may be reached.
    @Test
    @RunTestInLooperThread
    public void moreFieldsChangedThanLocalRefTableSize() {
        final String CLASS_NAME = "ManyFields";
        final int FIELD_COUNT = 1024;
        RealmConfiguration config = looperThread.createConfiguration("many_fields");
        final DynamicRealm realm = DynamicRealm.getInstance(config);

        realm.beginTransaction();
        RealmSchema schema = realm.getSchema();
        RealmObjectSchema objectSchema = schema.create(CLASS_NAME);
        for (int i = 0; i < FIELD_COUNT; i++) {
            objectSchema.addField("field" + i, int.class);
        }
        DynamicRealmObject obj = realm.createObject(CLASS_NAME);
        realm.commitTransaction();

        obj.addChangeListener(new RealmObjectChangeListener<DynamicRealmObject>() {
            @Override
            public void onChange(DynamicRealmObject object, ObjectChangeSet changeSet) {
                assertEquals(FIELD_COUNT, changeSet.getChangedFields().length);
                realm.close();
                looperThread.testComplete();
            }
        });

        realm.beginTransaction();
        for (int i = 0; i < FIELD_COUNT; i++) {
            obj.setInt("field" + i, 42);
        }
        realm.commitTransaction();
    }

    // For https://github.com/realm/realm-java/issues/4474
    @Test
    @RunTestInLooperThread
    public void allParentObjectShouldBeInChangeSet() {
        Realm realm = looperThread.getRealm();

        realm.beginTransaction();
        Owner owner = realm.createObject(Owner.class);
        Dog dog1 = realm.createObject(Dog.class);
        dog1.setOwner(owner);
        dog1.setHasTail(true);
        owner.getDogs().add(dog1);
        Dog dog2 = realm.createObject(Dog.class);
        dog2.setOwner(owner);
        dog2.setHasTail(true);
        owner.getDogs().add(dog2);
        Dog dog3 = realm.createObject(Dog.class);
        dog3.setOwner(owner);
        dog3.setHasTail(true);
        owner.getDogs().add(dog3);

        realm.commitTransaction();

        RealmResults<Dog> dogs = realm.where(Dog.class).equalTo(Dog.FIELD_HAS_TAIL, true).findAll();
        looperThread.keepStrongReference(dogs);
        dogs.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Dog>>() {
            @Override
            public void onChange(RealmResults<Dog> collection, OrderedCollectionChangeSet changeSet) {
                assertEquals(1, changeSet.getDeletions().length);
                assertEquals(0, changeSet.getInsertions().length);

                assertEquals(1, changeSet.getChangeRanges().length);
                assertEquals(0, changeSet.getChangeRanges()[0].startIndex);
                assertEquals(2, changeSet.getChangeRanges()[0].length);

                looperThread.testComplete();
            }
        });

        realm.beginTransaction();
        dog3.setHasTail(false);
        realm.commitTransaction();
        looperThread.testComplete();
    }
}
