package com.tightdb.typed;

import java.io.File;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.ReadTransaction;
import com.tightdb.SharedGroup;
import com.tightdb.WriteTransaction;

public class SharedGroupTest {
    
    protected SharedGroup db;

    protected String testFile = "transact.tightdb";

    protected void deleteFile(String filename) {
        File f = new File(filename);
        if (f.exists())
            f.delete();
    }

    @BeforeMethod
    public void init() {
        deleteFile(testFile);
        db = new SharedGroup(testFile);
    }

    //@AfterMethod
    public void clear() {
        db.close();
        deleteFile(testFile);
    }

    @Test
    public void onlyOneWriteTransaction() {

        WriteTransaction wt0 = db.beginWrite();

        try {
            db.beginWrite(); //Except exception. Only 1 exception is allowed
            assert(false);
            
        } catch (IllegalStateException e){
            wt0.rollback();
            clear(); // Important to clear
        }
    }

    @Test
    public void onlyOneReadTransaction() {

        ReadTransaction rt0 = db.beginRead();

        try {
            db.beginRead(); //Except exception. Only 1 exception is allowed
            
        } catch (IllegalStateException e){
            rt0.endRead();
            clear(); // Important to clear
        }
    }
    
    @Test
    public void noCloseSharedGroupDuringTransaction() {

        ReadTransaction rt0 = db.beginRead();

        try {
            db.close(); //Except exception. Must not close shared group during active transaction
            
        } catch (IllegalStateException e){
            rt0.endRead();
            clear(); // Important to clear
        }
    }
}
