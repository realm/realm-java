package com.tightdb.doc;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.tightdb.Group;
import com.tightdb.TableBase;

public class GroupExamples {

	@SuppressWarnings("unused")
	public static void main(String[] args) {

		Group group = new Group();
		
		/* EXAMPLE: close */

		group.close();
		
		/* EXAMPLE: getTable */

		TableBase a = group.getTable("PersonTable");
		
		/* EXAMPLE: getTableCount */

		long tableCount = group.getTableCount();
		
		/* EXAMPLE: getTableName */

		String firstTableName = group.getTableName(0);
		
		/* EXAMPLE: hasTable */

		if (group.hasTable("PersonTable")) {
			  // do something
		}
		
		/* EXAMPLE: isValid */

		if (group.isValid()) {
			  // do something
		}
		
		/* EXAMPLE: writeToByteBuffer */

		ByteBuffer buffer = group.writeToByteBuffer();
		
		/* EXAMPLE: writeToFile */

		File file = new File("data.tdb");
		try {
			group.writeToFile(file);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't write to file!", e);
		}
		
		/* EXAMPLE: writeToFile-2 */

		try {
			group.writeToFile("data.tdb");
		} catch (IOException e) {
			throw new RuntimeException("Couldn't write to file!", e);
		}
		
		
		/* EXAMPLE: writeToMem */
		
		byte[] mem = group.writeToMem();

		/* EXAMPLE: END! */
		
	}
	
}
