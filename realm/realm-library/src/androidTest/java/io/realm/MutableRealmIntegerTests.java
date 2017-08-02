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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

import io.realm.entities.MutableRealmIntegerTypes;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


@RunWith(AndroidJUnit4.class)
public class MutableRealmIntegerTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    private Realm realm;

    @Before
    public void setUp() throws Exception {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() throws Exception {
        if (realm != null) {
            if (realm.isInTransaction()) {
                realm.cancelTransaction();
            }
            realm.close();
        }
    }

    /**
     * Validate basic functions: set, increment and decrement.
     */
    @Test
    public void basic_unmanaged() {
        testBasic(MutableRealmInteger.ofNull(), MutableRealmInteger.ofNull());
    }

    /**
     * Validate basic equality semantics.
     */
    @Test
    public void equality_unmanaged() {
        testEquality(new MutableRealmIntegerTypes(), new MutableRealmIntegerTypes());
    }

    /**
     * Validate basic nullability semantics.
     */
    @Test
    public void nullability_unmanaged() {
        testNullability(new MutableRealmIntegerTypes());
    }

    /**
     * Validate basic validity/managed semantics.
     */
    @Test
    public void validAndManaged_unmanaged() {
        testValidityAndManagement(new MutableRealmIntegerTypes());
    }

    /**
     * Validate basic functions: set, increment and decrement.
     */
    @Test
    public void basic_managed() {
        realm.beginTransaction();
        MutableRealmIntegerTypes c1 = realm.createObject(MutableRealmIntegerTypes.class);
        MutableRealmIntegerTypes c2 = realm.createObject(MutableRealmIntegerTypes.class);
        realm.commitTransaction();

        realm.beginTransaction();
        testBasic(c1.columnNullableMutableRealmInteger, c2.columnNullableMutableRealmInteger);
        realm.commitTransaction();
    }

    /**
     * Validate basic equality semantics.
     */
    @Test
    public void equality_managed() {
        realm.beginTransaction();
        MutableRealmIntegerTypes c1 = realm.createObject(MutableRealmIntegerTypes.class);
        MutableRealmIntegerTypes c2 = realm.createObject(MutableRealmIntegerTypes.class);
        realm.commitTransaction();

        realm.beginTransaction();
        testEquality(c1, c2);
        realm.commitTransaction();
    }

    /**
     * Validate basic nullability semantics.
     */
    @Test
    public void nullability_managed() {
        realm.beginTransaction();
        MutableRealmIntegerTypes c1 = realm.createObject(MutableRealmIntegerTypes.class);
        realm.commitTransaction();

        realm.beginTransaction();
        testNullability(c1);
        realm.commitTransaction();
    }

    /**
     * Validate basic validity/managed semantics.
     */
    @Test
    public void validAndManaged_managed() {
        realm.beginTransaction();
        MutableRealmIntegerTypes c1 = realm.createObject(MutableRealmIntegerTypes.class);
        realm.commitTransaction();

        realm.beginTransaction();
        testValidityAndManagement(c1);
        realm.commitTransaction();
    }

    /**
     * {@literal @}Required MutableRealmIntegers should not be nullable.
     * There are other tests testing nullabilty: just need to test @Required here.
     * There is no attempt to control the nullability of an unmanaged MutableRealmInteger.
     * An attempt to copy an unmanaged model object with a null-valued MutableRealmInteger
     * into an @Required field should fail.
     */
    @Test
    public void required() {
        realm.beginTransaction();
        MutableRealmIntegerTypes c1 = realm.createObject(MutableRealmIntegerTypes.class);
        realm.commitTransaction();

        assertFalse(
                realm.getSchema().get("MutableRealmIntegerTypes")
                        .isNullable(MutableRealmIntegerTypes.FIELD_NONNULLABLE_MUTABLEREALMINTEGER));

        realm.beginTransaction();
        try {
            c1.columnNonNullableMutableRealmInteger.set(null);
            fail("should not be able to set an @Required MutableRealmInteger null");
        } catch(IllegalArgumentException ignore) {
            checkException(ignore, "is not nullable");
        }
        realm.commitTransaction();

        c1 = new MutableRealmIntegerTypes();
        c1.columnNonNullableMutableRealmInteger.set(null);
        realm.beginTransaction();
        try {
            MutableRealmIntegerTypes c2 = realm.copyToRealm(c1);
            fail("should not be able to copy a null value to a @Required MutableRealmInteger");
        } catch(IllegalArgumentException ignore) {
            checkException(ignore, "is not nullable");
        }
        realm.commitTransaction();
    }


    /**
     * MutableRealmIntegers annotated with {@literal @}Index should have indices.
     * Without {@literal @}Index they should not.
     */
    @Test
    public void indexed() {
        realm.beginTransaction();
        MutableRealmIntegerTypes c1 = realm.createObject(MutableRealmIntegerTypes.class);
        realm.commitTransaction();

        assertTrue(
                realm.getSchema().get("MutableRealmIntegerTypes")
                        .hasIndex(MutableRealmIntegerTypes.FIELD_INDEXED_MUTABLEREALMINTEGER));
        assertFalse(
                realm.getSchema().get("MutableRealmIntegerTypes")
                        .hasIndex(MutableRealmIntegerTypes.FIELD_NULLABLE_MUTABLEEALMINTEGER));
    }

    /**
     * Be absolutely certain that we can actually compare two longs.
     */
    @Test
    public void compareTo_unmanaged() {
        MutableRealmInteger ri1 = MutableRealmInteger.valueOf(0);
        MutableRealmInteger ri2 = MutableRealmInteger.valueOf(Long.MAX_VALUE);
        assertEquals(-1, ri1.compareTo(ri2));

        ri2.decrement(Long.MAX_VALUE);
        assertEquals(0, ri1.compareTo(ri2));

        ri2.decrement(Long.MAX_VALUE);
        assertEquals(1, ri1.compareTo(ri2));
    }

    /**
     * Be absolutely certain that we can actually compare two longs.
     */
    @Test
    public void compareTo_managed() {
        realm.beginTransaction();
        MutableRealmInteger ri1 = realm.createObject(MutableRealmIntegerTypes.class).getColumnNullableMutableRealmInteger();
        ri1.set(0);
        MutableRealmInteger ri2 = realm.createObject(MutableRealmIntegerTypes.class).getColumnNullableMutableRealmInteger();
        ri2.set(Long.MAX_VALUE);
        realm.commitTransaction();
        assertEquals(-1, ri1.compareTo(ri2));

        realm.beginTransaction();
        ri2.decrement(Long.MAX_VALUE);
        realm.commitTransaction();
        assertEquals(0, ri1.compareTo(ri2));

        realm.beginTransaction();
        ri2.decrement(Long.MAX_VALUE);
        realm.commitTransaction();
        assertEquals(1, ri1.compareTo(ri2));
    }

    /**
     * Assure that an attempt to change the value of a managed MutableRealmInteger, outside a transaction, fails.
     */
    @Test
    public void updateOutsideTransactionThrows() {
        realm.beginTransaction();
        realm.createObject(MutableRealmIntegerTypes.class).getColumnNullableMutableRealmInteger().set(42);
        realm.commitTransaction();

        MutableRealmInteger managedRI = realm.where(MutableRealmIntegerTypes.class).findFirst().getColumnNullableMutableRealmInteger();
        try {
            managedRI.set(1);
            fail("Setting a managed MutableRealmInteger outside a transaction should fail");
        } catch (IllegalStateException e) {
            checkTransactionException(e);
        }

        try {
            managedRI.increment(1);
            fail("Incrementing a managed MutableRealmInteger outside a transaction should fail");
        } catch (IllegalStateException e) {
            checkTransactionException(e);
        }

        try {
            managedRI.decrement(1);
            fail("Decrementing a managed MutableRealmInteger outside a transaction should fail");
        } catch (IllegalStateException e) {
            checkTransactionException(e);
        }
    }

    /**
     * Assure that changes to a MutableRealmInteger acquired from a managed object are reflected in the object.
     */
    @Test
    public void isLive() {
        realm.beginTransaction();
        realm.createObject(MutableRealmIntegerTypes.class).getColumnNullableMutableRealmInteger().set(42);
        realm.commitTransaction();

        MutableRealmInteger managedRI = realm.where(MutableRealmIntegerTypes.class).findFirst().getColumnNullableMutableRealmInteger();

        realm.beginTransaction();
        MutableRealmInteger ri = realm.where(MutableRealmIntegerTypes.class).findFirst().getColumnNullableMutableRealmInteger();
        ri.set(37);
        ri.increment(17);
        ri.decrement(7);
        realm.commitTransaction();

        assertEquals(Long.valueOf(47), managedRI.get());
    }

    /**
     * Assure that changes to a MutableRealmInteger acquired from a managed object are reflected in the object.
     */
    @Test
    public void copyToIsLive() {
        MutableRealmIntegerTypes obj = new MutableRealmIntegerTypes();
        MutableRealmInteger unmanagedRI = obj.getColumnNullableMutableRealmInteger();
        unmanagedRI.set(42L);

        realm.beginTransaction();
        MutableRealmInteger managedRI = realm.copyToRealm(obj).getColumnNullableMutableRealmInteger();
        realm.commitTransaction();

        realm.beginTransaction();
        MutableRealmInteger ri = realm.where(MutableRealmIntegerTypes.class).findFirst().getColumnNullableMutableRealmInteger();
        ri.set(37);
        ri.increment(17);
        ri.decrement(7);
        realm.commitTransaction();

        assertEquals(Long.valueOf(42L), unmanagedRI.get());
        assertEquals(Long.valueOf(47L), managedRI.get());
    }

    /**
     * Assure that a MutableRealmInteger acquired from an unmanaged object is not affected by changes in the Realm.
     */
    @Test
    public void copyFromIsNotLive() {
        realm.beginTransaction();
        realm.createObject(MutableRealmIntegerTypes.class).getColumnNullableMutableRealmInteger().set(42L);
        realm.commitTransaction();

        MutableRealmIntegerTypes obj = realm.where(MutableRealmIntegerTypes.class).findFirst();
        MutableRealmInteger managedRI = obj.getColumnNullableMutableRealmInteger();
        MutableRealmInteger unmanagedRI = realm.copyFromRealm(obj).getColumnNullableMutableRealmInteger();

        realm.beginTransaction();
        MutableRealmInteger ri = realm.where(MutableRealmIntegerTypes.class).findFirst().getColumnNullableMutableRealmInteger();
        ri.set(37);
        ri.increment(17);
        ri.decrement(7);
        realm.commitTransaction();

        assertEquals(Long.valueOf(42L), unmanagedRI.get());
        assertEquals(Long.valueOf(47L), managedRI.get());
    }

    @Test
    public void testJSON() throws JSONException {
        JSONObject json = new JSONObject();
        realm.beginTransaction();
        MutableRealmIntegerTypes obj = realm.createObjectFromJson(MutableRealmIntegerTypes.class, json);
        realm.commitTransaction();
        assertTrue(obj.columnNullableMutableRealmInteger.isNull());

        json = new JSONObject();
        json.put("columnNullableMutableRealmInteger", 8589934592L);
        realm.beginTransaction();
        obj = realm.createObjectFromJson(MutableRealmIntegerTypes.class, json);
        realm.commitTransaction();
        assertEquals(Long.valueOf(8589934592L), obj.columnNullableMutableRealmInteger.get());

        json = new JSONObject();
        json.put("columnNullableMutableRealmInteger", 22);
        realm.beginTransaction();
        obj = realm.createObjectFromJson(MutableRealmIntegerTypes.class, json);
        realm.commitTransaction();
        assertEquals(Long.valueOf(22), obj.columnNullableMutableRealmInteger.get());

        json = new JSONObject();
        json.put("columnNullableMutableRealmInteger", JSONObject.NULL);
        realm.beginTransaction();
        obj = realm.createObjectFromJson(MutableRealmIntegerTypes.class, json);
        realm.commitTransaction();
        assertTrue(obj.columnNullableMutableRealmInteger.isNull());

        json = new JSONObject();
        json.put("columnNonNullableMutableRealmInteger", JSONObject.NULL);
        realm.beginTransaction();
        try {
            obj = realm.createObjectFromJson(MutableRealmIntegerTypes.class, json);
            fail("Attempt to set @Required Mutable Realm Integer null, from JSON, should fail");
        } catch (IllegalArgumentException ignore) {
            checkException(ignore, "is not nullable");
        }
        realm.commitTransaction();
    }

    @Test
    public void testStream() throws IOException {
        Context context = InstrumentationRegistry.getTargetContext();

        InputStream in = TestHelper.loadJsonFromAssets(context, "empty.json");
        realm.beginTransaction();
        MutableRealmIntegerTypes obj = realm.createObjectFromJson(MutableRealmIntegerTypes.class, in);
        realm.commitTransaction();
        assertTrue(obj.columnNullableMutableRealmInteger.isNull());

        in = TestHelper.loadJsonFromAssets(context, "mutablerealminteger-long.json");
        realm.beginTransaction();
        obj = realm.createObjectFromJson(MutableRealmIntegerTypes.class, in);
        realm.commitTransaction();
        assertEquals(Long.valueOf(8589934592L), obj.columnNullableMutableRealmInteger.get());

        in = TestHelper.loadJsonFromAssets(context, "mutablerealminteger-int.json");
        realm.beginTransaction();
        obj = realm.createObjectFromJson(MutableRealmIntegerTypes.class, in);
        realm.commitTransaction();
        assertEquals(Long.valueOf(22), obj.columnNullableMutableRealmInteger.get());

        in = TestHelper.loadJsonFromAssets(context, "mutablerealminteger-null.json");
        realm.beginTransaction();
        obj = realm.createObjectFromJson(MutableRealmIntegerTypes.class, in);
        realm.commitTransaction();
        assertTrue(obj.columnNullableMutableRealmInteger.isNull());

        in = TestHelper.loadJsonFromAssets(context, "mutablerealminteger-required-null.json");
        realm.beginTransaction();
        try {
            obj = realm.createObjectFromJson(MutableRealmIntegerTypes.class, in);
            fail("Attempt to set @Required Mutable Realm Integer null, from JSON, should fail");
        } catch (IllegalArgumentException ignore) {
            checkException(ignore, "is not nullable");
        }
        realm.commitTransaction();
    }

    private void checkTransactionException(Exception e) {
        checkException(e, "only be done from inside a transaction");
    }

    private void checkException(Exception e, String expected) {
        assertTrue(e.getMessage().contains(expected));
    }

    /**
     * Test basic arithmetic: set, increment, decrement and equals.
     * Since the implementations of managed and unmanaged MutableRealmIntegers are completely
     * different these tests should be run on both implementations.
     *
     * @param r1 a MutableRealmInteger
     * @param r2 another MutableRealmInteger
     */
    @SuppressWarnings({"ReferenceEquality", "EqualsIncompatibleType"})
    private void testBasic(MutableRealmInteger r1, MutableRealmInteger r2) {
        assertFalse(r1 == r2);

        r1.set(10);
        r2.set(Long.valueOf(10));
        assertEquals(r1, r2);
        assertEquals(r2, r1);

        r1.set(15);
        r1.decrement(2);
        r2.increment(3);
        assertEquals(r1, r2);

        MutableRealmInteger r3 = r1;
        r1.set(19);
        assertEquals(19, r3.get().intValue());

        assertFalse(r2.equals(r3));
        assertFalse(r3.equals(r2));
    }

    /**
     * Thorough tests of equality, as defined <a href="https://github.com/realm/realm-java/issues/4266#issuecomment-308772718">here</a>
     * and in subsequent comments.  The general principles are:
     * <ul>
     *     <li>MutableRealmInteger.equals tests the value of the wrapped integer<./li>
     *     <li>All references to a single MutableRealmInteger must be {@code .equals} to the same thing.</li>
     *     <li>Except when set to {@code null} MutableRealmInteger does not distinguish boxed and primitive types.</li>
     * </ul>
     * Since the implementations of managed and unmanaged MutableRealmIntegers are completely
     * different these tests should be run on both implementations.
     *
     * @param c1 a MutableRealmIntegerTypes
     * @param c2 another MutableRealmIntegerTypes
     */
    @SuppressWarnings({"ReferenceEquality", "EqualsIncompatibleType"})
    private void testEquality(MutableRealmIntegerTypes c1, MutableRealmIntegerTypes c2) {
        assertFalse(c1 == c2);

        c1.columnNullableMutableRealmInteger.set(7);
        c2.columnNullableMutableRealmInteger.set(Long.valueOf(7));
        assertTrue(c1.columnNullableMutableRealmInteger != c2.columnNullableMutableRealmInteger);
        assertTrue(c1.columnNullableMutableRealmInteger.equals(c2.columnNullableMutableRealmInteger));

        MutableRealmInteger r1 = c1.columnNullableMutableRealmInteger;
        r1.increment(1);
        assertTrue(r1.equals(c1.columnNullableMutableRealmInteger));
        assertTrue(r1 == c1.columnNullableMutableRealmInteger);
        assertTrue(c1.columnNullableMutableRealmInteger.get().equals(8L));
        assertFalse(c1.columnNullableMutableRealmInteger.get().equals(c2.columnNullableMutableRealmInteger.get()));
        assertTrue(c1.columnNullableMutableRealmInteger.get().intValue() == 8);

        Long n = c1.columnNullableMutableRealmInteger.get();
        assertTrue(n.equals(Long.valueOf(8)));
        assertTrue(n.equals(c1.columnNullableMutableRealmInteger.get()));
        assertTrue(n.intValue() == c1.columnNullableMutableRealmInteger.get().intValue());

        c1.columnNullableMutableRealmInteger.increment(1);
        assertFalse(n.intValue() == c1.columnNullableMutableRealmInteger.get().intValue());
        assertFalse(n.intValue() == r1.get().intValue());
    }

    /**
     * Thorough tests of nullability, as defined <a href="https://github.com/realm/realm-java/issues/4266#issuecomment-308772718">here</a>
     * and in subsequent comments.  The general principles are:
     * <ul>
     *     <li>Unless @Required, MutableRealmIntegers are nullable.</li>
     *     <li>0L and null are distinct values.</li>
     *     <li>All references to a single MutableRealmInteger must be {@code null} if any are./li>
     *     <li>A null value cannot be incremented or decremented/li>
     * </ul>
     * Since the implementations of managed and unmanaged MutableRealmIntegers are completely
     * different these tests should be run on both implementations.
     *
     * @param c1 a MutableRealmIntegerTypes
     */
    private void testNullability(MutableRealmIntegerTypes c1) {
        MutableRealmInteger r1 = c1.columnNullableMutableRealmInteger;

        c1.columnNullableMutableRealmInteger.set(0L);
        assertFalse(c1.columnNullableMutableRealmInteger.isNull());
        assertFalse(r1.isNull());

        c1.columnNullableMutableRealmInteger.set(null);
        assertFalse(c1.columnNullableMutableRealmInteger == null);
        assertTrue(c1.columnNullableMutableRealmInteger.isNull());
        assertTrue(r1.isNull());

        assertTrue(c1.columnNullableMutableRealmInteger.get() == null);
        assertTrue(r1.get() == null);

        try {
            c1.columnNullableMutableRealmInteger.increment(5);
            fail("Attempt to increment a null valued MutableRealmInteger should throw ISE");
        } catch (IllegalStateException ignore) {
            checkException(ignore, "Set its value first");
        }
        try {
            c1.columnNullableMutableRealmInteger.decrement(5);
            fail("Attempt to decrement a null valued MutableRealmInteger should throw ISE");
        } catch (IllegalStateException ignore) {
            checkException(ignore, "Set its value first");
        }
    }

    private void testValidityAndManagement(MutableRealmIntegerTypes c1) {
        MutableRealmInteger r1 = c1.columnNullableMutableRealmInteger;
        assertTrue(r1.isManaged() == c1.isManaged());
        assertTrue(r1.isValid() == c1.isValid());
    }
}
