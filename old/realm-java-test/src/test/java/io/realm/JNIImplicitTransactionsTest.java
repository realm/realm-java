/*
 * Copyright 2014 Realm Inc.
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

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.AssertJUnit.assertEquals;

public class JNIImplicitTransactionsTest {

    private void deleteFile(String filename) {
        File f = new File(filename);
        if (f.exists())
            f.delete();
        f = new File(filename + ".lock");
        if (f.exists())
            f.delete();
    }

    @Test(expectedExceptions=IllegalStateException.class)
    public void testImplicitTransactions() {

        deleteFile("implicit.realm");
        SharedGroup sg = new SharedGroup("implicit.realm", true);

        WriteTransaction wt = sg.beginWrite();

        if(!wt.hasTable("test")) {
            Table table = wt.getTable("test");
            table.addColumn(ColumnType.INTEGER, "integer");
            table.addEmptyRow();
        }

        wt.commit();

        ImplicitTransaction t = sg.beginImplicitTransaction();

        Table test = t.getTable("test");


        assertEquals(1, test.size());

        t.promoteToWrite();

        test.addEmptyRow();

        t.commitAndContinueAsRead();

        // Should throw as this is now a read transaction
        test.addEmptyRow();

    }

}
