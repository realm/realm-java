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

import io.realm.entities.PrimaryKeyRequiredAsBoxedByte;
import io.realm.entities.PrimaryKeyRequiredAsBoxedInteger;
import io.realm.entities.PrimaryKeyRequiredAsBoxedLong;
import io.realm.entities.PrimaryKeyRequiredAsBoxedShort;
import io.realm.entities.PrimaryKeyRequiredAsString;
import io.realm.objectid.NullPrimaryKey;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class RealmPrimaryKeyTests {
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
     * Base parameters for testing null/not-null primary key value. The parameters are aligned in an
     * order of 1) a test target class, 2) a primary key field class, 3) a primary Key value, 4) a
     * secondary field class, and 5) a secondary field value, accommodating {@interface NullPrimaryKey}
     * to condense unit tests.
     */
    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
            // 1) Test target class                  2) PK Class    3) PK value            4) 2nd Class  5) 2nd field value
            {PrimaryKeyRequiredAsString.class,       String.class,  "424123",              String.class, "SomeSecondaryValue"},
            {PrimaryKeyRequiredAsBoxedByte.class,    Byte.class,    Byte.valueOf("67"),    String.class, "This-Is-Second-One"},
            {PrimaryKeyRequiredAsBoxedShort.class,   Short.class,   Short.valueOf("1729"), String.class, "AnyValueIsAccepted"},
            {PrimaryKeyRequiredAsBoxedInteger.class, Integer.class, Integer.valueOf("19"), String.class, "PlayWithSeondFied!"},
            {PrimaryKeyRequiredAsBoxedLong.class,    Long.class,    Long.valueOf("62914"), String.class, "Let's name a value"}
        });
    }

    final private Class<? extends RealmObject> testClazz;
    final private Class primaryKeyFieldType;
    final private Object primaryKeyFieldValue;
    final private Class secondaryFieldType;
    final private Object secondaryFieldValue;

    public RealmPrimaryKeyTests(Class<? extends RealmObject> testClazz, Class primaryKeyFieldType, Object primaryKeyFieldValue, Class secondaryFieldType, Object secondaryFieldValue) {
        this.testClazz = testClazz;
        this.primaryKeyFieldType = primaryKeyFieldType;
        this.primaryKeyFieldValue = primaryKeyFieldValue;
        this.secondaryFieldType = secondaryFieldType;
        this.secondaryFieldValue = secondaryFieldValue;
    }

    // @PrimaryKey + @Required annotation accept not-null value properly as a primary key value for Realm version 0.89.1+.
    @Test
    public void copyToRealmOrUpdate_requiredPrimaryKey() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        RealmObject obj = (RealmObject)testClazz.getConstructor(primaryKeyFieldType, secondaryFieldType).newInstance(primaryKeyFieldValue, secondaryFieldValue);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(obj);
        realm.commitTransaction();

        RealmResults results = realm.where(testClazz).findAll();
        assertEquals(1, results.size());
        assertEquals(primaryKeyFieldValue, ((NullPrimaryKey)results.first()).getId());
        assertEquals(secondaryFieldValue, ((NullPrimaryKey)results.first()).getName());
    }

    // @PrimaryKey + @Required annotation does accept null as a primary key value for Realm version 0.89.1+.
    @Test
    public void copyToRealmOrUpdate_requiredPrimaryKeyThrows() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        RealmObject obj = (RealmObject)testClazz.getConstructor(primaryKeyFieldType, secondaryFieldType).newInstance(null, null);

        realm.beginTransaction();
        try {
            realm.copyToRealmOrUpdate(obj);
            fail("@PrimaryKey + @Required field cannot be null");
        } catch (RuntimeException expected) {
            if (testClazz.equals(PrimaryKeyRequiredAsString.class)) {
                assertTrue(expected instanceof IllegalArgumentException);
            } else {
                assertTrue(expected instanceof NullPointerException);
            }

        } finally {
            realm.cancelTransaction();
        }
    }

    // @PrimaryKey + @Required annotation does not accept null as a primary key value for Realm version 0.89.1+.
    @Test
    public void createObject_nullPrimaryKeyValueThrows() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        realm.beginTransaction();
        try {
            realm.createObject(testClazz, null);
            fail("@PrimaryKey + @Required field cannot be null");
        } catch (RuntimeException expected) {
            assertTrue(expected instanceof IllegalArgumentException);
        } finally {
            realm.cancelTransaction();
        }
    }
}
