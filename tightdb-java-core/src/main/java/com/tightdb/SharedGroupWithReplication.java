package com.tightdb;

import java.io.IOException;

public class SharedGroupWithReplication extends SharedGroup {

    public SharedGroupWithReplication() throws IOException
    {
        super("", true);
    }

    public SharedGroupWithReplication(String databaseFile) throws IOException
    {
        super(databaseFile, true);
    }

    public static String getDefaultDatabaseFileName()
    {
        return nativeGetDefaultReplicationDatabaseFileName();
    }
}
