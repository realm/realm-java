package com.tightdb.typed;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.*;

import org.testng.annotations.DataProvider;
//import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tightdb.ReadTransaction;
import com.tightdb.SharedGroup;
import com.tightdb.SharedGroup.Durability;
import com.tightdb.WriteTransaction;

public class SharedGroupTest {
    
    protected SharedGroup db;

    protected String testFile = "transact.tightdb";

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
        } catch (IllegalStateException e){
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

    @Test(dataProvider = "durabilityProvider")
    public void noCreateParameter(Durability durability) {
    	// test not applicable for MEM_ONLY
    	if (durability == Durability.MEM_ONLY)
    		return;
    	
    	deleteFile(testFile);
        
    	// First create file
        db = new SharedGroup(testFile, durability, false);
        db.close();

        try {
        	// Then set no_create=true, and it should fail
            db = new SharedGroup(testFile, durability, true);
        } catch (IllegalStateException e){
            clear();
        	return;
        }
        assert(false);
    }

    @Test(dataProvider = "durabilityProvider")
    public void shouldReserve(Durability durability) {
    	// test not applicable for MEM_ONLY
    	if (durability == Durability.MEM_ONLY)
    		return;

    	// First create file
    	deleteFile("sizefile");
        db = new SharedGroup("sizefile", durability);
        db.reserve(50012);
        
        File f = new File("sizefile");
        assertEquals( 50012, f.length() );
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
