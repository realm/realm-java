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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.entities.MutableRealmIntegerTypes;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


// FIXME MutableRealmInteger: Need JSON tests.
// FIXME MutableRealmInteger: Need tests for @Required, @Index

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
        testBasic(c1.columnMutableRealmInteger, c2.columnMutableRealmInteger);
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
        MutableRealmInteger ri1 = realm.createObject(MutableRealmIntegerTypes.class).getColumnMutableRealmInteger();
        ri1.set(0);
        MutableRealmInteger ri2 = realm.createObject(MutableRealmIntegerTypes.class).getColumnMutableRealmInteger();
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
        realm.createObject(MutableRealmIntegerTypes.class).getColumnMutableRealmInteger().set(42);
        realm.commitTransaction();

        MutableRealmInteger managedRI = realm.where(MutableRealmIntegerTypes.class).findFirst().getColumnMutableRealmInteger();
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
        realm.createObject(MutableRealmIntegerTypes.class).getColumnMutableRealmInteger().set(42);
        realm.commitTransaction();

        MutableRealmInteger managedRI = realm.where(MutableRealmIntegerTypes.class).findFirst().getColumnMutableRealmInteger();

        realm.beginTransaction();
        MutableRealmInteger ri = realm.where(MutableRealmIntegerTypes.class).findFirst().getColumnMutableRealmInteger();
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
    public void copyToisLive() {
        MutableRealmIntegerTypes obj = new MutableRealmIntegerTypes();
        MutableRealmInteger unmanagedRI = obj.getColumnMutableRealmInteger();
        unmanagedRI.set(42L);

        realm.beginTransaction();
        MutableRealmInteger managedRI = realm.copyToRealm(obj).getColumnMutableRealmInteger();
        realm.commitTransaction();

        realm.beginTransaction();
        MutableRealmInteger ri = realm.where(MutableRealmIntegerTypes.class).findFirst().getColumnMutableRealmInteger();
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
        realm.createObject(MutableRealmIntegerTypes.class).getColumnMutableRealmInteger().set(42L);
        realm.commitTransaction();

        MutableRealmIntegerTypes obj = realm.where(MutableRealmIntegerTypes.class).findFirst();
        MutableRealmInteger managedRI = obj.getColumnMutableRealmInteger();
        MutableRealmInteger unmanagedRI = realm.copyFromRealm(obj).getColumnMutableRealmInteger();

        realm.beginTransaction();
        MutableRealmInteger ri = realm.where(MutableRealmIntegerTypes.class).findFirst().getColumnMutableRealmInteger();
        ri.set(37);
        ri.increment(17);
        ri.decrement(7);
        realm.commitTransaction();

        assertEquals(Long.valueOf(42L), unmanagedRI.get());
        assertEquals(Long.valueOf(47L), managedRI.get());
    }

    private void checkTransactionException(IllegalStateException e) {
        assertTrue(e.getMessage().contains("only be done from inside a transaction"));
    }

    @SuppressWarnings({"ReferenceEquality", "EqualsIncompatibleType"})
    private void testBasic(MutableRealmInteger r1, MutableRealmInteger r2) {
        assertFalse(r1 == r2);

        r1.set(10);
        r2.set(Long.valueOf(10));
        assertEquals(r1, r2);

        r1.set(15);
        r1.decrement(2);
        r2.increment(3);
        assertEquals(r1, r2);

        MutableRealmInteger r3 = r1;
        r1.set(19);
        assertEquals(19, r3.get().intValue());
    }

    @SuppressWarnings({"ReferenceEquality", "EqualsIncompatibleType"})
    private void testEquality(MutableRealmIntegerTypes c1, MutableRealmIntegerTypes c2) {
        assertFalse(c1 == c2);

        c1.columnMutableRealmInteger.set(7);
        c2.columnMutableRealmInteger.set(Long.valueOf(7));
        assertTrue(c1.columnMutableRealmInteger != c2.columnMutableRealmInteger);
        assertTrue(c1.columnMutableRealmInteger.equals(c2.columnMutableRealmInteger));

        MutableRealmInteger r1 = c1.columnMutableRealmInteger;
        r1.increment(1);
        assertTrue(r1.equals(c1.columnMutableRealmInteger));
        assertTrue(r1 == c1.columnMutableRealmInteger);
        assertTrue(c1.columnMutableRealmInteger.get().equals(8L));
        assertFalse(c1.columnMutableRealmInteger.equals(c2.columnMutableRealmInteger.get()));
        assertTrue(c1.columnMutableRealmInteger.get().intValue() == 8);

        Long n = c1.columnMutableRealmInteger.get();
        assertTrue(n.equals(Long.valueOf(8)));
        assertTrue(n.equals(c1.columnMutableRealmInteger.get()));
        assertTrue(n.intValue() == c1.columnMutableRealmInteger.get().intValue());

        c1.columnMutableRealmInteger.increment(1);
        assertFalse(n.intValue() == c1.columnMutableRealmInteger.get().intValue());
        assertFalse(n.intValue() == r1.get().intValue());
    }

    private void testNullability(MutableRealmIntegerTypes c1) {
        MutableRealmInteger r1 = c1.columnMutableRealmInteger;

        c1.columnMutableRealmInteger.set(0L);
        assertFalse(c1.columnMutableRealmInteger.isNull());
        assertFalse(r1.isNull());

        c1.columnMutableRealmInteger.set(null);
        assertFalse(c1.columnMutableRealmInteger == null);
        assertTrue(c1.columnMutableRealmInteger.isNull());
        assertTrue(r1.isNull());

        assertTrue(c1.columnMutableRealmInteger.get() == null);
        assertTrue(r1.get() == null);

        try {
            c1.columnMutableRealmInteger.increment(5);
            fail("Attempt to increment a null valued MutableRealmInteger should throw NPE");
        }
        catch (IllegalArgumentException ignore) { }
        try {
            c1.columnMutableRealmInteger.decrement(5);
            fail("Attempt to decrement a null valued MutableRealmInteger should throw NPE");
        }
        catch (IllegalArgumentException ignore) { }
    }

    private void testValidityAndManagement(MutableRealmIntegerTypes c1) {
        MutableRealmInteger r1 = c1.columnMutableRealmInteger;
        assertTrue(r1.isManaged() == c1.isManaged());
        assertTrue(r1.isValid() == c1.isValid());
    }
}
