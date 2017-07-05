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

import io.realm.entities.Counters;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


// FIXME Counters: Need JSON tests.

@RunWith(AndroidJUnit4.class)
public class RealmIntegerTests {

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
        testEquality(new Counters(), new Counters());
    }

    /**
     * Validate basic nullability semantics.
     */
    @Test
    public void nullability_unmanaged() {
        testNullability(new Counters());
    }

    /**
     * Validate basic validity/managed semantics.
     */
    @Test
    public void validAndManaged_unmanaged() {
        testValidityAndManagement(new Counters());
    }

    /**
     * Validate basic functions: set, increment and decrement.
     */
    @Test
    public void basic_managed() {
        realm.beginTransaction();
        Counters c1 = realm.createObject(Counters.class);
        Counters c2 = realm.createObject(Counters.class);
        testBasic(c1.columnCounter, c2.columnCounter);
        realm.commitTransaction();
    }

    /**
     * Validate basic equality semantics.
     */
    @Test
    public void equality_managed() {
        realm.beginTransaction();
        testEquality(realm.createObject(Counters.class), realm.createObject(Counters.class));
        realm.commitTransaction();
    }

    /**
     * Validate basic nullability semantics.
     */
    @Test
    public void nullability_managed() {
        realm.beginTransaction();
        testNullability(realm.createObject(Counters.class));
        realm.commitTransaction();
    }

    /**
     * Validate basic validity/managed semantics.
     */
    @Test
    public void validAndManaged_managed() {
        realm.beginTransaction();
        testValidityAndManagement(realm.createObject(Counters.class));
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
        MutableRealmInteger ri1 = realm.createObject(Counters.class).getColumnCounter();
        ri1.set(0);
        MutableRealmInteger ri2 = realm.createObject(Counters.class).getColumnCounter();
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
        realm.createObject(Counters.class).getColumnCounter().set(42);
        realm.commitTransaction();

        MutableRealmInteger managedRI = realm.where(Counters.class).findFirst().getColumnCounter();
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
        realm.createObject(Counters.class).getColumnCounter().set(42);
        realm.commitTransaction();

        MutableRealmInteger managedRI = realm.where(Counters.class).findFirst().getColumnCounter();

        realm.beginTransaction();
        MutableRealmInteger ri = realm.where(Counters.class).findFirst().getColumnCounter();
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
        Counters obj = new Counters();
        MutableRealmInteger unmanagedRI = obj.getColumnCounter();

        realm.beginTransaction();
        MutableRealmInteger managedRI = realm.copyToRealm(obj).getColumnCounter();
        realm.commitTransaction();

        realm.beginTransaction();
        MutableRealmInteger ri = realm.where(Counters.class).findFirst().getColumnCounter();
        ri.set(37);
        ri.increment(17);
        ri.decrement(7);
        realm.commitTransaction();

        assertEquals(Long.valueOf(42L), unmanagedRI.get());
        assertEquals(Long.valueOf(47L), managedRI.get());
    }


    /**
     * Assure that a MutableRealmInteger acquired from an unmanaged object is not affected by changes in the DB.
     */
    @Test
    public void copyFromIsNotLive() {
        realm.beginTransaction();
        realm.createObject(Counters.class).getColumnCounter().set(42);
        realm.commitTransaction();

        Counters obj = realm.where(Counters.class).findFirst();
        MutableRealmInteger managedRI = obj.getColumnCounter();
        MutableRealmInteger unmanagedRI = realm.copyFromRealm(obj).getColumnCounter();

        realm.beginTransaction();
        MutableRealmInteger ri = realm.where(Counters.class).findFirst().getColumnCounter();
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

    private void testBasic(MutableRealmInteger r1, MutableRealmInteger r2) {
        r1.set(10);
        r2.set(Long.valueOf(10));

        assertEquals(r1, r2);

        r1.set(15);
        r1.decrement(2);
        r2.increment(3);
        assertEquals(r1, r2);
    }

    private void testEquality(Counters c1, Counters c2) {
        c1.columnCounter.set(7);
        c2.columnCounter.set(Long.valueOf(7));
        assert c1.columnCounter.equals(c2.columnCounter);
        assert c1.columnCounter != c2.columnCounter;

        MutableRealmInteger r1 = c1.columnCounter;
        r1.increment(1);
        assert r1.equals(c1.columnCounter);
        assert r1 == c1.columnCounter;
        assert c1.columnCounter.get().equals(8L);
        assert !c1.columnCounter.equals(c2.columnCounter.get());
        assert c1.columnCounter.get().intValue() == 8;

        Long n = c1.columnCounter.get();
        assert n.equals(Long.valueOf(8));
        assert n.equals(c1.columnCounter.get());
        assert n.intValue() == c1.columnCounter.get().intValue();

        c1.columnCounter.increment(1);
        assert n.intValue() != c1.columnCounter.get().intValue();
        assert n.intValue() != r1.get().intValue();
    }

    private void testNullability(Counters c1) {
        MutableRealmInteger r1 = c1.columnCounter;

        assert !c1.columnCounter.isNull();
        c1.columnCounter.set(null);
        assert c1.columnCounter != null;
        assert c1.columnCounter.isNull();
        assert r1.isNull();
        assert r1.get() == null;

        assert r1.isValid();
        assert !r1.isManaged();
    }

    private void testValidityAndManagement(Counters c1) {
        MutableRealmInteger r1 = c1.columnCounter;

        assert r1.isValid() == c1.isValid();
        assert !r1.isManaged() == c1.isManaged();
    }
}
