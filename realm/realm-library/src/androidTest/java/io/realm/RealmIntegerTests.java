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

    @Test
    public void testBasic() {
        RealmInteger ri1 = new RealmInteger(10);
        RealmInteger ri2 = new RealmInteger("10");
        assertEquals(ri1, ri2);

        ri1.set(15);
        ri1.decrement(2);
        ri2.increment(3);
        assertEquals(ri1, ri2);
    }

    @Test
    public void testGetters() {
        RealmInteger ri = new RealmInteger(0x04433332211L);

        // positive
        assertEquals(ri.longValue(), 0x04433332211L);
        assertEquals(ri.intValue(), 0x033332211);
        assertEquals(ri.shortValue(), 0x02211);
        assertEquals(ri.byteValue(), 0x011);

        assertEquals(ri.floatValue(), 2.92916756E11F);
        assertEquals(ri.doubleValue(), 2.92916765201E11);

        // negative
        ri.set(0x8888444483338281L);
        assertEquals(ri.longValue(), 0x8888444483338281L);
        assertEquals(ri.intValue(), 0x083338281);
        assertEquals(ri.shortValue(), (short) 0xf8281);
        assertEquals(ri.byteValue(), (byte) 0xf81);

        assertEquals(ri.floatValue(), -8.6085554E18F);
        assertEquals(ri.doubleValue(), -8.6085556266690468E18);
    }

    @Ignore("not yet implemented")
    @Test
    public void testImport() {
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
                        realm.where(AllTypes.class).findFirst().getColumnRealmInteger().decrement(1);
                    }
                });

        assertEquals(ri.longValue(), 4);
    }

    @Ignore("not yet implemented")
    @Test
    public void testExport() {
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnRealmInteger(new RealmInteger(5));
        realm.commitTransaction();

        final RealmInteger ri = allTypes.getColumnRealmInteger();

        realm.executeTransaction(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        ri.decrement(1);
                    }
                });

        allTypes = realm.where(AllTypes.class).findFirst();
        assertEquals(allTypes.getColumnRealmInteger().longValue(), 4);
    }


    @Ignore("not yet implemented")
    @Test
    public void testCopyTo() {
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
                        realm.where(AllTypes.class).findFirst().getColumnRealmInteger().decrement(1);
                    }
                });

        assertEquals(ri.longValue(), 4);
    }

    @Ignore("not yet implemented")
    @Test
    public void testCopyFrom() {
        realm.executeTransaction(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.createObject(AllTypes.class).setColumnRealmInteger(new RealmInteger(5));
                    }
                });

        realm.beginTransaction();
        AllTypes unmanagedAllTypes = realm.copyFromRealm(realm.where(AllTypes.class).findFirst());
        realm.commitTransaction();

        realm.executeTransaction(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.where(AllTypes.class).findFirst().getColumnRealmInteger().decrement(1);
                    }
                });

        assertEquals(unmanagedAllTypes.getColumnRealmInteger().longValue(), 5);
    }

    @Ignore("not yet implemented")
    @Test
    public void testUpdateManagedOutsideTransactionFails() {
        realm.executeTransaction(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.createObject(AllTypes.class).setColumnRealmInteger(new RealmInteger(5));
                    }
                });

        RealmInteger ri = realm.where(AllTypes.class).findFirst().getColumnRealmInteger();
        try {
            ri.decrement(1);
            fail("Mutating a managed RealmInteger outside a transaction should fail");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("must be in a transaction"));
        }
    }
}
