package com.tightdb.doc;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.tightdb.Group;
import com.tightdb.TableBase;

public class GroupExamples {

	public static void main(String[] args) {

		Group group = new Group();
		
		/* EXAMPLE: constructor-1 */

		Group group = new Group();
		
		/* EXAMPLE: constructor-2 */

		Group group = new Group(new File("data.tdb"));
		
		/* EXAMPLE: constructor-3 */

		Group group = new Group("data.tdb", true);
		
		/* EXAMPLE: constructor-4 */

		Group group = new Group("data.tdb");
		
		/* EXAMPLE: constructor-5 */

		byte[] data = loadData();
		Group group = new Group(data);
		
		/* EXAMPLE: constructor-6 */

		ByteBuffer buffer = loadBuffer();
		Group group = new Group(buffer);
		
		/* EXAMPLE: close */

		group.close();
		
		/* EXAMPLE: commit */

		group.commit();
		
		/* EXAMPLE: getTable */

		TableBase a = group.getTable("PersonTable");
		
		/* EXAMPLE: size */

		int tableCount = group.size();
		
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
