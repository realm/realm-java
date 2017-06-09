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

package io.realm;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import io.realm.entities.PrimaryKeyAsBoxedByte;
import io.realm.entities.PrimaryKeyAsBoxedInteger;
import io.realm.entities.PrimaryKeyAsBoxedLong;
import io.realm.entities.PrimaryKeyAsBoxedShort;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import io.realm.objectid.NullPrimaryKey;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class RealmNullPrimaryKeyTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    protected Realm realm;

    @Before
    public void setUp() {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    /**
     * Base parameters for testing null-primary key value. The parameters are aligned in an order of
     * 1) a test target class, 2) a primary key field class, 3) a secondary field class, 4) a secondary
     * field value, and 5) an update value, accommodating {@interface NullPrimaryKey} to condense unit tests.
     */
    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
            // 1) Test target class          2) PK Class    3) 2nd Class  4) 2nd field value   5) 2nd field value for update
            {PrimaryKeyAsString.class,       String.class,  long.class,   Long.valueOf(492412), Long.valueOf(991241)},
            {PrimaryKeyAsBoxedByte.class,    Byte.class,    String.class, "This-Is-Second-One", "Gosh Didnt KnowIt"},
            {PrimaryKeyAsBoxedShort.class,   Short.class,   String.class, "AnyValueIsAccepted", "?YOUNOWKNOWRIGHT?"},
            {PrimaryKeyAsBoxedInteger.class, Integer.class, String.class, "PlayWithSeondFied!", "HaHaHaHaHaHaHaHaH"},
            {PrimaryKeyAsBoxedLong.class,    Long.class,    String.class, "Let's name a value", "KeyValueTestIsFun"}
        });
    }

    final private Class<? extends RealmObject> testClazz;
    final private Class primaryKeyFieldType;
    final private Class secondaryFieldType;
    final private Object secondaryFieldValue;
    final private Object updatingFieldValue;

    public RealmNullPrimaryKeyTests(Class<? extends RealmObject> testClazz, Class primaryKeyFieldType, Class secondaryFieldType, Object secondaryFieldValue, Object updatingFieldValue) {
        this.testClazz = testClazz;
        this.primaryKeyFieldType = primaryKeyFieldType;
        this.secondaryFieldType = secondaryFieldType;
        this.secondaryFieldValue = secondaryFieldValue;
        this.updatingFieldValue = updatingFieldValue;
    }

    // Adds a PrimaryKey object to a realm with values for its PrimaryKey field and secondary field.
    private RealmObject addPrimaryKeyObjectToTestRealm(Realm testRealm) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        RealmObject obj = (RealmObject) testClazz.getConstructor(primaryKeyFieldType, secondaryFieldType).newInstance(null, secondaryFieldValue);
        testRealm.beginTransaction();
        testRealm.copyToRealm(obj);
        testRealm.commitTransaction();
        return obj;
    }

    // Creates a RealmObject with null primarykey.
    private void createNullPrimaryKeyObjectFromTestRealm(Realm testRealm) {
        testRealm.beginTransaction();

        RealmObject obj = testRealm.createObject(testClazz, null);
        if (testClazz.equals(PrimaryKeyAsString.class)) {
            ((PrimaryKeyAsString)obj).setId((long) secondaryFieldValue);
        } else {
            ((NullPrimaryKey)obj).setName(secondaryFieldValue);
        }

        testRealm.commitTransaction();
    }

    // Updates existing null PrimaryKey object with a new updating value.
    private void updatePrimaryKeyObject(Realm testRealm, RealmObject realmObject) {
        if (testClazz.equals(PrimaryKeyAsString.class)) {
            ((PrimaryKeyAsString) realmObject).setId((long) updatingFieldValue);
        } else {
            ((NullPrimaryKey) realmObject).setName(updatingFieldValue);
        }

        testRealm.beginTransaction();
        testRealm.copyToRealmOrUpdate(realmObject);
        testRealm.commitTransaction();
    }

    // @PrimaryKey annotation accept null value properly as a primary key value for Realm version 0.89.1+.
    @Test
    public void copyToRealm_primaryKeyIsNull() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        addPrimaryKeyObjectToTestRealm(realm);

        if (testClazz.equals(PrimaryKeyAsString.class)) {
            RealmResults<PrimaryKeyAsString> results = realm.where(PrimaryKeyAsString.class).findAll();
            assertEquals(1, results.size());
            assertEquals(null, results.first().getName());
            assertEquals(secondaryFieldValue, results.first().getId());

        } else {
            RealmResults results = realm.where(testClazz).findAll();
            assertEquals(1, results.size());
            assertEquals(null, ((NullPrimaryKey) results.first()).getId());
            assertEquals(secondaryFieldValue, ((NullPrimaryKey) results.first()).getName());
        }
    }

    // @PrimaryKey annotation accept & update null value properly as a primary key value for Realm version 0.89.1+.
    @Test
    public void copyToRealmOrUpdate_primaryKeyFieldIsNull() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        RealmObject obj = addPrimaryKeyObjectToTestRealm(realm);

        if (testClazz.equals(PrimaryKeyAsString.class)) {
            RealmResults<PrimaryKeyAsString> results = realm.where(PrimaryKeyAsString.class).findAll();
            assertEquals(1, results.size());
            assertEquals(null, results.first().getName());
            assertEquals(secondaryFieldValue, results.first().getId());

        } else {
            RealmResults results = realm.where(testClazz).findAll();
            assertEquals(1, results.size());
            assertEquals(null, ((NullPrimaryKey) results.first()).getId());
            assertEquals(secondaryFieldValue, ((NullPrimaryKey) results.first()).getName());

        }

        // Commits to the Realm.
        updatePrimaryKeyObject(realm, obj);

        if (testClazz.equals(PrimaryKeyAsString.class)) {
            assertEquals(updatingFieldValue, realm.where(PrimaryKeyAsString.class).findFirst().getId());
        } else {
            assertEquals(updatingFieldValue, ((NullPrimaryKey) realm.where(testClazz).findFirst()).getName());
        }
    }

    // @PrimaryKey annotation creates null value properly as a primary key value for Realm version 0.89.1+.
    @Test
    public void createObject_primaryKeyFieldIsNull() {
        createNullPrimaryKeyObjectFromTestRealm(realm);

        if (testClazz.equals(PrimaryKeyAsString.class)) {
            RealmResults<PrimaryKeyAsString> results = realm.where(PrimaryKeyAsString.class).findAll();
            assertEquals(1, results.size());
            assertEquals(null, results.first().getName());
            assertEquals(secondaryFieldValue, results.first().getId());

        } else {
            RealmResults results = realm.where(testClazz).findAll();
            assertEquals(1, results.size());
            assertEquals(null, ((NullPrimaryKey) results.first()).getId());
            assertEquals(secondaryFieldValue, ((NullPrimaryKey) results.first()).getName());
        }
    }

    // @PrimaryKey annotation checked duplicated null value properly as a primary key value for Realm version 0.89.1+.
    @Test
    public void createObject_duplicatedNullPrimaryKeyThrows() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        addPrimaryKeyObjectToTestRealm(realm);

        realm.beginTransaction();
        try {
            realm.createObject(testClazz, null);
            fail("Null value as primary key already exists.");
        } catch (RealmPrimaryKeyConstraintException expected) {
            assertTrue("Exception message is: " + expected.getMessage(),
                    expected.getMessage().contains("Primary key value already exists: 'null' ."));
        } finally {
            realm.cancelTransaction();
        }
    }
}
