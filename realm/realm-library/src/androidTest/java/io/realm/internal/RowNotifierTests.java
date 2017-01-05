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

package io.realm.internal;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.TestHelper;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class RowNotifierTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    private RealmConfiguration config;
    private SharedRealm sharedRealm;
    private Table table;
    private final static String TABLE_NAME = "test_table";
    private final static long STRING_COLUMN_INDEX = 0;

    @Before
    public void setUp() {
        config = configFactory.createConfiguration();
        sharedRealm = getSharedRealm();
        populateData();
    }

    @After
    public void tearDown() {
        sharedRealm.close();
    }

    private SharedRealm getSharedRealm() {
        return SharedRealm.getInstance(config, null, true);
    }

    private void populateData() {
        sharedRealm.beginTransaction();
        table = sharedRealm.getTable(TABLE_NAME);
        // Specify the column types and names
        assertEquals(STRING_COLUMN_INDEX, table.addColumn(RealmFieldType.STRING, "string"));
        table.addEmptyRow();
        sharedRealm.commitTransaction();
    }

    private void changeRowAsync() {
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedRealm sharedRealm = getSharedRealm();
                changeRow(sharedRealm);
                sharedRealm.close();
                latch.countDown();
            }
        }).start();
        TestHelper.awaitOrFail(latch);
    }

    private void changeRow(SharedRealm sharedRealm) {
        sharedRealm.beginTransaction();
        table = sharedRealm.getTable(TABLE_NAME);
        UncheckedRow row = table.getUncheckedRow(0);
        row.setString(STRING_COLUMN_INDEX, "changed");
        sharedRealm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void listener_triggeredByRemoteCommit() {
        SharedRealm sharedRealm = getSharedRealm();
        Table table = sharedRealm.getTable(TABLE_NAME);
        UncheckedRow row = table.getUncheckedRow(0);
        looperThread.keepStrongReference.add(row);
        sharedRealm.rowNotifier.registerListener(row, row, new RealmChangeListener<UncheckedRow>() {
            @Override
            public void onChange(UncheckedRow row) {
                assertEquals("changed", row.getString(STRING_COLUMN_INDEX));
                looperThread.testComplete();
            }
        });

        changeRowAsync();
    }

    @Test
    @RunTestInLooperThread
    public void listener_triggeredByLocalCommit() {
        SharedRealm sharedRealm = getSharedRealm();
        Table table = sharedRealm.getTable(TABLE_NAME);
        UncheckedRow row = table.getUncheckedRow(0);
        looperThread.keepStrongReference.add(row);
        sharedRealm.rowNotifier.registerListener(row, row, new RealmChangeListener<UncheckedRow>() {
            @Override
            public void onChange(UncheckedRow row) {
                String testString = row.getString(STRING_COLUMN_INDEX);
                //assertEquals("changed", row.getString(STRING_COLUMN_INDEX));
                //looperThread.testComplete();
            }
        });

        changeRow(sharedRealm);
    }

    @Test
    @RunTestInLooperThread
    public void listener_triggeredByLocalTransactionBegin() {
        SharedRealm sharedRealm = getSharedRealm();
        Table table = sharedRealm.getTable(TABLE_NAME);

        changeRow(sharedRealm);

        UncheckedRow row = table.getUncheckedRow(0);
        looperThread.keepStrongReference.add(row);
        sharedRealm.rowNotifier.registerListener(row, row, new RealmChangeListener<UncheckedRow>() {
            @Override
            public void onChange(UncheckedRow row) {
                assertEquals("changed", row.getString(STRING_COLUMN_INDEX));
                looperThread.testComplete();
            }
        });

        sharedRealm.beginTransaction();
    }
}
