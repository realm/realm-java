package com.tightdb;

import com.tightdb.TableBase;
import com.tightdb.Group;

// Singleton for handling synchronized close of tables and groups

public class CloseHandler {

    private static CloseHandler ourInstance = new CloseHandler();

    // Creation not possible externally
    private CloseHandler() {
        //System.out.println("CloseHandler created!!!");
    }

    public static CloseHandler getInstance() {
        return ourInstance;
    }

    public void finalize() {
        //System.out.println(" ******** closing CloseHandler ***********");
    }

    synchronized public void close(TableBase table) {
        //System.out.println("CLOSING Table");
        table.doClose();
    }

    synchronized public void close(Group group) {
        //System.out.println("CLOSING Group");
        group.doClose();
    }

    synchronized public void close(SharedGroup group) {
        //System.out.println("CLOSING SharedGroup");
        group.doClose();
    }
}

