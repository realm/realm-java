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

package io.realm.typed;

import static org.testng.AssertJUnit.fail;

import java.io.File;

import io.realm.IOException;
import io.realm.WriteTransaction;
import org.testng.annotations.DataProvider;
//import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.realm.ReadTransaction;
import io.realm.SharedGroup;
import io.realm.SharedGroup.Durability;

public class SharedGroupTest {

    protected SharedGroup db;

    protected String testFile = "transact.realm";

    protected void deleteFile(String filename) {
        File f = new File(filename);
        if (f.exists())
            f.delete();
        f = new File(filename + ".lock");
        if (f.exists())
            f.delete();
    }

    public void init(Durability durability) {
        deleteFile(testFile);
        db = new SharedGroup(testFile, durability);
    }

    public void clear() {
        db.close();
        deleteFile(testFile);
    }

    @Test
    public void endReadTransactionOnClosedGroup() {
        SharedGroup g = new SharedGroup("closeTest.realm");
        ReadTransaction rt = g.beginRead();
        rt.endRead(); // Close transaction
        g.close(); // then close group, possible only if no active transactions exist
        try { rt.endRead(); fail("Group is closed, illegal read transaction"); } catch (IllegalStateException e ) { } // try closing the transaction again
    }

    @Test
    public void endWriteTransactionRollbackOnClosedGroup() {
        SharedGroup g = new SharedGroup("closeTest.realm");
        WriteTransaction wt = g.beginWrite();
        wt.rollback(); // Close transaction
        g.close(); // then close group, possible only if no active transactions exist
        try { wt.rollback(); fail("Group is closed, illegal write transaction"); } catch (IllegalStateException e ) { } // try closing the transaction again
    }

    @Test
    public void endWriteTransactionCommitOnClosedGroup() {
        SharedGroup g = new SharedGroup("closeTest.realm");
        WriteTransaction wt = g.beginWrite();
        wt.commit(); // Close transaction
        g.close(); // then close group, possible only if no active transactions exist
        try { wt.commit(); fail("Group is closed, illegal write transaction"); } catch (IllegalStateException e ) { } // try closing the transaction again
    }




    @Test(enabled=false)
    public void testExistingLockFileWithDeletedDb() {
        String uniqueName = "test991UniqueName.realm";

        SharedGroup sg = new SharedGroup(uniqueName);

        WriteTransaction wt = sg.beginWrite();
        try {
            wt.getTable("tableName");
            wt.commit();
        } catch (Throwable t){
            wt.rollback();
        }

        wt = sg.beginWrite();
        wt.getTable("tableName");
        // Do not end the write transaction - leaving the .lock file there
        // Delete realm file, but NOT .lock file
        new File(uniqueName).delete();

        // If the lock file still exist (which it does until garbage collector has been run)
        if(new File(uniqueName + ".lock").exists()) {
            // Try creating new shared group, while lock file is still there
            try { SharedGroup sg2 = new SharedGroup(uniqueName); fail("The database file is missing, but a .lock file is present."); } catch(IOException e) { }
        }
    }


    @Test(dataProvider = "durabilityProvider")
    public void expectExceptionWhenMultipleBeginWrite(Durability durability) {
        init(durability);

        WriteTransaction wt = db.beginWrite();
        try {
            db.beginWrite(); //Expect exception. Only 1 beginWrite() is allowed
        } catch (IllegalStateException e){
            wt.rollback();
            clear();
            return;
        }
        assert(false);
    }

    @Test(dataProvider = "durabilityProvider")
    public void onlyOneReadTransaction(Durability durability) {
        init(durability);

        ReadTransaction rt = db.beginRead();
        try {
            db.beginRead(); // Expect exception. Only 1 begibRead() is allowed
        } catch (IllegalStateException e) {
            rt.endRead();
            clear();
            return;
        }
        assert(false);
    }

    @Test(dataProvider = "durabilityProvider")
    public void noCloseSharedGroupDuringTransaction(Durability durability) {
        init(durability);

        ReadTransaction rt = db.beginRead();
        try {
            db.close(); // Expect exception. Must not close shared group during active transaction
        } catch (IllegalStateException e){
            rt.endRead();
            clear();
            return;
        }
        assert(false);
    }

    @Test(enabled=false, dataProvider = "durabilityProvider")
    public void fileMustExistParameter(Durability durability) {
        // test not applicable for MEM_ONLY
        if (durability == Durability.MEM_ONLY)
            return;

        String mustExistFile = "mustexistcheck.realm";

        // Check that SharedGroup asserts when there is no file
        deleteFile(mustExistFile);
        try {
            db = new SharedGroup(mustExistFile, durability, true);
            assert(false);
        } catch (IOException e) {
            // expected
        } catch (Exception e) {
            assert(false);
        }
        // Don't expect anything to close due to failure.

        // Create file and see that it can be opened now
        db = new SharedGroup(mustExistFile, durability, false);
        db.close();
        // Then set fileMustExist=true, and it should work
        db = new SharedGroup(mustExistFile, durability, true);
        db.close();

        deleteFile(mustExistFile);
    }

    @Test(dataProvider = "durabilityProvider")
    public void shouldReserve(Durability durability) {
        // test not applicable for MEM_ONLY
        if (durability == Durability.MEM_ONLY)
            return;

        // First create file
        String fileName = "sizefile.realm";
        deleteFile(fileName);
        db = new SharedGroup(fileName, durability);
        db.reserve(50012);

        File f = new File(fileName);
        // Not all platforms support this:   assertEquals( 50012, f.length() );
        db.close();

        deleteFile(testFile);
    }


    @DataProvider(name = "durabilityProvider")
    public Object[][] durabilityProvider() {
        return new Object[][] {
                {Durability.FULL},
                {Durability.MEM_ONLY},
                {Durability.ASYNC} };
    }

}
