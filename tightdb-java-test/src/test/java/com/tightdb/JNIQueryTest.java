package com.tightdb;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

public class JNIQueryTest {

	TableBase table;
	
	void init() {
		table = new TableBase();
		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeInt, "number");
		tableSpec.addColumn(ColumnType.ColumnTypeString, "name");
		table.updateFromSpec(tableSpec);

		long i = 0;
		table.insertLong(0, i, 10); table.insertString(1, i++, "A"); table.insertDone();
		table.insertLong(0, i, 11); table.insertString(1, i++, "B"); table.insertDone();
		table.insertLong(0, i, 12); table.insertString(1, i++, "C"); table.insertDone();
		table.insertLong(0, i, 13); table.insertString(1, i++, "B"); table.insertDone();
		table.insertLong(0, i, 14); table.insertString(1, i++, "D"); table.insertDone();
		table.insertLong(0, i, 16); table.insertString(1, i++, "D"); table.insertDone();
		assertEquals(6, table.size());
	}

	@Test
	public void shouldQuery() {
		init();
		TableQuery query = table.where();
		
		long cnt = query.equal(1, "D").count();
		assertEquals(2, cnt);
		
		cnt = query.minimum(0);
		assertEquals(14, cnt);

		cnt = query.maximum(0);
		assertEquals(16, cnt);

		cnt = query.sum(0);
		assertEquals(14+16, cnt);
		
		double avg = query.average(0);
		assertEquals(15.0, avg);
		
		// TODO: Add tests with all parameters
	}
}
