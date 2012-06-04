package com.tightdb.doc;

import java.util.List;

import com.tightdb.Group;

public class GroupDesc extends AbstractDesc {

	public GroupDesc(List<Method> methods) {
		super(methods);
	}

	public void describe() {

		method("void", "close", "Close the group and release resources");
		method("TableBase", "getTable", "(Low-level) Create and get a table with the specified name");
		method("int", "getTableCount", "Get the number of tables");
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
