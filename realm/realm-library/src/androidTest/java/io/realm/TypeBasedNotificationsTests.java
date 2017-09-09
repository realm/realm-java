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

import android.content.Context;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesPrimaryKey;
import io.realm.entities.Dog;
import io.realm.entities.PrimaryKeyAsLong;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

@RunWith(AndroidJUnit4.class)
public class TypeBasedNotificationsTests {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private AtomicInteger globalCommitInvocations;
    private AtomicInteger typebasedCommitInvocations;
    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();
        globalCommitInvocations = new AtomicInteger(0);
        typebasedCommitInvocations = new AtomicInteger(0);
    }

    // ****************************************************************************************** //
    // UC 0.
    // Callback should be notified if we create a RealmObject without the async mechanism.
    // ex: using (createObject, copyOrUpdate, createObjectFromJson etc.)
    // ***************************************************************************************** //

    //UC 0 Uses Realm.createObject.
    @Test
    @RunTestInLooperThread
    public void callback_should_trigger_for_createObject() {
        final Realm realm = looperThread.getRealm();
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                if (globalCommitInvocations.incrementAndGet() == 1) {
                    looperThread.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            assertEquals(1, typebasedCommitInvocations.get());
                            looperThread.testComplete();
                        }
                    });
                }
            }
        });

        realm.beginTransaction();
        final Dog dog = realm.createObject(Dog.class);
        realm.commitTransaction();

        looperThread.keepStrongReference(dog);
        dog.addChangeListener(new RealmChangeListener<Dog>() {
            @Override
            public void onChange(Dog object) {
                assertEquals("Akamaru", dog.getName());
                typebasedCommitInvocations.incrementAndGet();
            }
        });

        realm.beginTransaction();
        dog.setName("Akamaru");
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void callback_should_trigger_for_createObject_dynamic_realm() {
        final DynamicRealm realm = DynamicRealm.getInstance(looperThread.getConfiguration());
        looperThread.keepStrongReference(realm);
        realm.addChangeListener(new RealmChangeListener<DynamicRealm>() {
            @Override
            public void onChange(DynamicRealm object) {
                if (globalCommitInvocations.incrementAndGet() == 1) {
                    looperThread.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            realm.close();
                            assertEquals(1, typebasedCommitInvocations.get());
                            looperThread.testComplete();
                        }
                    });
                }
            }
        });

        realm.beginTransaction();
        final DynamicRealmObject dog = realm.createObject("Dog");
        realm.commitTransaction();

        looperThread.keepStrongReference(dog);
        dog.addChangeListener(new RealmChangeListener<DynamicRealmObject>() {
            @Override
            public void onChange(DynamicRealmObject object) {
                assertEquals("Akamaru", dog.getString("name"));
                typebasedCommitInvocations.incrementAndGet();
            }
        });

        realm.beginTransaction();
        dog.setString("name", "Akamaru");
        realm.commitTransaction();
    }

    //UC 0 Uses Realm.copyToRealm.
    @Test
    @RunTestInLooperThread
    public void callback_should_trigger_for_copyToRealm() {
        final Realm realm = looperThread.getRealm();
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                if (globalCommitInvocations.incrementAndGet() == 1) {
                    looperThread.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            assertEquals(1, typebasedCommitInvocations.get());
                            looperThread.testComplete();
                        }
                    });
                }
            }
        });

        realm.beginTransaction();
        Dog akamaru = new Dog();
        akamaru.setName("Akamaru");
        final Dog dog = realm.copyToRealm(akamaru);
        realm.commitTransaction();

        looperThread.keepStrongReference(dog);
        dog.addChangeListener(new RealmChangeListener<Dog>() {
            @Override
            public void onChange(Dog object) {
                assertEquals(8, dog.getAge());
                typebasedCommitInvocations.incrementAndGet();
            }
        });

        realm.beginTransaction();
        dog.setAge(8);
        realm.commitTransaction();
    }

    //UC 0 Uses Realm.copyToRealmOrUpdate.
    @Test
    @RunTestInLooperThread
    public void callback_should_trigger_for_copyToRealmOrUpdate() {
        final Realm realm = looperThread.getRealm();
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                if (globalCommitInvocations.incrementAndGet() == 1) {
                    looperThread.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            assertEquals(1, typebasedCommitInvocations.get());
                            looperThread.testComplete();
                        }
                    });
                }
            }
        });

        PrimaryKeyAsLong obj = new PrimaryKeyAsLong();
        obj.setId(1);
        obj.setName("Foo");

        realm.beginTransaction();
        final PrimaryKeyAsLong primaryKeyAsLong = realm.copyToRealmOrUpdate(obj);
        realm.commitTransaction();

        looperThread.keepStrongReference(primaryKeyAsLong);
        primaryKeyAsLong.addChangeListener(new RealmChangeListener<PrimaryKeyAsLong>() {
            @Override
            public void onChange(PrimaryKeyAsLong object) {
                assertEquals(1, primaryKeyAsLong.getId());
                assertEquals("Bar", primaryKeyAsLong.getName());
                assertEquals(1, realm.where(PrimaryKeyAsLong.class).count());
                typebasedCommitInvocations.incrementAndGet();
            }
        });

        PrimaryKeyAsLong obj2 = new PrimaryKeyAsLong();
        obj2.setId(1);
        obj2.setName("Bar");
        realm.beginTransaction();
        PrimaryKeyAsLong primaryKeyAsLong2 = realm.copyToRealmOrUpdate(obj2);
        realm.commitTransaction();

        assertEquals(primaryKeyAsLong, primaryKeyAsLong2);
    }

    //UC 0 Uses Realm.copyToRealmOrUpdate.
    @Test
    @RunTestInLooperThread
    public void callback_should_trigger_for_createObjectFromJson() {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        final Realm realm = looperThread.getRealm();
        try {
            InputStream in = TestHelper.loadJsonFromAssets(InstrumentationRegistry.getTargetContext(), "all_simple_types.json");
            realm.beginTransaction();
            final AllTypes objectFromJson = realm.createObjectFromJson(AllTypes.class, in);
            realm.commitTransaction();
            in.close();

            looperThread.keepStrongReference(objectFromJson);
            objectFromJson.addChangeListener(new RealmChangeListener<AllTypes>() {
                @Override
                public void onChange(AllTypes object) {
                    assertEquals("ObjectFromJson", objectFromJson.getColumnString());
                    assertEquals(1L, objectFromJson.getColumnLong());
                    assertEquals(1.23F, objectFromJson.getColumnFloat(), 0F);
                    assertEquals(1.23D, objectFromJson.getColumnDouble(), 0D);
                    assertEquals(true, objectFromJson.isColumnBoolean());
                    assertArrayEquals(new byte[]{1, 2, 3}, objectFromJson.getColumnBinary());
                    looperThread.testComplete();
                }
            });

            realm.beginTransaction();
            objectFromJson.setColumnString("ObjectFromJson");
            realm.commitTransaction();

        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    //UC 0 Uses Realm.copyToRealmOrUpdate.
    @Test
    @RunTestInLooperThread
    public void callback_should_trigger_for_createObjectFromJson_from_JSONObject() {
        final Realm realm = looperThread.getRealm();

        try {
            JSONObject json = new JSONObject();
            json.put("columnString", "String");
            json.put("columnLong", 1L);
            json.put("columnFloat", 1.23f);
            json.put("columnDouble", 1.23d);
            json.put("columnBoolean", true);
            json.put("columnBinary", new String(Base64.encode(new byte[]{1, 2, 3}, Base64.DEFAULT), UTF_8));

            realm.beginTransaction();
            final AllTypes objectFromJson = realm.createObjectFromJson(AllTypes.class, json);
            realm.commitTransaction();

            looperThread.keepStrongReference(objectFromJson);
            objectFromJson.addChangeListener(new RealmChangeListener<AllTypes>() {
                @Override
                public void onChange(AllTypes object) {
                    assertEquals("ObjectFromJson", objectFromJson.getColumnString());
                    assertEquals(1L, objectFromJson.getColumnLong());
                    assertEquals(1.23F, objectFromJson.getColumnFloat(), 0F);
                    assertEquals(1.23D, objectFromJson.getColumnDouble(), 0D);
                    assertEquals(true, objectFromJson.isColumnBoolean());
                    assertArrayEquals(new byte[]{1, 2, 3}, objectFromJson.getColumnBinary());
                    looperThread.testComplete();
                }
            });

            realm.beginTransaction();
            objectFromJson.setColumnString("ObjectFromJson");
            realm.commitTransaction();

        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    //UC 0 Uses Realm.createOrUpdateObjectFromJson.
    @Test
    @RunTestInLooperThread
    public void callback_should_trigger_for_createOrUpdateObjectFromJson() {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.HONEYCOMB));

        final Realm realm = looperThread.getRealm();
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                if (globalCommitInvocations.incrementAndGet() == 1) {
                    looperThread.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            assertEquals(1, typebasedCommitInvocations.get());
                            looperThread.testComplete();
                        }
                    });
                }
            }
        });

        try {
            AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
            Date date = new Date(0);
            // ID
            obj.setColumnLong(1);
            obj.setColumnBinary(new byte[]{1});
            obj.setColumnBoolean(true);
            obj.setColumnDate(date);
            obj.setColumnDouble(1);
            obj.setColumnFloat(1);
            obj.setColumnString("1");
            realm.beginTransaction();
            realm.copyToRealm(obj);
            realm.commitTransaction();

            InputStream in = TestHelper.loadJsonFromAssets(context, "all_types_primary_key_field_only.json");
            realm.beginTransaction();
            final AllTypesPrimaryKey objectFromJson = realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, in);
            realm.commitTransaction();
            in.close();

            looperThread.keepStrongReference(objectFromJson);
            objectFromJson.addChangeListener(new RealmChangeListener<AllTypesPrimaryKey>() {
                @Override
                public void onChange(AllTypesPrimaryKey object) {
                    assertEquals("ObjectFromJson", objectFromJson.getColumnString());
                    assertEquals(1L, objectFromJson.getColumnLong());
                    assertEquals(1F, objectFromJson.getColumnFloat(), 0F);
                    assertEquals(1D, objectFromJson.getColumnDouble(), 0D);
                    assertEquals(true, objectFromJson.isColumnBoolean());
                    assertArrayEquals(new byte[]{1}, objectFromJson.getColumnBinary());
                    assertNull(objectFromJson.getColumnRealmObject());
                    assertEquals(0, objectFromJson.getColumnRealmList().size());
                    typebasedCommitInvocations.incrementAndGet();
                }
            });

            realm.beginTransaction();
            objectFromJson.setColumnString("ObjectFromJson");
            realm.commitTransaction();

        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    //UC 0 Uses Realm.copyToRealmOrUpdate.
    @Test
    @RunTestInLooperThread
    public void callback_should_trigger_for_createOrUpdateObjectFromJson_from_JSONObject() throws JSONException {
        final Realm realm = looperThread.getRealm();
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                looperThread.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        assertEquals(1, typebasedCommitInvocations.get());
                        looperThread.testComplete();
                    }
                });
            }
        });

        AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
        obj.setColumnLong(1);
        obj.setColumnString("Foo");

        realm.beginTransaction();
        realm.copyToRealm(obj);
        realm.commitTransaction();


        JSONObject json = new JSONObject();
        json.put("columnLong", 1);
        json.put("columnString", "bar");

        realm.beginTransaction();
        final AllTypesPrimaryKey newObj = realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, json);
        realm.commitTransaction();

        looperThread.keepStrongReference(newObj);
        newObj.addChangeListener(new RealmChangeListener<AllTypesPrimaryKey>() {
            @Override
            public void onChange(AllTypesPrimaryKey object) {
                assertEquals(1, realm.where(AllTypesPrimaryKey.class).count());
                assertEquals("bar", newObj.getColumnString());
                assertTrue(newObj.getColumnBoxedBoolean());
                typebasedCommitInvocations.incrementAndGet();
            }
        });

        realm.beginTransaction();
        newObj.setColumnBoxedBoolean(Boolean.TRUE);
        realm.commitTransaction();
    }

    // ********************************************************************************* //
    // UC 1.
    // Callback should be invoked after a relevant commit. (one that should impact the
    // query from which we obtained our RealmObject or RealmResults.)
    // ********************************************************************************* //
    // UC 1 for Sync RealmObject
    @Test
    @RunTestInLooperThread
    public void callback_with_relevant_commit_realmobject_sync() {
        final Realm realm = looperThread.getRealm();

        // Step 1: Creates object
        realm.beginTransaction();
        final Dog akamaru = realm.createObject(Dog.class);
        akamaru.setName("Akamaru");
        realm.commitTransaction();

        final Dog dog = realm.where(Dog.class).findFirst();
        looperThread.keepStrongReference(dog);
        dog.addChangeListener(new RealmChangeListener<Dog>() {
            @Override
            public void onChange(Dog object) {
                // Step 3: Responds to relevant change.
                typebasedCommitInvocations.incrementAndGet();
                assertEquals("Akamaru", dog.getName());
                assertEquals(17, dog.getAge());
                looperThread.testComplete();
            }
        });

        // Step 2: Trigger non-related commit
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
            }
        });

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Dog.class).findFirst().setAge(17);
            }
        });
    }

    // UC 1 Async RealmObject.
    @Test
    @RunTestInLooperThread
    public void callback_with_relevant_commit_realmobject_async() {
        final Realm realm = looperThread.getRealm();

        // Step 1: Creates object.
        realm.beginTransaction();
        final Dog akamaru = realm.createObject(Dog.class);
        akamaru.setName("Akamaru");
        realm.commitTransaction();

        final Dog dog = realm.where(Dog.class).findFirstAsync();

        looperThread.keepStrongReference(dog);
        dog.addChangeListener(new RealmChangeListener<Dog>() {
            @Override
            public void onChange(Dog object) {
                switch (typebasedCommitInvocations.incrementAndGet()) {
                    case 1:
                        // Async query returns.
                        assertEquals("Akamaru", dog.getName());
                        assertEquals(0, dog.getAge());

                        // Step 2: Triggers non-related commit.
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                            }
                        });

                        // Step 3: Triggers related commit.
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.where(Dog.class).findFirst().setAge(17);
                            }
                        });
                        break;

                    case 2:
                        // Step 4: Responds to relevant change.
                        assertEquals(17, dog.getAge());
                        looperThread.testComplete();
                        break;
                    default:
                        fail();
                }
            }
        });
    }

    // UC 1 Sync RealmResults.
    @Test
    @RunTestInLooperThread
    public void callback_with_relevant_commit_realmresults_sync() {
        final Realm realm = looperThread.getRealm();

        // Step 1: Creates object.
        realm.beginTransaction();
        final Dog akamaru = realm.createObject(Dog.class);
        akamaru.setName("Akamaru");
        realm.commitTransaction();

        final RealmResults<Dog> dogs = realm.where(Dog.class).findAll();
        looperThread.keepStrongReference(dogs);
        dogs.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
            @Override
            public void onChange(RealmResults<Dog> object) {
                // Step 4: Responds to relevant change.
                typebasedCommitInvocations.incrementAndGet();
                assertEquals(1, dogs.size());
                assertEquals("Akamaru", dogs.get(0).getName());
                assertEquals(17, dogs.get(0).getAge());
                looperThread.testComplete();
            }
        });

        // Step 2: Trigger non-related commit. If this triggered the results listener, assertion will happen there.
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
            }
        });

        // Step 3: Triggers related commit.
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Dog.class).findFirst().setAge(17);
            }
        });
    }

    // UC 1 Async RealmResults.
    @Test
    @RunTestInLooperThread
    public void callback_with_relevant_commit_realmresults_async() {
        final Realm realm = looperThread.getRealm();

        // Step 1: Creates object.
        realm.beginTransaction();
        final Dog akamaru = realm.createObject(Dog.class);
        akamaru.setName("Akamaru");
        realm.commitTransaction();

        final RealmResults<Dog> dogs = realm.where(Dog.class).findAllAsync();
        looperThread.keepStrongReference(dogs);
        dogs.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
            @Override
            public void onChange(RealmResults<Dog> object) {
                // Step 4: Responds to relevant change.
                int commits = typebasedCommitInvocations.incrementAndGet();
                switch (commits) {
                    case 1:
                        // Async query returns.
                        assertEquals(1, dogs.size());
                        assertEquals("Akamaru", dogs.get(0).getName());
                        // Step 2: Trigger non-related commit. If this triggered the results listener,
                        // assertion will happen there.
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                            }
                        });

                        // Step 3: Triggers related commit.
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.where(Dog.class).findFirst().setAge(17);
                            }
                        });
                        break;
                    case 2:
                        assertEquals(17, dogs.get(0).getAge());
                        looperThread.testComplete();
                        break;
                    default:
                        fail();
                }
            }
        });
    }

    // ********************************************************************************* //
    // UC 2.
    // Multiple callbacks should be invoked after a relevant commit.
    // ********************************************************************************* //
    // UC 2 for Sync RealmObject.
    @Test
    @RunTestInLooperThread
    public void multiple_callbacks_should_be_invoked_realmobject_sync() {
        final int NUMBER_OF_LISTENERS = 7;
        final Realm realm = looperThread.getRealm();
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                looperThread.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        assertEquals(NUMBER_OF_LISTENERS, typebasedCommitInvocations.get());
                        looperThread.testComplete();
                    }
                });
            }
        });

        realm.beginTransaction();
        Dog akamaru = realm.createObject(Dog.class);
        realm.commitTransaction();

        Dog dog = realm.where(Dog.class).findFirst();
        looperThread.keepStrongReference(dog);
        for (int i = 0; i < NUMBER_OF_LISTENERS; i++) {
            dog.addChangeListener(new RealmChangeListener<Dog>() {
                @Override
                public void onChange(Dog object) {
                    typebasedCommitInvocations.incrementAndGet();
                }
            });
        }

        realm.beginTransaction();
        akamaru.setAge(17);
        realm.commitTransaction();
    }

    // UC 2 Async RealmObject.
    @Test
    @RunTestInLooperThread
    public void multiple_callbacks_should_be_invoked_realmobject_async() {
        final int NUMBER_OF_LISTENERS = 7;
        final Realm realm = looperThread.getRealm();

        realm.beginTransaction();
        Dog akamaru = realm.createObject(Dog.class);
        realm.commitTransaction();

        Dog dog = realm.where(Dog.class).findFirstAsync();
        assertTrue(dog.load());
        looperThread.keepStrongReference(dog);
        for (int i = 0; i < NUMBER_OF_LISTENERS; i++) {
            dog.addChangeListener(new RealmChangeListener<Dog>() {
                @Override
                public void onChange(Dog object) {
                    typebasedCommitInvocations.incrementAndGet();
                    if (typebasedCommitInvocations.get() > NUMBER_OF_LISTENERS) {
                        fail();
                    } else if (typebasedCommitInvocations.get() == NUMBER_OF_LISTENERS) {
                        // Delayed post in case the listener gets triggered more time than expected.
                        looperThread.postRunnableDelayed(new Runnable() {
                            @Override
                            public void run() {
                                looperThread.testComplete();
                            }
                        }, 500);
                    }
                }
            });
        }

        realm.beginTransaction();
        akamaru.setAge(17);
        realm.commitTransaction();
    }

    // UC 2 Sync RealmResults.
    @Test
    @RunTestInLooperThread
    public void multiple_callbacks_should_be_invoked_realmresults_sync() {
        final int NUMBER_OF_LISTENERS = 7;
        final Realm realm = looperThread.getRealm();

        realm.beginTransaction();
        Dog akamaru = realm.createObject(Dog.class);
        realm.commitTransaction();

        RealmResults<Dog> dogs = realm.where(Dog.class).findAll();
        looperThread.keepStrongReference(dogs);
        for (int i = 0; i < NUMBER_OF_LISTENERS; i++) {
            dogs.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
                @Override
                public void onChange(RealmResults<Dog> results) {
                    assertEquals(17, results.first().getAge());
                    if (typebasedCommitInvocations.incrementAndGet() == NUMBER_OF_LISTENERS) {
                        looperThread.testComplete();
                    }
                    assertTrue(typebasedCommitInvocations.get() <= NUMBER_OF_LISTENERS);
                }
            });
        }

        realm.beginTransaction();
        akamaru.setAge(17);
        realm.commitTransaction();
    }

    // UC 2 Async RealmResults.
    @Test
    @RunTestInLooperThread
    public void multiple_callbacks_should_be_invoked_realmresults_async() {
        final int NUMBER_OF_LISTENERS = 7;
        final Realm realm = looperThread.getRealm();

        realm.beginTransaction();
        Dog akamaru = realm.createObject(Dog.class);
        realm.commitTransaction();

        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                looperThread.postRunnableDelayed(new Runnable() {
                    @Override
                    public void run() {
                        assertEquals(NUMBER_OF_LISTENERS, typebasedCommitInvocations.get());
                        looperThread.testComplete();
                    }
                }, 100L /* wait for listeners in RealmResults. Next run loop is not enough. */);
            }
        });

        RealmResults<Dog> dogs = realm.where(Dog.class).findAllAsync();
        assertTrue(dogs.load());

        looperThread.keepStrongReference(dogs);
        for (int i = 0; i < NUMBER_OF_LISTENERS; i++) {
            dogs.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
                @Override
                public void onChange(RealmResults<Dog> object) {
                    typebasedCommitInvocations.incrementAndGet();
                }
            });
        }

        realm.beginTransaction();
        akamaru.setAge(17);
        realm.commitTransaction();
    }

    // ********************************************************************************* //
    // UC 3.
    // Callback should be invoked when a non Looper thread commits.
    // ********************************************************************************* //

    // UC 3 for Sync RealmObject.
    // 1. Adds listener to RealmObject which is queried synchronized.
    // 2. Commits transaction in another non-looper thread.
    // 3. Listener on the RealmObject gets triggered.
    @Test
    @RunTestInLooperThread
    public void non_looper_thread_commit_realmobject_sync() {
        final Realm realm = looperThread.getRealm();

        realm.beginTransaction();
        realm.createObject(Dog.class);
        realm.commitTransaction();

        Dog dog = realm.where(Dog.class).findFirst();
        looperThread.keepStrongReference(dog);
        dog.addChangeListener(new RealmChangeListener<Dog>() {
            @Override
            public void onChange(Dog object) {
                assertEquals(17, object.getAge());
                looperThread.testComplete();
            }
        });

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Dog.class).findFirst().setAge(17);
            }
        });
    }

    // UC 3 Async RealmObject.
    // 1. Creates RealmObject async query.
    // 2. Waits async returns then change the object.
    // 3. Listener on the RealmObject gets triggered again.
    @Test
    @RunTestInLooperThread
    public void non_looper_thread_commit_realmobject_async() {
        final Realm realm = looperThread.getRealm();

        realm.beginTransaction();
        realm.createObject(Dog.class).setAge(1);
        realm.commitTransaction();

        Dog dog = realm.where(Dog.class).findFirstAsync();
        looperThread.keepStrongReference(dog);
        dog.addChangeListener(new RealmChangeListener<Dog>() {
            @Override
            public void onChange(Dog object) {
                switch (typebasedCommitInvocations.incrementAndGet()) {
                    case 1:
                        assertEquals(1, object.getAge());
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.where(Dog.class).findFirst().setAge(17);
                            }
                        });
                        break;
                    case 2:
                        assertEquals(17, object.getAge());
                        looperThread.testComplete();
                        break;
                    default:
                        fail();
                }
            }
        });
    }

    // UC 3 Sync RealmResults.
    // 1. Adds listener to RealmResults which is queried synchronized.
    // 2. Commits transaction in another non-looper thread.
    // 3. Listener on the RealmResults gets triggered.
    @Test
    @RunTestInLooperThread
    public void non_looper_thread_commit_realmresults_sync() {
        final Realm realm = looperThread.getRealm();
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                if (realm.where(Dog.class).count() == 2) {
                    looperThread.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            assertEquals(1, typebasedCommitInvocations.get());
                            looperThread.testComplete();
                        }
                    });
                }
            }
        });

        realm.beginTransaction();
        realm.createObject(Dog.class);
        realm.commitTransaction();

        final RealmResults<Dog> dogs = realm.where(Dog.class).findAll();
        looperThread.keepStrongReference(dogs);
        dogs.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
            @Override
            public void onChange(RealmResults<Dog> object) {
                typebasedCommitInvocations.incrementAndGet();
                assertEquals(2, dogs.size());
            }
        });

        Thread thread = new Thread() {
            @Override
            public void run() {
                Realm bgRealm = Realm.getInstance(realm.getConfiguration());
                bgRealm.beginTransaction();
                bgRealm.createObject(Dog.class);
                bgRealm.commitTransaction();
                bgRealm.close();
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    // UC 3 Async RealmResults.
    // 1. Creates RealmResults async query.
    // 2. Waits COMPLETED_ASYNC_REALM_RESULTS then commits transaction in another non-looper thread.
    // 3. Listener on the RealmResults gets triggered again.
    @Test
    @RunTestInLooperThread
    public void non_looper_thread_commit_realmresults_async() {
        final Realm realm = looperThread.getRealm();
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                if (realm.where(Dog.class).count() == 2) {
                    looperThread.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            assertEquals(2, typebasedCommitInvocations.get());
                            looperThread.testComplete();
                        }
                    });
                }
            }
        });

        realm.beginTransaction();
        realm.createObject(Dog.class);
        realm.commitTransaction();

        final Thread thread = new Thread() {
            @Override
            public void run() {
                Realm bgRealm = Realm.getInstance(realm.getConfiguration());
                bgRealm.beginTransaction();
                bgRealm.createObject(Dog.class);
                bgRealm.commitTransaction();
                bgRealm.close();
            }
        };

        final RealmResults<Dog> dogs = realm.where(Dog.class).findAllAsync();
        looperThread.keepStrongReference(dogs);
        dogs.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
            @Override
            public void onChange(RealmResults<Dog> object) {
                typebasedCommitInvocations.incrementAndGet();
                if (typebasedCommitInvocations.get() == 1) {
                    // COMPLETED_ASYNC_REALM_RESULTS arrived.
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        fail(e.getMessage());
                    }
                }
            }
        });
    }

    // ****************************************************************************************** //
    // UC 4.
    // Callback should throw if registered on a non Looper thread.
    // No tests for async RealmObject & RealmResults, since those already require a Looper thread.
    // ***************************************************************************************** //

    // UC 4 for Realm
    @Test
    public void should_throw_on_non_looper_thread_realm() {
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                Realm bgRealm = Realm.getInstance(configFactory.createConfiguration());
                try {
                    bgRealm.beginTransaction();
                    bgRealm.createObject(Dog.class);
                    bgRealm.commitTransaction();

                    bgRealm.addChangeListener(new RealmChangeListener<Realm>() {
                        @Override
                        public void onChange(Realm object) {
                            fail("Callback should not be registered and invoked on a non-Looper thread");
                        }
                    });
                    fail("Callback should not be registered and invoked on a non-Looper thread");
                } catch (IllegalStateException ignored) {

                } finally {
                    bgRealm.close();
                    signalTestFinished.countDown();
                }
            }
        }.start();
        TestHelper.awaitOrFail(signalTestFinished);
    }

    // UC 4 for RealmObject.
    @Test
    public void should_throw_on_non_looper_thread_realmobject() {
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                Realm bgRealm = Realm.getInstance(configFactory.createConfiguration());
                try {
                    bgRealm.beginTransaction();
                    bgRealm.createObject(Dog.class);
                    bgRealm.commitTransaction();

                    Dog dog = bgRealm.where(Dog.class).findFirst();
                    dog.addChangeListener(new RealmChangeListener<Dog>() {
                        @Override
                        public void onChange(Dog object) {
                            fail("Callback should not be registered and invoked on a non-Looper thread");
                        }
                    });
                    fail("Callback should not be registered and invoked on a non-Looper thread");
                } catch (IllegalStateException ignored) {

                } finally {
                    bgRealm.close();
                    signalTestFinished.countDown();
                }
            }
        }.start();
        TestHelper.awaitOrFail(signalTestFinished);
    }

    // UC 4 RealmObject.
    @Test
    public void should_throw_on_non_looper_thread_realmresults() {
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                Realm bgRealm = Realm.getInstance(configFactory.createConfiguration());
                try {
                    bgRealm.beginTransaction();
                    bgRealm.createObject(Dog.class);
                    bgRealm.commitTransaction();

                    RealmResults<Dog> dogs = bgRealm.where(Dog.class).findAll();
                    dogs.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
                        @Override
                        public void onChange(RealmResults<Dog> object) {
                            fail("Callback should not be registered and invoked on a non-Looper thread");
                        }
                    });
                    fail("Callback should not be registered and invoked on a non-Looper thread");
                } catch (IllegalStateException ignored) {

                } finally {
                    bgRealm.close();
                    signalTestFinished.countDown();
                }
            }
        }.start();
        TestHelper.awaitOrFail(signalTestFinished);
    }

    // Builds a RealmResults from a RealmList, and delete the RealmList. Test the behavior of ChangeListener on the
    // "invalid" RealmResults.
    @Test
    @RunTestInLooperThread
    public void changeListener_onResultsBuiltOnDeletedLinkView() {
        final Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        for (int i = 0; i < 10; i++) {
            Dog dog = new Dog();
            dog.setName("name_" + i);
            allTypes.getColumnRealmList().add(dog);
        }
        realm.commitTransaction();

        final RealmResults<Dog> dogs =
                allTypes.getColumnRealmList().where().equalTo(Dog.FIELD_NAME, "name_0").findAll();
        looperThread.keepStrongReference(dogs);
        dogs.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
            @Override
            public void onChange(RealmResults<Dog> object) {
                if (typebasedCommitInvocations.getAndIncrement() == 0) {
                    assertTrue(dogs.isValid());
                    assertEquals(0, dogs.size());
                } else {
                    fail("This listener should only be called once.");
                }
            }
        });

        // Triggers the listener at the first time.
        realm.beginTransaction();
        allTypes.deleteFromRealm();
        realm.commitTransaction();

        // Tries to trigger the listener second time.
        realm.beginTransaction();
        realm.commitTransaction();

        // Closes the realm and finishes the test. This needs to follow the REALM_CHANGED in the queue.
        looperThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                realm.close();
                assertEquals(1, typebasedCommitInvocations.get());
                looperThread.testComplete();
            }
        });
    }

}
