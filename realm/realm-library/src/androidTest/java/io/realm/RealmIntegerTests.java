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

import io.realm.entities.AllTypes;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;
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
    public void basic_basic() {
        RealmInteger ri1 = new RealmInteger(10);
        RealmInteger ri2 = new RealmInteger("10");
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
     * Caution.  The assertion functions will cast back up to int
     * if either arg is an int.  That will cause sign extension.
     */
    @Test
    public void basic_getters() {
        RealmInteger ri = new RealmInteger(0x5555444433332211L);

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
    public void basic_compareTo() {
        RealmInteger ri1 = new RealmInteger(10);
        RealmInteger ri2 = new RealmInteger("11");

        assertEquals(ri1.compareTo(ri2), -1);
        ri2.decrement(1);
        assertEquals(ri1.compareTo(ri2), 0);
        ri2.decrement(1);
        assertEquals(ri1.compareTo(ri2), 1);
    }

    /**
     * Be absolutely certain that this is a long.
     */
    @Test
    public void basic_underflowAndOverflow() {
        RealmInteger ri = new RealmInteger(Long.MAX_VALUE);
        ri.increment(1);
        assertEquals(Long.MIN_VALUE, ri.longValue());

        ri = new RealmInteger(1);
        ri.increment(Long.MAX_VALUE);
        assertEquals(Long.MIN_VALUE, ri.longValue());

        ri = new RealmInteger(0);
        ri.decrement(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, ri.longValue());

        ri = new RealmInteger(Long.MIN_VALUE);
        ri.decrement(1);
        assertEquals(Long.MAX_VALUE, ri.longValue());

        ri = new RealmInteger(-1);
        ri.increment(Long.MIN_VALUE);
        assertEquals(Long.MAX_VALUE, ri.longValue());

        ri = new RealmInteger(-2);
        ri.decrement(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, ri.longValue());
    }

    /**
     * Assure that assigning a RealmInteger to a managed object causes the RealmInteger to become managed.
     */
    @Ignore("not yet implemented")
    @Test
    public void managed_import() {
        final RealmInteger ri = new RealmInteger(5);

        realm.executeTransaction(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.createObject(AllTypes.class).setColumnRealmInteger(ri);
                    }
                });

        realm.executeTransaction(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmInteger ri = realm.where(AllTypes.class).findFirst().getColumnRealmInteger();
                        ri.set(37);
                        ri.increment(17);
                        ri.decrement(7);
                    }
                });

        assertEquals(ri.longValue(), 47);
    }

    /**
     * Assure that changes to a RealmInteger acquired from a managed object are reflected in the object.
     */
    @Ignore("not yet implemented")
    @Test
    public void managed_export() {
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnRealmInteger(new RealmInteger(5));
        realm.commitTransaction();

        final RealmInteger ri = allTypes.getColumnRealmInteger();

        realm.executeTransaction(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        ri.set(37);
                        ri.increment(17);
                        ri.decrement(7);
                    }
                });

        allTypes = realm.where(AllTypes.class).findFirst();
        assertEquals(allTypes.getColumnRealmInteger().longValue(), 47);
    }

    /**
     * Assure that an unmanaged object's RealmInteger becomes managed when the object does.
     */
    @Ignore("not yet implemented")
    @Test
    public void managed_copyTo() {
        final AllTypes allTypes = new AllTypes();
        RealmInteger ri = new RealmInteger(5);
        allTypes.setColumnRealmInteger(ri);

        realm.executeTransaction(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealm(allTypes);
                    }
                });

        realm.executeTransaction(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmInteger ri = realm.where(AllTypes.class).findFirst().getColumnRealmInteger();
                        ri.set(37);
                        ri.increment(17);
                        ri.decrement(7);
                    }
                });

        assertEquals(ri.longValue(), 47);
    }

    /**
     * Assure that an managed object's RealmInteger becomes unmanaged when the object does.
     */
    @Ignore("not yet implemented")
    @Test
    public void managed_copyFrom() {
        realm.executeTransaction(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.createObject(AllTypes.class).setColumnRealmInteger(new RealmInteger(5));
                    }
                });

        AllTypes unmanagedAllTypes = realm.copyFromRealm(realm.where(AllTypes.class).findFirst());

        realm.executeTransaction(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmInteger ri = realm.where(AllTypes.class).findFirst().getColumnRealmInteger();
                        ri.set(37);
                        ri.increment(17);
                        ri.decrement(7);
                    }
                });

        assertEquals(unmanagedAllTypes.getColumnRealmInteger().longValue(), 5);
    }

    /**
     * Assure that an attempt to change the value of a RealmInteger, outside a transaction, fails.
     */
    @Ignore("not yet implemented")
    @Test
    public void managed_updateOutsideTransactionThrows() {
        realm.executeTransaction(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.createObject(AllTypes.class).setColumnRealmInteger(new RealmInteger(5));
                    }
                });

        RealmInteger ri = realm.where(AllTypes.class).findFirst().getColumnRealmInteger();
        try {
            ri.set(1);
            fail("Setting a managed RealmInteger outside a transaction should fail");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("must be in a transaction"));
        }
        try {
            ri.increment(1);
            fail("Incrementing a managed RealmInteger outside a transaction should fail");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("must be in a transaction"));
        }
        try {
            ri.decrement(1);
            fail("Decrementing a managed RealmInteger outside a transaction should fail");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("must be in a transaction"));
        }
    }

    /**
     * Assure that an attempt to assign a RealmInteger field, outside a transaction, fails.
     */
    @Ignore("not yet implemented")
    @Test
    public void managed_assignOutsideTransactionThrows() {
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnRealmInteger(new RealmInteger(5));
        realm.commitTransaction();

        try {
            allTypes.setColumnRealmInteger(new RealmInteger(7));
            fail("Assigning a managed RealmInteger outside a transaction should fail");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("must be in a transaction"));
        }
    }
}
