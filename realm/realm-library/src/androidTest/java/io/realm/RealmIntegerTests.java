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

import io.realm.entities.Counters;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

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
     * Validate basic constructor parameters and functions set, increment and decrement.
     */
    @Test
    public void basic() {
        RealmInteger ri1 = RealmInteger.valueOf(10);
        RealmInteger ri2 = RealmInteger.valueOf("10");
        assertEquals(ri1, ri2);

        ri1.set(15);
        ri1.decrement(2);
        ri2.increment(3);
        assertEquals(ri1, ri2);
    }

    /**
     * Validate various getters.
     * Expected behaviour is that gets of smaller sized quantities should truncate on the left:
     * that they should return exactly the rightmost N bits of the underlying value.
     * <p>
     * Caution. The assertion functions will cast back up to int
     * if either arg is an int. That will cause sign extension.
     */
    @Test
    public void getters_unmanaged() {
        RealmInteger ri = RealmInteger.valueOf(0x5555444433332211L);

        // positive
        assertEquals(0x5555444433332211L, ri.longValue());
        assertEquals(0x033332211, ri.intValue());
        assertEquals((short) 0x02211, ri.shortValue());
        assertEquals((byte) 0x011, ri.byteValue());

        assertEquals(6.1488962E18F, ri.floatValue());
        assertEquals(6.1488959259517348E18, ri.doubleValue());

        // negative
        ri.set(0x8888444483338281L);
        assertEquals(0x8888444483338281L, ri.longValue());
        assertEquals(0x083338281, ri.intValue());
        assertEquals((short) 0xf8281, ri.shortValue());
        assertEquals((byte) 0xf81, ri.byteValue());

        assertEquals(-8.6085554E18F, ri.floatValue());
        assertEquals(-8.6085556266690468E18, ri.doubleValue());
    }

    /**
     * Same test as above, but a managed object
     */
    @Ignore("Not yet implemented")
    @Test
    public void getters_managed() {
        realm.beginTransaction();
        RealmInteger ri = realm.createObject(Counters.class).getColumnRealmInteger();
        ri.set(0x5555444433332211L);
        realm.commitTransaction();

        // positive
        assertEquals(0x5555444433332211L, ri.longValue());
        assertEquals(0x033332211, ri.intValue());
        assertEquals((short) 0x02211, ri.shortValue());
        assertEquals((byte) 0x011, ri.byteValue());

        assertEquals(6.1488962E18F, ri.floatValue());
        assertEquals(6.1488959259517348E18, ri.doubleValue());

        // negative
        ri.set(0x8888444483338281L);
        assertEquals(0x8888444483338281L, ri.longValue());
        assertEquals(0x083338281, ri.intValue());
        assertEquals((short) 0xf8281, ri.shortValue());
        assertEquals((byte) 0xf81, ri.byteValue());

        assertEquals(-8.6085554E18F, ri.floatValue());
        assertEquals(-8.6085556266690468E18, ri.doubleValue());
    }

    /**
     * Be absolutely certain that we can actually compare two longs.
     */
    @Test
    public void compareTo_unmanaged() {
        RealmInteger ri1 = RealmInteger.valueOf(0);
        RealmInteger ri2 = RealmInteger.valueOf(Long.MAX_VALUE);
        assertEquals(-1, ri1.compareTo(ri2));

        ri2.decrement(Long.MAX_VALUE);
        assertEquals(0, ri1.compareTo(ri2));

        ri2.decrement(Long.MAX_VALUE);
        assertEquals(1, ri1.compareTo(ri2));
    }

    /**
     * Be absolutely certain that we can actually compare two longs.
     */
    @Ignore("Not yet implemented")
    @Test
    public void compareTo_managed() {
        realm.beginTransaction();
        RealmInteger ri1 = realm.createObject(Counters.class).getColumnRealmInteger();
        ri1.set(0);
        RealmInteger ri2 = realm.createObject(Counters.class).getColumnRealmInteger();
        ri1.set(Long.MAX_VALUE);
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
     * Be absolutely certain that this overflows like a long, part I.
     */
    @Test
    public void incrementUnderFlowAndOverflow_unmanaged() {
        RealmInteger ri = RealmInteger.valueOf(Long.MAX_VALUE);
        ri.increment(1);
        assertEquals(Long.MIN_VALUE, ri.longValue());

        ri = RealmInteger.valueOf(1);
        ri.increment(Long.MAX_VALUE);
        assertEquals(Long.MIN_VALUE, ri.longValue());

        ri = RealmInteger.valueOf(-1);
        ri.increment(Long.MIN_VALUE);
        assertEquals(Long.MAX_VALUE, ri.longValue());
    }

    /**
     * Be absolutely certain that this overflows like a long, part II.
     */
    @Test
    public void decrementUnderFlowAndOverflow_unmanaged() {
        RealmInteger ri = RealmInteger.valueOf(Long.MIN_VALUE);
        ri.decrement(1);
        assertEquals(Long.MAX_VALUE, ri.longValue());

        ri = RealmInteger.valueOf(0);
        ri.decrement(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, ri.longValue());

        ri = RealmInteger.valueOf(-2);
        ri.decrement(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, ri.longValue());
    }

    /**
     * Be absolutely certain that this overflows like a long, part III.
     */
    @Ignore("Not yet implemented")
    @Test
    public void incrementUnderFlowAndOverflow_managed() {
        realm.beginTransaction();
        RealmInteger ri = realm.createObject(Counters.class).getColumnRealmInteger();

        ri.set(Long.MAX_VALUE);
        ri.increment(1);
        assertEquals(Long.MIN_VALUE, ri.longValue());

        ri = RealmInteger.valueOf(1);
        ri.increment(Long.MAX_VALUE);
        assertEquals(Long.MIN_VALUE, ri.longValue());

        ri = RealmInteger.valueOf(-1);
        ri.increment(Long.MIN_VALUE);
        assertEquals(Long.MAX_VALUE, ri.longValue());

        realm.commitTransaction();
    }

    /**
     * Be absolutely certain that this overflows like a long, part IV.
     */
    @Ignore("Not yet implemented")
    @Test
    public void decrementUnderFlowAndOverflow_managed() {
        realm.beginTransaction();
        RealmInteger ri = realm.createObject(Counters.class).getColumnRealmInteger();

        ri.decrement(1);
        assertEquals(Long.MAX_VALUE, ri.longValue());

        ri = RealmInteger.valueOf(0);
        ri.decrement(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, ri.longValue());

        ri = RealmInteger.valueOf(-2);
        ri.decrement(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, ri.longValue());

        realm.commitTransaction();
    }

    /**
     * Assure that an attempt to change the value of a managed RealmInteger, outside a transaction, fails.
     */
    @Ignore("Not yet implemented")
    @Test
    public void updateOutsideTransactionThrows() {
        realm.beginTransaction();
        realm.createObject(Counters.class).getColumnRealmInteger().set(42);
        realm.commitTransaction();

        RealmInteger managedRI = realm.where(Counters.class).findFirst().getColumnRealmInteger();
        try {
            managedRI.set(1);
            fail("Setting a managed RealmInteger outside a transaction should fail");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("must be in a transaction"));
        }

        try {
            managedRI.increment(1);
            fail("Incrementing a managed RealmInteger outside a transaction should fail");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("must be in a transaction"));
        }

        try {
            managedRI.decrement(1);
            fail("Decrementing a managed RealmInteger outside a transaction should fail");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("must be in a transaction"));
        }
    }

    /**
     * Assure that changes to a RealmInteger acquired from a managed object are reflected in the object.
     */
    @Ignore("Not yet implemented")
    @Test
    public void isLive() {
        realm.beginTransaction();
        realm.createObject(Counters.class).getColumnRealmInteger().set(42);
        realm.commitTransaction();

        RealmInteger managedRI = realm.where(Counters.class).findFirst().getColumnRealmInteger();

        realm.beginTransaction();
        RealmInteger ri = realm.where(Counters.class).findFirst().getColumnRealmInteger();
        ri.set(37);
        ri.increment(17);
        ri.decrement(7);
        realm.commitTransaction();

        assertEquals(47L, managedRI.longValue());
    }

    /**
     * Assure that changes to a RealmInteger acquired from a managed object are reflected in the object.
     */
    @Ignore("Not yet implemented")
    @Test
    public void copyToisLive() {
        Counters obj = new Counters();
        obj.setColumnRealmInteger(RealmInteger.valueOf(42));

        RealmInteger unmanagedRI = obj.getColumnRealmInteger();

        realm.beginTransaction();
        RealmInteger managedRI = realm.copyToRealm(obj).getColumnRealmInteger();
        realm.commitTransaction();

        realm.beginTransaction();
        RealmInteger ri = realm.where(Counters.class).findFirst().getColumnRealmInteger();
        ri.set(37);
        ri.increment(17);
        ri.decrement(7);
        realm.commitTransaction();

        assertEquals(42L, unmanagedRI.longValue());
        assertEquals(47L, managedRI.longValue());
    }


    /**
     * Assure that a RealmInteger acquired from an unmanaged object is not affected by changes in the DB.
     */
    @Ignore("Not yet implemented")
    @Test
    public void copyFromIsNotLive() {
        realm.beginTransaction();
        realm.createObject(Counters.class).getColumnRealmInteger().set(42);
        realm.commitTransaction();

        Counters obj = realm.where(Counters.class).findFirst();
        RealmInteger managedRI = obj.getColumnRealmInteger();
        RealmInteger unmanagedRI = realm.copyFromRealm(obj).getColumnRealmInteger();

        realm.beginTransaction();
        RealmInteger ri = realm.where(Counters.class).findFirst().getColumnRealmInteger();
        ri.set(37);
        ri.increment(17);
        ri.decrement(7);
        realm.commitTransaction();

        assertEquals(42L, unmanagedRI.longValue());
        assertEquals(47L, managedRI.longValue());
    }

    /**
     * Test the semantic definition.
     * @see <a href="https://github.com/realm/realm-java/issues/4266"/>
     */
    @Test
    @SuppressWarnings("ReferenceEquality")
    public void unmanagedRealmIntegers_semantics() {
        Counters obj1 = new Counters();
        obj1.columnRealmInteger = RealmInteger.valueOf(0);

        Counters obj2 = new Counters();
        obj2.columnRealmInteger = RealmInteger.valueOf(30);

        obj1.columnRealmInteger.increment(1);

        obj2.columnRealmInteger = obj1.columnRealmInteger;

        assertEquals(1L, obj2.columnRealmInteger.longValue());
        assertTrue(obj1.columnRealmInteger.equals(obj2.columnRealmInteger));
        assertTrue(obj1.columnRealmInteger == obj2.columnRealmInteger);
    }

    /**
     * Test the semantic definition.
     * @see <a href="https://github.com/realm/realm-java/issues/4266"/>
     */
    @Ignore("Not yet implemented")
    @Test
    @SuppressWarnings("ReferenceEquality")
    public void mixedManagedUnmanagedRealmIntegers_semantics() {
        realm.beginTransaction();
        Counters obj1 = realm.createObject(Counters.class);
        realm.commitTransaction();

        Counters obj2 = new Counters();
        obj2.columnRealmInteger = RealmInteger.valueOf(30);

        try {
            obj1.columnRealmInteger.increment(1);
            fail("Incrementing a managed RealmInteger outside a transaction should fail");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("must be in a transaction"));
        }

        realm.beginTransaction();
        obj1.columnRealmInteger.increment(1);
        realm.commitTransaction();

        try {
            obj2.columnRealmInteger = obj1.columnRealmInteger;
            fail("Assigning a managed RealmInteger into an unmanaged object should fail");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Cannot assign a managed RealmInteger to an unmanaged object"));
        }

        obj2.columnRealmInteger.set(obj1.columnRealmInteger.longValue());

        assertEquals(1L, obj2.columnRealmInteger.longValue());
        assertTrue(obj1.columnRealmInteger.equals(obj2.columnRealmInteger));
        assertFalse(obj1.columnRealmInteger == obj2.columnRealmInteger);

        obj2.columnRealmInteger.increment(1);
        try {
            obj1.columnRealmInteger.set(obj2.columnRealmInteger.longValue());
            fail("Setting a managed RealmInteger outside a transaction should fail");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("must be in a transaction"));
        }

        realm.beginTransaction();
        obj1.columnRealmInteger.set(obj2.columnRealmInteger.longValue());
        realm.commitTransaction();

        assertEquals(2L, obj2.columnRealmInteger.longValue());
        assertTrue(obj1.columnRealmInteger.equals(obj2.columnRealmInteger));
        assertFalse(obj1.columnRealmInteger == obj2.columnRealmInteger);
    }

    /**
     * Test the semantic definition.
     * @see <a href="https://github.com/realm/realm-java/issues/4266"/>
     */
    @Ignore("Not yet implemented")
    @Test
    @SuppressWarnings("ReferenceEquality")
    public void managedRealmIntegers_semantics() {
        realm.beginTransaction();
        Counters obj1 = realm.createObject(Counters.class);
        Counters obj2 = realm.createObject(Counters.class);
        obj1.columnRealmInteger.increment(1);
        realm.commitTransaction();

        try {
            obj2.columnRealmInteger = obj1.columnRealmInteger;
            fail("Assignment to a managed RealmInteger should fail");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Cannot assign to a managed RealmInteger"));
        }

        realm.beginTransaction();
        obj1.columnRealmInteger.set(obj2.columnRealmInteger.longValue());
        realm.commitTransaction();

        assertEquals(1L, obj2.columnRealmInteger.longValue());
        assertTrue(obj1.columnRealmInteger.equals(obj2.columnRealmInteger));
        assertFalse(obj1.columnRealmInteger == obj2.columnRealmInteger);
    }

    // FIXME!!! Need JSON tests.
}
