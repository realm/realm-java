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

import android.support.test.annotation.UiThreadTest;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.CyclicType;
import io.realm.entities.DefaultValueOfField;
import io.realm.entities.Dog;
import io.realm.entities.MappedAllJavaTypes;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.Owner;
import io.realm.entities.PrimaryKeyAsLong;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.entities.RandomPrimaryKey;
import io.realm.entities.StringOnly;
import io.realm.internal.OsResults;
import io.realm.log.RealmLog;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmResultsTests extends CollectionTests {

    private final static int TEST_DATA_SIZE = 100;
    private final static long YEAR_MILLIS = TimeUnit.DAYS.toMillis(365);
    private final static long DECADE_MILLIS = 10 * TimeUnit.DAYS.toMillis(365);

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    private Realm realm;
    private RealmResults<AllTypes> collection;

    @Before
    public void setUp() {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
        populateTestRealm();
        collection = realm.where(AllTypes.class)
                .sort(AllTypes.FIELD_LONG, Sort.ASCENDING)
                .findAll();
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    private void populateTestRealm() {
        populateTestRealm(TEST_DATA_SIZE);
    }

    @Test
    public void findFirst() {
        AllTypes result = realm.where(AllTypes.class).findFirst();
        assertEquals(0, result.getColumnLong());
        assertEquals("test data 0", result.getColumnString());

        AllTypes none = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "smurf").findFirst();
        assertNull(none);
    }

    @Test
    public void size_returns_Integer_MAX_VALUE_for_huge_results() {
        final OsResults osResults = Mockito.mock(OsResults.class);
        final RealmResults<AllTypes> targetResult = TestHelper.newRealmResults(realm, osResults, AllTypes.class);

        Mockito.when(osResults.isLoaded()).thenReturn(true);
        Mockito.when(osResults.size()).thenReturn(((long) Integer.MAX_VALUE) - 1);
        assertEquals(Integer.MAX_VALUE - 1, targetResult.size());
        Mockito.when(osResults.size()).thenReturn(((long) Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, targetResult.size());
        Mockito.when(osResults.size()).thenReturn(((long) Integer.MAX_VALUE) + 1);
        assertEquals(Integer.MAX_VALUE, targetResult.size());
    }

    @Test
    public void subList() {
        RealmResults<AllTypes> list = realm.where(AllTypes.class).findAll();
        list.sort("columnLong");
        List<AllTypes> sublist = list.subList(Math.max(list.size() - 20, 0), list.size());
        assertEquals(TEST_DATA_SIZE - 1, sublist.get(sublist.size() - 1).getColumnLong());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void unsupportedMethods() {
        for (CollectionMutatorMethod method : CollectionMutatorMethod.values()) {
            try {
                switch (method) {
                    case ADD_OBJECT: collection.add(new AllTypes()); break;
                    case ADD_ALL_OBJECTS: collection.addAll(Collections.singletonList(new AllTypes())); break;
                    case CLEAR: collection.clear(); break;
                    case REMOVE_OBJECT: collection.remove(new AllTypes()); break;
                    case REMOVE_ALL: collection.removeAll(Collections.singletonList(new AllTypes())); break;
                    case RETAIN_ALL: collection.retainAll(Collections.singletonList(new AllTypes())); break;

                    // Supported methods.
                    case DELETE_ALL:
                        continue;
                }
                fail("Unknown method or failed to throw:" + method);
            } catch (UnsupportedOperationException ignored) {
            }
        }

        for (OrderedCollectionMutatorMethod method : OrderedCollectionMutatorMethod.values()) {
            try {
                switch (method) {
                    case ADD_INDEX: collection.add(0, new AllTypes()); break;
                    case ADD_ALL_INDEX: collection.addAll(0, Collections.singletonList(new AllTypes())); break;
                    case SET: collection.set(0, new AllTypes()); break;
                    case REMOVE_INDEX: collection.remove(0); break;

                    // Supported methods.
                    case DELETE_INDEX:
                    case DELETE_FIRST:
                    case DELETE_LAST:
                        continue;
                }
                fail("Unknown method or failed to throw:" + method);
            } catch (UnsupportedOperationException ignored) {
            }
        }
    }

    // Triggers an ARM bug.
    @Test
    public void verifyArmComparisons() {
        realm.beginTransaction();
        realm.delete(AllTypes.class);
        long id = -1;
        for (int i = 0; i < 10; i++) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnLong(id--);
        }
        realm.commitTransaction();

        assertEquals(10, realm.where(AllTypes.class).between(AllTypes.FIELD_LONG, -10, -1).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).greaterThan(AllTypes.FIELD_LONG, -11).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).greaterThanOrEqualTo(AllTypes.FIELD_LONG, -10).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).lessThan(AllTypes.FIELD_LONG, 128).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).lessThan(AllTypes.FIELD_LONG, 127).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).lessThanOrEqualTo(AllTypes.FIELD_LONG, -1).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).lessThan(AllTypes.FIELD_LONG, 0).findAll().size());
    }

    @Test
    @RunTestInLooperThread
    public void changeListener_syncIfNeeded_updatedFromOtherThread() {
        final Realm realm = looperThread.getRealm();
        populateTestRealm(realm, 10);

        final RealmResults<AllTypes> results = realm.where(AllTypes.class).lessThan(AllTypes.FIELD_LONG, 10).findAll();
        assertEquals(10, results.size());

        // 1. Deletes first object from another thread.
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 0).findFirst().deleteFromRealm();
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                // 2. RealmResults are refreshed before onSuccess is called.
                assertEquals(9, results.size());
                realm.close();
                looperThread.testComplete();
            }
        });
    }

    // distinctAsync
    private void populateTestRealm(int objects) {
        realm.beginTransaction();
        realm.deleteAll();
        for (int i = 0; i < objects; ++i) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 2) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date(YEAR_MILLIS * (i - objects / 2)));
            allTypes.setColumnDouble(Math.PI + i);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
            Dog d = realm.createObject(Dog.class);
            d.setName("Foo " + i);
            allTypes.setColumnRealmObject(d);
            allTypes.getColumnRealmList().add(d);
            NonLatinFieldNames nonLatinFieldNames = realm.createObject(NonLatinFieldNames.class);
            nonLatinFieldNames.set델타(i);
            nonLatinFieldNames.setΔέλτα(i);
        }
        realm.commitTransaction();
    }


    private void populateTestRealm(Realm testRealm, int objects) {
        testRealm.beginTransaction();
        testRealm.deleteAll();
        for (int i = 0; i < objects; i++) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 3) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date(DECADE_MILLIS * (i - (objects / 2))));
            allTypes.setColumnDouble(Math.PI);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
            NonLatinFieldNames nonLatinFieldNames = testRealm.createObject(NonLatinFieldNames.class);
            nonLatinFieldNames.set델타(i);
            nonLatinFieldNames.setΔέλτα(i);
            nonLatinFieldNames.set베타(1.234567f + i);
            nonLatinFieldNames.setΒήτα(1.234567f + i);
        }
        testRealm.commitTransaction();
    }


    private RealmResults<Dog> populateRealmResultsOnLinkView(Realm realm) {
        realm.beginTransaction();
        Owner owner = realm.createObject(Owner.class);
        for (int i = 0; i < 10; i++) {
            Dog dog = new Dog();
            dog.setName("name_" + i);
            dog.setOwner(owner);
            dog.setAge(i);
            dog.setBirthday(new Date(i));
            owner.getDogs().add(dog);
        }
        realm.commitTransaction();


        return owner.getDogs().where().lessThan(Dog.FIELD_AGE, 5).findAll();
    }

    // If a RealmResults is built on a link view, when the link view is deleted on the same thread, within the same
    // event loop, the RealmResults stays without changes since it is detached until the next event loop. In the next
    // event loop, the results will be empty because of the parent link view is deleted.
    // 1. Create results from link view.
    // 2. Delete the parent link view by a local transaction.
    // 3. Within the same event loop, the results stays the same.
    // 4. The results change listener called, the results becomes empty.
    @Test
    @RunTestInLooperThread
    public void accessors_resultsBuiltOnDeletedLinkView_deletionAsALocalCommit() {
        Realm realm = looperThread.getRealm();
        // Step 1
        RealmResults<Dog> dogs = populateRealmResultsOnLinkView(realm);
        looperThread.keepStrongReference(dogs);
        dogs.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
            @Override
            public void onChange(RealmResults<Dog> dogs) {
                // Step 4.
                // The results is still valid, but empty.
                assertEquals(true, dogs.isValid());
                assertEquals(true, dogs.isEmpty());
                assertEquals(0, dogs.size());
                try {
                    dogs.first();
                    fail();
                } catch (IndexOutOfBoundsException ignored) {
                }

                assertEquals(0, dogs.sum(Dog.FIELD_AGE).intValue());
                assertEquals(0f, dogs.sum(Dog.FIELD_HEIGHT).floatValue(), 0f);
                assertEquals(0d, dogs.sum(Dog.FIELD_WEIGHT).doubleValue(), 0d);
                assertEquals(0d, dogs.average(Dog.FIELD_AGE), 0d);
                assertEquals(0d, dogs.average(Dog.FIELD_HEIGHT), 0d);
                assertEquals(0d, dogs.average(Dog.FIELD_WEIGHT), 0d);
                assertEquals(null, dogs.min(Dog.FIELD_AGE));
                assertEquals(null, dogs.max(Dog.FIELD_AGE));
                assertEquals(null, dogs.minDate(Dog.FIELD_BIRTHDAY));
                assertEquals(null, dogs.maxDate(Dog.FIELD_BIRTHDAY));

                assertEquals(0, dogs.where().findAll().size());

                looperThread.testComplete();
            }
        });

        // Step 2
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Owner.class).findAll().deleteAllFromRealm();
            }
        });

        // Step 3
        assertEquals(true, dogs.isValid());
        assertEquals(0, dogs.size());
        // The link view has been deleted.
        assertEquals(0, dogs.where().findAll().size());
    }

    // If a RealmResults is built on a link view, when the link view is deleted on a remote thread, within the same
    // event loop, the RealmResults stays without changes since the Realm version doesn't change. In the next
    // event loop, the results will be empty because of the parent link view is deleted.
    // 1. Create results from link view.
    // 2. Delete the parent link view by a remote transaction.
    // 3. Within the same event loop, the results stays the same.
    // 4. The results change listener called, the results becomes empty.
    @Test
    @RunTestInLooperThread
    public void accessors_resultsBuiltOnDeletedLinkView_deletionAsARemoteCommit() {
        // Step 1
        Realm realm = looperThread.getRealm();
        RealmResults<Dog> dogs = populateRealmResultsOnLinkView(realm);
        looperThread.keepStrongReference(dogs);
        dogs.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
            @Override
            public void onChange(RealmResults<Dog> dogs) {
                // Step 4
                // The results is still valid, but empty.
                assertEquals(true, dogs.isValid());
                assertEquals(true, dogs.isEmpty());
                assertEquals(0, dogs.size());
                try {
                    dogs.first();
                    fail();
                } catch (IndexOutOfBoundsException ignored) {
                }

                assertEquals(0, dogs.sum(Dog.FIELD_AGE).intValue());
                assertEquals(0f, dogs.sum(Dog.FIELD_HEIGHT).floatValue(), 0f);
                assertEquals(0d, dogs.sum(Dog.FIELD_WEIGHT).doubleValue(), 0d);
                assertEquals(0d, dogs.average(Dog.FIELD_AGE), 0d);
                assertEquals(0d, dogs.average(Dog.FIELD_HEIGHT), 0d);
                assertEquals(0d, dogs.average(Dog.FIELD_WEIGHT), 0d);
                assertEquals(null, dogs.min(Dog.FIELD_AGE));
                assertEquals(null, dogs.max(Dog.FIELD_AGE));
                assertEquals(null, dogs.minDate(Dog.FIELD_BIRTHDAY));
                assertEquals(null, dogs.maxDate(Dog.FIELD_BIRTHDAY));

                assertEquals(0, dogs.where().findAll().size());

                looperThread.testComplete();
            }
        });


        // Step 2
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Owner.class).findAll().deleteAllFromRealm();
            }
        });

        // Step 3
        assertEquals(true, dogs.isValid());
        assertEquals(5, dogs.size());
        // The link view still exists
        assertEquals(5, dogs.where().findAll().size());
    }

    @Test
    @RunTestInLooperThread
    public void addChangeListener() {
        Realm realm = looperThread.getRealm();
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();

        looperThread.keepStrongReference(collection);
        collection.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                looperThread.testComplete();
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void addChangeListener_twice() {
        final AtomicInteger listenersTriggered = new AtomicInteger(0);
        final Realm realm = looperThread.getRealm();
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();

        RealmChangeListener<RealmResults<AllTypes>> listener = new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                listenersTriggered.incrementAndGet();
            }
        };

        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                listenersTriggered.incrementAndGet();
                looperThread.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (listenersTriggered.get() == 1) {
                            looperThread.testComplete();
                        } else {
                            fail("Only global listener should be triggered");
                        }
                    }
                });
            }
        });

        // Adding it twice will be ignored, so removing it will not cause the listener to be triggered.
        looperThread.keepStrongReference(collection);
        collection.addChangeListener(listener);
        collection.addChangeListener(listener);
        collection.removeChangeListener(listener);

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }

    @Test
    @UiThreadTest
    public void addChangeListener_null() {
        try {
            collection.addChangeListener((RealmChangeListener<RealmResults<AllTypes>>) null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    @RunTestInLooperThread
    public void removeChangeListener() {
        final AtomicInteger listenersTriggered = new AtomicInteger(0);
        final Realm realm = looperThread.getRealm();
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();

        RealmChangeListener<RealmResults<AllTypes>> listener = new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                listenersTriggered.incrementAndGet();
            }
        };

        looperThread.keepStrongReference(collection);
        collection.addChangeListener(listener);
        collection.removeChangeListener(listener);

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        // The above commit should have put a REALM_CHANGED event on the Looper queue before this runnable.
        looperThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (listenersTriggered.get() == 0) {
                    looperThread.testComplete();
                } else {
                    fail("Listener wasn't removed");
                }
            }
        });
    }

    @Test
    @UiThreadTest
    public void removeChangeListener_null() {
        try {
            collection.removeChangeListener((RealmChangeListener) null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    @RunTestInLooperThread
    public void removeAllChangeListeners() {
        final AtomicInteger listenersTriggered = new AtomicInteger(0);
        final Realm realm = looperThread.getRealm();
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();

        RealmChangeListener<RealmResults<AllTypes>> listenerA = new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                listenersTriggered.incrementAndGet();
            }
        };
        RealmChangeListener<RealmResults<AllTypes>> listenerB = new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                listenersTriggered.incrementAndGet();
            }
        };

        looperThread.keepStrongReference(collection);
        collection.addChangeListener(listenerA);
        collection.addChangeListener(listenerB);
        collection.removeAllChangeListeners();

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        // The above commit should have put a REALM_CHANGED event on the Looper queue before this runnable.
        looperThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (listenersTriggered.get() == 0) {
                    looperThread.testComplete();
                } else {
                    fail("Listeners wasn't removed");
                }
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void removeAllChangeListeners_thenAdd() {
        final Realm realm = looperThread.getRealm();
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();

        collection.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> element) {
                fail();
            }
        });
        collection.removeAllChangeListeners();

        collection.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> results) {
                assertEquals(1, results.size());
                looperThread.testComplete();
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }

    @Test
    public void deleteAndDeleteAll() {
        realm.beginTransaction();
        for (int i = 0; i < 10; i++) {
            StringOnly stringOnly = realm.createObject(StringOnly.class);
            stringOnly.setChars("String " + i);
        }
        realm.commitTransaction();

        RealmResults<StringOnly> stringOnlies = realm.where(StringOnly.class).findAll();

        realm.beginTransaction();
        // Removes one object.
        stringOnlies.get(0).deleteFromRealm();
        realm.commitTransaction();

        realm.beginTransaction();
        // Removes the rest.
        stringOnlies.deleteAllFromRealm();
        realm.commitTransaction();

        assertEquals(0, realm.where(StringOnly.class).findAll().size());
    }

    @Test
    public void syncQuery_defaultValuesAreIgnored() {
        final String fieldIgnoredValue = DefaultValueOfField.FIELD_IGNORED_DEFAULT_VALUE + ".modified";
        final String fieldStringValue = DefaultValueOfField.FIELD_STRING_DEFAULT_VALUE + ".modified";
        final String fieldRandomStringValue = "non-random";
        final short fieldShortValue = (short) (DefaultValueOfField.FIELD_SHORT_DEFAULT_VALUE + 1);
        final int fieldIntValue = DefaultValueOfField.FIELD_INT_DEFAULT_VALUE + 1;
        final long fieldLongPrimaryKeyValue = DefaultValueOfField.FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE + 1;
        final long fieldLongValue = DefaultValueOfField.FIELD_LONG_DEFAULT_VALUE + 1;
        final byte fieldByteValue = (byte) (DefaultValueOfField.FIELD_BYTE_DEFAULT_VALUE + 1);
        final float fieldFloatValue = DefaultValueOfField.FIELD_FLOAT_DEFAULT_VALUE + 1;
        final double fieldDoubleValue = DefaultValueOfField.FIELD_DOUBLE_DEFAULT_VALUE + 1;
        final boolean fieldBooleanValue = !DefaultValueOfField.FIELD_BOOLEAN_DEFAULT_VALUE;
        final Date fieldDateValue = new Date(DefaultValueOfField.FIELD_DATE_DEFAULT_VALUE.getTime() + 1);
        final byte[] fieldBinaryValue = {(byte) (DefaultValueOfField.FIELD_BINARY_DEFAULT_VALUE[0] - 1)};
        final int fieldObjectIntValue = RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE + 1;
        final int fieldListIntValue = RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE + 2;

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final DefaultValueOfField obj = new DefaultValueOfField();
                obj.setFieldIgnored(fieldIgnoredValue);
                obj.setFieldString(fieldStringValue);
                obj.setFieldRandomString(fieldRandomStringValue);
                obj.setFieldShort(fieldShortValue);
                obj.setFieldInt(fieldIntValue);
                obj.setFieldLongPrimaryKey(fieldLongPrimaryKeyValue);
                obj.setFieldLong(fieldLongValue);
                obj.setFieldByte(fieldByteValue);
                obj.setFieldFloat(fieldFloatValue);
                obj.setFieldDouble(fieldDoubleValue);
                obj.setFieldBoolean(fieldBooleanValue);
                obj.setFieldDate(fieldDateValue);
                obj.setFieldBinary(fieldBinaryValue);

                final RandomPrimaryKey fieldObjectValue = new RandomPrimaryKey();
                fieldObjectValue.setFieldInt(fieldObjectIntValue);
                obj.setFieldObject(fieldObjectValue);

                final RealmList<RandomPrimaryKey> list = new RealmList<RandomPrimaryKey>();
                final RandomPrimaryKey listItem = new RandomPrimaryKey();
                listItem.setFieldInt(fieldListIntValue);
                list.add(listItem);
                obj.setFieldList(list);

                realm.copyToRealm(obj);
            }
        });

        final RealmResults<DefaultValueOfField> result = realm.where(DefaultValueOfField.class)
                .equalTo(DefaultValueOfField.FIELD_LONG_PRIMARY_KEY,
                        fieldLongPrimaryKeyValue).findAll();

        final DefaultValueOfField obj = result.first();

        assertEquals(DefaultValueOfField.FIELD_IGNORED_DEFAULT_VALUE/*not fieldIgnoredValue*/,
                obj.getFieldIgnored());
        assertEquals(fieldStringValue, obj.getFieldString());
        assertEquals(fieldRandomStringValue, obj.getFieldRandomString());
        assertEquals(fieldShortValue, obj.getFieldShort());
        assertEquals(fieldIntValue, obj.getFieldInt());
        assertEquals(fieldLongPrimaryKeyValue, obj.getFieldLongPrimaryKey());
        assertEquals(fieldLongValue, obj.getFieldLong());
        assertEquals(fieldByteValue, obj.getFieldByte());
        assertEquals(fieldFloatValue, obj.getFieldFloat(), 0f);
        assertEquals(fieldDoubleValue, obj.getFieldDouble(), 0d);
        assertEquals(fieldBooleanValue, obj.isFieldBoolean());
        assertEquals(fieldDateValue, obj.getFieldDate());
        assertTrue(Arrays.equals(fieldBinaryValue, obj.getFieldBinary()));
        assertEquals(fieldObjectIntValue, obj.getFieldObject().getFieldInt());
        assertEquals(1, obj.getFieldList().size());
        assertEquals(fieldListIntValue, obj.getFieldList().first().getFieldInt());
    }

    @Test
    public void getRealm() {
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();
        assertTrue(realm == collection.getRealm());
    }

    @Test
    public void getRealm_throwsIfDynamicRealm() {
        DynamicRealm dRealm = DynamicRealm.getInstance(realm.getConfiguration());
        RealmResults<DynamicRealmObject> collection = dRealm.where(AllTypes.CLASS_NAME).findAll();

        try {
            collection.getRealm();
            fail();
        } catch (IllegalStateException ignore) {
        } finally {
            dRealm.close();
        }
    }

    @Test
    public void getRealm_throwsIfRealmClosed() {
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();
        realm.close();
        try {
            collection.getRealm();
            fail();
        } catch (IllegalStateException ignore) {
        }
    }

    private void populateMappedAllJavaTypes(int objects) {
        realm.beginTransaction();
        realm.deleteAll();
        for (int i = 0; i < objects; ++i) {
            MappedAllJavaTypes obj = realm.createObject(MappedAllJavaTypes.class, i);
            obj.fieldBoolean =  ((i % 2) == 0);
            obj.fieldBinary = (new byte[]{1, 2, 3});
            obj.fieldDate = (new Date(YEAR_MILLIS * (i - objects / 2)));
            obj.fieldDouble = (Math.PI + i);
            obj.fieldFloat = (1.234567f + i);
            obj.fieldString = ("test data " + i);
            obj.fieldLong = i;
            obj.fieldObject = obj;
            obj.fieldList.add(obj);
        }
        realm.commitTransaction();
    }

    private void populateAllJavaTypes(int objects) {
        realm.beginTransaction();
        realm.deleteAll();
        for (int i = 0; i < objects; ++i) {
            AllJavaTypes obj = realm.createObject(AllJavaTypes.class, i);
            obj.setFieldBoolean((i % 2) == 0);
            obj.setFieldBinary(new byte[]{1, 2, 3});
            obj.setFieldDate(new Date(YEAR_MILLIS * (i - objects / 2)));
            obj.setFieldDouble(Math.PI + i);
            obj.setFieldFloat(1.234567f + i);
            obj.setFieldString("test data " + i);
            obj.setFieldLong(i);
            obj.setFieldObject(obj);
            obj.getFieldList().add(obj);
        }
        realm.commitTransaction();
    }

    enum BulkSetMethods {
        STRING,
        BOOLEAN,
        BYTE,
        SHORT,
        INTEGER,
        LONG,
        FLOAT,
        DOUBLE,
        BINARY,
        DATE,
        OBJECT,
        MODEL_LIST,
        STRING_VALUE_LIST,
        BOOLEAN_VALUE_LIST,
        BYTE_VALUE_LIST,
        SHORT_VALUE_LIST,
        INTEGER_VALUE_LIST,
        LONG_VALUE_LIST,
        FLOAT_VALUE_LIST,
        DOUBLE_VALUE_LIST,
        BINARY_VALUE_LIST,
        DATE_VALUE_LIST
    }

    interface ElementValidator<T> {
        void validate(T obj);
    }

    private <T extends RealmModel> void assertElements(RealmResults<T> collection, ElementValidator<T> validator) {
        for (T obj : collection) {
            validator.validate(obj);
        }
    }

    @Test
    public void setValue() {
        populateAllJavaTypes(5);
        RealmResults<AllJavaTypes> collection = realm.where(AllJavaTypes.class).findAll();
        realm.beginTransaction();
        for (BulkSetMethods type : BulkSetMethods.values()) {
            switch(type) {
                case STRING:
                    collection.setValue(AllJavaTypes.FIELD_STRING, "foo");
                    assertElements(collection, obj -> assertEquals("foo", obj.getFieldString()));
                    collection.setValue(AllJavaTypes.FIELD_STRING, null);
                    assertElements(collection, obj -> assertEquals(null, obj.getFieldString()));
                    break;
                case BOOLEAN:
                    collection.setValue(AllJavaTypes.FIELD_BOOLEAN, true);
                    assertElements(collection, obj -> assertTrue(obj.isFieldBoolean()));
                    break;
                case BYTE:
                    collection.setValue(AllJavaTypes.FIELD_BYTE, (byte) 1);
                    assertElements(collection, obj -> assertEquals((byte)1, obj.getFieldByte()));
                    break;
                case SHORT:
                    collection.setValue(AllJavaTypes.FIELD_SHORT, (short) 2);
                    assertElements(collection, obj -> assertEquals((short)2, obj.getFieldShort()));
                    break;
                case INTEGER:
                    collection.setValue(AllJavaTypes.FIELD_INT, 3);
                    assertElements(collection, obj -> assertEquals(3, obj.getFieldInt()));
                    break;
                case LONG:
                    collection.setValue(AllJavaTypes.FIELD_LONG, 4L);
                    assertElements(collection, obj -> assertEquals(4L, obj.getFieldLong()));
                    break;
                case FLOAT:
                    collection.setValue(AllJavaTypes.FIELD_FLOAT, 1.23F);
                    assertElements(collection, obj -> assertEquals(1.23F, obj.getFieldFloat(), 0F));
                    break;
                case DOUBLE:
                    collection.setValue(AllJavaTypes.FIELD_DOUBLE, 1.234);
                    assertElements(collection, obj -> assertEquals(1.234, obj.getFieldDouble(), 0F));
                    break;
                case BINARY:
                    collection.setValue(AllJavaTypes.FIELD_BINARY, new byte[]{1,2,3});
                    assertElements(collection, obj -> assertArrayEquals(new byte[]{1,2,3}, obj.getFieldBinary()));
                    collection.setValue(AllJavaTypes.FIELD_BINARY, null);
                    assertElements(collection, obj -> assertNull(obj.getFieldBinary()));
                    break;
                case DATE:
                    collection.setValue(AllJavaTypes.FIELD_DATE, new Date(1000));
                    assertElements(collection, obj -> assertEquals(new Date(1000), obj.getFieldDate()));
                    collection.setValue(AllJavaTypes.FIELD_DATE, null);
                    assertElements(collection, obj -> assertEquals(null, obj.getFieldDate()));
                    break;
                case OBJECT: {
                    AllJavaTypes childObj = realm.createObject(AllJavaTypes.class, 42);
                    collection.setValue(AllJavaTypes.FIELD_OBJECT, childObj);
                    assertElements(collection, obj -> assertEquals(childObj, obj.getFieldObject()));
                    collection.setValue(AllJavaTypes.FIELD_OBJECT, null);
                    assertElements(collection, obj -> assertNull(obj.getFieldObject()));
                    break;
                }
                case MODEL_LIST: {
                    AllJavaTypes childObj = realm.createObject(AllJavaTypes.class, 43);
                    collection.setValue(AllJavaTypes.FIELD_LIST, new RealmList<>(childObj));
                    assertElements(collection, obj -> {
                        assertEquals(1, obj.getFieldList().size());
                        assertEquals(childObj, obj.getFieldList().first());
                    });
                    break;
                }
                case STRING_VALUE_LIST: {
                    RealmList<String> list = new RealmList<>("Foo", "Bar");
                    collection.setValue(AllJavaTypes.FIELD_STRING_LIST, list);
                    assertElements(collection, obj -> {
                        assertEquals("Foo", obj.getFieldStringList().first());
                        assertEquals("Bar", obj.getFieldStringList().last());
                    });
                    break;
                }
                case BOOLEAN_VALUE_LIST: {
                    RealmList<Boolean> list = new RealmList<>(true, false);
                    collection.setValue(AllJavaTypes.FIELD_BOOLEAN_LIST, list);
                    assertElements(collection, obj -> {
                        assertTrue(obj.getFieldBooleanList().first());
                        assertFalse(obj.getFieldBooleanList().last());
                    });
                    break;
                }
                case BYTE_VALUE_LIST: {
                    RealmList<Byte> list = new RealmList<>((byte) 1, (byte) 2);
                    collection.setValue(AllJavaTypes.FIELD_BYTE_LIST, list);
                    assertElements(collection, obj -> {
                        assertEquals(Byte.valueOf((byte) 1), obj.getFieldByteList().first());
                        assertEquals(Byte.valueOf((byte) 2), obj.getFieldByteList().last());
                    });
                    break;
                }
                case SHORT_VALUE_LIST: {
                    RealmList<Short> list = new RealmList<>((short) 1, (short) 2);
                    collection.setValue(AllJavaTypes.FIELD_SHORT_LIST, list);
                    assertElements(collection, obj -> {
                        assertEquals(Short.valueOf((short) 1), obj.getFieldShortList().first());
                        assertEquals(Short.valueOf((short) 2), obj.getFieldShortList().last());
                    });
                    break;
                }
                case INTEGER_VALUE_LIST: {
                    RealmList<Integer> list = new RealmList<>(1, 2);
                    collection.setValue(AllJavaTypes.FIELD_INTEGER_LIST, list);
                    assertElements(collection, obj -> {
                        assertEquals(Integer.valueOf(1), obj.getFieldIntegerList().first());
                        assertEquals(Integer.valueOf(2), obj.getFieldIntegerList().last());
                    });
                    break;
                }
                case LONG_VALUE_LIST: {
                    RealmList<Long> list = new RealmList<>(1L, 2L);
                    collection.setValue(AllJavaTypes.FIELD_LONG_LIST, list);
                    assertElements(collection, obj -> {
                        assertEquals(Long.valueOf(1), obj.getFieldLongList().first());
                        assertEquals(Long.valueOf(2), obj.getFieldLongList().last());
                    });
                    break;
                }
                case FLOAT_VALUE_LIST: {
                    RealmList<Float> list = new RealmList<>(1.1F, 2.2F);
                    collection.setValue(AllJavaTypes.FIELD_FLOAT_LIST, list);
                    assertElements(collection, obj -> {
                        assertEquals(1.1F, obj.getFieldFloatList().first(), 0F);
                        assertEquals(2.2F, obj.getFieldFloatList().last(), 0F);
                    });
                    break;
                }
                case DOUBLE_VALUE_LIST: {
                    RealmList<Double> list = new RealmList<>(1.1D, 2.2D);
                    collection.setValue(AllJavaTypes.FIELD_DOUBLE_LIST, list);
                    assertElements(collection, obj -> {
                        assertEquals(1.1D, obj.getFieldDoubleList().first(), 0D);
                        assertEquals(2.2D, obj.getFieldDoubleList().last(), 0D);
                    });
                    break;
                }
                case BINARY_VALUE_LIST: {
                    RealmList<byte[]> list = new RealmList<>(new byte[] {1,2,3}, new byte[] {2,3,4});
                    collection.setValue(AllJavaTypes.FIELD_BINARY_LIST, list);
                    assertElements(collection, obj -> {
                        assertArrayEquals(new byte[] {1,2,3}, obj.getFieldBinaryList().first());
                        assertArrayEquals(new byte[] {2,3,4}, obj.getFieldBinaryList().last());
                    });
                    break;
                }
                case DATE_VALUE_LIST:  {
                    RealmList<Date> list = new RealmList<>(new Date(1000), new Date(2000));
                    collection.setValue(AllJavaTypes.FIELD_DATE_LIST, list);
                    assertElements(collection, obj -> {
                        assertEquals(new Date(1000), obj.getFieldDateList().first());
                        assertEquals(new Date(2000), obj.getFieldDateList().last());
                    });
                    break;
                }
                default:
                    fail("Unknown type: " + type);
            }
        }
    }

    @Test
    public void setValue_implicitConversions() {
        populateAllJavaTypes(5);
        RealmResults<AllJavaTypes> collection = realm.where(AllJavaTypes.class).findAll();
        realm.beginTransaction();
        for (BulkSetMethods type : BulkSetMethods.values()) {
            switch(type) {
                case BOOLEAN:
                    collection.setValue(AllJavaTypes.FIELD_BOOLEAN, "true");
                    assertElements(collection, obj -> assertTrue(obj.isFieldBoolean()));
                    collection.setValue(AllJavaTypes.FIELD_BOOLEAN, "FALSE");
                    assertElements(collection, obj -> assertFalse(obj.isFieldBoolean()));
                    collection.setValue(AllJavaTypes.FIELD_BOOLEAN, "True");
                    assertElements(collection, obj -> assertTrue(obj.isFieldBoolean()));
                    collection.setValue(AllJavaTypes.FIELD_BOOLEAN, "false");
                    assertElements(collection, obj -> assertFalse(obj.isFieldBoolean()));
                    collection.setValue(AllJavaTypes.FIELD_BOOLEAN, "TRUE");
                    assertElements(collection, obj -> assertTrue(obj.isFieldBoolean()));
                    break;
                case BYTE:
                    collection.setValue(AllJavaTypes.FIELD_BYTE, "1");
                    assertElements(collection, obj -> assertEquals((byte)1, obj.getFieldByte()));
                    break;
                case SHORT:
                    collection.setValue(AllJavaTypes.FIELD_SHORT, "2");
                    assertElements(collection, obj -> assertEquals((short)2, obj.getFieldShort()));
                    break;
                case INTEGER:
                    collection.setValue(AllJavaTypes.FIELD_INT, "3");
                    assertElements(collection, obj -> assertEquals(3, obj.getFieldInt()));
                    break;
                case LONG:
                    collection.setValue(AllJavaTypes.FIELD_LONG, Long.toString(Long.MAX_VALUE));
                    assertElements(collection, obj -> assertEquals(Long.MAX_VALUE, obj.getFieldLong()));
                    break;
                case FLOAT:
                    collection.setValue(AllJavaTypes.FIELD_FLOAT, "1.23F");
                    assertElements(collection, obj -> assertEquals(1.23F, obj.getFieldFloat(), 0F));
                    break;
                case DOUBLE:
                    collection.setValue(AllJavaTypes.FIELD_DOUBLE, "1.234");
                    assertElements(collection, obj -> assertEquals(1.234, obj.getFieldDouble(), 0F));
                    break;
                case DATE:
                    collection.setValue(AllJavaTypes.FIELD_DATE, "1000");
                    assertElements(collection, obj -> assertEquals(new Date(1000), obj.getFieldDate()));
                    collection.setValue(AllJavaTypes.FIELD_DATE, "/Date(2000+0000)/");
                    assertElements(collection, obj -> assertEquals(new Date(2000), obj.getFieldDate()));
                    break;

                // These types do not offer any implicit conversion
                case STRING:
                case BINARY:
                case OBJECT:
                case MODEL_LIST:
                case STRING_VALUE_LIST:
                case BOOLEAN_VALUE_LIST:
                case BYTE_VALUE_LIST:
                case SHORT_VALUE_LIST:
                case INTEGER_VALUE_LIST:
                case LONG_VALUE_LIST:
                case FLOAT_VALUE_LIST:
                case DOUBLE_VALUE_LIST:
                case BINARY_VALUE_LIST:
                case DATE_VALUE_LIST:
                    continue;

                default:
                    fail("Unknown type: " + type);
            }
        }
    }

    // Test for https://github.com/realm/realm-java/issues/6478
    @Test
    public void setDate_updateRemovesObjectFromQuery() {
        realm.beginTransaction();
        realm.deleteAll();
        int objects = 10;
        for (int i = 0; i < objects; ++i) {
            AllJavaTypes obj = realm.createObject(AllJavaTypes.class, i);
            obj.setFieldDate(i % 2 == 0 ? null : new Date(1000));
        }
        realm.commitTransaction();

        realm.beginTransaction();
        RealmResults<AllJavaTypes> collection = realm.where(AllJavaTypes.class)
                .isNull(AllJavaTypes.FIELD_DATE)
                .findAll();

        collection.setDate(AllJavaTypes.FIELD_DATE, new Date(2000));
        realm.commitTransaction();

        assertTrue(collection.isEmpty());
    }

    @Test
    public void setValue_specificType() {
        populateAllJavaTypes(5);
        RealmResults<AllJavaTypes> collection = realm.where(AllJavaTypes.class).findAll();
        realm.beginTransaction();
        for (BulkSetMethods type : BulkSetMethods.values()) {
            switch(type) {
                case STRING:
                    collection.setString(AllJavaTypes.FIELD_STRING, "foo");
                    assertElements(collection, obj -> assertEquals("foo", obj.getFieldString()));
                    collection.setString(AllJavaTypes.FIELD_STRING, null);
                    assertElements(collection, obj -> assertEquals(null, obj.getFieldString()));
                    break;
                case BOOLEAN:
                    collection.setBoolean(AllJavaTypes.FIELD_BOOLEAN, true);
                    assertElements(collection, obj -> assertTrue(obj.isFieldBoolean()));
                    break;
                case BYTE:
                    collection.setByte(AllJavaTypes.FIELD_BYTE, (byte) 1);
                    assertElements(collection, obj -> assertEquals((byte)1, obj.getFieldByte()));
                    break;
                case SHORT:
                    collection.setShort(AllJavaTypes.FIELD_SHORT, (short) 2);
                    assertElements(collection, obj -> assertEquals((short)2, obj.getFieldShort()));
                    break;
                case INTEGER:
                    collection.setInt(AllJavaTypes.FIELD_INT, 3);
                    assertElements(collection, obj -> assertEquals(3, obj.getFieldInt()));
                    break;
                case LONG:
                    collection.setLong(AllJavaTypes.FIELD_LONG, 4L);
                    assertElements(collection, obj -> assertEquals(4L, obj.getFieldLong()));
                    break;
                case FLOAT:
                    collection.setFloat(AllJavaTypes.FIELD_FLOAT, 1.23F);
                    assertElements(collection, obj -> assertEquals(1.23F, obj.getFieldFloat(), 0F));
                    break;
                case DOUBLE:
                    collection.setDouble(AllJavaTypes.FIELD_DOUBLE, 1.234);
                    assertElements(collection, obj -> assertEquals(1.234, obj.getFieldDouble(), 0F));
                    break;
                case BINARY:
                    collection.setBlob(AllJavaTypes.FIELD_BINARY, new byte[]{1,2,3});
                    assertElements(collection, obj -> assertArrayEquals(new byte[]{1,2,3}, obj.getFieldBinary()));
                    collection.setBlob(AllJavaTypes.FIELD_BINARY, null);
                    assertElements(collection, obj -> assertNull(obj.getFieldBinary()));
                    break;
                case DATE:
                    collection.setDate(AllJavaTypes.FIELD_DATE, new Date(1000));
                    assertElements(collection, obj -> assertEquals(new Date(1000), obj.getFieldDate()));
                    collection.setDate(AllJavaTypes.FIELD_DATE, null);
                    assertElements(collection, obj -> assertEquals(null, obj.getFieldDate()));
                    break;
                case OBJECT: {
                    AllJavaTypes childObj = realm.createObject(AllJavaTypes.class, 42);
                    collection.setObject(AllJavaTypes.FIELD_OBJECT, childObj);
                    assertElements(collection, obj -> assertEquals(childObj, obj.getFieldObject()));
                    collection.setObject(AllJavaTypes.FIELD_OBJECT, null);
                    assertElements(collection, obj -> assertNull(obj.getFieldObject()));
                    break;
                }
                case MODEL_LIST: {
                    AllJavaTypes childObj = realm.createObject(AllJavaTypes.class, 43);
                    collection.setList(AllJavaTypes.FIELD_LIST, new RealmList<>(childObj));
                    assertElements(collection, obj -> {
                        assertEquals(1, obj.getFieldList().size());
                        assertEquals(childObj, obj.getFieldList().first());
                    });
                    break;
                }
                case STRING_VALUE_LIST: {
                    RealmList<String> list = new RealmList<>("Foo", "Bar");
                    collection.setList(AllJavaTypes.FIELD_STRING_LIST, list);
                    assertElements(collection, obj -> {
                        assertEquals("Foo", obj.getFieldStringList().first());
                        assertEquals("Bar", obj.getFieldStringList().last());
                    });
                    break;
                }
                case BOOLEAN_VALUE_LIST: {
                    RealmList<Boolean> list = new RealmList<>(true, false);
                    collection.setList(AllJavaTypes.FIELD_BOOLEAN_LIST, list);
                    assertElements(collection, obj -> {
                        assertTrue(obj.getFieldBooleanList().first());
                        assertFalse(obj.getFieldBooleanList().last());
                    });
                    break;
                }
                case BYTE_VALUE_LIST: {
                    RealmList<Byte> list = new RealmList<>((byte) 1, (byte) 2);
                    collection.setList(AllJavaTypes.FIELD_BYTE_LIST, list);
                    assertElements(collection, obj -> {
                        assertEquals(Byte.valueOf((byte) 1), obj.getFieldByteList().first());
                        assertEquals(Byte.valueOf((byte) 2), obj.getFieldByteList().last());
                    });
                    break;
                }
                case SHORT_VALUE_LIST: {
                    RealmList<Short> list = new RealmList<>((short) 1, (short) 2);
                    collection.setList(AllJavaTypes.FIELD_SHORT_LIST, list);
                    assertElements(collection, obj -> {
                        assertEquals(Short.valueOf((short) 1), obj.getFieldShortList().first());
                        assertEquals(Short.valueOf((short) 2), obj.getFieldShortList().last());
                    });
                    break;
                }
                case INTEGER_VALUE_LIST: {
                    RealmList<Integer> list = new RealmList<>(1, 2);
                    collection.setList(AllJavaTypes.FIELD_INTEGER_LIST, list);
                    assertElements(collection, obj -> {
                        assertEquals(Integer.valueOf(1), obj.getFieldIntegerList().first());
                        assertEquals(Integer.valueOf(2), obj.getFieldIntegerList().last());
                    });
                    break;
                }
                case LONG_VALUE_LIST: {
                    RealmList<Long> list = new RealmList<>(1L, 2L);
                    collection.setList(AllJavaTypes.FIELD_LONG_LIST, list);
                    assertElements(collection, obj -> {
                        assertEquals(Long.valueOf(1), obj.getFieldLongList().first());
                        assertEquals(Long.valueOf(2), obj.getFieldLongList().last());
                    });
                    break;
                }
                case FLOAT_VALUE_LIST: {
                    RealmList<Float> list = new RealmList<>(1.1F, 2.2F);
                    collection.setList(AllJavaTypes.FIELD_FLOAT_LIST, list);
                    assertElements(collection, obj -> {
                        assertEquals(1.1F, obj.getFieldFloatList().first(), 0F);
                        assertEquals(2.2F, obj.getFieldFloatList().last(), 0F);
                    });
                    break;
                }
                case DOUBLE_VALUE_LIST: {
                    RealmList<Double> list = new RealmList<>(1.1D, 2.2D);
                    collection.setList(AllJavaTypes.FIELD_DOUBLE_LIST, list);
                    assertElements(collection, obj -> {
                        assertEquals(1.1D, obj.getFieldDoubleList().first(), 0D);
                        assertEquals(2.2D, obj.getFieldDoubleList().last(), 0D);
                    });
                    break;
                }
                case BINARY_VALUE_LIST: {
                    RealmList<byte[]> list = new RealmList<>(new byte[] {1,2,3}, new byte[] {2,3,4});
                    collection.setList(AllJavaTypes.FIELD_BINARY_LIST, list);
                    assertElements(collection, obj -> {
                        assertArrayEquals(new byte[] {1,2,3}, obj.getFieldBinaryList().first());
                        assertArrayEquals(new byte[] {2,3,4}, obj.getFieldBinaryList().last());
                    });
                    break;
                }
                case DATE_VALUE_LIST:  {
                    RealmList<Date> list = new RealmList<>(new Date(1000), new Date(2000));
                    collection.setList(AllJavaTypes.FIELD_DATE_LIST, list);
                    assertElements(collection, obj -> {
                        assertEquals(new Date(1000), obj.getFieldDateList().first());
                        assertEquals(new Date(2000), obj.getFieldDateList().last());
                    });
                    break;
                }
                default:
                    fail("Unknown type: " + type);
            }
        }
    }

    @Test
    public void setObject_unmanagedObjectThrows() {
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();
        realm.beginTransaction();
        try {
            collection.setObject(AllTypes.FIELD_REALMOBJECT, new Dog());
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue("Wrong error message: " + e.getMessage(), e.getMessage().contains("is not a valid, managed Realm object."));
        }
    }

    @Test
    public void setObject_wrongObjectTypeThrows() {
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();
        realm.beginTransaction();
        try {
            collection.setObject(AllTypes.FIELD_REALMOBJECT, realm.createObject(AllTypes.class));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue("Wrong error message: " + e.getMessage(), e.getMessage().equals("Type of object is wrong. Was 'AllTypes', expected 'Dog'"));
        } finally {
            realm.cancelTransaction();
        }

        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
        RealmResults<DynamicRealmObject> dynamicCollection = dynamicRealm.where("AllTypes").findAll();
        dynamicRealm.beginTransaction();
        try {
            dynamicCollection.setObject(AllTypes.FIELD_REALMOBJECT, dynamicRealm.createObject("AllTypes"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue("Wrong error message: " + e.getMessage(), e.getMessage().equals("Type of object is wrong. Was 'AllTypes', expected 'Dog'"));
        } finally {
            dynamicRealm.close();
        }
    }

    @Test
    public void setList_unmanagedObjectThrows() {
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();
        realm.beginTransaction();
        try {
            collection.setList(AllTypes.FIELD_REALMLIST, new RealmList<>(new Dog()));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue("Wrong error message: " + e.getMessage(), e.getMessage().contains("is not a valid, managed Realm object."));
        }
    }

    @Test
    public void setList_wrongObjectTypeThrows() {
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();
        realm.beginTransaction();
        try {
            collection.setList(AllTypes.FIELD_REALMLIST, new RealmList<>(realm.createObject(AllTypes.class)));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue("Wrong error message: " + e.getMessage(), e.getMessage().equals("Type of object is wrong. Was 'AllTypes', expected 'Dog'"));
        } finally {
            realm.cancelTransaction();
        }

        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
        RealmResults<DynamicRealmObject> dynamicCollection = dynamicRealm.where("AllTypes").findAll();
        dynamicRealm.beginTransaction();
        try {
            dynamicCollection.setList(AllTypes.FIELD_REALMLIST, new RealmList<>(dynamicRealm.createObject("AllTypes")));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue("Wrong error message: " + e.getMessage(), e.getMessage().equals("Type of object is wrong. Was 'AllTypes', expected 'Dog'"));
        } finally {
            dynamicRealm.close();
        }
    }

    @Test
    public void setValue_specificType_wrongFieldNameThrows() {
        populateAllJavaTypes(5);
        RealmResults<AllTypes> collection = realm.where(AllTypes.class).findAll();
        realm.beginTransaction();
        for (BulkSetMethods type : BulkSetMethods.values()) {
            try {
                switch(type) {
                    case STRING: collection.setString("foo", "bar"); break;
                    case BOOLEAN: collection.setBoolean("foo", true); break;
                    case BYTE: collection.setByte("foo", (byte) 1); break;
                    case SHORT: collection.setShort("foo", (short) 2); break;
                    case INTEGER: collection.setInt("foo", 3); break;
                    case LONG: collection.setLong("foo", 4L); break;
                    case FLOAT: collection.setFloat("foo", 1.23F); break;
                    case DOUBLE: collection.setDouble("foo", 1.234); break;
                    case BINARY: collection.setBlob("foo", new byte[]{1,2,3}); break;
                    case DATE: collection.setDate("foo", new Date(1000)); break;
                    case OBJECT: collection.setObject("foo", realm.createObject(AllTypes.class)); break;
                    case MODEL_LIST: collection.setList("foo", new RealmList<>()); break;
                    case STRING_VALUE_LIST: collection.setList("foo", new RealmList<>("Foo")); break;
                    case BOOLEAN_VALUE_LIST: collection.setList("foo", new RealmList<>(true)); break;
                    case BYTE_VALUE_LIST: collection.setList("foo", new RealmList<>((byte) 1)); break;
                    case SHORT_VALUE_LIST: collection.setList("foo", new RealmList<>((short) 1)); break;
                    case INTEGER_VALUE_LIST: collection.setList("foo", new RealmList<>(1)); break;
                    case LONG_VALUE_LIST: collection.setList("foo", new RealmList<>(1L)); break;
                    case FLOAT_VALUE_LIST: collection.setList("foo", new RealmList<>(1.1F)); break;
                    case DOUBLE_VALUE_LIST: collection.setList("foo", new RealmList<>(1.1D)); break;
                    case BINARY_VALUE_LIST: collection.setList("foo", new RealmList<>(new byte[] {})); break;
                    case DATE_VALUE_LIST: collection.setList("foo", new RealmList<>(new Date())); break;
                    default:
                        fail("Unknown type: " + type);
                }
                fail(type + " should have thrown an exception");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("does not exist"));
            }
        }
    }

    @Test
    public void setValue_specificType_wrongTypeThrows() {
        populateAllJavaTypes(5);
        RealmResults<AllJavaTypes> collection = realm.where(AllJavaTypes.class).findAll();
        realm.beginTransaction();
        for (BulkSetMethods type : BulkSetMethods.values()) {
            try {
                switch(type) {
                    case STRING: collection.setString(AllJavaTypes.FIELD_BOOLEAN, "foo"); break;
                    case BOOLEAN: collection.setBoolean(AllJavaTypes.FIELD_STRING, true); break;
                    case BYTE: collection.setByte(AllJavaTypes.FIELD_STRING, (byte) 1); break;
                    case SHORT: collection.setShort(AllJavaTypes.FIELD_STRING, (short) 2); break;
                    case INTEGER: collection.setInt(AllJavaTypes.FIELD_STRING, 3); break;
                    case LONG:collection.setLong(AllJavaTypes.FIELD_STRING, 4L); break;
                    case FLOAT: collection.setFloat(AllJavaTypes.FIELD_STRING, 1.23F); break;
                    case DOUBLE: collection.setDouble(AllJavaTypes.FIELD_STRING, 1.234); break;
                    case BINARY: collection.setBlob(AllJavaTypes.FIELD_STRING, new byte[]{1,2,3}); break;
                    case DATE: collection.setDate(AllJavaTypes.FIELD_STRING, new Date(1000)); break;
                    case OBJECT: collection.setObject(AllJavaTypes.FIELD_STRING, realm.createObject(AllJavaTypes.class, 42)); break;
                    case MODEL_LIST: collection.setList(AllJavaTypes.FIELD_STRING, new RealmList<>(realm.createObject(AllJavaTypes.class, 43))); break;
                    case STRING_VALUE_LIST: collection.setList(AllJavaTypes.FIELD_STRING, new RealmList<>("Foo")); break;
                    case BOOLEAN_VALUE_LIST: collection.setList(AllJavaTypes.FIELD_STRING, new RealmList<>(true)); break;
                    case BYTE_VALUE_LIST: collection.setList(AllJavaTypes.FIELD_STRING, new RealmList<>((byte)1)); break;
                    case SHORT_VALUE_LIST: collection.setList(AllJavaTypes.FIELD_STRING, new RealmList<>((short)1)); break;
                    case INTEGER_VALUE_LIST: collection.setList(AllJavaTypes.FIELD_STRING, new RealmList<>(1)); break;
                    case LONG_VALUE_LIST: collection.setList(AllJavaTypes.FIELD_STRING, new RealmList<>(1L)); break;
                    case FLOAT_VALUE_LIST: collection.setList(AllJavaTypes.FIELD_STRING, new RealmList<>(1.1F)); break;
                    case DOUBLE_VALUE_LIST: collection.setList(AllJavaTypes.FIELD_STRING, new RealmList<>(2.2D)); break;
                    case BINARY_VALUE_LIST: collection.setList(AllJavaTypes.FIELD_STRING, new RealmList<>(new byte[]{})); break;
                    case DATE_VALUE_LIST: collection.setList(AllJavaTypes.FIELD_STRING, new RealmList<>(new Date())); break;
                    default:
                        fail("Unknown type: " + type);
                }
                fail(type + " should have thrown an exception");
            } catch (IllegalArgumentException e) {
                RealmLog.error(type + " -> " + e.getMessage());
                assertTrue(type + " failed", e.getMessage().contains("is not of the expected type")
                        || e.getMessage().contains("List contained the wrong type of elements")
                        || e.getMessage().contains("is not a list"));
            }
        }
    }

    @Test
    public void setValue_specificType_primaryKeyFieldThrows() {
        populateAllJavaTypes(5);
        realm.beginTransaction();
        try {
            RealmResults<PrimaryKeyAsString> collection = realm.where(PrimaryKeyAsString.class).findAll();
            collection.setString(PrimaryKeyAsString.FIELD_PRIMARY_KEY, "foo");
            fail();
        } catch (IllegalStateException ignore) {
        }

        try {
            RealmResults<PrimaryKeyAsLong> collection = realm.where(PrimaryKeyAsLong.class).findAll();
            collection.setLong(PrimaryKeyAsLong.FIELD_ID, 42);
            fail();
        } catch (IllegalStateException ignore) {
        }
    }

    @Test
    public void setValue_specificType_modelClassNameOnTypedRealms() {
        populateMappedAllJavaTypes(5);
        RealmResults<MappedAllJavaTypes> collection = realm.where(MappedAllJavaTypes.class).findAll();
        realm.beginTransaction();
        for (BulkSetMethods type : BulkSetMethods.values()) {
            switch(type) {
                case STRING:
                    collection.setString("fieldString", "foo");
                    assertElements(collection, obj -> assertEquals("foo", obj.fieldString));
                    break;
                case BOOLEAN:
                    collection.setBoolean("fieldBoolean", true);
                    assertElements(collection, obj -> assertTrue(obj.fieldBoolean));
                    break;
                case BYTE:
                    collection.setByte("fieldByte", (byte) 1);
                    assertElements(collection, obj -> assertEquals((byte) 1, obj.fieldByte));
                    break;
                case SHORT:
                    collection.setShort("fieldShort", (short) 2);
                    assertElements(collection, obj -> assertEquals((short) 2, obj.fieldShort));
                    break;
                case INTEGER:
                    collection.setInt("fieldInt", 3);
                    assertElements(collection, obj -> assertEquals(3, obj.fieldInt));
                    break;
                case LONG:
                    collection.setLong("fieldLong", 4L);
                    assertElements(collection, obj -> assertEquals(4L, obj.fieldLong));
                    break;
                case FLOAT:
                    collection.setFloat("fieldFloat", 1.23F);
                    assertElements(collection, obj -> assertEquals(1.23F, obj.fieldFloat, 0F));
                    break;
                case DOUBLE:
                    collection.setDouble("fieldDouble", 1.234);
                    assertElements(collection, obj -> assertEquals(1.234, obj.fieldDouble, 0F));
                    break;
                case BINARY:
                    collection.setBlob("fieldBinary", new byte[]{1,2,3});
                    assertElements(collection, obj -> assertArrayEquals(new byte[]{1,2,3}, obj.fieldBinary));
                    break;
                case DATE:
                    collection.setDate("fieldDate", new Date(1000));
                    assertElements(collection, obj -> assertEquals(new Date(1000), obj.fieldDate));
                    break;
                case OBJECT: {
                    MappedAllJavaTypes childObj = realm.createObject(MappedAllJavaTypes.class, 42);
                    collection.setObject("fieldObject", childObj);
                    assertElements(collection, obj -> assertEquals(childObj, obj.fieldObject));
                    break;
                }
                case MODEL_LIST: {
                    MappedAllJavaTypes childObj = realm.createObject(MappedAllJavaTypes.class, 43);
                    collection.setList("fieldList", new RealmList<>(childObj));
                    assertElements(collection, obj -> {
                        assertEquals(1, obj.fieldList.size());
                        assertEquals(childObj, obj.fieldList.first());
                    });
                    break;
                }
                case STRING_VALUE_LIST:
                    collection.setList("fieldStringList", new RealmList<>("Foo"));
                    assertElements(collection, obj -> {
                        assertEquals(1, obj.fieldStringList.size());
                        assertEquals("Foo", obj.fieldStringList.first());
                    });
                    break;
                case BOOLEAN_VALUE_LIST:
                    collection.setList("fieldBooleanList", new RealmList<>(true));
                    assertElements(collection, obj -> {
                        assertEquals(1, obj.fieldBooleanList.size());
                        assertEquals(true, obj.fieldBooleanList.first());
                    });
                    break;
                case BYTE_VALUE_LIST:
                    collection.setList("fieldByteList", new RealmList<>((byte)1));
                    assertElements(collection, obj -> {
                        assertEquals(1, obj.fieldByteList.size());
                        assertEquals(Byte.valueOf((byte) 1), obj.fieldByteList.first());
                    });
                    break;
                case SHORT_VALUE_LIST:
                    collection.setList("fieldShortList", new RealmList<>((short)1));
                    assertElements(collection, obj -> {
                        assertEquals(1, obj.fieldShortList.size());
                        assertEquals(Short.valueOf((short) 1), obj.fieldShortList.first());
                    });
                    break;
                case INTEGER_VALUE_LIST:
                    collection.setList("fieldIntegerList", new RealmList<>(1));
                    assertElements(collection, obj -> {
                        assertEquals(1, obj.fieldIntegerList.size());
                        assertEquals(Integer.valueOf(1), obj.fieldIntegerList.first());
                    });
                    break;
                case LONG_VALUE_LIST:
                    collection.setList("fieldLongList", new RealmList<>(1L));
                    assertElements(collection, obj -> {
                        assertEquals(1, obj.fieldLongList.size());
                        assertEquals(Long.valueOf((byte) 1), obj.fieldLongList.first());
                    });
                    break;
                case FLOAT_VALUE_LIST:
                    collection.setList("fieldFloatList", new RealmList<>(1.1F));
                    assertElements(collection, obj -> {
                        assertEquals(1, obj.fieldFloatList.size());
                        assertEquals(1.1F, obj.fieldFloatList.first(), 0F);
                    });
                    break;
                case DOUBLE_VALUE_LIST:
                    collection.setList("fieldDoubleList", new RealmList<>(1.1D));
                    assertElements(collection, obj -> {
                        assertEquals(1, obj.fieldDoubleList.size());
                        assertEquals(1.1D, obj.fieldDoubleList.first(), 0F);
                    });
                    break;
                case BINARY_VALUE_LIST:
                    collection.setList("fieldBinaryList", new RealmList<>(new byte[] {1,2,3}));
                    assertElements(collection, obj -> {
                        assertEquals(1, obj.fieldBinaryList.size());
                        assertArrayEquals(new byte[] {1,2,3}, obj.fieldBinaryList.first());
                    });
                    break;
                case DATE_VALUE_LIST:
                    collection.setList("fieldDateList", new RealmList<>(new Date(1000)));
                    assertElements(collection, obj -> {
                        assertEquals(1, obj.fieldDateList.size());
                        assertEquals(new Date(1000), obj.fieldDateList.first());
                    });
                    break;
                default:
                    fail("Unknown type: " + type);
            }
        }
    }

    @Test
    public void setValue_specificType_internalNameOnDynamicRealms() {
        populateMappedAllJavaTypes(5);
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
        dynamicRealm.beginTransaction();
        try {
            RealmResults<DynamicRealmObject> collection = dynamicRealm.where("MappedAllJavaTypes").findAll();
            for (BulkSetMethods type : BulkSetMethods.values()) {
                switch(type) {
                    case STRING:
                        collection.setString("field_string", "foo");
                        assertElements(collection, obj -> assertEquals("foo", obj.getString("field_string")));
                        break;
                    case BOOLEAN:
                        collection.setBoolean("field_boolean", true);
                        assertElements(collection, obj -> assertTrue(obj.getBoolean("field_boolean")));
                        break;
                    case BYTE:
                        collection.setByte("field_byte", (byte) 1);
                        assertElements(collection, obj -> assertEquals((byte) 1, obj.getByte("field_byte")));
                        break;
                    case SHORT:
                        collection.setShort("field_short", (short) 2);
                        assertElements(collection, obj -> assertEquals((short) 2, obj.getShort("field_short")));
                        break;
                    case INTEGER:
                        collection.setInt("field_int", 3);
                        assertElements(collection, obj -> assertEquals(3, obj.getInt("field_int")));
                        break;
                    case LONG:
                        collection.setLong("field_long", 4L);
                        assertElements(collection, obj -> assertEquals(4L, obj.getLong("field_long")));
                        break;
                    case FLOAT:
                        collection.setFloat("field_float", 1.23F);
                        assertElements(collection, obj -> assertEquals(1.23F, obj.getFloat("field_float"), 0F));
                        break;
                    case DOUBLE:
                        collection.setDouble("field_double", 1.234);
                        assertElements(collection, obj -> assertEquals(1.234, obj.getDouble("field_double"), 0F));
                        break;
                    case BINARY:
                        collection.setBlob("field_binary", new byte[]{1,2,3});
                        assertElements(collection, obj -> assertArrayEquals(new byte[]{1,2,3}, obj.getBlob("field_binary")));
                        break;
                    case DATE:
                        collection.setDate("field_date", new Date(1000));
                        assertElements(collection, obj -> assertEquals(new Date(1000), obj.getDate("field_date")));
                        break;
                    case OBJECT: {
                        DynamicRealmObject childObj = dynamicRealm.createObject("MappedAllJavaTypes", 42);
                        collection.setObject("field_object", childObj);
                        assertElements(collection, obj -> assertEquals(childObj, obj.getObject("field_object")));
                        break;
                    }
                    case MODEL_LIST: {
                        DynamicRealmObject childObj = dynamicRealm.createObject("MappedAllJavaTypes", 43);
                        collection.setList("field_list", new RealmList<>(childObj));
                        assertElements(collection, obj -> {
                            RealmList<DynamicRealmObject> list = obj.getList("field_list");
                            assertEquals(1, list.size());
                            assertEquals(childObj, list.first());
                        });
                        break;
                    }
                    case STRING_VALUE_LIST:
                        collection.setList("field_string_list", new RealmList<>("Foo"));
                        assertElements(collection, obj -> {
                            RealmList<String> list = obj.getList("field_string_list", String.class);
                            assertEquals(1, list.size());
                            assertEquals("Foo", list.first());
                        });
                        break;
                    case BOOLEAN_VALUE_LIST:
                        collection.setList("field_boolean_list", new RealmList<>(true));
                        assertElements(collection, obj -> {
                            RealmList<Boolean> list = obj.getList("field_boolean_list", Boolean.class);
                            assertEquals(1, list.size());
                            assertEquals(true, list.first());
                        });
                        break;
                    case BYTE_VALUE_LIST:
                        collection.setList("field_byte_list", new RealmList<>((byte)1));
                        assertElements(collection, obj -> {
                            RealmList<Byte> list = obj.getList("field_byte_list", Byte.class);
                            assertEquals(1, list.size());
                            assertEquals(Byte.valueOf((byte) 1), list.first());
                        });
                        break;
                    case SHORT_VALUE_LIST:
                        collection.setList("field_short_list", new RealmList<>((short)1));
                        assertElements(collection, obj -> {
                            RealmList<Short> list = obj.getList("field_short_list", Short.class);
                            assertEquals(1, list.size());
                            assertEquals(Short.valueOf((short) 1), list.first());
                        });
                        break;
                    case INTEGER_VALUE_LIST:
                        collection.setList("field_integer_list", new RealmList<>(1));
                        assertElements(collection, obj -> {
                            RealmList<Integer> list = obj.getList("field_integer_list", Integer.class);
                            assertEquals(1, list.size());
                            assertEquals(Integer.valueOf(1), list.first());
                        });
                        break;
                    case LONG_VALUE_LIST:
                        collection.setList("field_long_list", new RealmList<>(1L));
                        assertElements(collection, obj -> {
                            RealmList<Long> list = obj.getList("field_long_list", Long.class);
                            assertEquals(1, list.size());
                            assertEquals(Long.valueOf((byte) 1), list.first());
                        });
                        break;
                    case FLOAT_VALUE_LIST:
                        collection.setList("field_float_list", new RealmList<>(1.1F));
                        assertElements(collection, obj -> {
                            RealmList<Float> list = obj.getList("field_float_list", Float.class);
                            assertEquals(1, list.size());
                            assertEquals(1.1F, list.first(), 0F);
                        });
                        break;
                    case DOUBLE_VALUE_LIST:
                        collection.setList("field_double_list", new RealmList<>(1.1D));
                        assertElements(collection, obj -> {
                            RealmList<Double> list = obj.getList("field_double_list", Double.class);
                            assertEquals(1, list.size());
                            assertEquals(1.1D, list.first(), 0F);
                        });
                        break;
                    case BINARY_VALUE_LIST:
                        collection.setList("field_binary_list", new RealmList<>(new byte[] {1,2,3}));
                        assertElements(collection, obj -> {
                            RealmList<byte[]> list = obj.getList("field_binary_list", byte[].class);
                            assertEquals(1, list.size());
                            assertArrayEquals(new byte[] {1,2,3}, list.first());
                        });
                        break;
                    case DATE_VALUE_LIST:
                        collection.setList("field_date_list", new RealmList<>(new Date(1000)));
                        assertElements(collection, obj -> {
                            RealmList<Date> list = obj.getList("field_date_list", Date.class);
                            assertEquals(1, list.size());
                            assertEquals(new Date(1000), list.first());
                        });
                        break;
                    default:
                        fail("Unknown type: " + type);
                }
            }
        } finally {
            dynamicRealm.close();
        }
    }

    @Test
    public void asJSON() throws JSONException {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT")); // Core return dates in UTC time
        String now = sdf.format(date);

        realm.beginTransaction();

        AllTypes allTypes = realm.createObject(AllTypes.class);
        Dog dog1 = realm.createObject(Dog.class);
        Dog dog2 = realm.createObject(Dog.class);
        Dog dog3 = realm.createObject(Dog.class);

        dog1.setName("dog1");
        dog1.setAge(1);
        dog1.setBirthday(date);
        dog1.setHasTail(true);
        dog1.setHeight(1.1f);
        dog1.setWeight(10.1f);

        dog2.setName("dog2");
        dog2.setAge(2);
        dog2.setBirthday(date);
        dog2.setHasTail(false);
        dog2.setHeight(2.1f);
        dog2.setWeight(20.1f);

        dog3.setName("dog3");
        dog3.setAge(3);
        dog3.setBirthday(date);
        dog3.setHasTail(true);
        dog3.setHeight(3.1f);
        dog3.setWeight(30.1f);

        Owner owner = realm.createObject(Owner.class);
        owner.setName("Dog owner 1");
        dog3.setOwner(owner);

        allTypes.setColumnString("alltypes1");
        allTypes.setColumnLong(1337L);
        allTypes.setColumnFloat(3.14f);
        allTypes.setColumnDouble(0.89123);
        allTypes.setColumnBoolean(false);
        allTypes.setColumnDate(date);
        allTypes.setColumnBinary(new byte[]{1, 2, 3});
        allTypes.setColumnRealmObject(dog1);
        allTypes.getColumnRealmList().add(dog2);
        allTypes.getColumnRealmList().add(dog3);
        allTypes.getColumnStringList().add("Foo");
        allTypes.getColumnStringList().add("Bar");
        allTypes.getColumnBooleanList().add(false);
        allTypes.getColumnBooleanList().add(true);
        allTypes.getColumnLongList().add(1000L);
        allTypes.getColumnLongList().add(2000L);
        allTypes.getColumnDoubleList().add(1.123);
        allTypes.getColumnDoubleList().add(5.321);
        allTypes.getColumnFloatList().add(0.12f);
        allTypes.getColumnFloatList().add(0.13f);
        allTypes.getColumnDateList().add(date);
        allTypes.getColumnDateList().add(date);

        AllTypes allTypes2 = realm.createObject(AllTypes.class);
        allTypes2.setColumnString("alltypes2");
        realm.commitTransaction();

        RealmResults<AllTypes> all = realm.where(AllTypes.class)
                .equalTo("columnString", "alltypes1").findAll();
        assertEquals(1, all.size());
        String json = all.asJSON();
        final String expectedJSON = "[\n" +
                "    {\n" +
                "        \"columnString\": \"alltypes1\",\n" +
                "        \"columnLong\": 1337,\n" +
                "        \"columnFloat\": 3.1400001,\n" +
                "        \"columnDouble\": 0.89122999999999997,\n" +
                "        \"columnBoolean\": false,\n" +
                "        \"columnDate\": \"" + now + "\",\n" +
                "        \"columnBinary\": \"010203\",\n" +
                "        \"columnMutableRealmInteger\": 0,\n" +
                "        \"columnRealmObject\": [\n" +
                "            {\n" +
                "                \"name\": \"dog1\",\n" +
                "                \"age\": 1,\n" +
                "                \"height\": 1.1,\n" +
                "                \"weight\": 10.100000381469727,\n" +
                "                \"hasTail\": true,\n" +
                "                \"birthday\": \"" + now + "\",\n" +
                "                \"owner\": []\n" +
                "            }\n" +
                "        ],\n" +
                "        \"columnRealmList\": [\n" +
                "            {\n" +
                "                \"name\": \"dog2\",\n" +
                "                \"age\": 2,\n" +
                "                \"height\": 2.0999999,\n" +
                "                \"weight\": 20.100000381469727,\n" +
                "                \"hasTail\": false,\n" +
                "                \"birthday\": \"" + now + "\",\n" +
                "                \"owner\": []\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"dog3\",\n" +
                "                \"age\": 3,\n" +
                "                \"height\": 3.0999999,\n" +
                "                \"weight\": 30.100000381469727,\n" +
                "                \"hasTail\": true,\n" +
                "                \"birthday\": \"" + now + "\",\n" +
                "                \"owner\": [\n" +
                "                    {\n" +
                "                        \"name\": \"Dog owner 1\",\n" +
                "                        \"dogs\": [],\n" +
                "                        \"cat\": []\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"columnStringList\": [\n" +
                "            {\n" +
                "                \"!ARRAY_VALUE\": \"Foo\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"!ARRAY_VALUE\": \"Bar\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"columnBinaryList\": [],\n" +
                "        \"columnBooleanList\": [\n" +
                "            {\n" +
                "                \"!ARRAY_VALUE\": false\n" +
                "            },\n" +
                "            {\n" +
                "                \"!ARRAY_VALUE\": true\n" +
                "            }\n" +
                "        ],\n" +
                "        \"columnLongList\": [\n" +
                "            {\n" +
                "                \"!ARRAY_VALUE\": 1000\n" +
                "            },\n" +
                "            {\n" +
                "                \"!ARRAY_VALUE\": 2000\n" +
                "            }\n" +
                "        ],\n" +
                "        \"columnDoubleList\": [\n" +
                "            {\n" +
                "                \"!ARRAY_VALUE\": 1.123\n" +
                "            },\n" +
                "            {\n" +
                "                \"!ARRAY_VALUE\": 5.3209999999999997\n" +
                "            }\n" +
                "        ],\n" +
                "        \"columnFloatList\": [\n" +
                "            {\n" +
                "                \"!ARRAY_VALUE\": 0.12\n" +
                "            },\n" +
                "            {\n" +
                "                \"!ARRAY_VALUE\": 0.13\n" +
                "            }\n" +
                "        ],\n" +
                "        \"columnDateList\": [\n" +
                "            {\n" +
                "                \"!ARRAY_VALUE\": \"" + now + "\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"!ARRAY_VALUE\": \"" + now + "\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "]";
        JSONAssert.assertEquals(expectedJSON, json, false);
    }

    @Test
    public void asJSON_cycles() throws JSONException {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT")); // Core return dates in UTC time
        String now = sdf.format(date);

        CyclicType oneCyclicType = new CyclicType();
        oneCyclicType.setName("One");
        oneCyclicType.setDate(date);

        CyclicType anotherCyclicType = new CyclicType();
        anotherCyclicType.setName("Two");
        anotherCyclicType.setDate(date);

        oneCyclicType.setObject(anotherCyclicType);
        anotherCyclicType.setObject(oneCyclicType);

        realm.beginTransaction();
        realm.insert(Arrays.asList(oneCyclicType, anotherCyclicType));
        realm.commitTransaction();

        RealmResults<CyclicType> realmObjects = realm.where(CyclicType.class).sort(CyclicType.FIELD_NAME).findAll();
        assertEquals(2, realmObjects.size());
        String json = realmObjects.asJSON();
        String expectedJSON = "[\n" +
                "    {\n" +
                "        \"id\": 0,\n" +
                "        \"name\": \"One\",\n" +
                "        \"date\": \"" + now + "\",\n" +
                "        \"object\": [\n" +
                "            {\n" +
                "                \"id\": 0,\n" +
                "                \"name\": \"Two\",\n" +
                "                \"date\": \"" + now + "\",\n" +
                "                \"object\": \"0\",\n" +
                "                \"otherObject\": [],\n" +
                "                \"objects\": []\n" +
                "            }\n" +
                "        ],\n" +
                "        \"otherObject\": [],\n" +
                "        \"objects\": []\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 0,\n" +
                "        \"name\": \"Two\",\n" +
                "        \"date\": \"" + now + "\",\n" +
                "        \"object\": [\n" +
                "            {\n" +
                "                \"id\": 0,\n" +
                "                \"name\": \"One\",\n" +
                "                \"date\": \"" + now + "\",\n" +
                "                \"object\": \"1\",\n" +
                "                \"otherObject\": [],\n" +
                "                \"objects\": []\n" +
                "            }\n" +
                "        ],\n" +
                "        \"otherObject\": [],\n" +
                "        \"objects\": []\n" +
                "    }\n" +
                "]";
        JSONAssert.assertEquals(expectedJSON, json, false);
    }

}
