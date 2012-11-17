package com.tightdb;

import static org.testng.AssertJUnit.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tightdb.test.DataProviderUtil;

public class JNITableSpecTest {

	@Test(dataProvider = "oneColumn")
	public void shouldDefineOneColumnTable(ColumnType columnType) {
		TableSpec spec = new TableSpec();
		spec.addColumn(columnType, "foo");
		assertEquals(0, spec.getColumnIndex("foo"));
		assertEquals(-1, spec.getColumnIndex("xx"));
		
		TableSpec spec2 = new TableSpec();
		spec2.addColumn(columnType, "foo");
		checkSpecIdentity(spec, spec2);

		TableBase table = new TableBase();
		table.updateFromSpec(spec);
	}

	@Test(dataProvider = "twoColumns")
	public void shouldDefineTwoColumnsTable(ColumnType columnType, ColumnType columnType2) {
		TableSpec spec = new TableSpec();
		spec.addColumn(columnType, "foo");
		TableSpec subspec = spec.addSubtableColumn("bar");
		subspec.addColumn(columnType2, "subbar");
		assertEquals(0, spec.getColumnIndex("foo"));
		assertEquals(1, spec.getColumnIndex("bar"));
		assertEquals(0, subspec.getColumnIndex("subbar"));
		assertEquals(-1, subspec.getColumnIndex("xx"));

		TableSpec spec2 = new TableSpec();
		spec2.addColumn(columnType, "foo");
		TableSpec subspec2 = spec2.addSubtableColumn("bar");
		subspec2.addColumn(columnType2, "subbar");
		
		checkSpecIdentity(spec, spec2);

		TableBase table = new TableBase();
		table.updateFromSpec(spec);
	}

	@DataProvider(name = "oneColumn")
	public Iterator<Object[]> oneColumn() {
		return DataProviderUtil.allCombinations(columnTypes());
	}

	@DataProvider(name = "twoColumns")
	public Iterator<Object[]> twoColumns() {
		return DataProviderUtil.allCombinations(columnTypes(), columnTypes());
	}

	private void checkSpecIdentity(TableSpec spec, TableSpec spec2) {
		assertEquals(spec, spec2);
		assertEquals(spec.hashCode(), spec2.hashCode());
	}
	
	private List<?> columnTypes() {
		return Arrays.asList(ColumnType.ColumnTypeBinary, ColumnType.ColumnTypeBool, ColumnType.ColumnTypeDate,
				ColumnType.ColumnTypeInt, ColumnType.ColumnTypeMixed, ColumnType.ColumnTypeString, ColumnType.ColumnTypeTable);
		//return Arrays.asList(ColumnType.values()); // Can't include the ColumnTypeStringEnum - it's not valid user type
	}

}
