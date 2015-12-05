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

package io.realm.internal;

import android.test.AndroidTestCase;

import java.io.File;
import java.util.Arrays;

import io.realm.RealmFieldType;

public class JNIImplicitTransactionsTest extends AndroidTestCase {

    String testFile;

    @Override
    protected void setUp() throws Exception {
        testFile = new File(this.getContext().getFilesDir(), "implicit.realm").toString();
    }

    private void deleteFile() {
        for (String fileToDelete : Arrays.asList(testFile, testFile + ".lock")) {
            File f = new File(fileToDelete);
            if (f.exists()) {
                boolean result = f.delete();
                if (!result) {
                    fail();
                }
            }
        }
    }

    public void testImplicitTransactions() {
        deleteFile();
        SharedGroup sg = new SharedGroup(testFile, true, SharedGroup.Durability.FULL, null); // TODO: try with encryption

        // Create a table
        WriteTransaction wt = sg.beginWrite();
        if (!wt.hasTable("test")) {
            Table table = wt.getTable("test");
            table.addColumn(RealmFieldType.INTEGER, "integer");
            table.addEmptyRow();
        }
        wt.commit();

        // Add a row in a write transaction and continue with read transaction
        ImplicitTransaction t = sg.beginImplicitTransaction();
        Table test = t.getTable("test");
        assertEquals(1, test.size());
        t.promoteToWrite();
        test.addEmptyRow();
        t.commitAndContinueAsRead();

        // Should throw as this is now a read transaction
        try {
            test.addEmptyRow();
            fail();
        } catch (IllegalStateException e) {
            assertNotNull(e);
        }
    }

    public void testCannotUseClosedImplicitTransaction() {
        deleteFile();
        SharedGroup sg = new SharedGroup(testFile, true, SharedGroup.Durability.FULL, null);
        WriteTransaction wt = sg.beginWrite();
        if (!wt.hasTable("test")) {
            Table table = wt.getTable("test");
            table.addColumn(RealmFieldType.INTEGER, "integer");
            table.addEmptyRow();
        }
        wt.commit();
        ImplicitTransaction t = sg.beginImplicitTransaction();

        sg.close();
        try {
            t.advanceRead();
        } catch (IllegalStateException e) {
            return;
        }

        fail("It should not be possible to advanceRead on a transaction which SharedGroup is closed");
    }
}
