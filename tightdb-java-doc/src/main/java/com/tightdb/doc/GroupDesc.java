package com.tightdb.doc;

import java.util.List;

import com.tightdb.Group;

public class GroupDesc extends AbstractDesc {

    public GroupDesc(List<Constructor> constructors, List<Method> methods) {
        super("group", constructors, methods);
    }

    @SuppressWarnings("unused")
    public void describe() {
        constructor("Instantiates a TightDB table group");
        constructor("Instantiates a TightDB table group with file (de)serialization", "File", "file");
        constructor("Instantiates a TightDB table group with file (de)serialization", "File", "file", "boolean", "readOnly");
        constructor("Instantiates a TightDB table group with file (de)serialization", "String", "fileName");
        constructor("Instantiates a TightDB table group with byte array (de)serialization", "byte[]", "data");
        constructor("Instantiates a TightDB table group with ByteBuffer (de)serialization", "ByteBuffer", "buffer");

        method("void", "close", "Close the group and release resources");
        method("TableBase", "getTable", "(Low-level) Create and get a table with the specified name", "String", "name");
        method("int", "size", "Get the number of tables");
        method("String", "getTableName", "Get the table at the specified position", "int", "index");
        method("boolean", "hasTable", "Check if group contains a specific named table", "String", "name");
        method("boolean", "isValid", "Validate the group");
        method("ByteBuffer", "writeToByteBuffer", "Serialize the group to a ByteBuffer");
        method("void", "writeToFile", "Serialize the group to disk", "File", "file");
        method("void", "writeToFile", "Serialize the group to disk", "String", "fileName");
        method("byte[]", "writeToMem", "Serialize the group to a memory buffer");
        
        Group g = null;
    }
}
